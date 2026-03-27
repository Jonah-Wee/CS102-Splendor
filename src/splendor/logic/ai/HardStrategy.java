package splendor.logic.ai;

import java.util.*;
import splendor.entities.Card;
import splendor.entities.GemBank;
import splendor.entities.GemColor;

import splendor.entities.Player;
import splendor.entities.Tier;
import splendor.logic.GameState;

/**
 * Hard AI strategy.
 *
 * Decision priority (highest to lowest):
 *
 *  1.  Buy the reserved tier-3 card if affordable.
 *  2.  Buy any other affordable reserved card (e.g. from blocking).
 *  3.  Opportunistic buy — grab any visible card worth 3+ points if affordable.
 *  4.  (2-player only) Block opponent if they have 10+ points and are close to a card.
 *  5.  Reserve a tier-3 card (or tier-2 fallback) if we don't have one.
 *  6.  Buy an affordable engine card whose bonus helps pay for the reserved target.
 *  7.  Buy any affordable visible card (never waste a turn when a purchase exists).
 *  8.  Take gems toward the best engine card we can't yet afford.
 *  9.  Take gems directly toward the reserved high-value card.
 *  10. Reserve a useful visible card whose bonus helps the target.
 *  11. Take whatever gems are available (3 different, 2 same, or fewer).
 *  12. Reserve from top of deck as a last resort for gold.
 */

public class HardStrategy implements AIStrategy {
    @Override
    public AIAction selectAction(GameState state, Player self) {

        // --- 1. Buy the reserved tier-3 card if we can afford it ---------------------
        AIAction buyTier3 = tryBuyReservedTier3(self);
        if (buyTier3 != null) return buyTier3;

        // --- 2. Buy any affordable reserved card (from blocking, etc.) ---------------
        AIAction buyReserved = tryBuyAnyReserved(self);
        if (buyReserved != null) return buyReserved;

        // --- 3. Opportunistic buy — grab any visible card with 3+ points we can afford
        AIAction opportunistic = tryOpportunisticBuy(state, self);
        if (opportunistic != null) return opportunistic;

        // --- 4. (2-player only) Block opponent if they have 10+ points and are
        //         close to buying a high-point visible card
        AIAction block = tryBlockOpponent(state, self);
        if (block != null) return block;

        // --- 5. Reserve a tier-3 (or high-point tier-2) if we don't have one ---------
        AIAction reserveHighValue = tryReserveHighValue(state, self);
        if (reserveHighValue != null) return reserveHighValue;

        // --- 6. Buy an affordable engine card (bonus helps the reserved target) ------
        AIAction buyEngine = tryBuyEngineCard(state, self);
        if (buyEngine != null) return buyEngine;

        // --- 7. Buy any affordable visible card (never waste a turn when a buy exists)
        AIAction buyAny = tryBuyAnyVisible(state, self);
        if (buyAny != null) return buyAny;

        // --- 8. Take gems toward the best engine card we can't yet afford ------------
        AIAction gemsForEngine = tryTakeGemsForEngine(state, self);
        if (gemsForEngine != null) return gemsForEngine;

        // --- 9. Take gems directly toward the reserved high-value card ---------------
        AIAction gemsForTarget = tryTakeGemsTowardTarget(state, self);
        if (gemsForTarget != null) return gemsForTarget;

        // --- 10. Reserve a useful visible card whose bonus helps the target -----------
        AIAction reserveEngine = tryReserveUsefulCard(state, self);
        if (reserveEngine != null) return reserveEngine;

        // --- 11. Take whatever gems are available ------------------------------------
        AIAction fallbackGems = takeAvailableGems(state);
        if (fallbackGems != null) return fallbackGems;

        // --- 12. Reserve from top of deck as last resort for gold --------------------
        AIAction reserveTop = tryReserveTopOfDeck(state, self);
        if (reserveTop != null) return reserveTop;

        return AIAction.takeGems(List.of());
    }

    // Step 1 — buy the reserved tier-3 card if we can afford it
    private AIAction tryBuyReservedTier3(Player self) {
        List<Card> reserved = self.getReservedCards();
        for (int i = 0; i < reserved.size(); i++) {
            Card card = reserved.get(i);
            if (card.getTier() == Tier.THREE && canAfford(self, card)) {
                return AIAction.buyReserved(i);
            }
        }
        return null;
    }

    // Step 2 — buy any affordable reserved card (may have been reserved via blocking)
    private AIAction tryBuyAnyReserved(Player self) {
        List<Card> reserved = self.getReservedCards();
        Card best = null;
        int bestIndex = -1;
        int bestPoints = -1;

        for (int i = 0; i < reserved.size(); i++) {
            Card card = reserved.get(i);
            if (card.getTier() == Tier.THREE) continue; // handled by step 1
            if (canAfford(self, card)) {
                if (card.getPoints() > bestPoints) {
                    bestPoints = card.getPoints();
                    best = card;
                    bestIndex = i;
                }
            }
        }
        return best != null ? AIAction.buyReserved(bestIndex) : null;
    }

    // Step 3 — opportunistic buy: grab any visible card with 3+ points we can afford
    private AIAction tryOpportunisticBuy(GameState state, Player self) {
        Card best = null;
        Tier bestTier = null;
        int bestSlot = -1;
        int bestPoints = 0;

        for (Tier tier : Tier.values()) {
            List<Card> visible = state.getVisibleCards(tier);
            for (int i = 0; i < visible.size(); i++) {
                Card card = visible.get(i);
                if (card == null) continue;
                if (card.getPoints() < 3) continue;

                if (canAfford(self, card) && card.getPoints() > bestPoints) {
                    bestPoints = card.getPoints();
                    best = card;
                    bestTier = tier;
                    bestSlot = i;
                }
            }
        }
        return best != null ? AIAction.buyVisible(bestTier, bestSlot) : null;
    }

    // Step 4 — (2-player only) block opponent by reserving a card they're close to buying
    private AIAction tryBlockOpponent(GameState state, Player self) {
        // Only activate in 2-player games
        if (state.getPlayers().size() != 2) return null;
        if (self.getReservedCards().size() >= 3) return null;

        // Find the opponent
        Player opponent = null;
        for (Player p : state.getPlayers()) {
            if (p != self) {
                opponent = p;
                break;
            }
        }
        if (opponent == null || opponent.getPoints() < 10) return null;

        // Scan all visible cards for ones the opponent is close to affording
        Card bestBlock = null;
        Tier bestTier = null;
        int bestSlot = -1;
        int bestScore = -1; // higher = more urgent to block

        for (Tier tier : Tier.values()) {
            List<Card> visible = state.getVisibleCards(tier);
            for (int i = 0; i < visible.size(); i++) {
                Card card = visible.get(i);
                if (card == null) continue;
                if (card.getPoints() <= 0) continue; // only block point-giving cards

                // Compute opponent's shortage accounting for bonuses
                Map<GemColor, Integer> shortage = computeShortage(opponent, card);
                int totalShortage = shortage.values().stream()
                        .mapToInt(Integer::intValue).sum();

                // Account for opponent's gold tokens
                int effectiveShortage = Math.max(0, totalShortage - opponent.getGemCount(GemColor.GOLD));

                if (effectiveShortage > 2) continue; // too far away, not worth blocking

                // Score: prioritize cards they can buy NOW, then by points
                // Lower effective shortage = more urgent; higher points = more dangerous
                int score = (3 - effectiveShortage) * 100 + card.getPoints() * 10;
                if (score > bestScore) {
                    bestScore = score;
                    bestBlock = card;
                    bestTier = tier;
                    bestSlot = i;
                }
            }
        }

        return bestBlock != null ? AIAction.reserveVisible(bestTier, bestSlot) : null;
    }

    // Step 5 — reserve a tier-3 card (or high-point tier-2 if no tier-3 available)
    private AIAction tryReserveHighValue(GameState state, Player self) {
        if (self.getReservedCards().size() >= 3) return null;

        // Check if we already have a tier-3 reserved (tier-2 from blocking doesn't count)
        for (Card card : self.getReservedCards()) {
            if (card.getTier() == Tier.THREE) return null;
        }

        // First try tier-3: pick the one with fewest distinct gem types
        Card best = null;
        Tier bestTier = null;
        int bestSlot = -1;
        int fewestTypes = Integer.MAX_VALUE;

        List<Card> tier3Visible = state.getVisibleCards(Tier.THREE);
        for (int i = 0; i < tier3Visible.size(); i++) {
            Card card = tier3Visible.get(i);
            if (card == null) continue;
            int types = countGemTypes(card);
            if (types < fewestTypes) {
                fewestTypes = types;
                best = card;
                bestTier = Tier.THREE;
                bestSlot = i;
            }
        }

        // If no tier-3 available, fall back to highest-point tier-2 card
        if (best == null) {
            int bestPoints = 0;
            List<Card> tier2Visible = state.getVisibleCards(Tier.TWO);
            for (int i = 0; i < tier2Visible.size(); i++) {
                Card card = tier2Visible.get(i);
                if (card == null) continue;
                if (card.getPoints() > bestPoints) {
                    bestPoints = card.getPoints();
                    best = card;
                    bestTier = Tier.TWO;
                    bestSlot = i;
                }
            }
        }

        return best != null ? AIAction.reserveVisible(bestTier, bestSlot) : null;
    }

    // Step 6 — buy an affordable engine card (bonus helps the reserved target)
    private AIAction tryBuyEngineCard(GameState state, Player self) {
        Card target = findReservedHighValue(self);
        if (target == null) return null;

        Map<GemColor, Integer> targetShortage = computeShortage(self, target);
        if (targetShortage.isEmpty()) return null; // already affordable

        Set<GemColor> neededColors = targetShortage.keySet();

        Card bestCard = null;
        Tier bestTier = null;
        int bestSlot = -1;
        int bestScore = -1;

        for (Tier tier : List.of(Tier.ONE, Tier.TWO)) {
            List<Card> visible = state.getVisibleCards(tier);
            for (int i = 0; i < visible.size(); i++) {
                Card card = visible.get(i);
                if (card == null) continue;
                if (!canAfford(self, card)) continue;
                GemColor bonus = card.getBonus();
                if (bonus == null || !neededColors.contains(bonus)) continue;

                int score = card.getPoints() * 10 + 200;
                if (score > bestScore) {
                    bestScore = score;
                    bestCard = card;
                    bestTier = tier;
                    bestSlot = i;
                }
            }
        }

        return bestCard != null ? AIAction.buyVisible(bestTier, bestSlot) : null;
    }

    // Step 7 — buy any affordable visible card (don't waste a turn when a buy is possible)
    private AIAction tryBuyAnyVisible(GameState state, Player self) {
        Card best = null;
        Tier bestTier = null;
        int bestSlot = -1;
        int bestScore = -1;

        Card target = findReservedHighValue(self);
        Set<GemColor> neededColors = target != null
                ? computeShortage(self, target).keySet()
                : Collections.emptySet();

        for (Tier tier : Tier.values()) {
            List<Card> visible = state.getVisibleCards(tier);
            for (int i = 0; i < visible.size(); i++) {
                Card card = visible.get(i);
                if (card == null) continue;
                if (!canAfford(self, card)) continue;

                // Score: points first, then bonus usefulness, then cheaper tier
                int score = card.getPoints() * 100;
                GemColor bonus = card.getBonus();
                if (bonus != null && neededColors.contains(bonus)) {
                    score += 50; // bonus helps the target
                }
                if (tier == Tier.ONE) score += 10; // prefer cheap cards

                if (score > bestScore) {
                    bestScore = score;
                    best = card;
                    bestTier = tier;
                    bestSlot = i;
                }
            }
        }

        return best != null ? AIAction.buyVisible(bestTier, bestSlot) : null;
    }

    // Step 8 — take gems toward the best engine card we can't yet afford
    private AIAction tryTakeGemsForEngine(GameState state, Player self) {
        Card target = findReservedHighValue(self);
        if (target == null) return null;

        Map<GemColor, Integer> targetShortage = computeShortage(self, target);
        if (targetShortage.isEmpty()) return null;

        Set<GemColor> neededColors = targetShortage.keySet();

        Card bestCard = null;
        int bestScore = Integer.MIN_VALUE;

        for (Tier tier : List.of(Tier.ONE, Tier.TWO)) {
            List<Card> visible = state.getVisibleCards(tier);
            for (int i = 0; i < visible.size(); i++) {
                Card card = visible.get(i);
                if (card == null) continue;
                GemColor bonus = card.getBonus();
                if (bonus == null || !neededColors.contains(bonus)) continue;

                int shortage = computeShortage(self, card).values().stream()
                        .mapToInt(Integer::intValue).sum();

                int score = card.getPoints() * 10 + 200 - shortage * 5;
                if (score > bestScore) {
                    bestScore = score;
                    bestCard = card;
                }
            }
        }

        return bestCard != null ? takeGemsToward(bestCard, state, self) : null;
    }

    // Step 9 — take gems directly toward the reserved high-value card
    private AIAction tryTakeGemsTowardTarget(GameState state, Player self) {
        Card target = findReservedHighValue(self);
        if (target == null) return null;

        return takeGemsToward(target, state, self);
    }

    // Step 10 — reserve a useful visible card whose bonus helps the target
    private AIAction tryReserveUsefulCard(GameState state, Player self) {
        if (self.getReservedCards().size() >= 3) return null;

        Card target = findReservedHighValue(self);
        Set<GemColor> neededColors = target != null
                ? computeShortage(self, target).keySet()
                : Collections.emptySet();

        if (neededColors.isEmpty()) return null;

        Card bestCard = null;
        Tier bestTier = null;
        int bestSlot = -1;
        int bestScore = -1;

        for (Tier tier : Tier.values()) {
            List<Card> visible = state.getVisibleCards(tier);
            for (int i = 0; i < visible.size(); i++) {
                Card card = visible.get(i);
                if (card == null) continue;
                GemColor bonus = card.getBonus();
                if (bonus == null) continue;

                if (!neededColors.contains(bonus)) continue;

                int score = 200 + card.getPoints() * 10;

                if (score > bestScore) {
                    bestScore = score;
                    bestCard = card;
                    bestTier = tier;
                    bestSlot = i;
                }
            }
        }

        return bestCard != null ? AIAction.reserveVisible(bestTier, bestSlot) : null;
    }

    // Step 11 — take whatever gems are available
    private AIAction takeAvailableGems(GameState state) {
        GemBank bank = state.getGemBank();

        List<GemColor> available = new ArrayList<>();
        for (GemColor color : GemColor.values()) {
            if (color != GemColor.GOLD && bank.getGemCount(color) > 0) {
                available.add(color);
            }
        }

        if (available.size() >= 3) {
            return AIAction.takeGems(available.subList(0, 3));
        }

        for (GemColor color : GemColor.values()) {
            if (color != GemColor.GOLD && bank.getGemCount(color) >= 4) {
                return AIAction.takeGems(List.of(color, color));
            }
        }

        if (!available.isEmpty()) {
            return AIAction.takeGems(available);
        }

        return null;
    }

    // Step 12 — reserve from top of deck as last resort for gold
    private AIAction tryReserveTopOfDeck(GameState state, Player self) {
        if (self.getReservedCards().size() >= 3) return null;

        for (Tier tier : List.of(Tier.THREE, Tier.TWO, Tier.ONE)) {
            if (!state.getDeck(tier).isEmpty()) {
                return AIAction.reserveTopOfDeck(tier);
            }
        }
        return null;
    }


    @Override
    public GemColor chooseGemToDiscard(GameState state, Player self) {
        Card target = findReservedHighValue(self);

        // Discard the non-gold color with the most excess relative to the target
        GemColor bestExcess = null;
        int maxExcess = 0;

        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            if (self.getGemCount(color) == 0) continue;

            int effectiveCost = target == null ? 0
                    : Math.max(0, target.getCost().getOrDefault(color, 0) - self.getBonusCount(color));
            int excess = self.getGemCount(color) - effectiveCost;

            if (excess > maxExcess) {
                maxExcess = excess;
                bestExcess = color;
            }
        }

        if (bestExcess != null) return bestExcess;

        // All gems contribute — pick any non-gold gem
        List<GemColor> held = new ArrayList<>();
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            if (self.getGemCount(color) > 0) {
                held.add(color);
            }
        }

        if (!held.isEmpty()) {
            return held.get(new Random().nextInt(held.size()));
        }

        return GemColor.GOLD;
    }
    
    // Helpers

    /**
     * Find the highest-value reserved card (tier-3 first, then tier-2).
     */
    private Card findReservedHighValue(Player self) {
        Card best = null;
        for (Card card : self.getReservedCards()) {
            if (card.getTier() == Tier.THREE) return card; // tier-3 always wins
            if (card.getTier() == Tier.TWO) {
                if (best == null || card.getPoints() > best.getPoints()) {
                    best = card;
                }
            }
        }
        return best;
    }

    /**
     * Take gems toward a target card (prefer 2-same over 3-different).
     */
    private AIAction takeGemsToward(Card target, GameState state, Player self) {
        Map<GemColor, Integer> shortage = computeShortage(self, target);
        if (shortage.isEmpty()) return null;

        GemBank bank = state.getGemBank();

        // Prefer taking 2 of the same color we're most short on
        GemColor bestDouble = null;
        int bestNeed = 0;
        for (Map.Entry<GemColor, Integer> entry : shortage.entrySet()) {
            GemColor color = entry.getKey();
            int need = entry.getValue();
            if (need >= 2 && bank.getGemCount(color) >= 4 && need > bestNeed) {
                bestNeed = need;
                bestDouble = color;
            }
        }
        if (bestDouble != null) {
            return AIAction.takeGems(List.of(bestDouble, bestDouble));
        }

        // Fall back to up to 3 different colors we're short on
        List<GemColor> toTake = new ArrayList<>();
        shortage.entrySet().stream()
                .filter(e -> bank.getGemCount(e.getKey()) > 0)
                .sorted(Map.Entry.<GemColor, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> toTake.add(e.getKey()));

        // Pad with other available colors if fewer than 3
        if (toTake.size() < 3) {
            for (GemColor color : GemColor.values()) {
                if (toTake.size() >= 3) break;
                if (color == GemColor.GOLD) continue;
                if (toTake.contains(color)) continue;
                if (bank.getGemCount(color) > 0) {
                    toTake.add(color);
                }
            }
        }

        return toTake.isEmpty() ? null : AIAction.takeGems(toTake);
    }

    /**
     * Can the player afford a card, accounting for bonuses and gold as wild?
     * Mirrors GameEngine.canAfford exactly.
     */
    private boolean canAfford(Player player, Card card) {
        int goldNeeded = 0;
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            int cost = card.getCost().getOrDefault(color, 0);
            int discount = player.getBonusCount(color);
            int remainingCost = Math.max(0, cost - discount);
            int gemsOwned = player.getGemCount(color);
            if (gemsOwned < remainingCost) {
                goldNeeded += remainingCost - gemsOwned;
            }
        }
        return player.getGemCount(GemColor.GOLD) >= goldNeeded;
    }

    /**
     * Returns the number of additional gems of each colour needed
     * to buy the card (after applying bonuses), clamped to > 0.
     */
    private Map<GemColor, Integer> computeShortage(Player player, Card card) {
        Map<GemColor, Integer> shortage = new EnumMap<>(GemColor.class);
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            int cost   = card.getCost().getOrDefault(color, 0);
            int bonus  = player.getBonusCount(color);
            int gems   = player.getGemCount(color);
            int deficit = Math.max(0, cost - bonus - gems);
            if (deficit > 0) shortage.put(color, deficit);
        }
        return shortage;
    }

    /**
     * Count how many distinct non-zero gem colors a card costs
     */
    private int countGemTypes(Card card) {
        int count = 0;
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            if (card.getCost().getOrDefault(color, 0) > 0) {
                count++;
            }
        }
        return count;
    }
}
