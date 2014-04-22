/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff;

import de.uni_leipzig.simba.hr3.Mapping;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public interface Hausdorff {
    public float computeDistance(Polygon X, Polygon Y, float threshold);
    public Mapping run(Set<Polygon> source, Set<Polygon> target, float threshold);
    public int getComputations();
    public String getName();
}
