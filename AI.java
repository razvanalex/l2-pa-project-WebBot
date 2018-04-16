import hlt.*;

import java.util.*;

class Pair<F, S> {
	private F first;
	private S second;

	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	public F getFirst() {
		return this.first;
	}

	public S getSecond() {
		return this.second;
	}
}

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

    public static Pair<Integer, Planet> initialStrat(GameMap gameMap, List<Ship> attackShips, List<Planet> noOnePlanets) {
        TreeMap<Double, List<Pair<Ship, Planet>>> tree = sort(gameMap, attackShips, noOnePlanets);
        if (tree.isEmpty())
            return;

        double min = Double.MAX_VALUE;
        Planet bestPlanet = null;

        for (Map.Entry<Double, List<Pair<Ship, Planet>>> list = tree.pollFirstEntry(); !tree.isEmpty(); list = tree.pollFirstEntry()) {
            for (Pair<Ship, Planet> pair : list.getSecond()) {
                Planet p =  pair.getSecond();
                if (p.getNumShipsTowardsPlanet() > (p.getDockingSpots() - p.getDockedShips().size())) {
                    continue;
                }

                double score = (double) p.getDockingSpots() / list.getFirst();
                if (min > score || bestPlanet == null) {
                    min = score;
                    bestPlanet = p;
                }

            }
        }
        return new Pair<>(score, bestPlanet);
    }

}
