package strategy;

import hlt.*;
import utilsStrategy.*;

import java.util.*;

public class Strategy4v4 implements Strategy {
	private static int LIMIT = 3;
	private static final double PLANET_PROXIMITY = 5;
	private static final double RADIUS = 40;

	private int turn;
	private UtilStrategy utilStrategy;
	private static Strategy4v4 instance = null;

	private Strategy4v4() {
		this.turn = 0;
		this.utilStrategy = UtilStrategyV1.getInstance();
	}

	public static Strategy4v4 getInstance() {
		if (instance == null)
			instance = new Strategy4v4();

		return instance;
	}

	private boolean isBelowLimit(int limit, int idShip, int idEnemy,
	                             TreeMap<Integer, ArrayList<Integer>> targetCache) {

		ArrayList<Integer> list = targetCache.get(idEnemy);
		Log.log("" + (list == null || list.size() <= limit));
		return list == null || list.size() <= limit;
	}

	private void addToCache(int idEnemy, int idShip, TreeMap<Integer, ArrayList<Integer>> targetCache) {

		ArrayList<Integer> list = targetCache.get(idEnemy);
		if (list == null) {
			list = new ArrayList<>();
		}
		list.add(idShip);
		targetCache.put(idEnemy, list);
	}

	private void sortShips(ArrayList<Ship> copyShip) {
		Collections.sort(copyShip, new Comparator<Ship>() {
			@Override
			public int compare(Ship o1, Ship o2) {
				return o1.getXPos() < o1.getXPos() ? -1
						: (int) (o1.getXPos() == o1.getXPos() ? o1.getYPos() - o2.getYPos() : 1);
			}
		});
	}


	@Override
	public void runGame(ArrayList<Move> moves, GameMap gameMap) {
		turn++;

		TreeMap<Integer, ArrayList<Integer>> targetCache = new TreeMap<>();

		ArrayList<Ship> copyShip = new ArrayList<>();
		for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
			copyShip.add(ship);
		}

		sortShips(copyShip);

		for (final Ship ship : copyShip) {
			if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
				continue;
			}

			Log.log("Ship " + ship.getId() + " Start in " + turn);
			TreeMap<Double, Planet> treePlanet = new TreeMap<>();
			TreeMap<Double, Ship> treeShip = new TreeMap<>();
			for (final Planet planet : gameMap.getAllPlanets().values()) {
				if (planet.isFull()) {
					continue;
				} else {
					treePlanet.put(ship.getDistanceTo(planet), planet);
				}
			}

			//nave inamice
			for (final Ship ship1 : gameMap.getAllShips()) {
				if (ship1.getOwner() != gameMap.getMyPlayerId()) {
					treeShip.put(ship.getDistanceTo(ship1), ship1);
				}
			}

			int areaOfMap = (gameMap.getHeight() * gameMap.getWidth());
			int minimumArea = 150 * 150;
			int numTurnsForMin = 4;
			int limit = (areaOfMap / minimumArea) * numTurnsForMin;

			if (turn < limit) {
				utilStrategy.construction(gameMap, moves, ship, treePlanet, limit, turn);
			} else {
				double planetDist;
				if (treePlanet.size() != 0) {
					planetDist = treePlanet.firstKey();
				} else {
					planetDist = Long.MAX_VALUE;
				}

				double enemyDist;
				if (treeShip.size() != 0) {
					enemyDist = treeShip.firstKey();
				} else {
					enemyDist = Long.MAX_VALUE;
				}

				boolean change = false;
				// pentru hartile max : 5.1
				if (enemyDist > planetDist + Constants.MAX_SPEED * PLANET_PROXIMITY) {
					// daca pot sa ajung la o nava inamica in mai putin de 6 ture
					for (Map.Entry<Double, Planet> entry : treePlanet.entrySet()) {
						Planet planet = entry.getValue();
						if (ship.canDock(planet)) {
							moves.add(new DockMove(ship, planet));
							change = true;
							Log.log("Ship " + ship.getId() + " Dock " + planet.getId() + " " + "turn " + turn);
							break;
						}
						final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moves, ship,
								planet, Constants.MAX_SPEED);
						if (newThrustMove != null) {
							moves.add(newThrustMove);
							Log.log("Ship " + ship.getId() + " Go to Planet" + planet.getId() + " " + "turn "
									+ turn);
						}
						change = true;
						break;
					}

				}
				if (change == true)
					continue;

				for (Map.Entry<Double, Ship> entry : treeShip.entrySet()) {
					// se ataca cea mai aproprita nava
					Ship ship1 = entry.getValue();

					double wholeMap = gameMap.getWidth() + gameMap.getHeight();
					double allShips = gameMap.getAllShips().size();
					if (utilStrategy.winningAttackRatio(wholeMap, ship, gameMap) / allShips > 0.5) {
						LIMIT = 1000;
					}
					if (isBelowLimit(LIMIT, ship.getId(), ship1.getId(), targetCache)) {
						// daca nu exista un target pt o nava anterioara
						final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moves, ship,
								ship1, Constants.MAX_SPEED);
						if (newThrustMove != null) {
							if (utilStrategy.desertation(newThrustMove, moves, gameMap)) {
								break;
							}
							moves.add(newThrustMove);
							Log.log("Ship " + ship.getId() + " Go to enemy target null" + ship1.getId() + " "
									+ "turn " + turn);
							addToCache(ship1.getId(), ship.getId(), targetCache);
							break;
						} else {
							continue;
						}
					} else {
						continue;
					}
				}
			}

		}
	}

	@Override
	public void updateTurn(int turn) {
		this.turn = turn;
	}
}
