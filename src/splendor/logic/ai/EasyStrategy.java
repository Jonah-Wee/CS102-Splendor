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
 *  2. Take 3 different gems from whatever is available (no targeting).
 *  TODO 3. Take 2 same gems if possible (no targeting)
 *  TODO 4. Reserve a card at random
 * 
 */
public class EasyStrategy implements AIStrategy {

    @Override
    public AIAction selectAction(GameState state, Player self) {
        AIAction buyVisible = tryBuyVisible(state, self);
        if (buyVisible != null) {
            return buyVisible;
        }

        AIAction takeGems = takeGems(state, self);
        if (takeGems != null) {
            return takeGems;
        }

        return null;
    }

    // Step 1 — buy the first affordable visible card, no preference for points
    private AIAction tryBuyVisible(GameState state, Player self) {
        for (Tier tier : Tier.values()) {
            List<Card> visible = state.getVisibleCards(tier);
            for (int i = 0; i < visible.size(); i++) {
                Card card = visible.get(i);

                // checks if there is a card unreplaced
                if (card == null) continue;
                
                if (canAfford(self, card)) {
                    return AIAction.buyVisible(tier, i);
                }
            }
        }
        return null;
    }

    // Step 2 — take up to 3 different gems from whatever is available in the bank
    private AIAction takeGems(GameState state, Player self) {
        GemBank bank = state.getGemBank();
        List<GemColor> toTake = new ArrayList<>();

        for (GemColor color : GemColor.values()) {
            if (toTake.size() == 3) break;
            if (color != GemColor.GOLD && bank.getGemCount(color) > 0) {
                toTake.add(color);
            }
        }

        if (toTake.size() >= 3 && self.getTotalGems() + 3 <= 10) {
            return AIAction.takeGems(toTake.subList(0, 3));
        }

        if (self.getTotalGems() + 2 <= 10) {
            for (GemColor color : GemColor.values()) {
                if (color != GemColor.GOLD && bank.getGemCount(color) >= 4) {
                    return AIAction.takeGems(List.of(color, color));
                }
            }
        }

        return null;
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
