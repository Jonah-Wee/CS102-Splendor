package splendor.entities;

import java.util.EnumMap;

public class GemBank {
    private final EnumMap<GemColor, Integer> gems;

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

    public int getGemCount(GemColor color) {
        Integer count = gems.get(color);
        return count == null ? 0 : count;
    }

    public EnumMap<GemColor, Integer> getGems() {
        return new EnumMap<GemColor, Integer>(gems);
    }

    public boolean hasAtLeast(GemColor color, int amount) {
        return getGemCount(color) >= amount;
    }

    public boolean takeGem(GemColor color) {
        if (!hasAtLeast(color, 1)) {
            return false;
        }
        gems.put(color, getGemCount(color) - 1);
        return true;
    }

    public void addGem(GemColor color) {
        gems.put(color, getGemCount(color) + 1);
    }

    @Override
    public String toString() {
        return "GemBank{" +
                "gems=" + gems +
                "}";
    }
}
