package superAPI;

import hlt.*;
import java.util.*;

public final class Helper {
    public static double getDistanceBetween(Position p1, Position p2) {
        return Math.sqrt(Math.pow(p1.getXPos() - p2.getXPos(), 2) + 
                         Math.pow(p1.getYPos() - p2.getYPos(), 2));
    }

    public static boolean checkInsideBox(Position p, 
            Position start, Position end) {

        if (p.getXPos() >= start.getXPos() && 
                p.getXPos() <= end.getXPos()) {
                    
            if (p.getYPos() >= start.getYPos() && 
                    p.getYPos() <= end.getYPos()) {
                return true;
            }
        }
        return false;
    }

    public static <T> int binarySearch(T elem, List<T> list, 
            Comparator<T> comp) {
        return bsAux(elem, list, 0, list.size(), comp);
    }

    private static <T> int bsAux(T elem, List<T> list, int l, int r, 
            Comparator<T> comp) {
        while (l <= r) {
            int mid = l + (r - l) / 2;
            if (elem == list.get(mid)) 
                return mid;
            if (comp.compare(elem, list.get(mid)) > 0) 
                r = mid - 1;
            if (comp.compare(elem, list.get(mid)) < 0) 
                r  = mid + 1;
        }

        return -1;
    }

    public static boolean insideBoundingCircle(Position elem, Position refElem, 
            double radius) {
        return getDistanceBetween(elem, refElem) <= radius;
    }

    public static double distanceOverX(Position p1, Position p2) {
        return Math.abs(p1.getXPos() - p2.getXPos());
    }
}