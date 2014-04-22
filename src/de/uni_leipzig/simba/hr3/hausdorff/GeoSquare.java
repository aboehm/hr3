/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff;

import java.util.HashSet;
import java.util.Set;

/**
 * Same as a hypercube for polygons
 *
 * @author ngonga
 */
public class GeoSquare {

    public Set<Polygon> elements;

    public GeoSquare() {
        elements = new HashSet<Polygon>();
    }
    public String toString()
    {
        return elements.toString();
    }
}
