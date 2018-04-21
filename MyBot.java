import hlt.*;

import java.util.*;

/**

**/
public class MyBot {
	private static int turn = 0;

	public static void main(final String[] args) {
		final Networking networking = new Networking();
		final GameMap gameMap = networking.initialize("Tamagocchi");

		// We now have 1 full minute to analyse the initial map.
		final String initialMapIntelligence =
				"width: " + gameMap.getWidth() +
						"; height: " + gameMap.getHeight() +
						"; players: " + gameMap.getAllPlayers().size() +
						"; planets: " + gameMap.getAllPlanets().size();
		Log.log(initialMapIntelligence);

		final ArrayList<Move> moveList = new ArrayList<>();
		for (; ; ) {
			turn++;
			moveList.clear();
			networking.updateMap(gameMap);

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
			Planet targetPlanet = null;
			for (final Ship ship : copyShip) {
				if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
					continue;
				}

				Log.log("Ship " + ship.getId() + " Start in " + turn);
				TreeMap<Double, Planet> treePlanet = new TreeMap<>();
				TreeMap<Double, Ship> treeShip = new TreeMap<>();
				for (final Planet planet : gameMap.getAllPlanets().values()) {
					if (planet.isFull()) {
						// TODO se poate considera ca o planeta ocupata partial(1/2 sau 3/4)
						// TODO este protejata, deci nu este nevoie de nave suplimentare
						// TODO implemlementarea unui nou nod de selectare
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

				int limit;
				if (gameMap.getHeight() == gameMap.getWidth())//getWidth()  < 200
					limit = 0; // pt harti mici
				else
					limit = 8; // pt harti mari

				//
				if (turn < limit) {
                    // trimiterea pe cea mai apropiata planeta
					for (Map.Entry<Double, Planet> entry : treePlanet.entrySet()) {
						Log.log("In for limit " + turn + " " + ship.getId());
						Planet planet = entry.getValue();
						if (ship.canDock(planet)) {
							moveList.add(new DockMove(ship, planet));
							Log.log("Ship " + ship.getId() + "Dock Start " + planet.getId() + " " +
									"turn " + turn);
							break;
						}
						final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moveList,
								ship, planet, Constants.MAX_SPEED);
						if (newThrustMove != null) {
							moveList.add(newThrustMove);
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
					// pentru hartile max : 5.1
					if (enemyDist > planetDist + Constants.MAX_SPEED * 5.1 ) {
						// daca pot sa ajung la o nava inamica in mai de 6 ture
						for (Map.Entry<Double, Planet> entry : treePlanet.entrySet()) {
							Planet planet = entry.getValue();
							if (ship.canDock(planet)) {
								moveList.add(new DockMove(ship, planet));
								change = true;
								Log.log("Ship " + ship.getId() + " Dock " + planet.getId() + " " +
										"turn " + turn);
								break;
							}
							final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moveList,
									ship, planet, Constants.MAX_SPEED);
							if (newThrustMove != null) {
								moveList.add(newThrustMove);
								Log.log("Ship " + ship.getId() + " Go to Planet" + planet.getId()
										+ " " + "turn " + turn);
							}
							change = true;
							break;
						}

					}
					if (change == true)
						break;//break

					for (Map.Entry<Double, Ship> entry : treeShip.entrySet()) {
						// se ataca cea mai aproprita nava
						Ship ship1 = entry.getValue();
						if (target == null) {
							// daca nu exista un target pt o nava anterioara
							final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moveList,
									ship, ship1, Constants.MAX_SPEED);
							if (newThrustMove != null) {
								moveList.add(newThrustMove);
								Log.log("Ship " + ship.getId() + " Go to enemy target null" +
										ship1.getId() + " " + "turn " + turn);
							}
							break;
						} else { // pentru hartile mari : 2, 0.5,
							if (ship.getDistanceTo(target) - Constants.MAX_SPEED * 2 < ship.getDistanceTo(ship1)) {
								// daca pot sa ajung in 2 ture la o nava care este target pt o alta nava a mea
								final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moveList,
										ship, target, Constants.MAX_SPEED);
								if (newThrustMove != null) {
									moveList.add(newThrustMove);
									Log.log("Ship " + ship.getId() + " Go to enemy  target " +
											ship1.getId() + " " + "turn " + turn);
								}
								break;
							} else {
								// daca este prea indepatata
								final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moveList,
										ship, ship1, Constants.MAX_SPEED);
								if (newThrustMove != null) {
									moveList.add(newThrustMove);
									Log.log("Ship " + ship.getId() + " Go to enemy new " +
											ship1.getId() + " " + "turn " + turn);
								}
								target = ship1;
								break;
							}
						}

					}
				}
				Log.log("Not good......... Try to solve this!");
			}
			Networking.sendMoves(moveList);
		}

	}
}

