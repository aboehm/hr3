/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ngonga
 */
public class Index {
    Map<List<Integer>, Hypercube> cubes;
    
    public Index()
    {
        cubes = new HashMap<List<Integer>, Hypercube>();
    }
    
    /** Assigns a point to a cube
     * 
     * @param p Point
     * @param cubeIndex Index of cube 
     */
    public void addPointToCube(Point p, List<Integer> cubeIndex)
    {
        if(!cubes.containsKey(cubeIndex))
            cubes.put(cubeIndex, new Hypercube());
        cubes.get(cubeIndex).elements.add(p);
    }
    
    /** Returns the hypercube for a given index
     * 
     * @param index Index of a hypercube
     * @return Hypercube
     */
    public Hypercube getCube(List<Integer> index)
    {
        return cubes.get(index);
    }
}
