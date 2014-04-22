/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff;

import java.util.*;

/**
 *
 * @author ngonga
 */
public class GeoIndex {
    Map<Integer, Map<Integer, GeoSquare>> squares;
    Map<String, Set<List<Integer>>> polygonsToSquares;
    
    public GeoIndex()
    {
        squares = new HashMap<Integer, Map<Integer, GeoSquare>>();
        polygonsToSquares = new HashMap<String, Set<List<Integer>>>();
    }
    
    
    /** Adds a polygon to an index
     * 
     * @param X Polygon to add
     * @param latIndex Index according to latitude
     * @param longIndex Index according to longitude
     */
    public void addPolygon(Polygon X, int latIndex, int longIndex)
    {
        if(!squares.containsKey(latIndex))
            squares.put(latIndex, new HashMap<Integer, GeoSquare>());
        if(!squares.get(latIndex).containsKey(longIndex))
            squares.get(latIndex).put(longIndex, new GeoSquare());
        squares.get(latIndex).get(longIndex).elements.add(X);
        
        if(!polygonsToSquares.containsKey(X.uri))
            polygonsToSquares.put(X.uri, new HashSet<List<Integer>>());
        polygonsToSquares.get(X.uri).add(Arrays.asList(new Integer[]{latIndex, longIndex}));
    }
    
    /** Returns the square with the coordinates latIndex, longIndex
     * 
     * @param latIndex Latitude of the square to return
     * @param longIndex Longitude of the same
     * @return GeoSquare
     */
    public GeoSquare getSquare(int latIndex, int longIndex)
    {
        if(squares.containsKey(latIndex))
        {
            if(squares.get(latIndex).containsKey(longIndex))
                return squares.get(latIndex).get(longIndex);
        }
        return new GeoSquare();
    }
    
    /**
     * 
     * @param X Polygon whose indexes are required
     * @return Set of all indexes for this polygon
     */
    public Set<List<Integer>> getIndexes(Polygon X)
    {
        return polygonsToSquares.get(X.uri);
    }
    
    public String toString()
    {
        return squares.toString();
    }
}
