package hlt;

import java.util.ArrayList;
import java.util.Map;

class CustomInteger {
	int value;

	public CustomInteger(int v) {
		this.value = v;
	}

	public int getValue() {
		return this.value;
	}

	public void setValue(int v) {
		this.value = v;
	}
	public void increment() {
		this.value++;
	}
}

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
		final double maxCorrections2 = 3;
		ThrustMove m = null;

		for (int i = 0; i < maxCorrections2; i++) {
			if (m == null) {
				double newThrust = ((double)maxThrust) / (Math.pow(2, i));
				m = navigateShipTowardsTargetImproved(gameMap, moveList, ship, targetPos,
					(int)newThrust, avoidObstacles, maxCorrections,
					angularStepRad);
			} else {
				break;
			}
		}
		
		if (m == null) {
			Log.log("Null...... Stay!");
			return new ThrustMove(ship, 0, 0);
		}

		return m;
	}

	public static ThrustMove navigateShipTowardsTargetImproved(
			final GameMap gameMap,
			final ArrayList<Move> moveList,
			final Ship ship,
			final Position targetPos,
			final int maxThrust,
			final boolean avoidObstacles,
			final int maxCorrections,
			final double angularStepRad)
	{
		CustomInteger ci1 = new CustomInteger(0);
		CustomInteger ci2 = new CustomInteger(0);

		ThrustMove move1 = navigateShipTowardsTargetOrg(gameMap, moveList, ship, 
			targetPos, maxThrust, avoidObstacles, maxCorrections, angularStepRad, ci1);
		ThrustMove move2 = navigateShipTowardsTargetOrg(gameMap, moveList, ship, 
			targetPos, maxThrust, avoidObstacles, maxCorrections, -angularStepRad, ci2);

		if (move1 == null && move2 != null) {
			return move2;
		} else if (move1 != null && move2 == null) {
			return move1;
		}

		ThrustMove m = ci1.getValue() > ci2.getValue() ? move2 : move1;        
		return m;
	}

	public static ThrustMove navigateShipTowardsTargetOrg(
		final GameMap gameMap,
		final ArrayList<Move> moveList,
		final Ship ship,
		final Position targetPos,
		final int maxThrust,
		final boolean avoidObstacles,
		final int maxCorrections,
		final double angularStepRad, 
		final CustomInteger crtDistance) 
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
			   && validNextMove(pridictMove, moveList, gameMap) && !hitBorder(pridictMove, gameMap))) {
			final double newTargetDx = Math.cos(angleRad + angularStepRad) * distance;
			final double newTargetDy = Math.sin(angleRad + angularStepRad) * distance;
			final Position newTarget = new Position(ship.getXPos() + newTargetDx, ship.getYPos() + newTargetDy);

			crtDistance.increment();
			return navigateShipTowardsTargetOrg(gameMap, moveList, ship, 
				newTarget, maxThrust, true, maxCorrections - 1, angularStepRad, crtDistance);
		}

		return pridictMove;
	}

	private static boolean validNextMove( 
			final ThrustMove crtMove, 
			final ArrayList<Move> moveList,
			final GameMap gameMap) 
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

	private static boolean hitBorder(final ThrustMove move, final GameMap gameMap) {
		Ship ship = move.getShip();
		double thrust = move.getThrust();
		int angle = move.getAngle();

		Position newPosition = computeVector(ship, thrust, Math.toRadians(angle));

		return newPosition.getXPos() <= 0 || newPosition.getXPos() >= gameMap.getWidth() 
				|| newPosition.getYPos() <= 0 || newPosition.getYPos() >= gameMap.getHeight();
	}

	private static boolean twoPathsIntersect(ThrustMove move1, ThrustMove move2) {
		final double radius = Constants.FORECAST_FUDGE_FACTOR;

		Position start1 = move1.getShip();
		double radAngle1 = Math.toRadians(move1.getAngle());
		Position end1 = computeVector(start1, move1.getThrust(), radAngle1);

		Position start2 = move2.getShip();
		double radAngle2 = Math.toRadians(move2.getAngle());
		Position end2 = computeVector(start2, move2.getThrust(), radAngle2);

		double x1 = start1.getXPos();
		double y1 = start1.getYPos();
		double x2 = end1.getXPos();
		double y2 = end1.getYPos();

		double x3 = start2.getXPos();
		double y3 = start2.getYPos();
		double x4 = end2.getXPos();
		double y4 = end2.getYPos();

		double dx = (x2 - x1) - (x4 - x3);
		double dy = (y2 - y1) - (y4 - y3);
		double diffX = x1 - x3;
		double diffY = y1 - y3;

		double a = square(dx) + square(dy);
		double b = 2 * (diffX * dx + diffY * dy);
		double c = square(diffX) + square(diffY);

		double diff = 4 * square(radius) - c;
		
		if (a == 0.0) {
			return b != 0 ? diff / b >= 0.0 : diff >= 0.0;
		}

		double delta = square(b) + 4 * a * diff;
		
		if (delta < 0.0)
			return false;

		double t1 = (-b - Math.sqrt(delta)) / (2 * a);
		double t2 = (-b + Math.sqrt(delta)) / (2 * a);

		return Collision.intervalIntersect(0.0, 1.0, t1, t2);
	}

	private static double square(double n) {
		return n * n;
	}

	public static Position computeVector(Position initial, double value, double angle) {
		return Collision.add2D(initial, 
			new Position(value * Math.cos(angle), value * Math.sin(angle))
		);
	}
}
