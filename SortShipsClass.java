import hlt.Planet;
import hlt.Ship;

import java.util.*;

import hlt.*;

//TODO de refacut alegerea initiala
//TODO nu mai se impart navele
//TODO de facut functii pt attack , def

public class SortShipsClass {
    static class ComparatorSP implements Comparator<MyEntry> {

        public int compare(MyEntry entity1, MyEntry entity2) {
            double d1 = entity1.getKey().getDistanceTo(entity1.getValue());
            double d2 = entity2.getKey().getDistanceTo(entity2.getValue());
            if (d1 - d2 < 0) return -1;
            else if (d1 - d2 > 0) return 1;

            return 0;
        }
    }

    static class MyEntry implements Map.Entry<Ship, Planet> {
        private final Ship key;
        private Planet value;

        public MyEntry(Ship key, Planet value) {
            this.key = key;
            this.value = value;
        }

        public Ship getKey() {
            return key;
        }

        public Planet getValue() {
            return value;
        }

        public Planet setValue(Planet value) {
            Planet old = this.value;
            this.value = value;
            return old;
        }

    }


    public static List<MyEntry> choose(List<Ship> ships, List<Planet> planets){
        List<MyEntry> new_ships = new LinkedList<MyEntry>();

        ArrayList<Ship> copyShips = new ArrayList<>(ships.size());
        for (Iterator<Ship> iterShip = ships.iterator(); iterShip.hasNext(); )
            copyShips.add(iterShip.next());

        TreeSet<Integer> scores = new TreeSet<>();

        for(Ship ship : copyShips){
            long dist = Long.MAX_VALUE;
            for(Planet planet : planets){

            }
        }

        return new_ships;
    }

    static List<MyEntry> sortShips(List<Ship> ships, List<Planet> planets) {
        List<Ship> copyShips = new LinkedList<Ship>();
        for (Iterator<Ship> iterShip = ships.iterator(); iterShip.hasNext(); )
            copyShips.add(iterShip.next());

        TreeSet<MyEntry> dist_s_p = new TreeSet<MyEntry>(new ComparatorSP());

        List<MyEntry> new_ships = new LinkedList<MyEntry>();

        while (copyShips.isEmpty() == false) { /** pentru fiecafre nava*/
            List<Planet> copyPlanets = new LinkedList<Planet>();
            for (Iterator<Planet> iterPlanet = planets.iterator(); iterPlanet.hasNext(); )
                copyPlanets.add(iterPlanet.next());

            while (copyPlanets.isEmpty() == false) {
                for (Iterator<Ship> iterShips = copyShips.iterator(); iterShips.hasNext(); ) {
                    Ship ship = iterShips.next();
                    for (Iterator<Planet> iterPlanets = copyPlanets.iterator(); iterPlanets.hasNext(); ) {
                        Planet planet = iterPlanets.next();
                        int ok = 1;
                        double d = ship.getDistanceTo(planet);

                        for (Iterator<MyEntry> iter = dist_s_p.iterator(); iter.hasNext(); ) {
                            MyEntry myEntry = iter.next();
                            Ship myShip = myEntry.getKey();
                            Planet myPlanet = myEntry.getValue();
                            double myd = myShip.getDistanceTo(myPlanet);

                            if (planet == myPlanet) {
                                if (d < myd) {
                                    iter.remove();
                                    ok = 1;
                                } else {
                                    ok = 0;
                                }
                                break;
                            }
                        }
                        if (ok == 0) continue;

                        dist_s_p.add(new MyEntry(ship, planet));
                    }
                }

                for (Iterator<MyEntry> myIter = dist_s_p.iterator(); myIter.hasNext(); ) {
                    MyEntry myEntry = myIter.next();
                    Ship myShip = myEntry.getKey();
                    Planet myPlanet = myEntry.getValue();

                    for (Iterator<Ship> iter = copyShips.iterator(); iter.hasNext(); ) {
                        Ship ship = iter.next();
                        if (ship == myShip) {
                            iter.remove();
                            copyPlanets.remove(myPlanet);
                            new_ships.add(new MyEntry(ship, myPlanet));
                            break;
                        }
                    }
                }
                dist_s_p.clear();
                if (copyShips.isEmpty() == true) break;
            }
        }
        return new_ships;
    }
}
    	
    	