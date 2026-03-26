# Splendor Test Cases

Result: `65/65` test cases passed.

## Setup & Config

- [x] TC01: Start a 2-player game and verify bank setup is 4 of each non-gold gem, 5 gold, 3 nobles, and up to 4 visible cards per tier.
- [x] TC02: Start a 3-player game and verify bank setup is 5 of each non-gold gem, 5 gold, 4 nobles, and up to 4 visible cards per tier.
- [x] TC03: Start a 4-player game and verify bank setup is 7 of each non-gold gem, 5 gold, 5 nobles, and up to 4 visible cards per tier.
- [x] TC04: Verify the first player in the input list always gets the first turn.
- [x] TC05: Set `win.points` in `config.properties` to a non-default value like 10 and verify the game ends using that target.
- [x] TC06: Leave `win.points` blank or invalid and verify it falls back to 15.
- [x] TC07: Set `cards.filepath` or `nobles.filepath` to an invalid file and verify startup fails with a clear error.
- [x] TC08: Use valid alternate card/noble files and verify the game loads them correctly.

## Gem-Taking Rules

- [x] TC09: Take 3 different gems with 3 available colors and verify player gains exactly 1 of each and bank loses exactly 1 of each.
- [x] TC10: Try taking 3 different gems but pick the same color twice and verify the move is rejected.
- [x] TC11: Try taking 3 different gems including GOLD and verify the move is rejected.
- [x] TC12: Reduce the bank so only 2 non-gold colors remain and verify `Take 3 different gems` is unavailable/rejected.
- [x] TC13: Take 2 same gems from a color with exactly 4 in bank and verify it succeeds.
- [x] TC14: Try taking 2 same gems from a color with only 3 in bank and verify it fails.
- [x] TC15: Try taking 2 same gems of GOLD and verify it fails.
- [x] TC16: Put a player at 8 gems and verify `Take 3 different gems` is unavailable/rejected because of the 10-token limit.
- [x] TC17: Put a player at 9 gems and verify `Take 2 same gems` is unavailable/rejected because of the 10-token limit.
- [x] TC18: Verify gem-taking updates both player totals and bank totals correctly after repeated turns.

## Reserve Rules

- [x] TC19: Reserve a visible card while gold is available and verify the card moves to reserved hand, player gets 1 gold, and the board slot refills.
- [x] TC20: Reserve a visible card when gold is empty and verify the reserve still works but no gold is given.
- [x] TC21: Reserve the top card of a non-empty deck and verify the card goes to reserved hand and gold is handled correctly.
- [x] TC22: Try reserving from the top of an empty deck and verify the move fails.
- [x] TC23: Give a player 3 reserved cards and verify both reserve actions are unavailable/rejected.
- [x] TC24: Put a player at the token limit where taking gold from reserve would exceed 10, and verify reserve is unavailable/rejected.
- [x] TC25: Reserve a visible card from a tier whose deck is empty and verify the visible slot becomes empty instead of crashing.

## Buying & Payment

- [x] TC26: Buy a visible card using only colored gems and verify gems are removed from the player, returned to the bank, and the card moves to purchased cards.
- [x] TC27: Buy a visible card using permanent discounts from purchased bonuses and verify the reduced cost is applied correctly.
- [x] TC28: Buy a visible card using gold as a wildcard and verify the correct amount of gold is spent and returned to the bank.
- [x] TC29: Buy a card where discounts reduce some color costs below zero and verify those costs clamp to zero.
- [x] TC30: Try buying a visible card the player cannot afford and verify the move fails with no state change.
- [x] TC31: Buy a reserved card and verify it is removed from reserved hand, added to purchased cards, and no board refill happens.
- [x] TC32: Try buying a reserved card with an invalid index and verify the code handles it safely.
- [x] TC33: Verify visible card slots refill immediately after buying when the tier deck still has cards.
- [x] TC34: Verify visible card slots do not refill when the deck is empty.
- [x] TC35: Verify purchased cards increase bonus counts exactly by their bonus color and never by any other color.

## Nobles & Scoring

- [x] TC36: Make a player eligible for exactly one noble and verify it is auto-claimed after a purchase.
- [x] TC37: Make a player eligible for multiple nobles at once and verify only one noble is claimed that turn.
- [x] TC38: Verify a claimed noble is removed from nobles in play and added to the player permanently.
- [x] TC39: Verify noble requirements are based only on purchased card bonuses, not loose gems or reserved cards.
- [x] TC40: Verify player points equal sum of purchased card points + noble points.
- [x] TC41: Verify reserved cards never count as purchased development cards for points or tiebreaks.

## Endgame & Tiebreak

- [x] TC42: Have player 1 reach the target first and verify the game continues only until the rest of the players finish that round.
- [x] TC43: Have the last player in turn order reach the target and verify the game ends immediately after that turn.
- [x] TC44: Have two players tie on points at game end and verify the winner is the one with fewer purchased development cards.
- [x] TC45: Have two players tie on both points and purchased development cards and verify both are shown as winners.
- [x] TC46: Verify a player can exceed the target, like reaching 17, and still be evaluated normally.
- [x] TC47: Verify the final-round flag starts only once and does not restart if another player later reaches the target.

## AI-Specific

- [x] TC48: Run a full AI vs AI game to completion and verify there are no exceptions or illegal moves.
- [x] TC49: Verify the AI never attempts `takeThreeDifferentGems` with fewer than 3 gem colors.
- [x] TC50: Verify the AI respects the 10-token limit before taking gems or reserving.
- [x] TC51: Verify AI action text shows human-friendly 1-based slot numbers.
- [x] TC52: Verify AI can buy visible cards, buy reserved cards, reserve cards, and take gems without desync between printed action and actual state.
- [x] TC53: If the AI has no legal move, verify it skips safely instead of crashing.

## CLI / Input / UX

- [x] TC54: For every action flow, enter 0 at the first submenu and verify it goes back cleanly.
- [x] TC55: For gem selection, enter 0 on the second or third pick and verify the whole action is cancelled cleanly.
- [x] TC56: For gem confirmation, choose n and verify the action is cancelled with no state change.
- [x] TC57: Choose Quit, answer n, and verify the game resumes.
- [x] TC58: Choose Quit, answer y, and verify the game exits cleanly.
- [x] TC59: Enter invalid input like letters, blank lines, or out-of-range numbers and verify the CLI re-prompts instead of crashing.
- [x] TC60: Verify the board is printed once per turn and not duplicated after actions.

## Stress / Regression

- [x] TC61: Run 20+ AI-vs-AI games in a row and verify no crashes, stuck turns, or impossible states.
- [x] TC62: Play a game where all tier-1 cards are bought out as much as possible and verify empty rows/decks behave safely.
- [x] TC63: Play a game with heavy reserve usage and verify reserved hand limits, gold flow, and reserved buying remain correct.
- [x] TC64: Play a game that ends in a point tie to verify the tiebreak rule still works after repeated purchases and noble claims.
- [x] TC65: Re-test the previously fixed bugs: AI malformed gem actions, last-player final-round ending, duplicate board printing, and winner display on tied points.

## Notes

- No gameplay logic failures were found in this test run.
