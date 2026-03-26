package splendor.logic.ai;

public enum AIDifficulty {
    EASY,
    MEDIUM,
    HARD;

    public AIStrategy createStrategy() {
        if (this == EASY) {
            return new EasyStrategy();
        }
        if (this == MEDIUM) {
            return new MediumStrategy();
        }
        return new HardStrategy();
    }
}
