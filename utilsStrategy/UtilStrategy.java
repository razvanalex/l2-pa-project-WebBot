package utilsStrategy;

import hlt.*;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * fuctile utilizate mai multe posbile stategi in functie starea jocului :
 * - pozitia de inceput
 * - daca harta este simietrica
 * - numarul de inamici
 *
 */
public interface UtilStrategy {
	public abstract boolean goToTarget(GameMap gameMap, ArrayList<Move> moveList, Ship ship, Entity target);

	public abstract void construction(final GameMap gameMap, final ArrayList<Move> moveList, final Ship ship,
	                                  TreeMap<Double, Planet> treePlanet, int limit, int turn);

	public abstract boolean isLastRanked(Ship crtShip, GameMap gameMap);

	public abstract boolean desertation(ThrustMove move, final ArrayList<Move> moveList, GameMap gameMap);

	public abstract double winningAttackRatio(double radius, Ship crtShip, GameMap gameMap);

	public abstract double distanceToNearestOwnedPlanet(Ship ship, GameMap gameMap);

	public abstract double distanceToNearestEnemyShip(Ship ship, GameMap gameMap);
}
