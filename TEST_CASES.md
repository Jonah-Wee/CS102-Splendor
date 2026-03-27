# Splendor Test Cases

Result: Pending (`0/74` test cases run).

## Setup & Config

- [ ] TC01: Start a 2-player game and verify bank setup is 4 of each non-gold gem, 5 gold, 3 nobles, and up to 4 visible cards per tier.
- [ ] TC02: Start a 3-player game and verify bank setup is 5 of each non-gold gem, 5 gold, 4 nobles, and up to 4 visible cards per tier.
- [ ] TC03: Start a 4-player game and verify bank setup is 7 of each non-gold gem, 5 gold, 5 nobles, and up to 4 visible cards per tier.
- [ ] TC04: Verify the first player in the input list always gets the first turn.
- [ ] TC05: Set `win.points` in `config.properties` to a non-default value like 10 and verify the game ends using that target.
- [ ] TC06: Leave `win.points` blank or invalid and verify it falls back to 15.
- [ ] TC07: Set `cards.filepath` to an invalid file and verify startup fails with a clear error.
- [ ] TC08: Set `nobles.filepath` to an invalid file and verify startup fails with a clear error.
- [ ] TC09: Use valid alternate card/noble files and verify the game loads them correctly.
- [ ] TC10: Create AI players through the setup prompts and verify the chosen difficulty is stored and used for each AI player.

## Gem-Taking Rules & Token Limit

- [ ] TC11: Take 3 different gems with 3 available colors and verify the player gains exactly 1 of each and the bank loses exactly 1 of each.
- [ ] TC12: Try taking 3 different gems but pick the same color twice and verify the move is rejected.
- [ ] TC13: Try taking 3 different gems including `GOLD` and verify the move is rejected.
- [ ] TC14: Reduce the bank so only 2 non-gold colors remain and verify `Take 3 different gems` is unavailable/rejected.
- [ ] TC15: Take 2 same gems from a color with exactly 4 in the bank and verify it succeeds.
- [ ] TC16: Try taking 2 same gems from a color with only 3 in the bank and verify it fails.
- [ ] TC17: Try taking 2 same gems of `GOLD` and verify it fails.
- [ ] TC18: Put a player at 10 tokens, take 3 different gems, verify the action is allowed, the player temporarily reaches 13, and the discard flow starts.
- [ ] TC19: Put a player at 9 tokens, take 2 same gems, verify the action is allowed, the player temporarily reaches 11, and the discard flow starts.
- [ ] TC20: During forced discard after gem-taking, verify the player may return some or all of the gems just taken.
- [ ] TC21: Verify gem-taking plus forced discard updates both player totals and bank totals correctly.

## Reserve Rules & Token Limit

- [ ] TC22: Reserve a visible card while gold is available and verify the card moves to reserved hand, the player gets 1 gold, and the board slot refills.
- [ ] TC23: Reserve a visible card when gold is empty and verify the reserve still works but no gold is given.
- [ ] TC24: Reserve the top card of a non-empty deck and verify the card goes to reserved hand and gold is handled correctly.
- [ ] TC25: Try reserving from the top of an empty deck and verify the move fails.
- [ ] TC26: Give a player 3 reserved cards and verify both reserve actions are unavailable/rejected.
- [ ] TC27: Put a player at 10 tokens, reserve a card while gold is available, and verify the action is allowed, the player temporarily reaches 11, and forced discard starts.
- [ ] TC28: Put a player at 10 tokens, reserve a card while gold is unavailable, and verify the reserve succeeds without a discard prompt.
- [ ] TC29: During forced discard after reserving, verify the player may discard the gold token they just received.
- [ ] TC30: Reserve a visible card from a tier whose deck is empty and verify the visible slot becomes empty instead of crashing.

## Buying & Payment

- [ ] TC31: Buy a visible card using only colored gems and verify gems are removed from the player, returned to the bank, and the card moves to purchased cards.
- [ ] TC32: Buy a visible card using permanent discounts from purchased bonuses and verify the reduced cost is applied correctly.
- [ ] TC33: Buy a visible card using gold as a wildcard and verify the correct amount of gold is spent and returned to the bank.
- [ ] TC34: Buy a card where discounts reduce some color costs below zero and verify those costs clamp to zero.
- [ ] TC35: Try buying a visible card the player cannot afford and verify the move fails with no state change.
- [ ] TC36: Buy a reserved card and verify it is removed from reserved hand, added to purchased cards, and no board refill happens.
- [ ] TC37: Try buying a reserved card with an invalid index and verify the code handles it safely.
- [ ] TC38: Verify visible card slots refill immediately after buying when the tier deck still has cards.
- [ ] TC39: Verify visible card slots do not refill when the deck is empty.
- [ ] TC40: Verify purchased cards increase bonus counts exactly by their bonus color and never by any other color.

## Nobles & Scoring

- [ ] TC41: Make a player eligible for exactly one noble and verify it is auto-claimed after a purchase.
- [ ] TC42: Make a player eligible for multiple nobles at once and verify only one noble is claimed that turn.
- [ ] TC43: Verify a claimed noble is removed from nobles in play and added to the player permanently.
- [ ] TC44: Verify noble requirements are based only on purchased card bonuses, not loose gems or reserved cards.
- [ ] TC45: Verify player points equal the sum of purchased card points and noble points.
- [ ] TC46: Verify reserved cards never count as purchased development cards for points or tiebreaks.
- [ ] TC47: Verify buying cards never creates a token-overflow problem, since purchases spend or keep tokens rather than add new ones.

## Endgame & Tiebreak

- [ ] TC48: Have player 1 reach the target first and verify the game continues only until the rest of the players finish that round.
- [ ] TC49: Have the last player in turn order reach the target and verify the game ends immediately after that turn.
- [ ] TC50: Have two players tie on points at game end and verify the winner is the tied player with fewer purchased development cards.
- [ ] TC51: Have two players tie on both points and purchased development cards and verify both are shown as winners.
- [ ] TC52: Verify a player can exceed the target, like reaching 17, and still be evaluated normally.
- [ ] TC53: Verify the final-round flag starts only once and does not restart if another player later reaches the target.
- [ ] TC54: Verify the final results screen marks winners using the tiebreak-aware winner logic.

## AI-Specific

- [ ] TC55: Run turns with an `EASY` AI player and verify it only attempts legal actions.
- [ ] TC56: Run turns with a `MEDIUM` AI player and verify it only attempts legal actions.
- [ ] TC57: Verify the AI may legally go over 10 during a gem-taking action and then auto-discard back to 10 before the turn ends.
- [ ] TC58: Verify the AI may legally go over 10 during a reserve action with gold and then auto-discard back to 10 before the turn ends.
- [ ] TC59: Verify AI action text shows human-friendly 1-based slot numbers for visible and reserved card actions.
- [ ] TC60: Verify AI can buy visible cards, buy reserved cards, reserve cards, and take gems without desync between printed action and actual state.
- [ ] TC61: If the AI has no legal move, verify it skips safely instead of crashing.
- [ ] TC62: Verify selecting different AI difficulties at setup actually produces the intended strategy classes and behavior paths for `EASY` and `MEDIUM`.

## CLI / Input / UX

- [ ] TC63: For every action flow, enter `0` at the first submenu and verify it goes back cleanly.
- [ ] TC64: For gem selection, enter `0` on the second or third pick and verify the whole action is cancelled cleanly.
- [ ] TC65: For gem confirmation, choose `n` and verify the action is cancelled with no state change.
- [ ] TC66: During forced discard, enter valid gem choices and verify the prompt repeats until the player is back at 10 tokens.
- [ ] TC67: Choose `Quit`, answer `n`, and verify the game resumes.
- [ ] TC68: Choose `Quit`, answer `y`, and verify the game exits cleanly.
- [ ] TC69: Enter invalid input like letters, blank lines, or out-of-range numbers and verify the CLI re-prompts instead of crashing.
- [ ] TC70: Verify the board is printed once per turn and not duplicated after actions.

## Stress / Regression

- [ ] TC71: Run 20+ AI-vs-AI games in a row and verify no crashes, stuck turns, or impossible states.
- [ ] TC72: Play a game with heavy reserve and discard usage and verify token counts, bank counts, and forced discard all remain correct.
- [ ] TC73: Play a game where decks and visible rows empty out late-game and verify the board remains stable with no crashes.
- [ ] TC74: Re-test the previously fixed bugs: malformed AI gem actions, last-player final-round ending, duplicate board printing, winner display on ties, and the new discard-after-action flow.

## Notes

- All test cases are currently unchecked because this file has been regenerated for the latest rules and code, but not executed yet.
- `HardStrategy` is currently a work in progress and is expected to throw `UnsupportedOperationException` if selected, so exclude `HARD` from normal gameplay test runs until it is implemented.
