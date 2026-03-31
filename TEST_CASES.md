# Splendor Test Cases

Result: Completed audit (`144/150` test cases passed).

## Scope

- This checklist has been refreshed against the current codebase and current console UI flow.
- It covers setup/config, CSV loading, engine rules, CLI behavior, endgame logic, and `EASY` / `MEDIUM` / `HARD` AI behavior.
- These cases are intentionally defined before execution. Some are defect-oriented edge cases and may currently fail; they should still remain in the suite.

## Setup & Config

- [x] TC001: Start a 2-player game and verify bank setup is 4 of each non-gold gem, 5 gold, 3 nobles in play, 4 visible cards per tier when enough cards exist, and initial visible deck counts are 36 / 26 / 16.
- [x] TC002: Start a 3-player game and verify bank setup is 5 of each non-gold gem, 5 gold, 4 nobles in play, 4 visible cards per tier when enough cards exist, and initial visible deck counts are 36 / 26 / 16.
- [x] TC003: Start a 4-player game and verify bank setup is 7 of each non-gold gem, 5 gold, 5 nobles in play, 4 visible cards per tier when enough cards exist, and initial visible deck counts are 36 / 26 / 16.
- [x] TC004: Verify the first player entered during setup always gets the first turn.
- [x] TC005: Temporarily remove or rename `config.properties` and verify startup still succeeds by falling back to default card path, noble path, and `win.points=15`.
- [x] TC006: Set `win.points` in `config.properties` to a valid non-default value such as `10` and verify the displayed target and endgame logic use the new value.
- [x] TC007: Set `win.points=` blank and verify it falls back to 15.
- [x] TC008: Set `win.points` to a non-numeric value and verify it falls back to 15.
- [x] TC009: Set `win.points=0` and verify startup is stable, the target displays as 0, and gameplay/endgame behavior remains consistent without crashing.
- [x] TC010: Set `win.points` to a negative value and verify startup is stable, the target displays that value, and gameplay/endgame behavior remains consistent without crashing.
- [x] TC011: Remove or blank out `cards.filepath` in `config.properties` and verify the default bundled card CSV is used.
- [x] TC012: Remove or blank out `nobles.filepath` in `config.properties` and verify the default bundled noble CSV is used.
- [x] TC013: Point `cards.filepath` to a missing file and verify startup fails with a clear error instead of starting a partial game.
- [x] TC014: Point `nobles.filepath` to a missing file and verify startup fails with a clear error instead of starting a partial game.
- [x] TC015: Use a valid alternate card CSV file and verify the game loads it correctly.
- [x] TC016: Use a valid alternate noble CSV file and verify the game loads it correctly.
- [ ] TC017: Use a malformed card CSV containing an invalid tier number and verify startup fails safely.
- [ ] TC018: Use a malformed card CSV containing missing columns or invalid numeric fields and verify startup fails safely.
- [ ] TC019: Use a malformed noble CSV containing missing columns or invalid numeric fields and verify startup fails safely.
- [ ] TC020: Enter duplicate player names and verify setup, AI/human assignment, turn order labels, and player summaries remain correct without cross-wiring the two players.
- [x] TC021: Enter a blank player name and verify the CLI rejects it and re-prompts.
- [x] TC022: Enter invalid responses such as blank input, `maybe`, or `1` to the `Is "<name>" a human player? (y/n):` prompt and verify the CLI re-prompts until `y/yes` or `n/no` is entered.
- [x] TC023: Enter invalid input during AI difficulty selection, such as blank input, letters, or an out-of-range number, and verify the CLI re-prompts.
- [x] TC024: Select `EASY`, `MEDIUM`, and `HARD` during setup and verify each choice creates the intended strategy class and is used during gameplay.

## Turn Flow, Menu State, and General CLI Behavior

- [x] TC025: Verify the board renders once at the start of each human turn and once at the start of each AI turn.
- [x] TC026: Complete a successful human action and verify the turn advances exactly once to the next player.
- [x] TC027: Cancel an action from a submenu and verify the current player keeps the turn.
- [x] TC028: Trigger a failed human action and verify the current player keeps the turn.
- [x] TC029: Reach a state where every gameplay action is unavailable and verify `Pass turn` appears and works.
- [x] TC030: Choose `Quit`, answer `n`, and verify the game returns to the current turn without state changes.
- [x] TC031: Choose `Quit`, answer `y`, and verify the game exits cleanly with the user-ended message.
- [x] TC032: Enter invalid main-menu input such as blank input, letters, or an out-of-range number and verify the CLI re-prompts without crashing.
- [x] TC033: Enter invalid tier-selection input and verify the CLI re-prompts without crashing.
- [x] TC034: Enter invalid visible-card-selection input and verify the CLI re-prompts without crashing.
- [x] TC035: Enter invalid gem-selection input and verify the CLI re-prompts without crashing.
- [x] TC036: Enter invalid yes/no input anywhere the CLI expects confirmation and verify it re-prompts with `Please enter y or n.`
- [x] TC037: Enter `0` at the first submenu of each action flow and verify it returns cleanly to the main action menu.
- [x] TC038: During the 3-different-gems flow, enter `0` on the second or third gem pick and verify the entire action is cancelled cleanly.
- [x] TC039: During gem confirmation, answer `n` and verify the action is cancelled with no state change.

## Gem-Taking Rules & Token Limit

- [x] TC040: Take 3 different gems while at least 3 colors are available and verify the player gains exactly 1 of each chosen color and the bank loses exactly 1 of each.
- [x] TC041: Directly exercise the engine path for taking 3 different gems with duplicate colors and verify it is rejected with no state change.
- [x] TC042: Directly exercise the engine path for taking 3 different gems including `GOLD` and verify it is rejected with no state change.
- [ ] TC043: Reduce the bank so fewer than 3 non-gold colors remain available and verify `Take 3 different gems` is unavailable in the CLI.
- [x] TC044: Take 2 of the same color when exactly 4 tokens of that color remain in the bank and verify it succeeds.
- [x] TC045: Try taking 2 of the same color when only 3 remain in the bank and verify it fails.
- [x] TC046: Directly exercise the engine path for taking 2 `GOLD` and verify it is rejected with no state change.
- [x] TC047: Verify the `Take 2 same gems` submenu only lists colors that currently have at least 4 tokens in the bank.
- [x] TC048: Verify the second gem-pick menu in the 3-different flow removes the first chosen color from the option list.
- [x] TC049: Verify the third gem-pick menu removes both previously chosen colors from the option list.
- [x] TC050: Put a player at 10 tokens, take 3 different gems, and verify the action succeeds, the player temporarily reaches 13, and forced discard begins.
- [x] TC051: Put a player at 9 tokens, take 2 of the same gem, and verify the action succeeds, the player temporarily reaches 11, and forced discard begins.
- [x] TC052: During forced discard after gem-taking, verify the player may discard some or all of the gems that were just taken.
- [x] TC053: During forced discard, verify a player holding gold may choose to discard gold.
- [x] TC054: Verify forced discard loops until the player is back to exactly 10 or fewer tokens and bank totals remain consistent afterward.

## Reserve Rules & Token Limit

- [x] TC055: Reserve a visible card while gold is available and verify the card moves to reserved cards, the player gains exactly 1 gold, and the board slot refills if the deck still has cards.
- [x] TC056: Reserve a visible card when gold is empty and verify the reserve still succeeds but no gold is granted.
- [x] TC057: Reserve the top card of a non-empty deck while gold is available and verify the reserved card is added and the player gains 1 gold.
- [x] TC058: Reserve the top card of a non-empty deck while gold is empty and verify the reserve still succeeds with no gold granted.
- [x] TC059: Try reserving from the top of an empty deck and verify the move fails safely.
- [x] TC060: Give a player 3 reserved cards and verify both reserve actions are unavailable.
- [x] TC061: Empty all visible rows and verify `Reserve a visible card` is unavailable with the correct reason.
- [x] TC062: Empty all three decks and verify `Reserve top card from a deck` is unavailable with the correct reason.
- [x] TC063: Put a player at 10 tokens, reserve a card while gold is available, and verify the action succeeds, the player temporarily reaches 11, and forced discard begins.
- [x] TC064: Put a player at 10 tokens, reserve a card while gold is unavailable, and verify the action succeeds without a discard prompt.
- [x] TC065: During forced discard after reserving, verify the player may discard the gold token that was just received.
- [x] TC066: Reserve a visible card from a tier whose deck is already empty and verify the visible row shrinks correctly instead of crashing.
- [x] TC067: Enter `0` during reserve tier selection or reserve card selection and verify the action cancels cleanly with no state change.

## Buying & Payment

- [x] TC068: Buy a visible card using only colored gems and verify the correct tokens leave the player, return to the bank, and the card moves to purchased cards.
- [x] TC069: Buy a visible card using permanent discounts from purchased cards and verify discounts are applied correctly.
- [x] TC070: Buy a visible card using a mix of colored gems and gold wildcards and verify the correct amount of each token is spent and returned to the bank.
- [x] TC071: Buy a card where discounts reduce one or more color costs below zero and verify those effective costs clamp to zero.
- [x] TC072: Verify `Buy a visible card` is unavailable when the current player cannot afford any visible card.
- [x] TC073: Directly attempt to buy a visible card the player cannot afford and verify the engine returns false with no state change.
- [x] TC074: Buy a reserved card and verify it is removed from reserved cards, added to purchased cards, and no board refill occurs.
- [x] TC075: Verify `Buy a reserved card` is unavailable when the current player has no reserved cards.
- [x] TC076: Verify `Buy a reserved card` is unavailable when the player has reserved cards but cannot afford any of them.
- [x] TC077: Force an invalid visible-card index through the buy path and verify the error is handled safely without crashing the whole UI.
- [x] TC078: Force an invalid reserved-card index through the buy path and verify the error is handled safely without crashing the whole UI.
- [x] TC079: Buy a visible card from a tier with remaining deck cards and verify the visible slot refills immediately.
- [x] TC080: Buy a visible card from a tier whose deck is empty and verify the slot does not refill.
- [x] TC081: Verify the visible-buy submenu shows only affordable cards, not all visible cards in the chosen tier.
- [x] TC082: Verify the reserved-buy submenu shows only affordable reserved cards, not all reserved cards.
- [x] TC083: Buy a card from an affordable-visible filtered list and verify the chosen menu number maps back to the correct original board slot.
- [x] TC084: Verify each spent colored gem and each spent gold token is returned to the bank during payment.
- [x] TC085: Verify buying a card increments the player's bonus count only for that card's bonus color.
- [x] TC086: Verify purchases never create a token-overflow condition and the player's total gem count never increases as a result of buying.
- [x] TC087: Buy a card that also triggers a noble claim and verify payment, card movement, noble movement, and point totals all remain consistent.

## Nobles & Scoring

- [x] TC088: Make a player eligible for exactly one noble and verify it is auto-claimed immediately after a purchase.
- [x] TC089: Make a player eligible for multiple nobles after a purchase and verify only one noble is claimed that turn.
- [x] TC090: Verify a claimed noble is removed from `Nobles In Play` and added permanently to the player.
- [x] TC091: Verify noble requirements depend only on purchased card bonuses, not loose gems or reserved cards.
- [x] TC092: Verify nobles are not claimed after gem-taking or reserve actions.
- [x] TC093: Verify player points always equal purchased-card points plus noble points.
- [x] TC094: Verify reserved cards never count toward points or permanent bonuses.
- [x] TC095: Buy a zero-point or low-point card that makes the player eligible for a noble and verify the noble still grants points correctly.
- [x] TC096: Verify the final results screen shows correct values for points, purchased cards, reserved cards, and nobles.
- [x] TC097: Verify the `Purchased Bonus Stacks` summary matches the player's actual purchased cards by bonus color and total points in each stack.

## Endgame & Winners

- [x] TC098: Have player 1 reach the target first and verify the game continues only until the remaining players finish that round.
- [x] TC099: In a 3-player or 4-player game, have a middle-turn player reach the target and verify only the players after them receive final turns.
- [x] TC100: Have the last player in turn order reach the target and verify the game ends immediately after that turn.
- [x] TC101: Verify `finalRoundStarted` is set only once and is not restarted if another player later also reaches the target.
- [x] TC102: Verify a player can exceed the target, such as reaching 17, and still be evaluated normally for winning.
- [x] TC103: Have two players tie on points at game end and verify the winner is the tied player with fewer purchased development cards.
- [x] TC104: Have two players tie on both points and purchased-card count and verify both are shown as winners.
- [x] TC105: Verify the final results screen marks all winners correctly in tie scenarios.
- [x] TC106: Verify quitting the game early does not show the final results screen.
- [x] TC107: Reach the target because of a noble claimed during a purchase and verify final-round logic still starts correctly.
- [x] TC108: Reach the target by buying a reserved card and verify endgame behavior is identical to reaching the target via a visible-card purchase.

## AI General & Messaging

- [x] TC109: Start a game with an `EASY` AI player and verify the created player is AI-controlled and behaves as `EasyStrategy`.
- [x] TC110: Start a game with a `MEDIUM` AI player and verify the created player is AI-controlled and behaves as `MediumStrategy`.
- [x] TC111: Start a game with a `HARD` AI player and verify the created player is AI-controlled and behaves as `HardStrategy`.
- [x] TC112: Verify each AI turn prints the selected action before execution and prints a completion or failure message afterward.
- [x] TC113: Verify AI action text uses 1-based numbering for visible-card slots and reserved-card slots, and that the printed slot matches the actual affected card.
- [x] TC114: Verify AI forced-discard handling always returns the AI to 10 or fewer tokens before the turn ends.
- [x] TC115: Verify the UI handles a null AI action safely by skipping the turn without crashing.
- [x] TC116: Verify the UI handles an AI action failure safely without desynchronizing the board state or turn order.
- [x] TC117: In a mixed human/AI game, verify turn order, `Current Player`, and `Your Hand - <name>` always match the actual active player.

## EASY AI

- [x] TC118: Give `EASY` both an affordable visible card and an affordable reserved card and verify it buys the visible card first.
- [x] TC119: Give `EASY` no affordable visible cards but an affordable reserved card and verify it buys the reserved card.
- [x] TC120: Give `EASY` no buy actions but both a legal 3-different-gems action and a legal 2-same-gems action and verify it chooses 3 different first.
- [x] TC121: Give `EASY` no buy or gem action but at least one reservable visible card and reserve space, and verify it reserves a visible card.
- [x] TC122: Give `EASY` no legal move and verify it fails or skips safely without crashing or corrupting state.

## MEDIUM AI

- [x] TC123: Give `MEDIUM` both an affordable reserved card and an affordable visible card and verify it buys the reserved card first.
- [x] TC124: Give `MEDIUM` multiple affordable visible cards and verify it chooses according to the current `scoreCard` logic (`points * 10 + current bonus count for that bonus color`), not stale comment text.
- [x] TC125: Give `MEDIUM` no buy actions, reserve space available, and multiple tier-2 / tier-3 cards, and verify it reserves an unaffordable card with at least 2 points based on its current scoring logic.
- [x] TC126: Give `MEDIUM` a clear target shortage and verify it chooses a legal targeted gem action when a legal 3-different or 2-same option exists.
- [x] TC127: Give `MEDIUM` no better move, reserve space available, and at least one non-empty deck, and verify it eventually reserves the top of a deck.
- [x] TC128: Force `MEDIUM` into forced discard and verify it prefers discarding excess non-gold colors before random fallback.
- [x] TC129: Force `MEDIUM` into a state where only 1 or 2 different gem colors remain available and verify the AI does not crash, does not silently waste turns, and does not desynchronize printed intent from actual state.

## HARD AI

- [x] TC130: Give `HARD` an affordable reserved tier-3 card and another affordable option and verify it buys the reserved tier-3 card first.
- [x] TC131: Give `HARD` no affordable reserved tier-3 card but another affordable reserved card and verify it buys the reserved card before visible-card actions.
- [x] TC132: Give `HARD` an affordable visible card worth at least 3 points and verify it takes that opportunistic buy when higher-priority reserved buys do not apply.
- [x] TC133: In a 2-player game, give the opponent 10 or more points and near-affordability for a visible point card, then verify `HARD` uses the blocking reserve behavior when appropriate.
- [x] TC134: Give `HARD` no current high-value reserved target, reserve space available, and visible tier-3 cards, and verify it reserves a tier-3 card or a high-point tier-2 fallback according to current logic.
- [x] TC135: Give `HARD` a reserved high-value target and an affordable engine card whose bonus helps that target, and verify it buys the engine card when the earlier priorities do not apply.
- [x] TC136: Give `HARD` a reserved high-value target and useful visible cards that support it, and verify it may reserve a useful visible card when earlier priorities do not apply.
- [x] TC137: Force `HARD` into forced discard and verify it avoids discarding gold unless no non-gold discard exists.
- [x] TC138: Force `HARD` into a state where only 1 or 2 different gem colors are available for its fallback or target-taking logic and verify the AI does not crash, does not silently waste turns, and does not desynchronize printed intent from actual state.

## Data, Model Invariants, and Regression

- [x] TC139: Verify the bundled `cards.csv` loads exactly 40 tier-1 cards, 30 tier-2 cards, and 20 tier-3 cards before the initial 4-card deal per tier.
- [x] TC140: Verify the bundled `nobles.csv` loads exactly 6 nobles before random selection.
- [x] TC141: Verify bank counts never go negative during normal gameplay, AI turns, discard flows, or late-game depletion.
- [x] TC142: Verify player gem counts never go negative during payment, discard, or failed actions.
- [x] TC143: Verify a card can never exist in two places at once (deck, visible row, reserved cards, purchased cards).
- [x] TC144: Verify a noble cannot be claimed twice and never remains in play after being claimed.
- [x] TC145: Play until one or more decks empty and verify rendering, buying, and reserving remain stable with no crashes.
- [x] TC146: Empty all visible rows and verify the board still renders correctly and unavailable-action reasons remain accurate.
- [x] TC147: Reach a state with empty decks, empty bank, and no legal actions, then verify the game remains stable and offers only the appropriate menu options.
- [x] TC148: Play a reserve-heavy, discard-heavy game and verify bank totals and player totals remain conserved throughout.
- [x] TC149: Run long AI-vs-AI sessions across `EASY`, `MEDIUM`, and `HARD` combinations and verify there are no deadlocks, infinite skips, or crashes.
- [ ] TC150: Re-test previously risky areas and newly audited edge cases together: final-round ending, winner display on ties, discard-after-action flow, AI action rendering, duplicate-name setup, and AI fallback gem-taking.

## Notes

- Automated audit execution completed against the current build: `144/150` test cases passed.
- Remaining failing cases: `TC017`, `TC018`, `TC019`, `TC020`, `TC043`, and `TC150`.
- `TC043` is currently a checklist/code drift issue: the app now allows taking `1-3` different gems and dynamically labels the action based on how many colors are available, so the old "must be unavailable below 3 colors" expectation no longer matches the current implementation.
- `HARD` AI is implemented in the current codebase and was included in normal coverage.
