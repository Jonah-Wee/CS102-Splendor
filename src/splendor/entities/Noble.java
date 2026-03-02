package splendor.entities;

import java.util.EnumMap;

public class Noble {
    private String id;
    private int points;
    private EnumMap<GemColor, Integer> requirements;
    public Noble(String id, int points, EnumMap<GemColor, Integer> requirements) {
        this.id = id;
        this.points = points;
        this.requirements = requirements;
    }
    public String getId() {
        return id;
    }
    public int getPoints() {
        return points;
    }
    public EnumMap<GemColor, Integer> getRequirements() {
        return requirements;
    }
    @Override
    public String toString() {
        return String.format("Noble{id=%s, points=%d, requirements=%s}", id, points, requirements);
    }
}
