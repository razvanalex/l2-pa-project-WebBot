package hlt;

import java.util.ArrayList;

public class Navigation {

    public static ThrustMove navigateShipToDock(
            final GameMap gameMap,
            final ArrayList<Move> moveList,
            final Ship ship,
            final Entity dockTarget,
            final int maxThrust)
    {
        final int maxCorrections = Constants.MAX_NAVIGATION_CORRECTIONS;
        final boolean avoidObstacles = true;
        final double angularStepRad = (Math.PI/180.0);
        final Position targetPos = ship.getClosestPoint(dockTarget);

        return navigateShipTowardsTarget(gameMap, moveList, ship, targetPos, maxThrust, avoidObstacles, maxCorrections, angularStepRad);
    }

    public static ThrustMove navigateShipTowardsTarget(
            final GameMap gameMap,
            final ArrayList<Move> moveList,
            final Ship ship,
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
        final double angleRad = ship.orientTowardsInRad(targetPos);
        final int angleDeg = Util.angleRadToDegClipped(angleRad);
        
        final int thrust;
        if (distance < maxThrust) {
            // Do not round up, since overshooting might cause collision.
            thrust = (int) distance;
        }
        else {
            thrust = maxThrust;
        }

        final ThrustMove pridictMove = new ThrustMove(ship, angleDeg, thrust);

        if (avoidObstacles && !(gameMap.objectsBetween(ship, targetPos).isEmpty()
               && validNextMove(pridictMove, moveList))) {
            final double newTargetDx = Math.cos(angleRad + angularStepRad) * distance;
            final double newTargetDy = Math.sin(angleRad + angularStepRad) * distance;
            final Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);

            return navigateShipTowardsTarget(gameMap, moveList, ship, newTarget, maxThrust, true, (maxCorrections-1), angularStepRad);
        }
        
        return pridictMove;
    }

    private static boolean validNextMove( 
            final ThrustMove crtMove, 
            final ArrayList<Move> moveList) 
    {
        for (Move m : moveList) {
            if (m instanceof ThrustMove) {
                if (twoPathsIntersect(crtMove, (ThrustMove)m)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private static boolean twoPathsIntersect(ThrustMove move1, ThrustMove move2) {
        Position start1 = move1.getShip();
        double radAngle1 = Math.toRadians(move1.getAngle());
        Position end1 = computeVector(start1, move1.getThrust() * 2, radAngle1);
        Position[] positions1 = computePositions(start1, end1);

        Position start2 = move2.getShip();
        double radAngle2 = Math.toRadians(move2.getAngle());
        Position end2 = computeVector(start2, move2.getThrust() * 2, radAngle2);
        Position[] positions2 = computePositions(start2, end2);

        // Frontal collision
        if (Collision.twoSgmentIntersect(start1, end1, start2, end2)) {
            return true;
        }

        // Side collision
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                if (Collision.twoSgmentIntersect(
                        positions1[i], positions1[i + 1], 
                        positions2[j], positions2[j + 1])) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Position computeVector(Position initial, double value, double angle) {
        return Collision.add2D(initial, 
            new Position(value * Math.cos(angle), value * Math.sin(angle))
        );
    }

    private static Position[] computePositions(Position start, Position end) {
        double angle = Math.atan((end.getYPos() -  start.getYPos()) / 
                       (end.getXPos() -  start.getXPos()));

        double lowerDistanceX = Constants.FORECAST_FUDGE_FACTOR *
            Math.cos(angle - Math.PI / 2);
        double lowerDistanceY = Constants.FORECAST_FUDGE_FACTOR *
            Math.sin(angle - Math.PI / 2);
        double upperDistanceX = Constants.FORECAST_FUDGE_FACTOR *
            Math.cos(angle + Math.PI / 2);
        double upperDistanceY = Constants.FORECAST_FUDGE_FACTOR *
            Math.sin(angle + Math.PI / 2);

        Position[] p = new Position[4];

        p[0] = new Position(
            start.getXPos() + lowerDistanceX, 
            start.getYPos() + lowerDistanceY);
           
        p[1] = new Position(
            end.getXPos() + lowerDistanceX, 
            end.getYPos() + lowerDistanceY);
        
        p[2] = new Position(
            start.getXPos() + upperDistanceX, 
            start.getYPos() + upperDistanceY);
        
        p[3] = new Position(
            end.getXPos() + upperDistanceX, 
            end.getYPos() + upperDistanceY);

        return p;
    }
}
