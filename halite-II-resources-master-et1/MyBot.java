import hlt.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MyBot {

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
        //incepe jocul
        for (;;) {
            moveList.clear();
            networking.updateMap(gameMap);
            
            //o tura
            List<Ship>ships=new LinkedList<Ship>();
            for(Ship ship:gameMap.getMyPlayer().getShips().values())
            	ships.add(ship);
            List<Planet>planets=new LinkedList<Planet>();
            for(Planet planet:gameMap.getAllPlanets().values())
            	planets.add(planet);
            double[][]dist =new double[ships.size()][planets.size()];
            for(int i=0;i<ships.size();i++)
            	for(int j=0;j<planets.size();j++)
            		dist[i][j]=ships.get(i).getDistanceTo(planets.get(j));
           
            //pentru fiecare nava ship
            for(int i=0;i<ships.size();i++){
            	Ship ship=ships.get(i);
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }
                
                
               double dmin=Double.MAX_VALUE;
               int indmin=-1;
                //pentru fiecare planeta netargetata
                for(int j=0;j<planets.size();j++) {
                	final Planet planet=planets.get(j);
                    if (planet.isOwned()) {     
                        continue;
                    }

                    if (ship.canDock(planet)) {
                        moveList.add(new DockMove(ship, planet)); 
                        indmin=-1;
                        break;
                    }
                    
                    
                    if(dist[i][j]<dmin){
                    	dmin=dist[i][j];
                    	indmin=j;
                    }
                }
                    	
                if(indmin==-1) continue;
                for(int j=0;j<ships.size();j++){
                	if(j!=i)
                		dist[j][indmin]=Integer.MAX_VALUE;
                }
                
                final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planets.get(indmin), Constants.MAX_SPEED);
                if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                    }
                }
            Networking.sendMoves(moveList);
        }
    }
}
