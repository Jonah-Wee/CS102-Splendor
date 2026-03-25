package splendor.logic.ai;

import java.util.*;
import java.util.stream.Collectors;
import splendor.entities.Card;
import splendor.entities.GemBank;
import splendor.entities.GemColor;
import splendor.entities.Player;
import splendor.entities.Tier;
import splendor.logic.GameState;

/**
 * Medium AI strategy.
 *
 * Decision priority (highest to lowest):
 *  1. Buy a reserved card if affordable.
 *  2. Buy a visible card — prefer highest points, break ties by cheapest cost.
 *  3. Reserve a tier-2 or tier-3 card if hand has room and a good target exists.
 *  4. Take gems that make the most progress toward the cheapest reachable card.
 *  5. Fallback: take up to 3 different gems of whatever is available.
 */
public class MediumStrategy implements AIStrategy {

    @Override
    public AIAction selectAction(GameState state, Player self) {

        // --- 1. Buy from reserved hand ------------------------------------------
        AIAction buyReserved = tryBuyReserved(state, self);
        if (buyReserved != null) return buyReserved;

        // --- 2. Buy a visible card -----------------------------------------------
        AIAction buyVisible = tryBuyVisible(state, self);
        if (buyVisible != null) return buyVisible;

        // --- 3. Reserve a promising card -----------------------------------------
        AIAction reserve = tryReserve(state, self);
        if (reserve != null) return reserve;

        // --- 4. Take gems toward a target ----------------------------------------
        AIAction takeGems = tryTakeGemsTowardTarget(state, self);
        if (takeGems != null) return takeGems;

        // --- 5. Fallback: take any available gems --------------------------------
        return fallbackTakeGems(state);
    }


    // Step 1 — buy from reserved hand
    private AIAction tryBuyReserved(GameState state, Player self) {
        List<Card> reserved = self.getReservedCards();
        Card best = null;
        int bestIndex = -1;
        int bestScore = -1;

        for (int i = 0; i < reserved.size(); i++) {
            Card card = reserved.get(i);
            if (canAfford(self, card, state.getGemBank())) {
                int score = scoreCard(card, self);
                if (score > bestScore) {
                    bestScore = score;
                    best = card;
                    bestIndex = i;
                }
            }
        }
        return best != null ? AIAction.buyReserved(bestIndex) : null;
    }

    // Step 2 — buy a visible card

    private AIAction tryBuyVisible(GameState state, Player self) {
        Card best = null;
        Tier bestTier = null;
        int bestSlot = -1;
        int bestScore = -1;

        for (Tier tier : Tier.values()) {
            List<Card> visible = state.getVisibleCards(tier);
            for (int i = 0; i < visible.size(); i++) {
                Card card = visible.get(i);
                if (card == null) continue;
                if (canAfford(self, card, state.getGemBank())) {
                    int score = scoreCard(card, self);
                    if (score > bestScore) {
                        bestScore = score;
                        best = card;
                        bestTier = tier;
                        bestSlot = i;
                    }
                }
            }
        }
        return best != null ? AIAction.buyVisible(bestTier, bestSlot) : null;
    }

    // Step 3 — reserve a promising card

    private AIAction tryReserve(GameState state, Player self) {
        // Only reserve if we have room
        if (self.getReservedCards().size() >= 3) return null;

        // Look for high-value cards in tier 2 and 3 that we can't yet afford
        Card best = null;
        Tier bestTier = null;
        int bestSlot = -1;
        int bestScore = -1;

        for (Tier tier : List.of(Tier.TWO, Tier.THREE)) {
            List<Card> visible = state.getVisibleCards(tier);
            for (int i = 0; i < visible.size(); i++) {
                Card card = visible.get(i);
                if (card == null) continue;
                if (!canAfford(self, card, state.getGemBank())) {
                    int score = scoreCard(card, self);
                    // Only reserve if it looks genuinely good
                    if (score > bestScore && card.getPoints() >= 2) {
                        bestScore = score;
                        best = card;
                        bestTier = tier;
                        bestSlot = i;
                    }
                }
            }
        }
        return best != null ? AIAction.reserveVisible(bestTier, bestSlot) : null;
    }

    // Step 4 — take gems toward a target card

    private AIAction tryTakeGemsTowardTarget(GameState state, Player self) {
        // Find the card we're closest to affording (fewest missing gems)
        Card target = findBestTarget(state, self);
        if (target == null) return null;

        // Figure out what we're short on for that card
        Map<GemColor, Integer> shortage = computeShortage(self, target);

        GemBank bank = state.getGemBank();

        // Try to take 2 of the same colour if shortage is high and bank has ≥4
        Optional<GemColor> doubleGem = shortage.entrySet().stream()
                .filter(e -> e.getValue() >= 2
                        && e.getKey() != GemColor.GOLD
                        && bank.getGemCount(e.getKey()) >= 4)
                .map(Map.Entry::getKey)
                .findFirst();

        if (doubleGem.isPresent()) {
            return AIAction.takeGems(List.of(doubleGem.get(), doubleGem.get()));
        }

        // Otherwise take up to 3 different colours we're short on
        // collect up to 3 colors we're short on, padding with other available colors if needed
        List<GemColor> toTake = shortage.entrySet().stream()
                .filter(e -> e.getKey() != GemColor.GOLD
                        && e.getValue() > 0
                        && bank.getGemCount(e.getKey()) > 0)
                .sorted(Map.Entry.<GemColor, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));

        // pad up to 3 with any other available colors
        if (toTake.size() < 3) {
            Arrays.stream(GemColor.values())
                    .filter(c -> c != GemColor.GOLD
                            && !toTake.contains(c)
                            && bank.getGemCount(c) > 0)
                    .limit(3 - toTake.size())
                    .forEach(toTake::add);
        }

        return toTake.isEmpty() ? null : AIAction.takeGems(toTake);
    }

    // Step 5 — fallback gem collection

    private AIAction fallbackTakeGems(GameState state) {
        GemBank bank = state.getGemBank();
        List<GemColor> available = Arrays.stream(GemColor.values())
                .filter(c -> c != GemColor.GOLD && bank.getGemCount(c) > 0)
                .limit(3)
                .collect(Collectors.toList());

        // Never return fewer gems than available up to 3
        if (available.isEmpty()) return AIAction.takeGems(List.of());
        while (available.size() < 3) {
            // pad with a repeat only if bank allows it — otherwise leave as-is
            break;
        }
        return AIAction.takeGems(available);
    }

    // Discard logic

    @Override
    public GemColor chooseGemToDiscard(GameState state, Player self) {
        Card target = findBestTarget(state, self);

        // Find the non-gold color with the most excess gems relative to the target
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

        if (bestExcess != null) {
            return bestExcess;
        }

        // All gems contribute — collect non-gold gems held and pick one at random
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
     * Can the player afford a card, accounting for bonuses from purchased cards?
     * Gold gems act as wild cards for any shortfall.
     */
    private boolean canAfford(Player player, Card card, GemBank bank) {
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

    /**
     * Score a card from the AI's perspective.
     * Higher is better. Factors: prestige points and colour usefulness.
     */
    private int scoreCard(Card card, Player self) {
        int score = card.getPoints() * 10;
        // Slight bonus for colours the player is already collecting
        GemColor bonus = card.getBonus();
        if (bonus != null) {
            score += self.getBonusCount(bonus);
        }
        return score;
    }

    /**
     * Among all visible cards we cannot yet afford, find the one
     * we're closest to (fewest total gems missing).
     */
    private Card findBestTarget(GameState state, Player self) {
        Card bestCard = null;
        int  minShortage = Integer.MAX_VALUE;

        for (Tier tier : Tier.values()) {
            for (Card card : state.getVisibleCards(tier)) {
                if (card == null) continue;
                int shortage = computeShortage(self, card).values()
                        .stream().mapToInt(Integer::intValue).sum();
                if (shortage < minShortage) {
                    minShortage = shortage;
                    bestCard = card;
                }
            }
        }
        return bestCard;
    }

    /**
     * Returns the number of additional gems of each colour needed
     * to buy the card (after applying bonuses), clamped to ≥0.
     */
    private Map<GemColor, Integer> computeShortage(Player player, Card card) {
        Map<GemColor, Integer> shortage = new EnumMap<>(GemColor.class);
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) continue;
            int cost    = card.getCost().getOrDefault(color, 0);
            int bonus   = player.getBonusCount(color);
            int gems    = player.getGemCount(color);
            int deficit = Math.max(0, cost - bonus - gems);
            if (deficit > 0) shortage.put(color, deficit);
        }
        return shortage;
    }
}
