package superAPI;

import hlt.*;
import java.util.*;

public class MoveShips {
    private class ShipComparatorOverX implements Comparator<TShip> {
        @Override
        public int compare(TShip s1, TShip s2) {
            int precission = 1000;
            double diff = s2.getShip().getXPos() - s1.getShip().getXPos();
            return (int)(diff * precission);
        }
    }
    
    public List<Cluster> clusters;
    private double maxDistance = 99999999;

    public MoveShips(final GameMap gameMap) {
        maxDistance = gameMap.getHeight() > gameMap.getWidth() ? 
            gameMap.getHeight() : gameMap.getWidth();
            
        clusters = new ArrayList<>();

        for (Ship s : gameMap.getMyPlayer().getShips().values()) {
            Cluster firstCluster = new Cluster();
            firstCluster.addShip(new TShip(s));
            clusters.add(firstCluster);
        }
    }

    public void mergeNearClusters() {
        for (Cluster c1 : clusters) {
            for (Cluster c2 : clusters) {
                if (c1 != null && c2 != null && c1 == c2)
                    continue;
                
                if (getDistanceBetweenClusters(c1, 
                        c2) <= Cluster.MAX_DISTANCE_BETWEEN_SHIPS) {
                    c1.merge(c2);
                    c2 = null;
                }
            }
        }

    }

    public void unmergeFarClusters() {
        for (Cluster c : clusters) {
            for (TShip s1 : c.getShips()) {
                for (TShip s2 : c.getShips()) {
                    if (s1 == s2)
                        continue;

                    if (Helper.getDistanceBetween(s1.getShip(), 
                        s2.getShip()) > Cluster.MAX_DISTANCE_BETWEEN_SHIPS) {
                        c.removeShip(s2);
                        
                        Cluster newCluster = new Cluster();
                        newCluster.addShip(s2);
                        clusters.add(newCluster);
                    }
                }
            }
        }
        
    }

    private double getDistanceBetweenClusters(Cluster c1, Cluster c2) {
        double minDistance = maxDistance;

        for (TShip si : c1.getShips()) {
            for (TShip sj : c2.getShips()) {
                int distance = 0;
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
        }

        return minDistance;
    }

}