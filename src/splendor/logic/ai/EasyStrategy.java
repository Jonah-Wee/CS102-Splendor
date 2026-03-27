package splendor.logic.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import splendor.entities.Card;
import splendor.entities.GemBank;
import splendor.entities.GemColor;
import splendor.entities.Player;
import splendor.entities.Tier;
import splendor.logic.GameState;

/**
 * Easy AI strategy.
 *
 * Decision priority (highest to lowest):
 *  1. Buy the first affordable visible card found (no scoring).
 *  2. Buy the first affordable reserved card found (no scoring).
 *  3. Take 3 different gems from whatever is available (no targeting).
 *  4. Take 2 same gems if possible (no targeting).
 *  5. Reserve a card at random.
 */
public class EasyStrategy implements AIStrategy {

    private final Random random = new Random();

    @Override
    public AIAction selectAction(GameState state, Player self) {
        AIAction buyVisible = tryBuyVisible(state, self);
        if (buyVisible != null) {
            return buyVisible;
        }

        // --- 2. Buy the first affordable reserved card ---------------------------
        AIAction buyReserved = tryBuyReserved(self);
        if (buyReserved != null) return buyReserved;

        // --- 3. Take 3 different gems (no targeting) -----------------------------
        AIAction threeGems = tryTakeThreeGems(state);
        if (threeGems != null) return threeGems;

        // --- 4. Take 2 same gems (no targeting) ----------------------------------
        AIAction twoGems = tryTakeTwoSameGems(state);
        if (twoGems != null) return twoGems;

        // --- 5. Reserve a card at random -----------------------------------------
        AIAction reserve = tryReserveRandom(state, self);
        if (reserve != null) return reserve;

        return AIAction.takeGems(List.of());
    }

    // Step 1 — buy the first affordable visible card, no preference
    private AIAction tryBuyVisible(GameState state, Player self) {
        for (Tier tier : Tier.values()) {
            List<Card> visible = state.getVisibleCards(tier);
            for (int i = 0; i < visible.size(); i++) {
                Card card = visible.get(i);
                if (card == null) continue;
                if (canAfford(self, card)) {
                    return AIAction.buyVisible(tier, i);
                }
            }
        }
        return null;
    }

    // Step 2 — buy the first affordable reserved card, no preference
    private AIAction tryBuyReserved(Player self) {
        List<Card> reserved = self.getReservedCards();
        for (int i = 0; i < reserved.size(); i++) {
            if (canAfford(self, reserved.get(i))) {
                return AIAction.buyReserved(i);
            }
        }
        return null;
    }

    // Step 3 — take up to 3 different gems from whatever is available in the bank
    private AIAction tryTakeThreeGems(GameState state) {
        GemBank bank = state.getGemBank();
        List<GemColor> toTake = new ArrayList<>();

        for (GemColor color : GemColor.values()) {
            if (toTake.size() == 3) break;
            if (color != GemColor.GOLD && bank.getGemCount(color) > 0) {
                toTake.add(color);
            }
        }

        return toTake.size() >= 3 ? AIAction.takeGems(toTake) : null;
    }

    // Step 4 — take 2 of the same gem if any color has at least 4 in the bank
    private AIAction tryTakeTwoSameGems(GameState state) {
        GemBank bank = state.getGemBank();
        for (GemColor color : GemColor.values()) {
            if (color != GemColor.GOLD && bank.getGemCount(color) >= 4) {
                return AIAction.takeGems(List.of(color, color));
            }
        }
        return null;
    }

    // Step 5 — reserve a random visible card if hand has room
    private AIAction tryReserveRandom(GameState state, Player self) {
        if (self.getReservedCards().size() >= 3) return null;

        List<Tier> tiersWithCards = new ArrayList<>();
        for (Tier tier : Tier.values()) {
            if (!state.getVisibleCards(tier).isEmpty()) {
                tiersWithCards.add(tier);
            }
        }
        if (tiersWithCards.isEmpty()) return null;

        Tier tier = tiersWithCards.get(random.nextInt(tiersWithCards.size()));
        List<Card> visible = state.getVisibleCards(tier);
        int slot = random.nextInt(visible.size());
        return AIAction.reserveVisible(tier, slot);
    }

    // Discard logic — pick a random gem from the player's hand

    @Override
    public GemColor chooseGemToDiscard(GameState state, Player self) {
        List<GemColor> held = new ArrayList<>();
        for (GemColor color : GemColor.values()) {
            if (self.getGemCount(color) > 0) {
                held.add(color);
            }
        }
        return held.get(new Random().nextInt(held.size()));
    }

    // Helpers

    /**
     * Can the player afford a card, accounting for bonuses from purchased cards?
     * Gold gems act as wild cards for any shortfall.
     */
    private boolean canAfford(Player player, Card card) {
        int goldNeeded = 0;
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            int cost    = card.getCost().getOrDefault(color, 0);
            int bonus   = player.getBonusCount(color);
            int gems    = player.getGemCount(color);
            int deficit = Math.max(0, cost - bonus - gems);
            goldNeeded += deficit;
        }
        return goldNeeded <= player.getGemCount(GemColor.GOLD);
    }
}
