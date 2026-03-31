package splendor.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import splendor.entities.Card;
import splendor.entities.CardDeck;
import splendor.entities.GemColor;
import splendor.entities.Tier;

/**
 * Loads development cards from CSV data.
 */
public class CardLoader {
    /**
     * Loads one deck per tier from a CSV file.
     */
    public static Map<Tier, CardDeck> loadDecks(String filePath) throws IOException {
        List<Card> tierOneCards = new ArrayList<Card>();
        List<Card> tierTwoCards = new ArrayList<Card>();
        List<Card> tierThreeCards = new ArrayList<Card>();

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        boolean isFirstLine = true;

        while ((line = br.readLine()) != null) {
            line = line.trim();

            // skip blank lines
            if (line.isEmpty()) {
                continue;
            }

            // skip header
            if (isFirstLine) {
                isFirstLine = false;
                continue;
            }

            String[] parts = line.split(",");

            int tierNumber = Integer.parseInt(parts[0].trim());
            int points = Integer.parseInt(parts[1].trim());
            GemColor bonus = GemColor.valueOf(parts[2].trim());

            // column 3-7 are: diamond, sapphire, emerald, ruby, onyx
            EnumMap<GemColor, Integer> cost = new EnumMap<GemColor, Integer>(GemColor.class);
            cost.put(GemColor.DIAMOND, Integer.parseInt(parts[3].trim()));
            cost.put(GemColor.SAPPHIRE, Integer.parseInt(parts[4].trim()));
            cost.put(GemColor.EMERALD, Integer.parseInt(parts[5].trim()));
            cost.put(GemColor.RUBY, Integer.parseInt(parts[6].trim()));
            cost.put(GemColor.ONYX, Integer.parseInt(parts[7].trim()));
            cost.put(GemColor.GOLD, 0); // cards should not cost gold

            Tier tier;
            if (tierNumber == 1) {
                tier = Tier.ONE;
            } else if (tierNumber == 2) {
                tier = Tier.TWO;
            } else if (tierNumber == 3) {
                tier = Tier.THREE;
            } else {
                br.close();
                throw new IllegalArgumentException("Invalid tier number: " + tierNumber);
            }

            Card card = new Card(tier, points, bonus, cost);

            if (tier == Tier.ONE) {
                tierOneCards.add(card);
            } else if (tier == Tier.TWO) {
                tierTwoCards.add(card);
            } else {
                tierThreeCards.add(card);
            }
        }

        br.close();

        CardDeck tierOneDeck = new CardDeck(Tier.ONE, tierOneCards);
        CardDeck tierTwoDeck = new CardDeck(Tier.TWO, tierTwoCards);
        CardDeck tierThreeDeck = new CardDeck(Tier.THREE, tierThreeCards);

        Map<Tier, CardDeck> decks = new HashMap<Tier, CardDeck>();
        decks.put(Tier.ONE, tierOneDeck);
        decks.put(Tier.TWO, tierTwoDeck);
        decks.put(Tier.THREE, tierThreeDeck);

        return decks;
    }
}
