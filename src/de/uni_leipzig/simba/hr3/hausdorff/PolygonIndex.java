/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff;

import de.uni_leipzig.simba.hr3.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public class PolygonIndex {

    public Map<String, Map<Point, Map<Point, Float>>> distanceIndex;
    public Map<String, Polygon> polygonIndex;
    public int computations;

    public PolygonIndex() {
        distanceIndex = new HashMap<String, Map<Point, Map<Point, Float>>>();
        polygonIndex = new HashMap<String, Polygon>();
        computations = 0;
    }

    public String toString() {
        return distanceIndex.toString();
    }

    /**
     * Indexes a list of polygons by mapping the uri of each polygon to the
     * corresponding distanceIndex
     *
     * @param polygons
     */
    public void index(Set<Polygon> polygons) {
        for (Polygon x : polygons) {
            index(x);
        }
    }

    /**
     * Indexes the distances between the points in a given polygon and adds
     * polygon to list of indexes
     *
     * @param p Input polygon
     * @return Distances between all points in the polygon
     */
    public void index(Polygon p) {
        Map<Point, Map<Point, Float>> index = new HashMap<Point, Map<Point, Float>>();
        Map<Point, Float> distances;
        for (int i = 0; i < p.points.size(); i++) {
            distances = new HashMap<Point, Float>();
            for (int j = i + 1; j < p.points.size(); j++) {
                distances.put(p.points.get(j), OrthodromicDistance.getDistanceInDegrees(p.points.get(i), p.points.get(j)));
                computations++;
            }
            if (!distances.isEmpty()) {
                index.put(p.points.get(i), distances);
            }
        }
        distanceIndex.put(p.uri, index);
        polygonIndex.put(p.uri, p);
    }

    /**
     * Returns the distances between two points x and y from the polygon with
     * label uri Returns -1 if nothing is found
     *
     * @param uri Label of the polygon
     * @param x First point from the polygon
     * @param y Second point from the polygon
     * @return Distance between x and y
     */
    public float getDistance(String uri, Point x, Point y) {
        if (x.equals(y)) {
            return 0f;
        }
        Polygon p = polygonIndex.get(uri);
        if (p.points.indexOf(x) < p.points.indexOf(y)) {
            return distanceIndex.get(uri).get(x).get(y);
        } else {
            return distanceIndex.get(uri).get(y).get(x);
        }
    }
}
