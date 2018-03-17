package hlt;

import java.util.ArrayList;

public class Ship extends Entity {

    public enum DockingStatus { Undocked, Docking, Docked, Undocking }

    private final DockingStatus dockingStatus;
    private final int dockedPlanet;
    private final int dockingProgress;
    private final int weaponCooldown;

    public Ship(final int owner, final int id, final double xPos, final double yPos,
                final int health, final DockingStatus dockingStatus, final int dockedPlanet,
                final int dockingProgress, final int weaponCooldown) {

        super(owner, id, xPos, yPos, health, Constants.SHIP_RADIUS);

        this.dockingStatus = dockingStatus;
        this.dockedPlanet = dockedPlanet;
        this.dockingProgress = dockingProgress;
        this.weaponCooldown = weaponCooldown;
    }

    public int getWeaponCooldown() {
        return weaponCooldown;
    }

    public DockingStatus getDockingStatus() {
        return dockingStatus;
    }

    public int getDockingProgress() {
        return dockingProgress;
    }

    public int getDockedPlanet() {
        return dockedPlanet;
    }

    public boolean canDock(final Planet planet) {
        return getDistanceTo(planet) <= Constants.SHIP_RADIUS + Constants.DOCK_RADIUS + planet.getRadius();
    }

    @Override
    public String toString() {
        return "Ship[" +
                super.toString() +
                ", dockingStatus=" + dockingStatus +
                ", dockedPlanet=" + dockedPlanet +
                ", dockingProgress=" + dockingProgress +
                ", weaponCooldown=" + weaponCooldown +
                "]";
    }
    
    public double getRealDistanceTo(GameMap gameMap,Planet planet){
    	return navigateShipToPlanet(gameMap, this, planet, Constants.MAX_SPEED); 	  	
    }
    
    public static double navigateShipToPlanet(
            final GameMap gameMap,
            final Ship ship,
            final Entity planetTarget,
            final int maxThrust)
    {
        final int maxCorrections = Constants.MAX_NAVIGATION_CORRECTIONS;
        final boolean avoidObstacles = true;
        final double angularStepRad =4*Math.PI/180.0;
        final Position targetPos = ship.getClosestPoint(planetTarget);

        double dist=ship.getDistanceTo(targetPos);
        Position initial=ship;
        Position nextPosition= navigateShipTowardsPlanet(gameMap, initial, targetPos, maxThrust, avoidObstacles, maxCorrections, angularStepRad);
        if(nextPosition==null) return Double.MAX_VALUE;
        dist+=nextPosition.getDistanceTo(initial)+nextPosition.getDistanceTo(targetPos)-initial.getDistanceTo(targetPos);
        	
        return dist;
        }
    
    public static Position navigateShipTowardsPlanet(
            final GameMap gameMap,
            final Position ship,
            final Position targetPos,
            final int maxThrust,
            final boolean avoidObstacles,
            final int maxCorrections,
            final double angularStepRad)
    {
        if (maxCorrections <= 0) {
            return null;
        }

        final double distance = ship.getDistanceTo(targetPos);
         double angleRad = ship.orientTowardsInRad(targetPos);

        ArrayList<Entity> obstacole=gameMap.objectsBetween(ship, targetPos);
        if (avoidObstacles && !obstacole.isEmpty()) {
       
            final double newTargetDx = Math.cos(angleRad + angularStepRad) * distance;
            final double newTargetDy = Math.sin(angleRad + angularStepRad) * distance;
            final Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);

            return navigateShipTowardsPlanet(gameMap, ship, newTarget, maxThrust, true, (maxCorrections-1), angularStepRad);
            
        }
        
        final int thrust;
        if (distance < maxThrust) {
            // Do not round up, since overshooting might cause collision.
            thrust = (int) distance;
        }
        else {
            thrust = maxThrust;
        }

        return targetPos;

    }
}

