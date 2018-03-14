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
            sortShips(ships,dist,ships.size(),planets.size());
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
                        continue;
                    }
                    
                    
                    if(dock==0&&dist[i][j]<dmin){
                    	dmin=dist[i][j];
                    	indmin=j;
                    }
                }
                   
                if(indmin==-1) {
                	//System.out.println(ship+" "+nrploc+" "+planets.size());
                	break;}
                nrploc++;
                for(int j=0;j<ships.size();j++){
                	if(j!=i)
                		dist[j][indmin]=Double.MAX_VALUE;
                }
                if(dock==1) continue;
                
                
                final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planets.get(indmin), Constants.MAX_SPEED);
                if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                    }
                }
            Networking.sendMoves(moveList);
        }
    }
    
    
    static double[] minShip(double d[][],int s,int p){
    	double[] res=new double[s];
    	
    	for(int i=0;i<s;i++){
    		double min=Double.MAX_VALUE;
    		for(int j=0;j<p;j++)
    			if(d[i][j]<min)
    				min=d[i][j];
    		res[i]=min;
    	}
    	return res;
    }
    
    
    static void sortShips(List<Ship> ships,double[][]dist,int s,int p){
    	double arr[]=minShip(dist,s,p);
    	sort(ships,arr,0,s-1);
    }
    
    static void merge(List<Ship> ships,double[] arr, int l, int m, int r)
    {
        int n1 = m - l + 1;
        int n2 = r - m;
        
        double L[] = new double [n1];
        double R[] = new double [n2];
        ArrayList<Ship>shipsL=new ArrayList<Ship>(n1);
        for(int i1=0;i1<n1;i1++)
        	shipsL.add(null);
        ArrayList<Ship>shipsR=new ArrayList<Ship>(n2);
        for(int i2=0;i2<n2;i2++)
        	shipsR.add(null);
 
        for (int i=0; i<n1; ++i){
            L[i] = arr[l + i];
            shipsL.set(i, ships.get(l+i));
        }
        for (int j=0; j<n2; ++j){
            R[j] = arr[m + 1+ j];
            shipsR.set(j, ships.get(m+1+j));
        }

        int i = 0, j = 0;

        int k = l;
        while (i < n1 && j < n2)
        {
            if (L[i] <R[j])
            {
            	arr[k]=L[i];
                ships.set(k,shipsL.get(i));
                i++;
            }
            else
            {
            	arr[k]=R[j];
                ships.set(k, shipsR.get(j));
                j++;
            }
            k++;
        }
 
        while (i < n1)
        {
        	arr[k]=L[i];
            ships.set(k,shipsL.get(i));
            k++;
            i++;
      
        }
 
        while (j < n2)
        {
        	arr[k]=R[j];
            ships.set(k,shipsR.get(j));
            k++;
            j++;
        }
        
    }
 
    static void sort(List<Ship> ships,double arr[], int l, int r)
    {
        if (l < r)
        {
            int m = (l+r)/2;
 
            sort(ships,arr, l, m);
            sort(ships,arr , m+1, r);
 
            merge(ships,arr, l, m, r);
        }
    }
}
