import hlt.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

//TODO de dat un score[i] : (num_ships) ^ 3 * d[i] distanta de la nava la spre aceiasi planeta i nava
//TODO raport attack_def = our_ship / (our_ship + enemy_ship )* C, de gasit cine este C

public class MyBot {
	public static int turn = 1;

	/**
	 * Compute the overall score of the board. It is computed as the sum of
	 * the percentage of our ships and the percentage of our planets times
	 * the difference between number of ships. A low value means a low chance
	 * of winning, so we have to defend ourselves; and a high value means that
	 * there is a chance of winning the game, so attack.
	 *
	 * The highest value is 100% + 100% * numOfShips = numOfShips + 1
	 * The lowest score is 0% + 0% * (0 - numEnemyPlanets) = 0
	 *
	 * Hence, a medium score should be somewhere between numOfShips + 1 and 0.
	 * Note that there is a chance of having a negative value.
	 * So, to maximize the chance of winning, the limit between attack and
	 * deffend will be chosen as 2 / 3 * (numOfShips + 1)
	 *
	 * TODO: This hypothesis has to be verified.
	 */
	public static double overallScore(int numOurShips, int numEnemyShips,
			int numOurPlanets, int numEnemyPlanets) {

		double shipScore = (double)numOurShips / (numOurShips + numEnemyShips);
		double planetScore = numOurPlanets + numEnemyPlanets > 0 ?
			(double)numOurPlanets / (numOurPlanets + numEnemyPlanets) : 0;
		
		// Compute effectiveness
		double effectiveness = shipScore + planetScore * (numOurShips - numEnemyShips);

		return effectiveness;
	}

	/**
	 * Give orders captain! :))
	 */
	public static void giveOrdersCaptain(final GameMap gameMap, final ArrayList<Move> moveList,
			final List<Planet> noOnePlanets, final List<Planet> ourPlanets,
			final List<Planet> enemyPlanets, final List<Ship> ships) {

		Collection<Ship> ourShips = gameMap.getMyPlayer().getShips().values();
		int numEnemyShips = gameMap.getAllShips().size() - ourShips.size();
		int ourID = gameMap.getMyPlayerId();

		double effectiveness =  overallScore(ourShips.size(), numEnemyShips,
			ourPlanets.size(), enemyPlanets.size());
		
		// && planet.getDockedShips().size()  <= planet.getDockingSpots())

		// Create TreeMap of <dangerScore, Planet>
		TreeMap<Double, List<Planet>> inDangerPlanets = new TreeMap<>();
		for (Planet p : ourPlanets) {
			double score = planetInDangerScore(ourID, p,  gameMap.getAllShips());

			// Our planet is not in danger
			if (score == 0)
				continue;
			
			List<Planet> l = inDangerPlanets.get(score);
			if (l == null)
				l = new ArrayList<>();
			l.add(p);
			inDangerPlanets.put(score, l);
		}
		
		if (effectiveness <= 2.0 / 3.0 * (ourShips.size() + 1)) {
			Log.log("Defending...");
			// Defend
			if (inDangerPlanets.isEmpty()) {
				// There are no planets in danger
				/** sortare ships */
				List<SortShipsClass.MyEntry> shipMoves;
				if (noOnePlanets.isEmpty() == false) {
					shipMoves = SortShipsClass.sortShips(ships, noOnePlanets);

					/** pentru fiecare nava ship */
					for (Iterator<SortShipsClass.MyEntry> iter = shipMoves.iterator(); iter.hasNext(); ) {
						SortShipsClass.MyEntry myEntry = iter.next();
						Ship ship = myEntry.getKey();
						Planet planet = myEntry.getValue();

						if (ship.canDock(planet)) {
							moveList.add(new DockMove(ship, planet));
							continue;
						}

						boolean avoidObstacles = true;
						final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap,
								ship, planet, Constants.MAX_SPEED, avoidObstacles);
						if (newThrustMove != null) {
							moveList.add(newThrustMove);
						}
					}
				}
			} else {
				List<Ship> enemyShips = new ArrayList<Ship>();
				for (Ship s : gameMap.getAllShips()) {
					if (s.getOwner() == ourID) {
						continue;
					}
					enemyShips.add(s);
				}
				int i = 0;
				for (Planet p : ourPlanets) {
					for (Iterator<Ship> iter = ships.iterator(); iter.hasNext(); ) {
						Ship ship = iter.next();
						i++;
						if (ship.canDock(p) && i % 2 == 0) {
							moveList.add(new DockMove(ship, p));
							iter.remove();
						}
					} 
				}
				
				List<Planet> list = inDangerPlanets.pollFirstEntry().getValue();
				if (!list.isEmpty()) {
					attackShipsNearPlanet(list.get(0), ships, enemyShips, gameMap, moveList);
				}
			}

		} else { 
			// Attack
			Log.log("Attacking...");
			List<Ship> enemyShips = new ArrayList<Ship>();
			for (Ship s : gameMap.getAllShips()) {
				if (s.getOwner() == ourID) {
					continue;
				}
				enemyShips.add(s);
			}

			List<Planet> list = inDangerPlanets.pollFirstEntry().getValue();
			if (!list.isEmpty()) {
				attackShipsNearPlanet(list.get(0), ships, enemyShips, gameMap, moveList);
			}
		}
	}

	public static double planetInDangerScore(final int crtPlayeID, final Planet planet, 
			final List<Ship> ships) {
				
		double score = 0;

		for (Ship s : ships) {
			if (s.getOwner() == crtPlayeID || s.getDockingStatus() == Ship.DockingStatus.Docked)
				continue;

			if (Collision.isInBoundingCircle(s, planet, planet.getRadius() * 2)) {
				score += s.getHealth() / 255.0;
			}
		}
		return score;
	}


	public static TreeMap<Double, Ship> nearbyEnemyShips(final Ship entity, final List<Ship> enemyShips, 
			GameMap gameMap) {
		final TreeMap<Double, Ship> entityByDistance = new TreeMap<>();
		
		for (final Ship ship : enemyShips) {
			entityByDistance.put(entity.getRealDistanceTo(gameMap, ship), ship);
		}

		return entityByDistance;
	}

	public static void attackShipsNearPlanet(final Planet planet, final List<Ship> ourShips, 
			final List<Ship> enemyShips, GameMap gameMap, List<Move> moveList) {

		for (Ship s : ourShips) {
			s.setMod(Ship.Mod.Attack);
			TreeMap<Double, Ship> map = nearbyEnemyShips(s, enemyShips, gameMap);
			Map.Entry<Double, Ship> nextTarget = map.pollLastEntry();

			boolean avoidObstacles = true;
			if (nextTarget.getKey() < Constants.MAX_SPEED) {
				// Kamikaze ships :)) 
				avoidObstacles = false;
			}

			final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap,
				s, nextTarget.getValue(), Constants.MAX_SPEED, avoidObstacles);

			if (newThrustMove != null) {
				moveList.add(newThrustMove);
			}
		}
	}

	public static void main(final String[] args) {
		final Networking networking = new Networking();
		final GameMap gameMap = networking.initialize("WebBot");
	
		// We now have 1 full minute to analyse the initial map.
		final String initialMapIntelligence =
				"width: " + gameMap.getWidth() +
						"; height: " + gameMap.getHeight() +
						"; players: " + gameMap.getAllPlayers().size() +
						"; planets: " + gameMap.getAllPlanets().size();
		Log.log(initialMapIntelligence);

		final ArrayList<Move> moveList = new ArrayList<>();
		//incepe jocul
		for (; ; turn++) {
			moveList.clear();
			networking.updateMap(gameMap);

			//o tura
			List<Ship> ships = new LinkedList<Ship>();
			for (Ship ship : gameMap.getMyPlayer().getShips().values())
				if (ship.getDockingStatus() == Ship.DockingStatus.Undocked)
					ships.add(ship);

			List<Planet> planets = new LinkedList<Planet>();
			List<Planet> ourPlanets = new LinkedList<Planet>();
			List<Planet> enemyPlanets = new LinkedList<Planet>();

			for (Planet planet : gameMap.getAllPlanets().values()) {
				/** Divide all planets in 3 sets: not owned, ours and enemies' */
				if (!planet.isOwned()) {
					planets.add(planet);
				} else if (planet.getOwner() == gameMap.getMyPlayerId()) {
					ourPlanets.add(planet);
				} else {
					enemyPlanets.add(planet);
				}
			}

			giveOrdersCaptain(gameMap, moveList, planets, ourPlanets, enemyPlanets, ships);

			Networking.sendMoves(moveList);
		}
	}


}