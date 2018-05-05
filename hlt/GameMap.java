package hlt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Collection;

public class GameMap {
	private final int width, height;
	private final int playerId;
	private final List<Player> players;
	private final List<Player> playersUnmodifiable;
	private final Map<Integer, Planet> planets;
	private final List<Ship> allShips;
	private final List<Ship> allShipsUnmodifiable;

	// used only during parsing to reduce memory allocations
	private final List<Ship> currentShips = new ArrayList<>();
	private int numberPlayer;

	public GameMap(final int width, final int height, final int playerId) {
		this.width = width;
		this.height = height;
		this.playerId = playerId;
		players = new ArrayList<>(Constants.MAX_PLAYERS);
		playersUnmodifiable = Collections.unmodifiableList(players);
		planets = new TreeMap<>();
		allShips = new ArrayList<>();
		allShipsUnmodifiable = Collections.unmodifiableList(allShips);
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public int getMyPlayerId() {
		return playerId;
	}

	public List<Player> getAllPlayers() {
		return playersUnmodifiable;
	}

	public Player getMyPlayer() {
		return getAllPlayers().get(getMyPlayerId());
	}

	public Ship getShip(final int playerId, final int entityId) throws IndexOutOfBoundsException {
		return players.get(playerId).getShip(entityId);
	}

	public Planet getPlanet(final int entityId) {
		return planets.get(entityId);
	}

	public Map<Integer, Planet> getAllPlanets() {
		return planets;
	}

	public List<Ship> getAllShips() {
		return allShipsUnmodifiable;
	}

	public int getNumberPlayer() {
		return numberPlayer;
	}

	public ArrayList<Entity> objectsBetween(Position start, Position target) {
		final ArrayList<Entity> entitiesFound = new ArrayList<>();

		addEntitiesBetween(entitiesFound, start, target, planets.values());
		addEntitiesBetween(entitiesFound, start, target, allShips);

		return entitiesFound;
	}

	private static void addEntitiesBetween(final List<Entity> entitiesFound,
	                                       final Position start, final Position target,
	                                       final Collection<? extends Entity> entitiesToCheck) {

		double angle = Math.atan((target.getYPos() - start.getYPos()) /
				(target.getXPos() - start.getXPos()));

		double lowerDistanceX = Constants.FORECAST_FUDGE_FACTOR *
				Math.cos(angle - Math.PI / 2);
		double lowerDistanceY = Constants.FORECAST_FUDGE_FACTOR *
				Math.sin(angle - Math.PI / 2);
		double upperDistanceX = Constants.FORECAST_FUDGE_FACTOR *
				Math.cos(angle + Math.PI / 2);
		double upperDistanceY = Constants.FORECAST_FUDGE_FACTOR *
				Math.sin(angle + Math.PI / 2);

		Position lowerStart = new Position(
				start.getXPos() + lowerDistanceX,
				start.getYPos() + lowerDistanceY);

		Position upperStart = new Position(
				start.getXPos() + upperDistanceX,
				start.getYPos() + upperDistanceY);

		Position lowerTarget = new Position(
				target.getXPos() + lowerDistanceX,
				target.getYPos() + lowerDistanceY);

		Position upperTarget = new Position(
				target.getXPos() + upperDistanceX,
				target.getYPos() + upperDistanceY);

		for (final Entity entity : entitiesToCheck) {
			if (entity.equals(start) || entity.equals(target)) {
				continue;
			}

			boolean low = Collision.segmentCircleIntersect(lowerStart, lowerTarget, entity, Constants.FORECAST_FUDGE_FACTOR);
			boolean high = Collision.segmentCircleIntersect(upperStart, upperTarget, entity, Constants.FORECAST_FUDGE_FACTOR);
			boolean middle = Collision.segmentCircleIntersect(start, target, entity, Constants.FORECAST_FUDGE_FACTOR);

			if (middle || low || high) {
				entitiesFound.add(entity);
			}

		}
	}

	public Map<Double, Entity> nearbyEntitiesByDistance(final Entity entity) {
		final Map<Double, Entity> entityByDistance = new TreeMap<>();

		for (final Planet planet : planets.values()) {
			if (planet.equals(entity)) {
				continue;
			}
			entityByDistance.put(entity.getDistanceTo(planet), planet);
		}

		for (final Ship ship : allShips) {
			if (ship.equals(entity)) {
				continue;
			}
			entityByDistance.put(entity.getDistanceTo(ship), ship);
		}

		return entityByDistance;
	}

	public GameMap updateMap(final Metadata mapMetadata) {
		final int numberOfPlayers = MetadataParser.parsePlayerNum(mapMetadata);

		players.clear();
		planets.clear();
		allShips.clear();
		this.numberPlayer = numberOfPlayers;

		// update players info
		for (int i = 0; i < numberOfPlayers; ++i) {
			currentShips.clear();
			final Map<Integer, Ship> currentPlayerShips = new TreeMap<>();
			final int playerId = MetadataParser.parsePlayerId(mapMetadata);

			final Player currentPlayer = new Player(playerId, currentPlayerShips);
			MetadataParser.populateShipList(currentShips, playerId, mapMetadata);
			allShips.addAll(currentShips);

			for (final Ship ship : currentShips) {
				currentPlayerShips.put(ship.getId(), ship);
			}
			players.add(currentPlayer);
		}

		final int numberOfPlanets = Integer.parseInt(mapMetadata.pop());

		for (int i = 0; i < numberOfPlanets; ++i) {
			final List<Integer> dockedShips = new ArrayList<>();
			final Planet planet = MetadataParser.newPlanetFromMetadata(dockedShips, mapMetadata);
			planets.put(planet.getId(), planet);
		}

		if (!mapMetadata.isEmpty()) {
			throw new IllegalStateException("Failed to parse data from Halite game engine. Please contact maintainers.");
		}

		return this;
	}
}
