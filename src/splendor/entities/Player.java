package splendor.entities;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class Player {
    private final String name;
    private final EnumMap<GemColor, Integer> gems;
    private final List<Card> purchasedCards;
    private final List<Card> reservedCards;
    private final List<Noble> nobles;

    public Player(String name) {
        this.name = name;
        this.gems = new EnumMap<>(GemColor.class);

        for (GemColor color : GemColor.values()) {
            this.gems.put(color, 0);
        }
        this.purchasedCards = new ArrayList<Card>();
        this.reservedCards = new ArrayList<Card>();
        this.nobles = new ArrayList<Noble>();
    }

    public String getName() {
        return name;
    }

    public EnumMap<GemColor, Integer> getGems() {
        return gems;
    }

    public List<Card> getPurchasedCards() {
        return purchasedCards;
    }

    public List<Card> getReservedCards() {
        return reservedCards;
    }

    public List<Noble> getNobles() {
        return nobles;
    }

    public int getGemCount(GemColor color) {
        Integer count = gems.get(color);
        return count == null ? 0 : count;
    }

    public int getTotalGems() {
        int total = 0;
        for (GemColor color : GemColor.values()) {
            total += getGemCount(color);
        }
        return total;
    }

    public int getBonusCount(GemColor color) {
        int total = 0;
        for (Card card : purchasedCards) {
            if (card.getBonus() == color) {
                total++;
            }
        }
        return total;
    }

    public void addGem(GemColor color, int amount) {
        gems.put(color, getGemCount(color) + amount);
    }

    public boolean removeGem(GemColor color, int amount) {
        if (getGemCount(color) < amount) {
            return false;
        }
        gems.put(color, getGemCount(color) - amount);
        return true;
    }

    public void addPurchasedCard(Card card) {
        purchasedCards.add(card);
    }

    public void addReservedCard(Card card) {
        reservedCards.add(card);
    }

    public Card removeReservedCard(int index) {
        return reservedCards.remove(index);
    }

    public void addNoble(Noble noble) {
        nobles.add(noble);
    }
    
    public int getPoints() {
        int total = 0;
        for (Card card : purchasedCards) {
            total += card.getPoints();
        }
        for (Noble noble : nobles) {
            total += noble.getPoints();
        }
        return total;
    }

    @Override
    public String toString() {
        return "Player{" +
                "\n name = " + name +
                ",\n gems = " + gems +
                ",\n purchasedCards = " + purchasedCards +
                ",\n reservedCards = " + reservedCards +
                ",\n nobles = " + nobles +
                "\n}";
    }

}
