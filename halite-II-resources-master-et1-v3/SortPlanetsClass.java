import hlt.Planet;
import hlt.Ship;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class SortPlanetsClass {
	static double[] minPlanets(List<Planet>planets,double[][]d,List<Ship>ships){
    	double[] result=new double[planets.size()];
    	for(int j=0;j<planets.size();j++){
    		double min=Double.MAX_VALUE;
    		for(int i=0;i<ships.size();i++){
    			if(d[i][j]<min)
    				min=d[i][j];
    		}
    		result[j]=min;
    	}
    	return result;	
    }
	
	
	static void sortPlanets(List<Planet> planets,double[][]dist,List<Ship>ships){
    	int p=planets.size();
    	double arr[]=minPlanets(planets,dist,ships);
    	sort(planets,arr,0,p-1);
    }
    
    static void merge(List<Planet> planets,double[] arr, int l, int m, int r)
    {
        int n1 = m - l + 1;
        int n2 = r - m;
        
        double L[] = new double [n1];
        double R[] = new double [n2];
        ArrayList<Planet>planetsL=new ArrayList<Planet>(n1);
        for(int i1=0;i1<n1;i1++)
        	planetsL.add(null);
        ArrayList<Planet>planetsR=new ArrayList<Planet>(n2);
        for(int i2=0;i2<n2;i2++)
        	planetsR.add(null);
 
        for (int i=0; i<n1; ++i){
            L[i] = arr[l + i];
            planetsL.set(i, planets.get(l+i));
        }
        for (int j=0; j<n2; ++j){
            R[j] = arr[m + 1+ j];
            planetsR.set(j, planets.get(m+1+j));
        }

        int i = 0, j = 0;

        int k = l;
        while (i < n1 && j < n2)
        {
            if (L[i] <R[j])
            {
            	arr[k]=L[i];
            	planets.set(k,planetsL.get(i));
                i++;
            }
            else
            {
            	arr[k]=R[j];
            	planets.set(k, planetsR.get(j));
                j++;
            }
            k++;
        }
 
        while (i < n1)
        {
        	arr[k]=L[i];
        	planets.set(k,planetsL.get(i));
            k++;
            i++;
      
        }
 
        while (j < n2)
        {
        	arr[k]=R[j];
        	planets.set(k,planetsR.get(j));
            k++;
            j++;
        }
        
    }
 
    static void sort(List<Planet> planets,double arr[], int l, int r)
    {
        if (l < r)
        {
            int m = (l+r)/2;
 
            sort(planets,arr, l, m);
            sort(planets,arr , m+1, r);
 
            merge(planets,arr, l, m, r);
        }
    }
}

