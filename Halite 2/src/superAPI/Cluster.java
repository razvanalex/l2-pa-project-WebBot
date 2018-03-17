package superAPI;

import hlt.*;
import java.sql.Array;
import java.util.*;

public class Cluster {
    public static final double MAX_DISTANCE_BETWEEN_SHIPS = 3;
    private List<TShip> ships;

    public Cluster() {
        ships = new ArrayList<TShip>();
    }

    public List<TShip> getShips() {
        return this.ships;
    }

    public void addShip(TShip ship) {
        ship.setCluster(this);
        ships.add(ship);
    }

    public void removeShip(TShip ship) {
        ship.setCluster(new Cluster());
        ships.remove(ship);
    }

    public void merge(Cluster cluster) {
        for (TShip s : cluster.getShips()) {
            this.addShip(s);
        }
    }

    public boolean hasShip(TShip ship) {
        for (TShip s : ships) {
            if (s == ship) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Cluster = [" + ships + "]";
    }
}