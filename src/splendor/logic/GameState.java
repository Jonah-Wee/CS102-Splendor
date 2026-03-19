package splendor.logic;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import splendor.entities.Card;
import splendor.entities.CardDeck;
import splendor.entities.GemBank;
import splendor.entities.Noble;
import splendor.entities.Player;
import splendor.entities.Tier;

public class GameState {
    private final List<Player> players;
    private final GemBank gemBank;
    private final Map<Tier, CardDeck> decks;
    private final Map<Tier, List<Card>> visibleCards;
    private final List<Noble> noblesInPlay;
    private final int winPoints;
    private int currentPlayerIndex;
    private boolean finalRoundStarted;
    private int finalRoundStarterIndex;
    private boolean gameOver;

    public GameState(List<Player> players, GemBank gemBank, Map<Tier, CardDeck> decks,
            Map<Tier, List<Card>> visibleCards, List<Noble> noblesInPlay, int winPoints) {
        this.players = new ArrayList<Player>(players);
        this.gemBank = gemBank;
        this.decks = new EnumMap<Tier, CardDeck>(Tier.class);
        this.decks.putAll(decks);
        this.visibleCards = new EnumMap<Tier, List<Card>>(Tier.class);
        for (Tier tier : Tier.values()) {
            List<Card> row = visibleCards.get(tier);
            if (row == null) {
                this.visibleCards.put(tier, new ArrayList<Card>());
            } else {
                this.visibleCards.put(tier, new ArrayList<Card>(row));
            }
        }
        this.noblesInPlay = new ArrayList<Noble>(noblesInPlay);
        this.winPoints = winPoints;
        this.currentPlayerIndex = 0;
        this.finalRoundStarted = false;
        this.finalRoundStarterIndex = -1;
        this.gameOver = false;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public GemBank getGemBank() {
        return gemBank;
    }

    public Map<Tier, CardDeck> getDecks() {
        return new HashMap<Tier, CardDeck>(decks);
    }

    public CardDeck getDeck(Tier tier) {
        return decks.get(tier);
    }

    public List<Card> getVisibleCards(Tier tier) {
        return visibleCards.get(tier);
    }

    public List<Noble> getNoblesInPlay() {
        return noblesInPlay;
    }

    public int getWinPoints() {
        return winPoints;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public boolean isFinalRoundStarted() {
        return finalRoundStarted;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void refillVisibleCard(Tier tier, int slotIndex) {
        CardDeck deck = decks.get(tier);
        if (deck == null || deck.isEmpty()) {
            return;
        }
        visibleCards.get(tier).add(slotIndex, deck.drawCard());
    }

    public Card removeVisibleCard(Tier tier, int slotIndex) {
        return visibleCards.get(tier).remove(slotIndex);
    }

    public void startFinalRound() {
        if (!finalRoundStarted) {
            finalRoundStarted = true;
            finalRoundStarterIndex = currentPlayerIndex;
        }
    }

    public void advanceTurn() {
        if (gameOver) {
            return;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        if (finalRoundStarted && currentPlayerIndex == finalRoundStarterIndex) {
            gameOver = true;
        }
    }

    @Override
    public String toString() {
        return "GameState{" +
                "currentPlayer=" + getCurrentPlayer().getName() +
                ", winPoints=" + winPoints +
                ", finalRoundStarted=" + finalRoundStarted +
                ", gameOver=" + gameOver +
                ", noblesInPlay=" + noblesInPlay +
                ", visibleTierOne=" + visibleCards.get(Tier.ONE) +
                ", visibleTierTwo=" + visibleCards.get(Tier.TWO) +
                ", visibleTierThree=" + visibleCards.get(Tier.THREE) +
                "}";
    }
}
