package splendor.app;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import splendor.data.CardLoader;
import splendor.entities.*;

public class Main {
    public static void main(String[] args) {
        //creates player object
        Player p1 = new Player("Danial");
        System.out.println(p1);
        System.out.println();

        //create one noble
        EnumMap<GemColor, Integer> req = new EnumMap<>(GemColor.class);
        req.put(GemColor.RUBY, 3);
        req.put(GemColor.EMERALD, 3);
        req.put(GemColor.ONYX, 3);
        Noble n1 = new Noble("N1", 3, req);
        System.out.println(n1);
        System.out.println();

        //create the gem bank
        GemBank gemBank = new GemBank(3);
        System.out.println(gemBank);
        System.out.println();


        //create CardDeck (shuffled)
        try {
            Map<Tier, CardDeck> decks = CardLoader.loadDecks("src/splendor/data/cards.csv");

            CardDeck tierOneDeck = decks.get(Tier.ONE);
            CardDeck tierTwoDeck = decks.get(Tier.TWO);
            CardDeck tierThreeDeck = decks.get(Tier.THREE);

            System.out.println(tierOneDeck);
            System.out.println(tierTwoDeck);
            System.out.println(tierThreeDeck);

            // optional: shuffle decks
            tierOneDeck.shuffle();
            tierTwoDeck.shuffle();
            tierThreeDeck.shuffle();

            System.out.println("Top Tier 1 card: " + tierOneDeck.drawCard());
            System.out.println("Top Tier 2 card: " + tierTwoDeck.drawCard());
            System.out.println("Top Tier 3 card: " + tierThreeDeck.drawCard());
            System.out.println();
        } catch (IOException e) {
            System.out.println("Error reading cards.csv: " + e.getMessage());
        }
    }
}
