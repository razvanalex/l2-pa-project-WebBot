package superAPI;
import hlt.*;

public class TShip {
    private Ship ship;
    private Cluster cluster;
    
    public TShip(Ship ship) {
        this.ship = ship;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Cluster getCluster() {
       return cluster;
    }

    public Ship getShip() {
        return ship;
    }

    @Override
    public String toString() {
        return "ship = " + ship;
    }
}