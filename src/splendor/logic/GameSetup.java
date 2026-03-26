package splendor.logic;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
    private static final String DEFAULT_CARD_FILE_PATH = "src/splendor/data/cards.csv";
    private static final String DEFAULT_NOBLE_FILE_PATH = "src/splendor/data/nobles.csv";

    public static GameState createGame(List<String> playerNames, Set<String> aiPlayerNames,
            String configFilePath) throws IOException {
        Map<String, AIDifficulty> aiDifficulties = new HashMap<String, AIDifficulty>();
        for (String name : aiPlayerNames) {
            aiDifficulties.put(name, AIDifficulty.MEDIUM);
        }
        return createGame(playerNames, aiDifficulties, configFilePath);
    }

    public static GameState createGame(List<String> playerNames, Map<String, AIDifficulty> aiDifficulties,
            String configFilePath) throws IOException {
        Properties properties = loadProperties(configFilePath);
        String cardFilePath = getFilePath(properties, "cards.filepath", DEFAULT_CARD_FILE_PATH);
        String nobleFilePath = getFilePath(properties, "nobles.filepath", DEFAULT_NOBLE_FILE_PATH);
        int winPoints = getWinPoints(properties);
        return createGame(playerNames, aiDifficulties, cardFilePath, nobleFilePath, winPoints);
    }

    public static GameState createGame(List<String> playerNames, Map<String, AIDifficulty> aiDifficulties,
            String cardFilePath, String nobleFilePath, String configFilePath) throws IOException {
        Properties properties = loadProperties(configFilePath);
        int winPoints = getWinPoints(properties);
        return createGame(playerNames, aiDifficulties, cardFilePath, nobleFilePath, winPoints);
    }

    private static GameState createGame(List<String> playerNames, Map<String, AIDifficulty> aiDifficulties,
            String cardFilePath, String nobleFilePath, int winPoints) throws IOException {
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

        return new GameState(players, gemBank, decks, visibleCards, noblesInPlay, winPoints);
    }

    private static Properties loadProperties(String configFilePath) {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(configFilePath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            return properties;
        }
        return properties;
    }

    private static String getFilePath(Properties properties, String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static int getWinPoints(Properties properties) {
        String value = properties.getProperty("win.points");
        if (value == null || value.trim().isEmpty()) {
            return DEFAULT_WIN_POINTS;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_WIN_POINTS;
        }
    }
}
