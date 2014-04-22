/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff;

import de.uni_leipzig.simba.hr3.HR3;
import de.uni_leipzig.simba.hr3.MainMemoryMapping;
import de.uni_leipzig.simba.hr3.Mapping;
import de.uni_leipzig.simba.hr3.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public class NaiveHausdorff implements Hausdorff{
    public int computations;
    /**
     * Brute force approach to computing the Hausdorff distance between two
     * polygons
     *
     * @param X First polygon
     * @param Y Second polygon
     * @return Distance between the two polygons
     */
    
    public NaiveHausdorff()
    {
        computations = 0;
    }
    
    public int getComputations()
    {
        return computations;
    }
    public float computeDistance(Polygon X, Polygon Y, float threshold) {
        float max = 0f;
        float d;
        float min;
        for (Point x : X.points) {
            min = Float.POSITIVE_INFINITY;
            for (Point y : Y.points) {
                d = distance(x, y);
                if (min > d) {
                    min = d;
                }
            }
            if (max < min) {
                max = min;
            }
        }
        return max;
    }

    
    public static float distance(Polygon X, Polygon Y, float threshold) {
        float max = 0f;
        float d;
        float min;
        for (Point x : X.points) {
            min = Float.POSITIVE_INFINITY;
            for (Point y : Y.points) {
                d = OrthodromicDistance.getDistanceInDegrees(x, y);
                if (min > d) {
                    min = d;
                }
            }
            if (max < min) {
                max = min;
            }
        }
        return max;
    }

    public String getName()
    {
        return "naive";
    }
    /**
     * Computes the Hausdorff distance for a source and target set
     *
     * @param source Source polygons
     * @param target Target polygons
     * @param threshold Distance threshold
     * @return Mapping of uris
     */
    public Mapping run(Set<Polygon> source, Set<Polygon> target, float threshold) {
        Mapping m = new MainMemoryMapping();
        for (Polygon s : source) {
            for (Polygon t : target) {
                float d = computeDistance(s, t, threshold);
                if (d <= threshold) {
                    m.add(s.uri, t.uri, d);
                }
            }
        }
        return m;
    }

    /**
     * @param x Point x
     * @param y Point y
     * @return Distance between x and y
     */
    public float distance(Point x, Point y) {
        computations++;
        return OrthodromicDistance.getDistanceInDegrees(x, y);
    }
}
