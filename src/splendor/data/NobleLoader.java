package splendor.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import splendor.entities.GemColor;
import splendor.entities.Noble;

public class NobleLoader {

    public static List<Noble> loadNobles(String filePath) throws IOException {
        List<Noble> nobles = new ArrayList<Noble>();

        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        boolean isFirstLine = true;

        while ((line = br.readLine()) != null) {
            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            if (isFirstLine) {
                isFirstLine = false;
                continue;
            }

            String[] parts = line.split(",");

            String id = parts[0].trim();
            int points = Integer.parseInt(parts[1].trim());

            EnumMap<GemColor, Integer> requirements = new EnumMap<GemColor, Integer>(GemColor.class);
            requirements.put(GemColor.DIAMOND, Integer.parseInt(parts[2].trim()));
            requirements.put(GemColor.SAPPHIRE, Integer.parseInt(parts[3].trim()));
            requirements.put(GemColor.EMERALD, Integer.parseInt(parts[4].trim()));
            requirements.put(GemColor.RUBY, Integer.parseInt(parts[5].trim()));
            requirements.put(GemColor.ONYX, Integer.parseInt(parts[6].trim()));
            requirements.put(GemColor.GOLD, 0);

            nobles.add(new Noble(id, points, requirements));
        }

        br.close();
        return nobles;
    }
}
