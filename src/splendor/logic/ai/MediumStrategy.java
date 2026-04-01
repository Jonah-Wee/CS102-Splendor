package splendor.logic.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import splendor.entities.Card;
import splendor.entities.GemBank;
import splendor.entities.GemColor;
import splendor.entities.Player;
import splendor.entities.Tier;
import splendor.logic.GameState;

/**
 * AI strategy that balances card value with short-term progress.
 */
public class MediumStrategy implements AIStrategy {

    public MediumStrategy() {
    }

    @Override
    public AIAction selectAction(GameState state, Player self) {
        // --- 1. Buy best affordable reserved card ----------------------------------
        AIAction buyReserved = tryBuyReserved(state, self);
        if (buyReserved != null) {
            return buyReserved;
        }

        // --- 2. Buy best affordable visible card -----------------------------------
        AIAction buyVisible = tryBuyVisible(state, self);
        if (buyVisible != null) {
            return buyVisible;
        }

        // --- 3. Reserve a high-tier unaffordable card -----------------------------
        AIAction reserve = tryReserve(state, self);
        if (reserve != null) {
            return reserve;
        }

        // --- 4. Take gems towards the closest highest tier card ------------------
        AIAction takeGems = tryTakeGemsTowardTarget(state, self);
        if (takeGems != null) {
            return takeGems;
        }

        // --- 5. Take whatever gems are available ---------------------------------
        AIAction gems = takeAvailableGems(state);
        if (gems != null) return gems;

        // --- 6. Reserve from top of deck if hand has room ------------------------
        AIAction reserveTop = tryReserveTopOfDeck(state, self);
        if (reserveTop != null) return reserveTop;

        return AIAction.takeGems(List.of());
    }

    // Step 1 - buy the highest-scoring reserved card the player can afford
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

    // Step 2 - buy the highest-scoring visible board card the player can afford
    private AIAction tryBuyVisible(GameState state, Player self) {
        Card best = null;
        Tier bestTier = null;
        int bestSlot = -1;
        int bestScore = -1;

        for (Tier tier : Tier.values()) {
            List<Card> visible = state.getVisibleCards(tier);
            for (int i = 0; i < visible.size(); i++) {
                Card card = visible.get(i);
                if (card == null) {
                    continue;
                }
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

    // Step 3 - reserve the highest-scoring unaffordable tier 2/3 card worth >= 2 points
    private AIAction tryReserve(GameState state, Player self) {
        if (self.getReservedCards().size() >= 3) {
            return null;
        }

        Card best = null;
        Tier bestTier = null;
        int bestSlot = -1;
        int bestScore = -1;

        for (Tier tier : List.of(Tier.TWO, Tier.THREE)) {
            List<Card> visible = state.getVisibleCards(tier);
            for (int i = 0; i < visible.size(); i++) {
                Card card = visible.get(i);
                if (card == null) {
                    continue;
                }
                if (!canAfford(self, card, state.getGemBank())) {
                    int score = scoreCard(card, self);
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

    // Step 4 - take gems that reduce shortage for the closest affordable target card
    private AIAction tryTakeGemsTowardTarget(GameState state, Player self) {
        Card target = findBestTarget(state, self);
        if (target == null) {
            return null;
        }

        Map<GemColor, Integer> shortage = computeShortage(self, target);
        GemBank bank = state.getGemBank();

        Optional<GemColor> doubleGem = shortage.entrySet().stream()
                .filter(entry -> entry.getValue() >= 2
                        && entry.getKey() != GemColor.GOLD
                        && bank.getGemCount(entry.getKey()) >= 4)
                .map(Map.Entry::getKey)
                .findFirst();

        if (doubleGem.isPresent()) {
            return AIAction.takeGems(List.of(doubleGem.get(), doubleGem.get()));
        }

        List<GemColor> toTake = shortage.entrySet().stream()
                .filter(entry -> entry.getKey() != GemColor.GOLD
                        && entry.getValue() > 0
                        && bank.getGemCount(entry.getKey()) > 0)
                .sorted(Map.Entry.<GemColor, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));

        if (toTake.size() < 3) {
            Arrays.stream(GemColor.values())
                    .filter(color -> color != GemColor.GOLD
                            && !toTake.contains(color)
                            && bank.getGemCount(color) > 0)
                    .limit(3 - toTake.size())
                    .forEach(toTake::add);
        }

        if (toTake.size() >= 3) {
            return AIAction.takeGems(toTake.subList(0, 3));
        }

        return null;
    }

    // Step 5 - take whatever gems are available

    private AIAction takeAvailableGems(GameState state) {
        GemBank bank = state.getGemBank();

        // Try to take 3 different gems
        List<GemColor> available = new ArrayList<>();
        for (GemColor color : GemColor.values()) {
            if (color != GemColor.GOLD && bank.getGemCount(color) > 0) {
                available.add(color);
            }
        }
        if (available.size() >= 3) {
            return AIAction.takeGems(available.subList(0, 3));
        }

        // Try to take 2 of the same if any color has >= 4
        for (GemColor color : GemColor.values()) {
            if (color != GemColor.GOLD && bank.getGemCount(color) >= 4) {
                return AIAction.takeGems(List.of(color, color));
            }
        }

        // Take whatever different gems remain (1 or 2)
        if (!available.isEmpty()) {
            return AIAction.takeGems(available);
        }

        return null;
    }

    // Step 6 - reserve from top of deck as a last resort to get gold
    private AIAction tryReserveTopOfDeck(GameState state, Player self) {
        if (self.getReservedCards().size() >= 3) return null;

        // Prefer higher tiers for better expected value
        for (Tier tier : List.of(Tier.THREE, Tier.TWO, Tier.ONE)) {
            if (!state.getDeck(tier).isEmpty()) {
                return AIAction.reserveTopOfDeck(tier);
            }
        }
        return null;
    }

    // Discard the non-gold color with the most excess relative to the target
    @Override
    public GemColor chooseGemToDiscard(GameState state, Player self) {
        Card target = findBestTarget(state, self);
        GemColor bestExcess = null;
        int maxExcess = 0;

        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD || self.getGemCount(color) == 0) {
                continue;
            }

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

        List<GemColor> held = new ArrayList<GemColor>();
        for (GemColor color : GemColor.values()) {
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
    // Returns true if the player's gems + bonuses + gold can cover the card's cost
    private boolean canAfford(Player player, Card card, GemBank bank) {
        int goldNeeded = 0;
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }
            int cost = card.getCost().getOrDefault(color, 0);
            int bonus = player.getBonusCount(color);
            int gems = player.getGemCount(color);
            int deficit = Math.max(0, cost - bonus - gems);
            goldNeeded += deficit;
        }
        return goldNeeded <= player.getGemCount(GemColor.GOLD);
    }

    private int scoreCard(Card card, Player self) {
        int score = card.getPoints() * 10;
        GemColor bonus = card.getBonus();
        if (bonus != null) {
            score += self.getBonusCount(bonus);
        }
        return score;
    }

    // Returns the visible card with the smallest total gem shortage (closest to affordable)
    private Card findBestTarget(GameState state, Player self) {
        Card bestCard = null;
        int minShortage = Integer.MAX_VALUE;

        for (Tier tier : Tier.values()) {
            for (Card card : state.getVisibleCards(tier)) {
                if (card == null) {
                    continue;
                }
                int shortage = computeShortage(self, card).values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();
                if (shortage < minShortage) {
                    minShortage = shortage;
                    bestCard = card;
                }
            }
        }
        return bestCard;
    }

    private Map<GemColor, Integer> computeShortage(Player player, Card card) {
        Map<GemColor, Integer> shortage = new EnumMap<GemColor, Integer>(GemColor.class);
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }
            int cost = card.getCost().getOrDefault(color, 0);
            int bonus = player.getBonusCount(color);
            int gems = player.getGemCount(color);
            int deficit = Math.max(0, cost - bonus - gems);
            if (deficit > 0) {
                shortage.put(color, deficit);
            }
        }
        return shortage;
    }
}
