package splendor.entities;

import java.util.EnumMap;

public class Card {
    private Tier tier;
    private int points;
    private GemColor bonus;
    private EnumMap<GemColor, Integer> cost;

    public Card(Tier tier, int points, GemColor bonus, EnumMap<GemColor, Integer> cost) {
        this.tier = tier;
        this.points = points;
        this.bonus = bonus;
        this.cost = cost;
    }

    public Tier getTier() {
        return tier;
    }

    public int getPoints() {
        return points;
    }

    public GemColor getBonus() {
        return bonus;
    }

    public EnumMap<GemColor, Integer> getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return String.format("Card{Tier=%d, Points=%d, Bonus=%s, Cost=%s}", 
                                tier, points, bonus, cost);
    }
}
