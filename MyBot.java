import hlt.*;
import strategy.*;

import java.util.ArrayList;

public class MyBot {

	public static void main(final String[] args) {
		final Networking networking = new Networking();
		final GameMap gameMap = networking.initialize("WebBot");
		final Strategy strategy;
		// We now have 1 full minute to analyse the initial map.
		final String initialMapIntelligence = "width: " + gameMap.getWidth() + "; height: " + gameMap.getHeight()
				+ "; players: " + gameMap.getAllPlayers().size() + "; planets: " + gameMap.getAllPlanets().size();
		Log.log(initialMapIntelligence);

		if (gameMap.getNumberPlayer() == 2) {
			strategy = Strategy2V2.getInstance();
		} else {
			strategy = Strategy4v4.getInstance();
		}

		final ArrayList<Move> moveList = new ArrayList<>();

		for (; ; ) {
			moveList.clear();
			networking.updateMap(gameMap);

			strategy.runGame(moveList, gameMap);

			Networking.sendMoves(moveList);
		}
	}
}
