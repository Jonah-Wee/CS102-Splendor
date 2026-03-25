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

    @Override
    public AIAction selectAction(GameState state, Player self) {
        // TODO: implement hard AI logic
        throw new UnsupportedOperationException("HardStrategy not yet implemented");
    }

    @Override
    public GemColor chooseGemToDiscard(GameState state, Player self) {
        // TODO: implement hard AI discard logic
        List<GemColor> held = new ArrayList<>();
        for (GemColor color : GemColor.values()) {
            if (self.getGemCount(color) > 0) {
                held.add(color);
            }
        }
        return held.get(new Random().nextInt(held.size()));
    }
}
