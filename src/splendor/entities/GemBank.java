package splendor.entities;

import java.util.EnumMap;

public class GemBank {
    private EnumMap<GemColor, Integer> gems;
    public GemBank(int numPlayers) {
        this.gems = new EnumMap<>(GemColor.class);

        int standardGemCount;

        if (numPlayers == 2) {
            standardGemCount = 4;
        } else if (numPlayers == 3) {
            standardGemCount = 5;
        } else if (numPlayers == 4) {
            standardGemCount = 7;
        } else {
            throw new IllegalArgumentException("Number of players must be 2, 3, or 4.");
        }

        for (GemColor color : GemColor.values()) {
            if (color == GemColor.GOLD) {
                gems.put(color, 5);
            } else {
                gems.put(color, standardGemCount);
            }
        }
    }

    @Override
    public String toString() {
        return "GemBank{" +
                "gems=" + gems +
                "}";
    }
}
