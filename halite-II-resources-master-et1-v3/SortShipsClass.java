import hlt.Planet;
import hlt.Ship;

import java.util.ArrayList;
import java.util.List;


public class SortShipsClass {
	static double[] minShip(double[][] d,List<Ship>ships,List<Planet>planets){
    	int s=ships.size();
    	int p=planets.size();
    	
    	ArrayList<Planet>planete_apropiate=new ArrayList<Planet>(s);
    	for(int i=0;i<s;i++)
    		planete_apropiate.add(null);
    	
    	double[] res=new double[s];
    	

    	for(int j=0;j<p;j++){
    		double dcopy[][] =new double[s][p];
    		for(int i1=0;i1<s;i1++)
    			for(int i2=0;i2<p;i2++)
    				dcopy[i1][i2]=d[i1][i2];
    		
    		
    		double min=Double.MAX_VALUE;
    		int indmin=-1;
    		
    		while(true){
    			int ok=1;
    			indmin=-1;
    			min=Double.MAX_VALUE;
    			for(int i=0;i<s;i++)
    				if(dcopy[i][j]<min){
    					min=dcopy[i][j];
    					indmin=i;
    				}
    			if(planete_apropiate.get(indmin)!=null){
    				dcopy[indmin][j]=Double.MAX_VALUE;
    				ok=0;
    			}		
    		
    			if(ok==1) break;
    		}
    		planete_apropiate.set(indmin, planets.get(j));
    		res[indmin]=min;
    		if(j==s-1) break;
    	}
    	
    	for(int i=0;i<s;i++){
    		if(planete_apropiate.get(i)==null){
    			int indmin=-1;
    			double min=Double.MAX_VALUE;
    			for(int j=0;j<p;j++)
    				if(d[i][j]<min){
    					min=d[i][j];
    					indmin=j;
    				}
    			planete_apropiate.set(i, planets.get(indmin));
        		res[i]=min;
    		}
    	}
    	return res;
    }
    
    
    static void sortShips(List<Ship> ships,double[][]dist,List<Planet>planets){
    	int s=ships.size();
    	double arr[]=minShip(dist,ships,planets);
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

