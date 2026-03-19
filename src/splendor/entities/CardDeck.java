package splendor.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardDeck {

    private final Tier tier;
    private final List<Card> cards; 
    public CardDeck(Tier tier, List<Card> cards) {
        if (tier == null) { throw new IllegalArgumentException("tier cannot be null"); } 
        if (cards == null) { throw new IllegalArgumentException("cards cannot be null"); } 
        this.tier = tier; 
        this.cards = new ArrayList<Card>(cards);
    } 
    
    public Tier getTier() { 
        return tier; 
    } 

    public int size() { 
        return cards.size(); 
    } 

    public boolean isEmpty() { 
        return cards.isEmpty(); 
    } 

    public void shuffle() { // shuffle deck in-place
        Collections.shuffle(cards); 
    } 

    public Card drawCard() { // draws top card, return null if empty.
        if (cards.isEmpty()) { 
            return null; 
        } 
        return cards.remove(0); 
    } 

    public Card peekTopCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.get(0);
    }

    @Override 
    public String toString() {
        return "CardDeck{" + "tier=" + tier + ", size=" + cards.size() + "}"; 
    } 
}
