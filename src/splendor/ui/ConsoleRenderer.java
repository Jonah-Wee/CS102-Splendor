package splendor.ui;

import java.util.ArrayList;
import java.util.List;
import splendor.entities.Card;
import splendor.entities.GemColor;
import splendor.entities.Noble;
import splendor.entities.Player;
import splendor.entities.Tier;
import splendor.logic.GameState;
// this class has methods to help format the output for aesthetics
public class ConsoleRenderer {
    private static final String TOP_LEFT_CORNER = "\u250C";
    private static final String TOP_RIGHT_CORNER = "\u2510";
    private static final String BOTTOM_LEFT_CORNER = "\u2514";
    private static final String BOTTOM_RIGHT_CORNER = "\u2518";
    private static final String HORIZONTAL_LINE = "\u2500";
    private static final String VERTICAL_LINE = "\u2502";

    private static final String HEAVY_TOP_LEFT_CORNER = "\u250F";
    private static final String HEAVY_TOP_RIGHT_CORNER = "\u2513";
    private static final String HEAVY_BOTTOM_LEFT_CORNER = "\u2517";
    private static final String HEAVY_BOTTOM_RIGHT_CORNER = "\u251B";
    private static final String HEAVY_HORIZONTAL_LINE = "\u2501";
    private static final String HEAVY_VERTICAL_LINE = "\u2503";

    private static final String DIAMOND_EMOJI = "\uD83D\uDC8E";
    private static final String SAPPHIRE_EMOJI = "\uD83D\uDD37";
    private static final String EMERALD_EMOJI = "\uD83D\uDFE2";
    private static final String RUBY_EMOJI = "\uD83D\uDD34";
    private static final String ONYX_EMOJI = "\uD83D\uDFE4";
    private static final String GOLD_EMOJI = "\uD83D\uDFE1";

    private static final int SCREEN_WIDTH = 96;
    private static final int CARD_INNER_WIDTH = 20;
    private static final int CARD_HEIGHT = 7;
    private static final int SUMMARY_CARD_INNER_WIDTH = 16;
    private static final int SUMMARY_CARD_HEIGHT = 5;
    private static final int NOBLE_INNER_WIDTH = 20;
    private static final int NOBLE_HEIGHT = 6;

    public void clearScreen() {
        System.out.print(Ansi.CLEAR_SCREEN + Ansi.CURSOR_HOME);
        System.out.flush();
    }

    public void printSetupBanner() {
        printBanner("SPLENDOR");
        System.out.println(Ansi.accent(center("Console Edition", SCREEN_WIDTH)));
        System.out.println();
    }

    public void printGameState(GameState gameState) {
        printBanner("SPLENDOR");
        printMetaLine(gameState);
        printRule();
        printBank(gameState);
        printNobles(gameState.getNoblesInPlay());
        printTierRow("Tier 3", Tier.THREE, gameState);
        printTierRow("Tier 2", Tier.TWO, gameState);
        printTierRow("Tier 1", Tier.ONE, gameState);
        printCurrentPlayer(gameState.getCurrentPlayer());
        printOtherPlayers(gameState);
        printRule();
    }

    public void printActionHeader() {
        System.out.println(sectionTitle("Available Actions"));
    }

    public void printActionOption(int number, String label) {
        System.out.println("  " + Ansi.accent("[" + number + "]") + " " + label);
    }

    public void printDisabledAction(String label, String reason) {
        System.out.println("  " + Ansi.dim("- " + label + " (Unavailable: " + reason + ")"));
    }

    public void printCardChoices(String title, List<Card> cards) {
        System.out.println(sectionTitle(title));
        if (cards.isEmpty()) {
            System.out.println(Ansi.dim("  No cards available."));
            System.out.println();
            return;
        }
        printBlocks(renderCardBlocks(cards));
        System.out.println();
    }

    public void printGemChoices(String title, List<GemColor> options, GameState gameState) {
        System.out.println(sectionTitle(title));
        for (int i = 0; i < options.size(); i++) {
            GemColor color = options.get(i);
            String label = "  [" + (i + 1) + "] "
                    + gemIcon(color) + " "
                    + padRight(Ansi.wrap(Ansi.colorForGem(color), color.name()), 18)
                    + " bank=" + styledAmount(color, gameState.getGemBank().getGemCount(color));
            System.out.println(label);
        }
        System.out.println();
    }

    public void printGemChoicesFromPlayer(String title, List<GemColor> options, Player player) {
        System.out.println(sectionTitle(title));
        for (int i = 0; i < options.size(); i++) {
            GemColor color = options.get(i);
            String label = "  [" + (i + 1) + "] "
                    + gemIcon(color) + " "
                    + padRight(Ansi.wrap(Ansi.colorForGem(color), color.name()), 18)
                    + " hand=" + styledAmount(color, player.getGemCount(color));
            System.out.println(label);
        }
        System.out.println();
    }

    public void printGemSelectionSummary(List<GemColor> selections) {
        System.out.println(sectionTitle("Selected Gems"));
        System.out.println("  " + formatGemSelection(selections));
        System.out.println();
    }

    public void printTierChoices(String title, List<Tier> tiers, GameState gameState) {
        System.out.println(sectionTitle(title));
        for (int i = 0; i < tiers.size(); i++) {
            Tier tier = tiers.get(i);
            String label = "  [" + (i + 1) + "] Tier " + tierNumber(tier)
                    + "   visible=" + gameState.getVisibleCards(tier).size()
                    + "   deck=" + gameState.getDeck(tier).size();
            System.out.println(Ansi.accent(label));
        }
        System.out.println();
    }

    public void printSuccess(String message) {
        System.out.println();
        System.out.println(Ansi.success("[OK] " + message));
    }

    public void printError(String message) {
        System.out.println();
        System.out.println(Ansi.error("[X] " + message));
    }

    public void printInfo(String message) {
        System.out.println();
        System.out.println(Ansi.accent("[i] " + message));
    }

    public void printFinalResults(GameState gameState) {
        printBanner("GAME OVER");
        int bestScore = -1;
        for (Player player : gameState.getPlayers()) {
            if (player.getPoints() > bestScore) {
                bestScore = player.getPoints();
            }
        }

        System.out.println(sectionTitle("Final Scores"));
        for (Player player : gameState.getPlayers()) {
            String line = padRight(player.getName(), 12)
                    + " points=" + player.getPoints()
                    + " cards=" + player.getPurchasedCards().size()
                    + " reserved=" + player.getReservedCards().size()
                    + " nobles=" + player.getNobles().size();
            if (player.getPoints() == bestScore) {
                System.out.println(Ansi.success("  " + line + "  <- winner"));
            } else {
                System.out.println("  " + line);
            }
        }
        System.out.println();
    }

    public String prompt(String text) {
        return Ansi.accent(text);
    }

    public String formatGemSelection(List<GemColor> selections) {
        List<String> tokens = new ArrayList<String>();
        List<GemColor> seenColors = new ArrayList<GemColor>();

        for (GemColor color : selections) {
            if (!seenColors.contains(color)) {
                seenColors.add(color);
            }
        }

        for (GemColor color : seenColors) {
            int count = 0;
            for (GemColor selectedColor : selections) {
                if (selectedColor == color) {
                    count++;
                }
            }
            tokens.add(gemToken(color, count));
        }

        return joinStrings(tokens, "   ");
    }

    private void printMetaLine(GameState gameState) {
        String line = "Current Player: " + gameState.getCurrentPlayer().getName()
                + "   |   Target Points: " + gameState.getWinPoints()
                + "   |   Final Round: " + (gameState.isFinalRoundStarted() ? "YES" : "NO");
        System.out.println(Ansi.accent(center(line, SCREEN_WIDTH)));
        System.out.println();
    }

    private void printBank(GameState gameState) {
        System.out.println(sectionTitle("Gem Bank"));
        List<String> chips = new ArrayList<String>();
        for (GemColor color : GemColor.values()) {
            String chip = "[" + gemToken(color, gameState.getGemBank().getGemCount(color)) + "]";
            chips.add(chip);
        }
        System.out.println("  " + joinStrings(chips, "  "));
        System.out.println();
    }

    private void printNobles(List<Noble> nobles) {
        System.out.println(sectionTitle("Nobles In Play"));
        if (nobles.isEmpty()) {
            System.out.println(Ansi.dim("  No nobles available."));
            System.out.println();
            return;
        }

        List<List<String>> blocks = new ArrayList<List<String>>();
        for (int i = 0; i < nobles.size(); i++) {
            blocks.add(renderNobleBlock(nobles.get(i), "[" + (i + 1) + "]"));
        }
        printBlocks(blocks);
        System.out.println();
    }

    private void printTierRow(String title, Tier tier, GameState gameState) {
        System.out.println(sectionTitle(title + "  (deck: " + gameState.getDeck(tier).size() + ")"));
        List<Card> cards = gameState.getVisibleCards(tier);
        if (cards.isEmpty()) {
            System.out.println(Ansi.dim("  No cards visible in this tier."));
            System.out.println();
            return;
        }
        printBlocks(renderCardBlocks(cards));
        System.out.println();
    }

    private void printCurrentPlayer(Player player) {
        System.out.println(sectionTitle("Your Hand - " + player.getName()));
        String summary = "Points=" + player.getPoints()
                + "   Tokens=" + player.getTotalGems() + "/10"
                + "   Bonuses=" + formatBonusSummary(player);
        System.out.println("  " + summary);
        System.out.println("  Gems: " + formatGemInventory(player));
        System.out.println("  Nobles: " + (player.getNobles().isEmpty() ? "none" : player.getNobles().size()));
        System.out.println();

        System.out.println(Ansi.accent("  Reserved Cards"));
        if (player.getReservedCards().isEmpty()) {
            System.out.println(Ansi.dim("    None"));
        } else {
            printBlocks(renderCardBlocks(player.getReservedCards()));
        }
        System.out.println();

        System.out.println(Ansi.accent("  Purchased Bonus Stacks"));
        List<List<String>> summaryCards = renderPurchasedStacks(player);
        if (summaryCards.isEmpty()) {
            System.out.println(Ansi.dim("    No purchased cards yet."));
        } else {
            printBlocks(summaryCards);
        }
        System.out.println();
    }

    private void printOtherPlayers(GameState gameState) {
        System.out.println(sectionTitle("Other Players"));
        boolean printedPlayer = false;
        for (Player player : gameState.getPlayers()) {
            if (player == gameState.getCurrentPlayer()) {
                continue;
            }

            printedPlayer = true;
            String line = padRight(player.getName(), 12)
                    + " points=" + player.getPoints()
                    + "  tokens=" + player.getTotalGems() + "/10"
                    + "  reserved=" + player.getReservedCards().size()
                    + "  nobles=" + player.getNobles().size()
                    + "  bonuses=" + formatBonusSummary(player);
            System.out.println("  " + line);
            System.out.println("  Gems: " + formatGemInventory(player));
            System.out.println();
        }

        if (!printedPlayer) {
            System.out.println(Ansi.dim("  No other players."));
            System.out.println();
        }
    }

    private List<List<String>> renderCardBlocks(List<Card> cards) {
        List<List<String>> blocks = new ArrayList<List<String>>();
        for (int i = 0; i < cards.size(); i++) {
            blocks.add(renderDevelopmentCard(cards.get(i), "[" + (i + 1) + "]"));
        }
        return blocks;
    }

    private List<String> renderDevelopmentCard(Card card, String label) {
        List<String> lines = new ArrayList<String>();
        String borderColor = Ansi.colorForGem(card.getBonus());
        String top = TOP_LEFT_CORNER + repeat(HORIZONTAL_LINE, CARD_INNER_WIDTH) + TOP_RIGHT_CORNER;
        String bottom = BOTTOM_LEFT_CORNER + repeat(HORIZONTAL_LINE, CARD_INNER_WIDTH) + BOTTOM_RIGHT_CORNER;

        lines.add(Ansi.wrap(borderColor, top)); //print top border
        lines.add(wrapCardLine(borderColor, fitTwoSides(label + " T" + tierNumber(card.getTier()), card.getPoints() + "P", CARD_INNER_WIDTH)));
        lines.add(wrapCardLine(borderColor, fitToWidth("", CARD_INNER_WIDTH))); //print empty line
        List<String> costs = buildCostParts(card);
        lines.add(wrapCardLine(borderColor, center(costs.get(0), CARD_INNER_WIDTH))); // 1st line of gems costs
        lines.add(wrapCardLine(borderColor, center(costs.get(1), CARD_INNER_WIDTH))); //2nd line of gem costs (may be empty), not empty if more than 3 types of gems cost
        lines.add(wrapCardLine(borderColor, fitTwoSides("bonus", gemIcon(card.getBonus()), CARD_INNER_WIDTH))); //the type of bonus
        lines.add(Ansi.wrap(borderColor, bottom)); //bottom line
        return lines;
    }

    private List<String> renderNobleBlock(Noble noble, String label) {
        List<String> lines = new ArrayList<String>();
        String top = HEAVY_TOP_LEFT_CORNER + repeat(HEAVY_HORIZONTAL_LINE, NOBLE_INNER_WIDTH) + HEAVY_TOP_RIGHT_CORNER;
        String bottom = HEAVY_BOTTOM_LEFT_CORNER + repeat(HEAVY_HORIZONTAL_LINE, NOBLE_INNER_WIDTH) + HEAVY_BOTTOM_RIGHT_CORNER;
        List<String> costs = buildNobleRequirementParts(noble);

        lines.add(Ansi.wrap(Ansi.WHITE, top));
        lines.add(wrapNobleLine(fitTwoSides(label + " " + noble.getId(), noble.getPoints() + "P", NOBLE_INNER_WIDTH)));
        lines.add(wrapNobleLine(center("NOBLE", NOBLE_INNER_WIDTH)));
        lines.add(wrapNobleLine(center(costs.get(0), NOBLE_INNER_WIDTH)));
        lines.add(wrapNobleLine(center(costs.get(1), NOBLE_INNER_WIDTH)));
        lines.add(Ansi.wrap(Ansi.WHITE, bottom));
        return lines;
    }

    private List<List<String>> renderPurchasedStacks(Player player) {
        List<List<String>> blocks = new ArrayList<List<String>>();
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }

            int cardCount = player.getBonusCount(color);
            if (cardCount == 0) {
                continue;
            }

            int points = 0;
            for (Card card : player.getPurchasedCards()) {
                if (card.getBonus() == color) {
                    points += card.getPoints();
                }
            }

            blocks.add(renderSummaryCard(color, cardCount, points));
        }
        return blocks;
    }

    private List<String> renderSummaryCard(GemColor color, int count, int points) {
        List<String> lines = new ArrayList<String>();
        String accent = Ansi.colorForGem(color);
        String top = TOP_LEFT_CORNER + repeat(HORIZONTAL_LINE, SUMMARY_CARD_INNER_WIDTH) + TOP_RIGHT_CORNER;
        String bottom = BOTTOM_LEFT_CORNER + repeat(HORIZONTAL_LINE, SUMMARY_CARD_INNER_WIDTH) + BOTTOM_RIGHT_CORNER;

        lines.add(Ansi.wrap(accent, top));
        lines.add(wrapCardLine(accent, fitTwoSides(gemIcon(color), styledAmount(color, count), SUMMARY_CARD_INNER_WIDTH)));
        lines.add(wrapCardLine(accent, center("points: " + points, SUMMARY_CARD_INNER_WIDTH)));
        lines.add(wrapCardLine(accent, center("cards: " + count, SUMMARY_CARD_INNER_WIDTH)));
        lines.add(Ansi.wrap(accent, bottom));
        return lines;
    }

    private List<String> buildCostParts(Card card) {
        List<String> costTokens = new ArrayList<String>();
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }

            int amount = card.getCost().get(color);
            if (amount > 0) {
                costTokens.add(gemToken(color, amount));
            }
        }

        if (costTokens.isEmpty()) {
            costTokens.add("FREE");
        }

        return splitTokens(costTokens);
    }

    private List<String> buildNobleRequirementParts(Noble noble) {
        List<String> tokens = new ArrayList<String>();
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }

            int amount = noble.getRequirements().get(color);
            if (amount > 0) {
                tokens.add(gemToken(color, amount));
            }
        }

        if (tokens.isEmpty()) {
            tokens.add("NONE");
        }

        return splitTokens(tokens);
    }

    // split the gem/requirement tokens into 2 lines so they can fit inside the box
    private List<String> splitTokens(List<String> tokens) {
        List<String> lines = new ArrayList<String>();
        String first = "";
        String second = "";
        for (int i = 0; i < tokens.size(); i++) {
            if (i < 3) {
                first = appendToken(first, tokens.get(i));
            } else {
                second = appendToken(second, tokens.get(i));
            }
        }

        lines.add(first.isEmpty() ? " " : first);
        lines.add(second.isEmpty() ? " " : second);
        return lines;
    }

    private String appendToken(String line, String token) {
        if (line.isEmpty()) {
            return token;
        }
        return line + " " + token;
    }

    // prints multiple card/noble boxes next to each other line by line
    private void printBlocks(List<List<String>> blocks) {
        if (blocks.isEmpty()) {
            return;
        }

        int blockHeight = blocks.get(0).size();
        for (int lineIndex = 0; lineIndex < blockHeight; lineIndex++) {
            StringBuilder row = new StringBuilder("  ");
            for (int blockIndex = 0; blockIndex < blocks.size(); blockIndex++) {
                if (blockIndex > 0) {
                    row.append("  ");
                }
                row.append(blocks.get(blockIndex).get(lineIndex));
            }
            System.out.println(row.toString());
        }
    }

    private void printBanner(String title) {
        String frame = "+" + repeat("-", 30) + "+";
        System.out.println();
        System.out.println(Ansi.accent(center(frame, SCREEN_WIDTH)));
        System.out.println(Ansi.accent(center("|" + center(title, 30) + "|", SCREEN_WIDTH)));
        System.out.println(Ansi.accent(center(frame, SCREEN_WIDTH)));
        System.out.println();
    }

    private String sectionTitle(String title) {
        String text = "[ " + title + " ]";
        return Ansi.accent(text);
    }

    private void printRule() {
        System.out.println(Ansi.accent(repeat("-", SCREEN_WIDTH)));
        System.out.println();
    }

    // returns one fixed-width line with the left text and right text spaced properly
    private String fitTwoSides(String left, String right, int width) {
        int spaces = width - displayLength(left) - displayLength(right);
        if (spaces < 1) {
            String merged = left + " " + right;
            return fitToWidth(merged, width);
        }
        return left + repeat(" ", spaces) + right;
    }

    private String formatBonusSummary(Player player) {
        List<String> parts = new ArrayList<String>();
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }
            parts.add(gemToken(color, player.getBonusCount(color)));
        }
        return joinStrings(parts, " ");
    }

    private String formatGemInventory(Player player) {
        List<String> parts = new ArrayList<String>();
        for (GemColor color : GemColor.values()) {
            String chip = gemToken(color, player.getGemCount(color));
            parts.add(chip);
        }
        return joinStrings(parts, "  ");
    }

    private String gemIcon(GemColor color) {
        if (color == GemColor.DIAMOND) {
            return DIAMOND_EMOJI;
        }
        if (color == GemColor.SAPPHIRE) {
            return SAPPHIRE_EMOJI;
        }
        if (color == GemColor.EMERALD) {
            return EMERALD_EMOJI;
        }
        if (color == GemColor.RUBY) {
            return RUBY_EMOJI;
        }
        if (color == GemColor.ONYX) {
            return ONYX_EMOJI;
        }
        return GOLD_EMOJI;
    }

    private String gemToken(GemColor color, int amount) {
        return gemIcon(color) + " " + styledAmount(color, amount);
    }

    private String styledAmount(GemColor color, int amount) {
        return Ansi.style(String.valueOf(amount), Ansi.colorForGem(color), Ansi.BOLD);
    }

    private int tierNumber(Tier tier) {
        if (tier == Tier.ONE) {
            return 1;
        }
        if (tier == Tier.TWO) {
            return 2;
        }
        return 3;
    }

    private String center(String text, int width) {
        if (displayLength(text) >= width) {
            return fitToWidth(text, width);
        }

        int leftPadding = (width - displayLength(text)) / 2;
        int rightPadding = width - displayLength(text) - leftPadding;
        return repeat(" ", leftPadding) + text + repeat(" ", rightPadding);
    }

    private String padRight(String text, int width) {
        int textWidth = displayLength(text);
        if (textWidth == width) {
            return text;
        }
        if (textWidth > width) {
            return fitToWidth(text, width);
        }
        return text + repeat(" ", width - textWidth);
    }

    private String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }

    private String joinStrings(List<String> values, String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(separator);
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }

    private String wrapCardLine(String borderColor, String content) {
        return Ansi.wrap(borderColor, VERTICAL_LINE) + content + Ansi.wrap(borderColor, VERTICAL_LINE);
    }

    private String wrapNobleLine(String content) {
        return Ansi.wrap(Ansi.WHITE, HEAVY_VERTICAL_LINE) + content + Ansi.wrap(Ansi.WHITE, HEAVY_VERTICAL_LINE);
    }

    // gets the visible width of the text in the terminal
    // this ignores ANSI color codes and counts wide symbols like emoji properly
    private int displayLength(String text) {
        String plain = stripAnsi(text);
        int width = 0;
        for (int i = 0; i < plain.length();) {
            int codePoint = plain.codePointAt(i);
            width += codePointWidth(codePoint);
            i += Character.charCount(codePoint);
        }
        return width;
    }
    // makes sure the text fits the exact width needed for the card layout
    // if it is too long, trim it; if it is too short, pad it with spaces
    private String fitToWidth(String text, int width) {
        int textWidth = displayLength(text);
        if (textWidth == width) {
            return text;
        }
        if (textWidth < width) {
            return text + repeat(" ", width - textWidth);
        }

        String plain = stripAnsi(text);
        StringBuilder builder = new StringBuilder();
        int currentWidth = 0;

        for (int i = 0; i < plain.length();) {
            int codePoint = plain.codePointAt(i);
            int codePointWidth = codePointWidth(codePoint);
            if (currentWidth + codePointWidth > width) {
                break;
            }
            builder.appendCodePoint(codePoint);
            currentWidth += codePointWidth;
            i += Character.charCount(codePoint);
        }

        while (currentWidth < width) {
            builder.append(' ');
            currentWidth++;
        }

        return builder.toString();
    }

    // removes ANSI color/style codes so they do not affect width calculations
    private String stripAnsi(String text) {
        return text.replaceAll("\\u001B\\[[;\\d]*m", "");
    }

    // estimates how many terminal columns one character takes
    private int codePointWidth(int codePoint) {
        if (codePoint == 0) {
            return 0;
        }
        if (codePoint == '\n' || codePoint == '\r') {
            return 0;
        }
        if (Character.getType(codePoint) == Character.NON_SPACING_MARK
                || Character.getType(codePoint) == Character.ENCLOSING_MARK
                || Character.getType(codePoint) == Character.COMBINING_SPACING_MARK) {
            return 0;
        }
        if (codePoint == 0x200D || codePoint == 0xFE0F) {
            return 0;
        }
        if (isWideCodePoint(codePoint)) {
            return 2;
        }
        return 1;
    }

    // checks if the character is usually displayed as a wide character
    // examples are emoji and some CJK symbols
    private boolean isWideCodePoint(int codePoint) {
        if (codePoint >= 0x1100 && codePoint <= 0x115F) {
            return true;
        }
        if (codePoint >= 0x2329 && codePoint <= 0x232A) {
            return true;
        }
        if (codePoint >= 0x2E80 && codePoint <= 0xA4CF) {
            return true;
        }
        if (codePoint >= 0xAC00 && codePoint <= 0xD7A3) {
            return true;
        }
        if (codePoint >= 0xF900 && codePoint <= 0xFAFF) {
            return true;
        }
        if (codePoint >= 0xFE10 && codePoint <= 0xFE19) {
            return true;
        }
        if (codePoint >= 0xFE30 && codePoint <= 0xFE6F) {
            return true;
        }
        if (codePoint >= 0xFF00 && codePoint <= 0xFF60) {
            return true;
        }
        if (codePoint >= 0xFFE0 && codePoint <= 0xFFE6) {
            return true;
        }
        if (codePoint >= 0x1F300 && codePoint <= 0x1FAFF) {
            return true;
        }
        if (codePoint >= 0x2600 && codePoint <= 0x27BF) {
            return true;
        }
        return false;
    }
}
