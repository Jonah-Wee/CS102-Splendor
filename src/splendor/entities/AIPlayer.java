package splendor.entities;

import splendor.logic.GameState;
import splendor.logic.ai.AIAction;
import splendor.logic.ai.AIStrategy;

/**
 * An AI-controlled player. Extends Player so it can be stored in
 * the same players list and passed to all existing GameEngine methods
 * without modification.
 *
 * The actual decision logic is fully delegated to the injected AIStrategy,
 * keeping AIPlayer itself thin and easy to test.
 */
public class AIPlayer extends Player {

    private final AIStrategy strategy;

    public AIPlayer(String name, AIStrategy strategy) {
        super(name);
        this.strategy = strategy;
    }

    /**
     * Ask the strategy to pick an action given the current game state.
     * Called by ConsoleGameUI instead of prompting the user for input.
     *
     * @param state the current (full) game state
     * @return the chosen AIAction
     */
    public AIAction chooseAction(GameState state) {
        return strategy.selectAction(state, this);
    }

    public GemColor chooseGemToDiscard(GameState state) {
        return strategy.chooseGemToDiscard(state, this);
    }

    /** Convenience check used in ConsoleGameUI and GameEngine. */
    public static boolean isAI(Player player) {
        return player instanceof AIPlayer;
    }
}
