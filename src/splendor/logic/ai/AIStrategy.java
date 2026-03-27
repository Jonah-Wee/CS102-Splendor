package splendor.logic.ai;

import splendor.entities.GemColor;
import splendor.entities.Player;
import splendor.logic.GameState;

/**
 * Strategy interface for AI decision-making.
 * Implement this to create different AI difficulty levels 
 */
public interface AIStrategy {

    /**
     * Select the best action for the current turn.
     *
     * @param state the full (read-only) game state
     * @param self  the AI player whose turn it is
     * @return a fully-specified AIAction ready to be executed
     */
    AIAction selectAction(GameState state, Player self);

    /**
     * Choose a gem to discard when the player is over the 10-token limit.
     *
     * @param state the current game state
     * @param self  the AI player who must discard
     * @return the gem color to discard
     */
    GemColor chooseGemToDiscard(GameState state, Player self);
}
