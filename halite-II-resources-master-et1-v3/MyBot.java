import hlt.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MyBot {
	public static int turn=1;
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
        for (;;turn++) {
            moveList.clear();
            networking.updateMap(gameMap);
            
            //o tura
            List<Ship>ships=new LinkedList<Ship>();
            for(Ship ship:gameMap.getMyPlayer().getShips().values())
            	ships.add(ship);
            List<Planet>planets=new LinkedList<Planet>();
            for(Planet planet:gameMap.getAllPlanets().values()){
            	if(!planet.isOwned()){
            		planets.add(planet);
            	}
            }
            double[][]dist =new double[ships.size()][planets.size()];
            for(int i=0;i<ships.size();i++)
            	for(int j=0;j<planets.size();j++)
            		dist[i][j]=ships.get(i).getDistanceTo(planets.get(j));
           //sortare ships
            if(planets.isEmpty()==false){
         //   	SortPlanetsClass.sortPlanets(planets,dist,ships);
         //   	SortShipsClass.sortShips(ships,dist,planets);
            }
            planets.clear();
            for(Planet planet:gameMap.getAllPlanets().values())
            	planets.add(planet);
            dist =new double[ships.size()][planets.size()];
            for(int i=0;i<ships.size();i++)
            	for(int j=0;j<planets.size();j++)
            		dist[i][j]=ships.get(i).getDistanceTo(planets.get(j));
            
            
            //pentru fiecare nava ship
            int nrploc=0;
            for(int i=0;i<ships.size();i++){
            	Ship ship=ships.get(i);
            	
            	
          //  	if(ship.getDockingStatus()==Ship.DockingStatus.Docking){
          //  		double dmin=Double.MAX_VALUE;
           //         int indmin=-1;
            //        for(int j=0;j<planets.size();j++) {
            //        	if(dist[i][j]<dmin){
            //            	dmin=dist[i][j];
            //            	indmin=j;
            //            }
            //        }
            //        for(int j=i+1;j<ships.size();j++)
           //     		dist[j][indmin]=Double.MAX_VALUE;
           //         continue;
           // 	}
            	
            	
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }
                
                
               if(nrploc==planets.size()){
                    for(int i1=0;i1<ships.size();i1++)
                        for(int i2=0;i2<planets.size();i2++)
                            dist[i1][i2]=ships.get(i1).getDistanceTo(planets.get(i2));
               }

               double dmin=Double.MAX_VALUE;
               int indmin=-1;

                nrploc=0;
                int dock=0;
                //pentru fiecare planeta netargetata
                for(int j=0;j<planets.size();j++) {

                    if(dist[i][j]==Double.MAX_VALUE) {nrploc++;continue;}
                	final Planet planet=planets.get(j);
                    if (planet.isOwned()) { 
                        nrploc++;    
                        continue;
                    }

                    if (dock==0&&ship.canDock(planet)) {
                        moveList.add(new DockMove(ship, planet));      
                        indmin=j;
                        dock=1;
                        nrploc++;
                        for(int j1=i+1;j1<ships.size();j1++)
                    		dist[j1][indmin]=Double.MAX_VALUE;
                        continue;
                    }
                    
                    
                    if(dock==0&&dist[i][j]<dmin){
                    	dmin=dist[i][j];
                    	indmin=j;
                    }
                }
                   
                if(indmin==-1) break;
                
                if(dock==1) continue;
                
                
                final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planets.get(indmin), Constants.MAX_SPEED);
                if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                        nrploc++;
                        for(int j=i+1;j<ships.size();j++)
                        		dist[j][indmin]=Double.MAX_VALUE;
                  
                    }
                }
            Networking.sendMoves(moveList);
        }
    }
    
    
    
}
