package hlt;

public class Collision {
    /**
     * Test whether a given line segment intersects a circular area.
     *
     * @param start  The start of the segment.
     * @param end    The end of the segment.
     * @param circle The circle to test against.
     * @param fudge  An additional safety zone to leave when looking for collisions. Probably set it to ship radius.
     * @return true if the segment intersects, false otherwise
     */
    public static boolean segmentCircleIntersect(final Position start, final Position end, final Entity circle, final double fudge) {
        // Parameterize the segment as start + t * (end - start),
        // and substitute into the equation of a circle
        // Solve for t
        final double circleRadius = circle.getRadius();
        final double startX = start.getXPos();
        final double startY = start.getYPos();
        final double endX = end.getXPos();
        final double endY = end.getYPos();
        final double centerX = circle.getXPos();
        final double centerY = circle.getYPos();
        final double dx = endX - startX;
        final double dy = endY - startY;

        final double a = square(dx) + square(dy);

        final double b = -2 * (square(startX) - (startX * endX)
                            - (startX * centerX) + (endX * centerX)
                            + square(startY) - (startY * endY)
                            - (startY * centerY) + (endY * centerY));

        if (a == 0.0) {
            // Start and end are the same point
            return start.getDistanceTo(circle) <= circleRadius + fudge;
        }

        // Time along segment when closest to the circle (vertex of the quadratic)
        final double t = Math.min(-b / (2 * a), 1.0);
        if (t < 0) {
            return false;
        }

        final double closestX = startX + dx * t;
        final double closestY = startY + dy * t;
        final double closestDistance = new Position(closestX, closestY).getDistanceTo(circle);

        return closestDistance <= circleRadius + fudge;
    }

    public static double square(final double num) {
        return num * num;
    }

    public static boolean twoSgmentIntersect(
            final Position start1, final Position end1, 
            final Position start2, final Position end2) 
    {
        Position r = add2D(end1, negate2D(start1));
        Position s = add2D(end2, negate2D(start2));
        Position diff21 = add2D(start2, negate2D(start1));
        double crossRR = crossProduct2D(r, r);
        double t0 = crossProduct2D(diff21, r) / crossRR;
        double t1 = t0 + crossProduct2D(s, r) / crossRR;
        double crossRS = crossProduct2D(r, s);

        if (crossRS == 0) { 
            if (crossProduct2D(diff21, r) == 0) {
                if (intervalIntersect(t0, t1, 0, 1)) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            double t = crossProduct2D(diff21, s) / crossRS;
            double u = crossProduct2D(diff21, s) / crossRS;

            if (0 <= t && t <= 1 && 0 <= u && u <= 1) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Computesthe cross product between two vectors
     *  
     * Formula : v X w =  v_x * w_y − v_y * w_x
     */
    public static double crossProduct2D(Position v, Position w) {
        return v.getXPos() * w.getYPos() - v.getYPos() * w.getXPos();
    }

    public static double dotProduct2D(Position v, Position w) {
        return v.getXPos() * w.getXPos() + v.getYPos() * w.getYPos();
    }

    public static Position add2D(Position v, Position w) {
        return new Position(v.getXPos() + w.getXPos(), v.getYPos() + w.getYPos());
    }

    public static Position negate2D(Position v) {
        return new Position(-v.getXPos(), -v.getYPos());
    }

    /**
     * Note that: s1 <= e1 and s2 <= e2
     */
    public static boolean intervalIntersect(
        double s1, double e1, 
        double s2, double e2) 
    {

        if (e2 < e1 && s1 < e2)
            return true;

        return false;
    }
}
