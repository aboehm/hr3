/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3;

import java.util.List;

/**
 *
 * @author ngonga
 */
public class Point {
    public List<Float> coordinates;
    public String label;
    
    public Point(String name, List<Float> position)
    {
        label = name;
        coordinates = position;
    }
    
    @Override
    public String toString()
    {
        return coordinates.toString();
    }
}
