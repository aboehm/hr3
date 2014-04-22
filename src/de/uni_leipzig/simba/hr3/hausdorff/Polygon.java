/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff;

import de.uni_leipzig.simba.hr3.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ngonga
 */
public class Polygon {

    public List<Point> points;
    public String uri;

    /**
     * Creates a new Polygon
     */
    public Polygon(String name) {
        uri = name;
        points = new ArrayList<Point>();
    }
    
    public Polygon(String name, List<Point> p) {
        uri = name;
        points = p;
    }

    /**
     * Adds a point to the polygon. Also updates the distance list
     *
     * @param y Point to add
     */
    public void add(Point y) {
        points.add(y);
    }
    
    public String toString()
    {
        return "Polygon "+uri;
    }
}
