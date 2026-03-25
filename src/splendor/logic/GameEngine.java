package splendor.logic;

import java.util.List;
import splendor.entities.Card;
import splendor.entities.GemBank;
import splendor.entities.GemColor;
import splendor.entities.Noble;
import splendor.entities.Player;
import splendor.entities.Tier;

public class GameEngine {
    private final GameState gameState;

    public GameEngine(GameState gameState) {
        if (gameState == null) {
            throw new IllegalArgumentException("gameState cannot be null");
        }
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Player getCurrentPlayer() {
        return gameState.getCurrentPlayer();
    }

    // can current player afford this visible card?
    public boolean canAffordCurrentPlayer(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("card cannot be null");
        }
        return canAfford(getCurrentPlayer(), card);
    }

    public boolean takeThreeDifferentGems(GemColor first, GemColor second, GemColor third) {
        if (first == null || second == null || third == null) {
            throw new IllegalArgumentException("Gem colors cannot be null");
        }
        if (first == GemColor.GOLD || second == GemColor.GOLD || third == GemColor.GOLD) {
            return false;
        }
        if (first == second || first == third || second == third) {
            return false;
        }

        Player player = getCurrentPlayer();
        GemBank bank = gameState.getGemBank();
        if (!bank.hasAtLeast(first, 1) || !bank.hasAtLeast(second, 1) || !bank.hasAtLeast(third, 1)) {
            return false;
        }

        bank.takeGem(first);
        bank.takeGem(second);
        bank.takeGem(third);
        player.addGem(first, 1);
        player.addGem(second, 1);
        player.addGem(third, 1);
        return true;
    }

    public boolean takeTwoSameGems(GemColor color) {
        if (color == null) {
            throw new IllegalArgumentException("Gem color cannot be null");
        }
        if (color == GemColor.GOLD) {
            return false;
        }

        Player player = getCurrentPlayer();
        GemBank bank = gameState.getGemBank();
        if (!bank.hasAtLeast(color, 4)) {
            return false;
        }

        bank.takeGem(color);
        bank.takeGem(color);
        player.addGem(color, 2);
        return true;
    }

    public boolean takeDifferentGems(List<GemColor> colors) {
        if (colors == null || colors.isEmpty()) return false;
        for (GemColor color : colors) {
            if (color == null || color == GemColor.GOLD) return false;
        }
        for (int i = 0; i < colors.size(); i++) {
            for (int j = i + 1; j < colors.size(); j++) {
                if (colors.get(i) == colors.get(j)) return false;
            }
        }
        GemBank bank = gameState.getGemBank();
        for (GemColor color : colors) {
            if (!bank.hasAtLeast(color, 1)) return false;
        }
        Player player = getCurrentPlayer();
        for (GemColor color : colors) {
            bank.takeGem(color);
            player.addGem(color, 1);
        }
        return true;
    }

    public boolean discardGem(GemColor color) {
        if (color == null) {
            throw new IllegalArgumentException("Gem color cannot be null");
        }
        Player player = getCurrentPlayer();
        if (player.getGemCount(color) < 1) {
            return false;
        }
        player.removeGem(color, 1);
        gameState.getGemBank().addGem(color);
        return true;
    }

    public boolean reserveVisibleCard(Tier tier, int slotIndex) {
        if (tier == null) {
            throw new IllegalArgumentException("Tier cannot be null");
        }

        List<Card> visibleCards = gameState.getVisibleCards(tier);
        if (slotIndex < 0 || slotIndex >= visibleCards.size()) {
            throw new IllegalArgumentException("Invalid visible card index");
        }

        Player player = getCurrentPlayer();
        if (player.getReservedCards().size() >= 3) {
            return false;
        }

        boolean takeGold = gameState.getGemBank().hasAtLeast(GemColor.GOLD, 1);

        Card card = gameState.removeVisibleCard(tier, slotIndex);
        player.addReservedCard(card);

        if (takeGold) {
            gameState.getGemBank().takeGem(GemColor.GOLD);
            player.addGem(GemColor.GOLD, 1);
        }

        gameState.refillVisibleCard(tier, slotIndex);
        return true;
    }

    public boolean reserveTopCard(Tier tier) {
        if (tier == null) {
            throw new IllegalArgumentException("Tier cannot be null");
        }

        Player player = getCurrentPlayer();
        if (player.getReservedCards().size() >= 3) {
            return false;
        }

        boolean takeGold = gameState.getGemBank().hasAtLeast(GemColor.GOLD, 1);

        Card card = gameState.getDeck(tier).drawCard();
        if (card == null) {
            return false;
        }

        player.addReservedCard(card);
        if (takeGold) {
            gameState.getGemBank().takeGem(GemColor.GOLD);
            player.addGem(GemColor.GOLD, 1);
        }
        return true;
    }

    public boolean buyVisibleCard(Tier tier, int slotIndex) {
        if (tier == null) {
            throw new IllegalArgumentException("Tier cannot be null");
        }

        List<Card> visibleCards = gameState.getVisibleCards(tier);
        if (slotIndex < 0 || slotIndex >= visibleCards.size()) {
            throw new IllegalArgumentException("Invalid visible card index");
        }

        Card card = visibleCards.get(slotIndex);
        if (!canAfford(getCurrentPlayer(), card)) {
            return false;
        }

        Card boughtCard = gameState.removeVisibleCard(tier, slotIndex);
        payForCard(getCurrentPlayer(), boughtCard);
        getCurrentPlayer().addPurchasedCard(boughtCard);
        gameState.refillVisibleCard(tier, slotIndex);
        claimEligibleNobles();
        checkWinCondition();
        return true;
    }

    public boolean buyReservedCard(int reservedIndex) {
        Player player = getCurrentPlayer();
        if (reservedIndex < 0 || reservedIndex >= player.getReservedCards().size()) {
            throw new IllegalArgumentException("Invalid reserved card index");
        }

        Card card = player.getReservedCards().get(reservedIndex);
        if (!canAfford(player, card)) {
            return false;
        }

        Card boughtCard = player.removeReservedCard(reservedIndex);
        payForCard(player, boughtCard);
        player.addPurchasedCard(boughtCard);
        claimEligibleNobles();
        checkWinCondition();
        return true;
    }

    public boolean claimEligibleNobles() {
        Player player = getCurrentPlayer();
        List<Noble> nobles = gameState.getNoblesInPlay();

        for (int i = 0; i < nobles.size(); i++) {
            Noble noble = nobles.get(i);
            if (meetsNobleRequirements(player, noble)) {
                player.addNoble(noble);
                nobles.remove(i);
                return true;
            }
        }

        return false;
    }

    public void nextTurn() {
        gameState.advanceTurn();
    }

    public boolean isGameOver() {
        return gameState.isGameOver();
    }

    private boolean meetsNobleRequirements(Player player, Noble noble) {
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }

            int needed = noble.getRequirements().get(color);
            if (player.getBonusCount(color) < needed) {
                return false;
            }
        }
        return true;
    }

    private boolean canAfford(Player player, Card card) {
        int goldNeeded = 0;

        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }

            int cost = card.getCost().get(color);
            int discount = player.getBonusCount(color);
            int remainingCost = cost - discount;
            if (remainingCost < 0) {
                remainingCost = 0;
            }

            int gemsOwned = player.getGemCount(color);
            if (gemsOwned < remainingCost) {
                goldNeeded += remainingCost - gemsOwned;
            }
        }

        return player.getGemCount(GemColor.GOLD) >= goldNeeded;
    }

    private void payForCard(Player player, Card card) {
        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                continue;
            }

            int cost = card.getCost().get(color);
            int discount = player.getBonusCount(color);
            int remainingCost = cost - discount;
            if (remainingCost < 0) {
                remainingCost = 0;
            }

            int colorSpent = Math.min(player.getGemCount(color), remainingCost);
            if (colorSpent > 0) {
                player.removeGem(color, colorSpent);
                for (int i = 0; i < colorSpent; i++) {
                    gameState.getGemBank().addGem(color);
                }
            }

            int goldSpent = remainingCost - colorSpent;
            if (goldSpent > 0) {
                player.removeGem(GemColor.GOLD, goldSpent);
                for (int i = 0; i < goldSpent; i++) {
                    gameState.getGemBank().addGem(GemColor.GOLD);
                }
            }
        }
    }

    private void checkWinCondition() {
        if (getCurrentPlayer().getPoints() >= gameState.getWinPoints()) {
            gameState.startFinalRound();
        }
    }
}
