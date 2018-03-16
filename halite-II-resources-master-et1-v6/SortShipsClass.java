import hlt.Planet;
import hlt.Ship;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import hlt.*;

public class SortShipsClass {
	static class ComparatorSP implements Comparator<MyEntry>{	
		
		public int compare(MyEntry entity1,MyEntry entity2){
			double d1=entity1.getKey().getDistanceTo(entity1.getValue());
			double d2=entity2.getKey().getDistanceTo(entity2.getValue());
			if(d1-d2<0)return -1;
			else if (d1-d2>0) return 1;
			return 0;
		}
	}
	
	static class MyEntry implements Map.Entry<Ship,Planet>{
	    private final Ship key;
	    private Planet value;

	    public MyEntry(Ship key, Planet value) {
	        this.key = key;
	        this.value = value;
	    }

	    public Ship getKey() {
	        return key;
	    }

	    public Planet getValue() {
	        return value;
	    }

	    public Planet setValue(Planet value) {
	        Planet old = this.value;
	        this.value = value;
	        return old;
	    }

	}
	
	
	static TreeSet<MyEntry> sortShips(List<Ship>ships,List<Planet>planets){
		List<Ship>copyShips=new LinkedList<Ship>();
		for(int i=0;i<ships.size();i++)
			copyShips.add(ships.get(i));
		
    	TreeSet<MyEntry>dist_s_p=new TreeSet<MyEntry>(new ComparatorSP());
    	
    	TreeSet<MyEntry> new_ships=new TreeSet<MyEntry>(new ComparatorSP());
    	
    	while(copyShips.isEmpty()==false){
    	List<Planet>copyPlanets=new LinkedList<Planet>();
    	for(int i=0;i<planets.size();i++)
    		copyPlanets.add(planets.get(i));
    	
    	while(copyPlanets.isEmpty()==false){
    		for(int i=0;i<copyShips.size();i++){
    			for(int j=0;j<copyPlanets.size();j++){
    				int ok=1;
    				Ship ship=copyShips.get(i);
    				Planet planet=copyPlanets.get(j);
    				double d=ship.getDistanceTo(planet);
    			
    				for(Iterator<MyEntry> iter=dist_s_p.iterator();iter.hasNext();){
    					MyEntry myEntry=iter.next();
    					Ship myShip=myEntry.getKey();
    					Planet myPlanet=myEntry.getValue();
    					double myd=myShip.getDistanceTo(myPlanet);
        			
    					if(planet==myPlanet){
    						if(d<myd){
        					iter.remove();
        					ok=1;
    						}
    						else{
    							ok=0;
    						}
    						break;
    					}
    				}
    				if(ok==0) continue;
    			
    				dist_s_p.add(new MyEntry(ship,planet));
    			}
    		}
    	
    		for(Iterator<MyEntry> myIter=dist_s_p.iterator();myIter.hasNext();){
    			MyEntry myEntry=myIter.next();
    			Ship myShip=myEntry.getKey();
    			Planet myPlanet=myEntry.getValue();
    			for(Iterator<Ship> iter=copyShips.iterator();iter.hasNext();){
    				Ship ship=iter.next();
    				if(ship==myShip){
    					iter.remove();
    					copyPlanets.remove(myPlanet);
        				new_ships.add(new MyEntry(ship,myPlanet));
        				break;
    				}
    			}
    		}
    		dist_s_p.clear();
    		if(copyShips.isEmpty()==true) break;
    	}
    	}
    	return new_ships;
	}
}
    	
    	
