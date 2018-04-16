import hlt.*;
import javafx.util.Pair;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.*;

public class AI {

    public static TreeMap<Double, List<Pair<Ship, Planet>>> sort(GameMap gameMap, List<Ship> ourShips, List<Planet> noOnePlanets) {
        TreeMap<Double, List<Pair<Ship, Planet>>> result = new TreeMap<>();

//        ArrayList<Planet> planets = new ArrayList<>();
        for (Planet p : noOnePlanets) {
            for (Ship s : ourShips) {
                double distance = s.getRealDistanceTo(gameMap, p);
                List<Pair<Ship, Planet>> l = result.get(distance);
                if (l == null) {
                    l = new ArrayList<>();
                }
                l.add(new Pair<>(s, p));
                result.put(distance, l);
            }
        }

        return result;
    }

    public static void nextTarget(GameMap gameMap, List<Ship> attackShips, List<Planet> noOnePlanets) {
        TreeMap<Double, List<Pair<Ship, Planet>>> tree = sort(gameMap, attackShips, noOnePlanets);
        if (tree.isEmpty())
            return;

        double min = Double.MAX_VALUE;

        for (Map.Entry<Double ,List<Pair<Ship, Planet>>> list = tree.pollFirstEntry(); !tree.isEmpty(); list = tree.pollFirstEntry()) {
            for (Pair<Ship, Planet> pair : list.getValue()) {
                Planet p =  pair.getValue();
                if (p.getNumShipsTowardsPlanet() > (p.getDockingSpots() - p.getDockedShips().size())) {
                    continue;
                }

                double score = (double) p.getDockingSpots() / list.getKey();
                if (min > score) {
                    min = score;

                }

            }
        }
        return;
    }
}
