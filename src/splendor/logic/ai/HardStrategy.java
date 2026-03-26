package splendor.logic.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import splendor.entities.GemColor;
import splendor.entities.Player;
import splendor.logic.GameState;

/**
 * Hard AI strategy.
 *
 * Intended behaviour (to be implemented):
 *  - Plans multiple turns ahead.
 *  - Tracks opponent progress and blocks high-value cards when needed.
 *  - Optimises noble acquisition as a primary goal.
 */
public class HardStrategy implements AIStrategy {
    private final MediumStrategy fallbackStrategy = new MediumStrategy();

    @Override
    public AIAction selectAction(GameState state, Player self) {
        return fallbackStrategy.selectAction(state, self);
    }

    @Override
    public GemColor chooseGemToDiscard(GameState state, Player self) {
        return fallbackStrategy.chooseGemToDiscard(state, self);
    }
}
