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
 * Adds the distance from the centroids, which are in the middle of the longest axis. 
 * Also stores the radius of the smallest circle which contains the polygon entirely
 * @author ngonga
 */
public class CentroidIndex extends PolygonIndex{
    
    public Map<String, Circle> centroids;
    public CentroidIndex()
    {
        super();
        centroids = new HashMap<String, Circle>();        
    }
    
    @Override
    public void index(Polygon p)
    {
        Map<Point, Map<Point, Float>> index = new HashMap<Point, Map<Point, Float>>();
        Map<Point, Float> distances;
        float maxDistance = 0f;
        float distance;
        int from = -1, to=-1;
        for (int i = 0; i < p.points.size(); i++) {
            distances = new HashMap<Point, Float>();            
            for (int j = i + 1; j < p.points.size(); j++) {
                distance = OrthodromicDistance.getDistanceInDegrees(p.points.get(i), p.points.get(j));
                distances.put(p.points.get(j), distance);
                if(distance > maxDistance)
                {
                    maxDistance = distance;
                    from = i;
                    to = j;
                }
                computations++;
            }
            if (!distances.isEmpty()) {
                index.put(p.points.get(i), distances);
            }
        }
        // if polygon size is above 1, then compute the middle of the longest axis
        if(from >= 0)
        {
            centroids.put(p.uri, new Circle(average(p.points.get(from), p.points.get(to)), maxDistance/2f));
        }
        
        //else take the point itself
        else
        {
            centroids.put(p.uri, new Circle(p.points.get(0), 0f));
        }
        distanceIndex.put(p.uri, index);
        polygonIndex.put(p.uri, p);        
    }
    
    public Point average(Point source, Point target)
    {
        List<Float> coordinates = new ArrayList<Float>();
        for(int i=0; i<source.coordinates.size(); i++)
        {
            coordinates.add((source.coordinates.get(i) + target.coordinates.get(i))/2f);
        }
        Point center = new Point("", coordinates);
        return center;
    }
    
    public class Circle
    {
        public Point center;
        public float radius;
        
        public Circle(Point c, float r)
        {
            center = c;
            radius = r;
        }
    }
}
