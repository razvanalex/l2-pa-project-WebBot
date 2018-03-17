import hlt.*;
//import superAPI.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class MyBot {

   
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
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }

                PriorityQueue<Planet> planets = new PriorityQueue<Planet>( new CompereDistanceToPlanet(ship) );
                for (final Planet planet : gameMap.getAllPlanets().values()) {
                    if (planet.isOwned() ) {
                        continue;
                   
                    } else{
                        planets.add(planet);
                    }
                }

                for(final Planet planet : planets){
                    if (ship.canDock(planet)) {
                        moveList.add(new DockMove(ship, planet));
                        break;
                    }

                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, moveList, ship, planet, Constants.MAX_SPEED);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                    }
                    break;
                }
            }
            Log.log("total moves: " + moveList.size() + " num ships: " + gameMap.getMyPlayer().getShips().size());
            Networking.sendMoves(moveList);            
        }
    }
}