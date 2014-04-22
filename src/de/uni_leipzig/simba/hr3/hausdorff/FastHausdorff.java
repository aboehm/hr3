/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff;

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
public class FastHausdorff extends NaiveHausdorff {

    /**
     * Efficient computation of the Hausdorff distance between two polygons
     *
     * @param X First polygon
     * @param Y Second polygon
     * @return Distance between the two polygons
     */
    @Override
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
            if (min > threshold) {
                return min;
            }
            if (max < min) {
                max = min;
            }
        }
        return max;
    }

    public FastHausdorff() {
        computations = 0;
    }
    
    public String getName()
    {
        return "fast";
    }
}
