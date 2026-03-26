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
 * Medium AI strategy.
 *
 * Decision priority (highest to lowest):
 *  1. Buy a reserved card if affordable.
 *  2. Buy a visible card.
 *  3. Reserve a good tier-2 or tier-3 card.
 *  4. Take gems toward the best target card.
 *  5. Fallback to another valid gem-taking move.
 */
public class MediumStrategy implements AIStrategy {

    @Override
    public AIAction selectAction(GameState state, Player self) {
        AIAction buyReserved = tryBuyReserved(state, self);
        if (buyReserved != null) {
            return buyReserved;
        }

        AIAction buyVisible = tryBuyVisible(state, self);
        if (buyVisible != null) {
            return buyVisible;
        }

        AIAction reserve = tryReserve(state, self);
        if (reserve != null) {
            return reserve;
        }

        AIAction takeGems = tryTakeGemsTowardTarget(state, self);
        if (takeGems != null) {
            return takeGems;
        }

        return fallbackTakeGems(state, self);
    }

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

    private AIAction tryReserve(GameState state, Player self) {
        if (self.getReservedCards().size() >= 3) {
            return null;
        }

        int goldToTake = state.getGemBank().hasAtLeast(GemColor.GOLD, 1) ? 1 : 0;
        if (self.getTotalGems() + goldToTake > 10) {
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

        if (doubleGem.isPresent() && self.getTotalGems() + 2 <= 10) {
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

        if (toTake.size() >= 3 && self.getTotalGems() + 3 <= 10) {
            return AIAction.takeGems(toTake.subList(0, 3));
        }

        return null;
    }

    private AIAction fallbackTakeGems(GameState state, Player self) {
        GemBank bank = state.getGemBank();
        List<GemColor> available = Arrays.stream(GemColor.values())
                .filter(color -> color != GemColor.GOLD && bank.getGemCount(color) > 0)
                .limit(3)
                .collect(Collectors.toList());

        if (available.size() >= 3 && self.getTotalGems() + 3 <= 10) {
            return AIAction.takeGems(available.subList(0, 3));
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
