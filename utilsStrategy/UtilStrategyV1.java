package utilsStrategy;

import hlt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UtilStrategyV1 implements UtilStrategy {
	private static final double PLANET_PROXIMITY = 5;
	private static final double RADIUS = 40;
	private static UtilStrategyV1 instance = null;

	private UtilStrategyV1() {
	}

	public static UtilStrategyV1 getInstance() {
		if(instance == null){
			instance = new UtilStrategyV1();
		}

		return instance;
	}

	@Override
	public boolean goToTarget(GameMap gameMap, ArrayList<Move> moveList, Ship ship, Entity target) {
		ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moveList, ship, target, Constants.MAX_SPEED);

		if (newThrustMove != null) {
			moveList.add(newThrustMove);
		}

		return newThrustMove != null;
	}

	@Override
	public void construction(final GameMap gameMap, final ArrayList<Move> moveList, final Ship ship,
	                          TreeMap<Double, Planet> treePlanet, int limit, int turn) {

		// trimiterea pe cea mai apropiata planeta
		for (Map.Entry<Double, Planet> entry : treePlanet.entrySet()) {
			Log.log("Construction time limit: " + limit + "; current ship ID = " + ship.getId());
			Planet planet = entry.getValue();
			if (ship.canDock(planet)) {
				moveList.add(new DockMove(ship, planet));
				Log.log("Ship " + ship.getId() + " starts docking at planet " + planet.getId() + " at turn " + turn);
				break;
			}

			if (goToTarget(gameMap, moveList, ship, planet)) {
				Log.log("Ship " + ship.getId() + "Go to Planet Start " + planet.getId() + " " + "turn " + turn);
				break;
			} else {
				continue;
			}
		}
	}

	@Override
	public boolean isLastRanked(Ship crtShip, GameMap gameMap) {
		List<Ship> allShips = gameMap.getAllShips();
		List<Player> allPlayers = gameMap.getAllPlayers();
		int[] ranks = new int[allPlayers.size()];
		int myID = 0;

		for (Ship s : allShips) {
			if (s.getDockingStatus() != Ship.DockingStatus.Undocked)
				continue;

			for (Player p : allPlayers) {
				if (p.getId() == s.getOwner()) {
					ranks[p.getId()]++;
				}
				if (p.getId() == gameMap.getMyPlayerId()) {
					myID = p.getId();
				}
			}
		}

		int min = 9999999;
		for (int i = 0; i < allPlayers.size(); i++) {
			if (ranks[i] < min)
				min = ranks[i];
		}

		return ranks[myID] == min;
	}


	@Override
	public boolean desertation(ThrustMove move, final ArrayList<Move> moveList, GameMap gameMap) {
		Ship ship = move.getShip();
		boolean result = false;

		if (distanceToNearestOwnedPlanet(ship, gameMap) < PLANET_PROXIMITY && !isLastRanked(ship, gameMap)) {
			result = winningAttackRatio(RADIUS, ship, gameMap) <= 0;
		} else {
			result = winningAttackRatio(RADIUS, ship, gameMap) < 0;
		}

		if (result) {
			Position panicTarget = Navigation.computeVector(ship,
					Constants.MAX_NAVIGATION_CORRECTIONS, Math.toRadians(move.getAngle() - 180));
			Ship panicShip = new Ship(0, 0,
					panicTarget.getXPos(), panicTarget.getYPos(), 0, null, 0, 0, 0);

			Log.log(panicTarget.getXPos() + " " + panicTarget.getYPos());

			if (goToTarget(gameMap, moveList, ship, panicShip)) {
				Log.log("DESERTATION!!!");
			} else {
				// Our ship will be distroyed...
			}
		}

		return result;
	}

	@Override
	public double winningAttackRatio(double radius, Ship crtShip, GameMap gameMap) {
		List<Ship> allShips = gameMap.getAllShips();
		double numOurShips = 0;
		double numEnemyShips = 0;

		for (Ship s : allShips) {
			if (s.getDockingStatus() != Ship.DockingStatus.Undocked)
				continue;

			if (s.getDistanceTo(crtShip) <= radius) {
				if (s.getOwner() == gameMap.getMyPlayerId()) {
					numOurShips++;
				} else {
					numEnemyShips++;
				}
			}
		}

		return numOurShips - numEnemyShips;
	}

	@Override
	public double distanceToNearestOwnedPlanet(Ship ship, GameMap gameMap) {
		TreeMap<Double, Planet> treePlanet = new TreeMap<>();

		for (final Planet planet : gameMap.getAllPlanets().values()) {
			if (planet.getOwner() == gameMap.getMyPlayerId()) {
				treePlanet.put(ship.getDistanceTo(planet), planet);
			}
		}

		return treePlanet.firstEntry() != null ? treePlanet.firstEntry().getKey() : 9999999;
	}

	@Override
	public double distanceToNearestEnemyShip(Ship ship, GameMap gameMap) {
		TreeMap<Double, Ship> treeShip = new TreeMap<>();

		for (final Ship enemy : gameMap.getAllShips()) {
			if (enemy.getOwner() != gameMap.getMyPlayerId()) {
				treeShip.put(ship.getDistanceTo(enemy), enemy);
			}
		}

		return treeShip.firstEntry() != null ? treeShip.firstEntry().getKey() : 9999999;
	}

}
