package splendor.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import splendor.entities.AIPlayer;
import splendor.entities.Card;
import splendor.entities.GemColor;
import splendor.entities.Player;
import splendor.entities.Tier;
import splendor.logic.GameEngine;
import splendor.logic.GameSetup;
import splendor.logic.GameState;
import splendor.logic.ai.AIAction;
import splendor.logic.ai.AIDifficulty;

public class ConsoleGameUI {
    private static final int ACTION_TAKE_THREE_DIFFERENT = 1;
    private static final int ACTION_TAKE_TWO_SAME = 2;
    private static final int ACTION_RESERVE_VISIBLE = 3;
    private static final int ACTION_RESERVE_TOP = 4;
    private static final int ACTION_BUY_VISIBLE = 5;
    private static final int ACTION_BUY_RESERVED = 6;
    private static final int ACTION_PASS = 7;
    private static final int ACTION_QUIT = 8;

    private final Scanner scanner;
    private final ConsoleRenderer renderer;
    private boolean quitRequested;
    private boolean actionCancelled;
    private String lastSuccessMessage;

    public ConsoleGameUI() {
        this.scanner = new Scanner(System.in);
        this.renderer = new ConsoleRenderer();
        this.quitRequested = false;
        this.actionCancelled = false;
        this.lastSuccessMessage = "Action completed.";
    }

    public void run() {
        try {
            renderer.clearScreen();
            GameState gameState = createGameFromInput();
            GameEngine engine = new GameEngine(gameState);

            renderer.printInfo("Game started.");
            pauseForEnter();

            while (!engine.isGameOver() && !quitRequested) {
                runTurn(engine);
            }

            if (quitRequested) {
                renderer.clearScreen();
                renderer.printSetupBanner();
                renderer.printInfo("Game ended by user.");
            } else {
                renderer.clearScreen();
                renderer.printFinalResults(gameState);
            }
        } catch (IOException e) {
            renderer.printError("Failed to start game: " + e.getMessage());
        }
    }

    private GameState createGameFromInput() throws IOException {
        renderer.printSetupBanner();
        int numPlayers = readIntInRange("Number of players (2-4): ", 2, 4);

        List<String> playerNames = new ArrayList<String>();
        for (int i = 1; i <= numPlayers; i++) {
            String name = readNonEmptyLine("Enter name for player " + i + ": ");
            playerNames.add(name);
        }

        Map<String, AIDifficulty> aiDifficulties = new HashMap<String, AIDifficulty>();
        for (String name : playerNames) {
            if (!readYesNo("Is \"" + name + "\" a human player? (y/n): ")) {
                AIDifficulty difficulty = readAIDifficulty(name);
                aiDifficulties.put(name, difficulty);
                System.out.println("  -> " + name + " will be controlled by the AI (" + difficulty + ").");
            }
        }

        return GameSetup.createGame(playerNames, aiDifficulties, "config.properties");
    }

    void runTurn(GameEngine engine) {
        Player current = engine.getCurrentPlayer();

        if (AIPlayer.isAI(current)) {
            handleAITurn(engine, (AIPlayer) current);
        } else {
            handleHumanTurn(engine);
        }
    }

    private void handleAITurn(GameEngine engine, AIPlayer ai) {
        renderer.clearScreen();
        renderer.printGameState(engine.getGameState());

        GameState state = engine.getGameState();
        AIAction action = ai.chooseAction(state);

        if (action == null) {
            renderer.printInfo("[AI] " + ai.getName() + " has no valid move and skips the turn.");
            pauseForEnter();
            if (!engine.isGameOver()) {
                engine.nextTurn();
            }
            return;
        }

        System.out.println("\n[AI] " + ai.getName() + " decides: " + action);

        boolean success = switch (action.type()) {
            case TAKE_GEMS -> {
                List<GemColor> gems = action.gems();
                if (gems.size() == 2 && gems.get(0) == gems.get(1)) {
                    yield engine.takeTwoSameGems(gems.get(0));
                }
                if (gems.size() == 3) {
                    yield engine.takeDifferentGems(gems);
                }
                yield false;
            }
            case BUY_CARD -> {
                if (action.buyingReserved()) {
                    yield engine.buyReservedCard(action.reservedIndex());
                } else {
                    yield engine.buyVisibleCard(action.tier(), action.slotIndex());
                }
            }
            case RESERVE_CARD -> {
                if (action.slotIndex() == -1) {
                    yield engine.reserveTopCard(action.tier());
                } else {
                    yield engine.reserveVisibleCard(action.tier(), action.slotIndex());
                }
            }
        };

        if (success) {
            handleAIDiscardExcessGems(engine, ai);
            lastSuccessMessage = "[AI] " + ai.getName() + " completed: " + action;
            renderer.printSuccess(lastSuccessMessage);
            pauseForEnter();
            if (!engine.isGameOver()) {
                engine.nextTurn();
            }
        } else {
            renderer.printError("[AI] Action failed - skipping turn.");
            pauseForEnter();
            engine.nextTurn();
        }
    }

    private void handleHumanTurn(GameEngine engine) {
        boolean turnFinished = false;

        while (!turnFinished && !engine.isGameOver() && !quitRequested) {
            renderer.clearScreen();
            renderer.printGameState(engine.getGameState());
            List<Integer> availableActions = printActionMenu(engine);

            int choice = readIntInRange("Choose action: ", 1, availableActions.size());
            int actionCode = availableActions.get(choice - 1);
            actionCancelled = false;
            lastSuccessMessage = "Action completed.";
            turnFinished = handleActionChoice(engine, actionCode);

            if (turnFinished) {
                if (quitRequested) {
                    return;
                }

                renderer.printSuccess(lastSuccessMessage);
                pauseForEnter();
                if (!engine.isGameOver()) {
                    engine.nextTurn();
                }
            } else if (!quitRequested && !actionCancelled) {
                pauseForEnter();
            }
        }
    }

    private boolean handleActionChoice(GameEngine engine, int choice) {
        try {
            if (choice == ACTION_TAKE_THREE_DIFFERENT) {
                return handleTakeThreeDifferentGems(engine);
            }
            if (choice == ACTION_TAKE_TWO_SAME) {
                return handleTakeTwoSameGems(engine);
            }
            if (choice == ACTION_RESERVE_VISIBLE) {
                return handleReserveVisibleCard(engine);
            }
            if (choice == ACTION_RESERVE_TOP) {
                return handleReserveTopCard(engine);
            }
            if (choice == ACTION_BUY_VISIBLE) {
                return handleBuyVisibleCard(engine);
            }
            if (choice == ACTION_BUY_RESERVED) {
                return handleBuyReservedCard(engine);
            }
            if (choice == ACTION_PASS) {
                lastSuccessMessage = "Turn passed - no actions available.";
                return true;
            }

            return confirmQuit();
        } catch (IllegalArgumentException e) {
            renderer.printError("Invalid input: " + e.getMessage());
            return false;
        }
    }

    private boolean handleTakeThreeDifferentGems(GameEngine engine) {
        List<GemColor> firstOptions = getAvailableGemColors(engine, 1, new ArrayList<GemColor>());
        if (firstOptions.size() < 3) {
            renderer.printError("You cannot take 3 different gems because fewer than 3 gem colors are available in the bank.");
            return false;
        }

        renderer.printInfo("Pick 3 different gem colors.");
        GemColor first = readGemColorFromOptionsOrCancel(
                firstOptions, engine.getGameState(), "First color (0 to go back): ");
        if (first == null) {
            return cancelCurrentAction();
        }

        List<GemColor> secondExcluded = new ArrayList<GemColor>();
        secondExcluded.add(first);
        GemColor second = readGemColorFromOptionsOrCancel(
                getAvailableGemColors(engine, 1, secondExcluded), engine.getGameState(),
                "Second color (0 to go back): ");
        if (second == null) {
            return cancelCurrentAction();
        }

        List<GemColor> thirdExcluded = new ArrayList<GemColor>();
        thirdExcluded.add(first);
        thirdExcluded.add(second);
        GemColor third = readGemColorFromOptionsOrCancel(
                getAvailableGemColors(engine, 1, thirdExcluded), engine.getGameState(),
                "Third color (0 to go back): ");
        if (third == null) {
            return cancelCurrentAction();
        }

        List<GemColor> selections = new ArrayList<GemColor>();
        selections.add(first);
        selections.add(second);
        selections.add(third);

        if (!confirmGemSelections(selections)) {
            return cancelCurrentAction();
        }

        boolean success = engine.takeThreeDifferentGems(first, second, third);
        if (success) {
            handleHumanDiscardExcessGems(engine);
            lastSuccessMessage = "Took gems: " + renderer.formatGemSelection(selections);
        }
        return success;
    }

    private boolean handleTakeTwoSameGems(GameEngine engine) {
        List<GemColor> options = getAvailableGemColors(engine, 4, new ArrayList<GemColor>());
        if (options.isEmpty()) {
            renderer.printError("You cannot take 2 of the same gem because no color has at least 4 tokens in the bank.");
            return false;
        }

        GemColor color = readGemColorFromOptionsOrCancel(
                options, engine.getGameState(), "Choose color to take 2 of (0 to go back): ");
        if (color == null) {
            return cancelCurrentAction();
        }

        List<GemColor> selections = new ArrayList<GemColor>();
        selections.add(color);
        selections.add(color);

        if (!confirmGemSelections(selections)) {
            return cancelCurrentAction();
        }

        boolean success = engine.takeTwoSameGems(color);
        if (success) {
            handleHumanDiscardExcessGems(engine);
            lastSuccessMessage = "Took gems: " + renderer.formatGemSelection(selections);
        }
        return success;
    }

    private boolean handleReserveVisibleCard(GameEngine engine) {
        if (!canReserveCard(engine)) {
            renderer.printError("You cannot reserve a card because " + getReserveCardUnavailableReason(engine) + ".");
            return false;
        }

        List<Tier> tierOptions = getTiersWithVisibleCards(engine);
        if (tierOptions.isEmpty()) {
            renderer.printError("You cannot reserve a visible card because there are no visible cards on the board.");
            return false;
        }

        Tier tier = readTierFromOptionsOrCancel(
                tierOptions, engine.getGameState(), "Choose tier to reserve from (0 to go back): ");
        if (tier == null) {
            return cancelCurrentAction();
        }
        List<Card> visibleCards = engine.getGameState().getVisibleCards(tier);

        renderer.printCardChoices("Visible Cards In Tier " + getTierNumber(tier), visibleCards);
        Integer index = readIntInRangeOrCancel("Card number to reserve (0 to go back): ", 1, visibleCards.size());
        if (index == null) {
            return cancelCurrentAction();
        }
        boolean success = engine.reserveVisibleCard(tier, index - 1);
        if (success) {
            handleHumanDiscardExcessGems(engine);
        }
        return success;
    }

    private boolean handleReserveTopCard(GameEngine engine) {
        if (!canReserveCard(engine)) {
            renderer.printError("You cannot reserve a card because " + getReserveCardUnavailableReason(engine) + ".");
            return false;
        }

        List<Tier> tierOptions = getTiersWithCardsInDeck(engine);
        if (tierOptions.isEmpty()) {
            renderer.printError("You cannot reserve from the top of a deck because all decks are empty.");
            return false;
        }

        Tier tier = readTierFromOptionsOrCancel(
                tierOptions, engine.getGameState(), "Choose tier to reserve from the top (0 to go back): ");
        if (tier == null) {
            return cancelCurrentAction();
        }
        boolean success = engine.reserveTopCard(tier);
        if (success) {
            handleHumanDiscardExcessGems(engine);
        }
        return success;
    }

    private boolean handleBuyVisibleCard(GameEngine engine) {
        List<Tier> tierOptions = getTiersWithAffordableVisibleCards(engine);
        if (tierOptions.isEmpty()) {
            renderer.printError("You cannot buy a visible card because you cannot afford any visible cards right now.");
            return false;
        }

        Tier tier = readTierFromOptionsOrCancel(
                tierOptions, engine.getGameState(), "Choose tier to buy from (0 to go back): ");
        if (tier == null) {
            return cancelCurrentAction();
        }
        List<Integer> affordableIndices = getAffordableVisibleCardIndices(engine, tier);
        List<Card> affordableCards = new ArrayList<Card>();
        for (int index : affordableIndices) {
            affordableCards.add(engine.getGameState().getVisibleCards(tier).get(index));
        }

        renderer.printCardChoices("Affordable Cards In Tier " + getTierNumber(tier), affordableCards);
        Integer option = readIntInRangeOrCancel("Card number to buy (0 to go back): ", 1, affordableCards.size());
        if (option == null) {
            return cancelCurrentAction();
        }
        int actualIndex = affordableIndices.get(option - 1);
        return engine.buyVisibleCard(tier, actualIndex);
    }

    private boolean handleBuyReservedCard(GameEngine engine) {
        Player player = engine.getCurrentPlayer();
        if (player.getReservedCards().isEmpty()) {
            renderer.printError("You cannot buy a reserved card because you do not have any reserved cards.");
            return false;
        }

        List<Integer> affordableIndices = getAffordableReservedCardIndices(engine);
        if (affordableIndices.isEmpty()) {
            renderer.printError("You cannot buy a reserved card because you cannot afford any of your reserved cards right now.");
            return false;
        }

        List<Card> affordableCards = new ArrayList<Card>();
        for (int index : affordableIndices) {
            affordableCards.add(player.getReservedCards().get(index));
        }

        renderer.printCardChoices("Affordable Reserved Cards", affordableCards);
        Integer option = readIntInRangeOrCancel(
                "Reserved card number to buy (0 to go back): ", 1, affordableCards.size());
        if (option == null) {
            return cancelCurrentAction();
        }
        int actualIndex = affordableIndices.get(option - 1);
        return engine.buyReservedCard(actualIndex);
    }

    private List<Integer> printActionMenu(GameEngine engine) {
        List<Integer> availableActions = new ArrayList<Integer>();

        renderer.printActionHeader();

        addActionMenuItem(availableActions, ACTION_TAKE_THREE_DIFFERENT, "Take 3 different gems",
                getTakeThreeDifferentGemsUnavailableReason(engine));
        addActionMenuItem(availableActions, ACTION_TAKE_TWO_SAME, "Take 2 same gems",
                getTakeTwoSameGemsUnavailableReason(engine));
        addActionMenuItem(availableActions, ACTION_RESERVE_VISIBLE, "Reserve a visible card",
                getReserveVisibleCardUnavailableReason(engine));
        addActionMenuItem(availableActions, ACTION_RESERVE_TOP, "Reserve top card from a deck",
                getReserveTopCardUnavailableReason(engine));
        addActionMenuItem(availableActions, ACTION_BUY_VISIBLE, "Buy a visible card",
                getBuyVisibleCardUnavailableReason(engine));
        addActionMenuItem(availableActions, ACTION_BUY_RESERVED, "Buy a reserved card",
                getBuyReservedCardUnavailableReason(engine));

        if (availableActions.isEmpty()) {
            availableActions.add(ACTION_PASS);
            renderer.printActionOption(availableActions.size(), "Pass turn");
        }

        availableActions.add(ACTION_QUIT);
        renderer.printActionOption(availableActions.size(), "Quit");
        return availableActions;
    }

    private Tier readTierFromOptionsOrCancel(List<Tier> tierOptions, GameState gameState, String prompt) {
        renderer.printTierChoices("Choose A Tier", tierOptions, gameState);
        Integer choice = readIntInRangeOrCancel(prompt, 1, tierOptions.size());
        if (choice == null) {
            return null;
        }
        return tierOptions.get(choice - 1);
    }

    private GemColor readGemColorFromOptionsOrCancel(List<GemColor> options, GameState gameState, String prompt) {
        renderer.printGemChoices("Choose A Gem Color", options, gameState);
        Integer choice = readIntInRangeOrCancel(prompt, 1, options.size());
        if (choice == null) {
            return null;
        }
        return options.get(choice - 1);
    }

    private GemColor readGemColorFromPlayerOptions(List<GemColor> options, Player player, String prompt) {
        renderer.printGemChoicesFromPlayer("Choose A Gem Color", options, player);
        int choice = readIntInRange(prompt, 1, options.size());
        return options.get(choice - 1);
    }

    private int readIntInRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(renderer.prompt(prompt));
            String line = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Ask again below.
            }

            renderer.printError("Please enter a number from " + min + " to " + max + ".");
        }
    }

    private Integer readIntInRangeOrCancel(String prompt, int min, int max) {
        while (true) {
            System.out.print(renderer.prompt(prompt));
            String line = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(line);
                if (value == 0) {
                    return null;
                }
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Ask again below.
            }

            renderer.printError("Please enter 0 to go back or a number from " + min + " to " + max + ".");
        }
    }

    private AIDifficulty readAIDifficulty(String playerName) {
        AIDifficulty[] values = AIDifficulty.values();
        System.out.println("  Select difficulty for " + playerName + ":");
        for (int i = 0; i < values.length; i++) {
            System.out.println("    [" + (i + 1) + "] " + values[i]);
        }
        int choice = readIntInRange("  Difficulty: ", 1, values.length);
        return values[choice - 1];
    }

    private String readNonEmptyLine(String prompt) {
        while (true) {
            System.out.print(renderer.prompt(prompt));
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            renderer.printError("Input cannot be blank.");
        }
    }

    private int getTierNumber(Tier tier) {
        if (tier == Tier.ONE) {
            return 1;
        }
        if (tier == Tier.TWO) {
            return 2;
        }
        return 3;
    }

    private List<GemColor> getAvailableGemColors(GameEngine engine, int requiredCount, List<GemColor> excludedColors) {
        List<GemColor> options = new ArrayList<GemColor>();
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }
            if (excludedColors.contains(color)) {
                continue;
            }
            if (engine.getGameState().getGemBank().getGemCount(color) >= requiredCount) {
                options.add(color);
            }
        }
        return options;
    }

    private boolean canReserveCard(GameEngine engine) {
        return getReserveCardUnavailableReason(engine) == null;
    }

    private String getTakeThreeDifferentGemsUnavailableReason(GameEngine engine) {
        if (getAvailableGemColors(engine, 1, new ArrayList<GemColor>()).isEmpty()) {
            return "no gem colors are available in the bank";
        }

        return null;
    }

    private String getTakeTwoSameGemsUnavailableReason(GameEngine engine) {
        if (getAvailableGemColors(engine, 4, new ArrayList<GemColor>()).isEmpty()) {
            return "no gem color has at least 4 tokens in the bank";
        }

        return null;
    }

    private String getReserveCardUnavailableReason(GameEngine engine) {
        Player player = engine.getCurrentPlayer();
        if (player.getReservedCards().size() >= 3) {
            return "you already have 3 reserved cards";
        }

        return null;
    }

    private String getReserveVisibleCardUnavailableReason(GameEngine engine) {
        String reserveReason = getReserveCardUnavailableReason(engine);
        if (reserveReason != null) {
            return reserveReason;
        }

        if (getTiersWithVisibleCards(engine).isEmpty()) {
            return "there are no visible cards on the board";
        }

        return null;
    }

    private String getReserveTopCardUnavailableReason(GameEngine engine) {
        String reserveReason = getReserveCardUnavailableReason(engine);
        if (reserveReason != null) {
            return reserveReason;
        }

        if (getTiersWithCardsInDeck(engine).isEmpty()) {
            return "all decks are empty";
        }

        return null;
    }

    private String getBuyVisibleCardUnavailableReason(GameEngine engine) {
        if (getTiersWithVisibleCards(engine).isEmpty()) {
            return "there are no visible cards on the board";
        }

        if (getTiersWithAffordableVisibleCards(engine).isEmpty()) {
            return "you cannot afford any visible cards right now";
        }

        return null;
    }

    private String getBuyReservedCardUnavailableReason(GameEngine engine) {
        Player player = engine.getCurrentPlayer();
        if (player.getReservedCards().isEmpty()) {
            return "you do not have any reserved cards";
        }

        if (getAffordableReservedCardIndices(engine).isEmpty()) {
            return "you cannot afford any of your reserved cards right now";
        }

        return null;
    }

    private List<Tier> getTiersWithVisibleCards(GameEngine engine) {
        List<Tier> tiers = new ArrayList<Tier>();
        for (Tier tier : Tier.values()) {
            if (!engine.getGameState().getVisibleCards(tier).isEmpty()) {
                tiers.add(tier);
            }
        }
        return tiers;
    }

    private List<Tier> getTiersWithCardsInDeck(GameEngine engine) {
        List<Tier> tiers = new ArrayList<Tier>();
        for (Tier tier : Tier.values()) {
            if (!engine.getGameState().getDeck(tier).isEmpty()) {
                tiers.add(tier);
            }
        }
        return tiers;
    }

    private List<Tier> getTiersWithAffordableVisibleCards(GameEngine engine) {
        List<Tier> tiers = new ArrayList<Tier>();
        for (Tier tier : Tier.values()) {
            if (!getAffordableVisibleCardIndices(engine, tier).isEmpty()) {
                tiers.add(tier);
            }
        }
        return tiers;
    }

    private List<Integer> getAffordableVisibleCardIndices(GameEngine engine, Tier tier) {
        List<Integer> indices = new ArrayList<Integer>();
        List<Card> visibleCards = engine.getGameState().getVisibleCards(tier);
        for (int i = 0; i < visibleCards.size(); i++) {
            if (engine.canAffordCurrentPlayer(visibleCards.get(i))) {
                indices.add(i);
            }
        }
        return indices;
    }

    private List<Integer> getAffordableReservedCardIndices(GameEngine engine) {
        List<Integer> indices = new ArrayList<Integer>();
        List<Card> reservedCards = engine.getCurrentPlayer().getReservedCards();
        for (int i = 0; i < reservedCards.size(); i++) {
            if (engine.canAffordCurrentPlayer(reservedCards.get(i))) {
                indices.add(i);
            }
        }
        return indices;
    }

    private void addActionMenuItem(List<Integer> availableActions, int actionCode, String label, String unavailableReason) {
        if (unavailableReason == null) {
            availableActions.add(actionCode);
            renderer.printActionOption(availableActions.size(), label);
        } else {
            renderer.printDisabledAction(label, unavailableReason);
        }
    }

    private void pauseForEnter() {
        System.out.print(renderer.prompt("Press Enter to continue..."));
        scanner.nextLine();
    }

    private void handleHumanDiscardExcessGems(GameEngine engine) {
        Player player = engine.getCurrentPlayer();
        while (player.getTotalGems() > 10) {
            int excess = player.getTotalGems() - 10;
            renderer.clearScreen();
            renderer.printGameState(engine.getGameState());
            renderer.printInfo("You have " + player.getTotalGems()
                    + " tokens, so discard " + excess + " more to finish your turn.");

            List<GemColor> options = getPlayerHeldGemColors(player);
            GemColor toDiscard = readGemColorFromPlayerOptions(options, player, "Choose a gem to discard: ");
            engine.discardGem(toDiscard);
        }
    }

    private void handleAIDiscardExcessGems(GameEngine engine, AIPlayer ai) {
        while (ai.getTotalGems() > 10) {
            GemColor toDiscard = ai.chooseGemToDiscard(engine.getGameState());
            if (toDiscard == null || ai.getGemCount(toDiscard) < 1) {
                List<GemColor> options = getPlayerHeldGemColors(ai);
                if (options.isEmpty()) {
                    return;
                }
                toDiscard = options.get(0);
            }

            if (!engine.discardGem(toDiscard)) {
                return;
            }
            renderer.printInfo("[AI] " + ai.getName() + " discards: " + toDiscard);
        }
    }

    private List<GemColor> getPlayerHeldGemColors(Player player) {
        List<GemColor> options = new ArrayList<GemColor>();
        for (GemColor color : GemColor.values()) {
            if (player.getGemCount(color) > 0) {
                options.add(color);
            }
        }
        return options;
    }

    private boolean cancelCurrentAction() {
        actionCancelled = true;
        return false;
    }

    private boolean confirmGemSelections(List<GemColor> selections) {
        renderer.printGemSelectionSummary(selections);
        return readYesNo("Confirm gem selections? (y/n): ");
    }

    private boolean confirmQuit() {
        if (readYesNo("Are you sure you want to quit? (y/n): ")) {
            quitRequested = true;
            return true;
        }
        return cancelCurrentAction();
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(renderer.prompt(prompt));
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) {
                return true;
            }
            if (line.equals("n") || line.equals("no")) {
                return false;
            }
            renderer.printError("Please enter y or n.");
        }
    }
}
