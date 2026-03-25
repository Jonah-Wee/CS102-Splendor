package splendor.logic;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import splendor.data.CardLoader;
import splendor.data.NobleLoader;
import splendor.entities.AIPlayer;
import splendor.entities.Card;
import splendor.entities.CardDeck;
import splendor.entities.GemBank;
import splendor.entities.Noble;
import splendor.entities.Player;
import splendor.entities.Tier;
import splendor.logic.ai.AIDifficulty;

public class GameSetup {
    private static final int DEFAULT_WIN_POINTS = 15;

    public static GameState createGame(List<String> playerNames, Map<String, AIDifficulty> aiDifficulties,
        String cardFilePath, String nobleFilePath, String configFilePath) throws IOException {

        List<Player> players = new ArrayList<Player>();

        for (String name : playerNames) {
            if (aiDifficulties.containsKey(name)) {
                players.add(new AIPlayer(name, aiDifficulties.get(name).createStrategy()));
            } else {
                players.add(new Player(name));
            }
        }
        

        GemBank gemBank = new GemBank(playerNames.size());
        Map<Tier, CardDeck> decks = CardLoader.loadDecks(cardFilePath);
        for (CardDeck deck : decks.values()) {
            deck.shuffle();
        }

        List<Noble> nobles = NobleLoader.loadNobles(nobleFilePath);
        Collections.shuffle(nobles);
        int noblesToUse = playerNames.size() + 1;
        List<Noble> noblesInPlay = new ArrayList<Noble>();
        for (int i = 0; i < noblesToUse && i < nobles.size(); i++) {
            noblesInPlay.add(nobles.get(i));
        }

        Map<Tier, List<Card>> visibleCards = new EnumMap<Tier, List<Card>>(Tier.class);
        for (Tier tier : Tier.values()) {
            List<Card> row = new ArrayList<Card>();
            CardDeck deck = decks.get(tier);
            for (int i = 0; i < 4 && deck != null && !deck.isEmpty(); i++) {
                row.add(deck.drawCard());
            }
            visibleCards.put(tier, row);
        }

        int winPoints = loadWinPoints(configFilePath);
        return new GameState(players, gemBank, decks, visibleCards, noblesInPlay, winPoints);
    }

    private static int loadWinPoints(String configFilePath) {
        Properties properties = new Properties();
        try {
            FileInputStream inputStream = new FileInputStream(configFilePath);
            properties.load(inputStream);
            inputStream.close();
            String value = properties.getProperty("win.points");
            if (value == null) {
                return DEFAULT_WIN_POINTS;
            }
            return Integer.parseInt(value.trim());
        } catch (IOException e) {
            return DEFAULT_WIN_POINTS;
        } catch (NumberFormatException e) {
            return DEFAULT_WIN_POINTS;
        }
    }
}
