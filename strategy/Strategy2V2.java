package strategy;

import hlt.*;

import java.util.*;

public class Strategy2V2 implements Strategy {
	private int turn;
	private static Strategy2V2 instance = null;

	private Strategy2V2() {
		this.turn = 0;
	}

	public static Strategy2V2 getInstance() {
		if(instance == null)
			instance = new Strategy2V2();

		return instance;
	}

	@Override
	public void runGame(ArrayList<Move> moves, GameMap gameMap) {
		turn++;


		ArrayList<Ship> copyShip = new ArrayList<>();
		for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
			copyShip.add(ship);
		}
		Collections.sort(copyShip, new Comparator<Ship>() {
			@Override
			public int compare(Ship o1, Ship o2) {
				return o1.getXPos() < o1.getXPos() ? -1 : (int) (o1.getXPos() == o1.getXPos() ? o1.getYPos() - o2.getYPos() : 1);
			}
		});

		Ship target = null;
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
			int someConstant = (int) (((double) areaOfMap / minimumArea) * 1.2);

			if (turn < limit) {
				// trimiterea pe cea mai apropiata planeta
				for (Map.Entry<Double, Planet> entry : treePlanet.entrySet()) {
					Log.log("In for limit " + turn + " " + ship.getId());
					Planet planet = entry.getValue();
					if (ship.canDock(planet)) {
						moves.add(new DockMove(ship, planet));
						Log.log("Ship " + ship.getId() + "Dock Start " + planet.getId() + " " +
								"turn " + turn);
						break;
					}
					final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moves,
							ship, planet, Constants.MAX_SPEED);
					if (newThrustMove != null) {
						moves.add(newThrustMove);
						Log.log("Ship " + ship.getId() + "Go to Planet Start " + planet.getId() +
								" " + "turn " + turn);
					}
					break;
				}
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

				if (enemyDist > planetDist + Constants.MAX_SPEED * someConstant) {
					// daca pot sa ajung la o nava inamica in mai de 6 ture
					for (Map.Entry<Double, Planet> entry : treePlanet.entrySet()) {
						Planet planet = entry.getValue();
						if (ship.canDock(planet)) {
							moves.add(new DockMove(ship, planet));
							change = true;
							Log.log("Ship " + ship.getId() + " Dock " + planet.getId() + " " +
									"turn " + turn);
							break;
						}
						final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moves,
								ship, planet, Constants.MAX_SPEED);
						if (newThrustMove != null) {
							moves.add(newThrustMove);
							Log.log("Ship " + ship.getId() + " Go to Planet" + planet.getId()
									+ " " + "turn " + turn);
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
					if (target == null) {
						// daca nu exista un target pt o nava anterioara
						final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moves,
								ship, ship1, Constants.MAX_SPEED);
						if (newThrustMove != null) {
							moves.add(newThrustMove);
							Log.log("Ship " + ship.getId() + " Go to enemy target null" +
									ship1.getId() + " " + "turn " + turn);
						}
						break;
					} else {
						if (ship.getDistanceTo(target) - Constants.MAX_SPEED * 2 < ship.getDistanceTo(ship1)) {
							// daca pot sa ajung in 2 ture la o nava care este target pt o alta nava a mea
							final ThrustMove newThrustMove = Navigation.navigateShipToDock
									(gameMap, moves, ship, target, Constants.MAX_SPEED);
							if (newThrustMove != null) {
								moves.add(newThrustMove);
								Log.log("Ship " + ship.getId() + " Go to enemy  target " +
										ship1.getId() + " " + "turn " + turn);
							}
							break;
						} else {

							// daca este prea indepatata
							final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moves,
									ship, ship1, Constants.MAX_SPEED);
							if (newThrustMove != null) {
								moves.add(newThrustMove);
								Log.log("Ship " + ship.getId() + " Go to enemy new " +
										ship1.getId() + " " + "turn " + turn);
							}
							target = ship1;
							break;
						}
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
