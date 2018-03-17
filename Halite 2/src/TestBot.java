import hlt.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class TestBot {

   
    public static class CompereDistanceToPlanet implements Comparator<Planet>{
        private final Ship ship;
        public CompereDistanceToPlanet(Ship ship) {
            this.ship = ship;
        }

        @Override
        public int compare(Planet o1, Planet o2) {
            return (int) (o1.getDistanceTo(this.ship) -o2.getDistanceTo(this.ship));
        }
    }

    public static void main(final String[] args) throws IOException {
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
        for (;;) {
            moveList.clear();
            networking.updateMap(gameMap);

            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                final ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, moveList, ship, new Position(0,0), 0, false, 0, 0);
                if (newThrustMove != null) {
                    moveList.add(newThrustMove);
                }
            }
            Networking.sendMoves(moveList);

        }

    }
}