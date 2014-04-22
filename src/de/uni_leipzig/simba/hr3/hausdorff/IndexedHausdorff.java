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
public class IndexedHausdorff implements Hausdorff {

    public PolygonIndex targetIndex;
    public int computations;

    /**
     * Initialization ensures that application fails if points were not indexed
     * before distances are computed
     *
     */
    public IndexedHausdorff() {
        targetIndex = null;
        computations = 0;
    }

    public int getComputations() {        
        return computations + targetIndex.computations;
    }

    public Mapping run(Set<Polygon> source, Set<Polygon> target, float threshold) {
        // first run indexing
        Mapping m = new MainMemoryMapping();
        targetIndex = new PolygonIndex();
        long begin = System.currentTimeMillis();
        targetIndex.index(target);
        long end = System.currentTimeMillis();
//        System.out.println("Indexing took " + (end - begin) + " ms and "+targetIndex.computations+" computations.");
        float d;
        for (Polygon s : source) {
            for (Polygon t : target) {
                d = computeDistance(s, t, threshold);
                if (d <= threshold) {
                    m.add(s.uri, t.uri, d);
                }
            }
        }
        return m;
    }

    public Map<Point, Map<Point, Float>> getInnerDistances(Polygon s) {
        Map<Point, Map<Point, Float>> distances = new HashMap<Point, Map<Point, Float>>();
        for (int i = 0; i < s.points.size(); i++) {
            Map<Point, Float> buffer = new HashMap<Point, Float>();
            for (int j = i + 1; j < s.points.size(); j++) {
                buffer.put(s.points.get(j), distance(s.points.get(i), s.points.get(j)));
            }
            distances.put(s.points.get(i), buffer);
        }
        return distances;
    }

    public Map<String, Map<String, Float>> runWithoutIndex(Set<Polygon> source, Set<Polygon> target, float threshold) {

        Map<String, Map<String, Float>> map = new HashMap<String, Map<String, Float>>();
        Map<String, Float> mapping;
        Map<Point, Map<Point, Float>> distances;
        Map<Point, Map<Point, Float>> exemplars;
        float min, max, d;
        boolean approximationWorked;
        for (Polygon s : source) {
            distances = getInnerDistances(s);
            //now run approximation
            mapping = new HashMap<String, Float>();
            for (Polygon t : target) {
                max = 0f;
                exemplars = new HashMap<Point, Map<Point, Float>>();
                for (Point x : s.points) {
                    // no exemplars yet, then simply compute distance to all points y
                    if (exemplars.isEmpty()) {
                        min = Float.POSITIVE_INFINITY;
                        for (Point y : t.points) {
                            d = distance(x, y);
                            if (!exemplars.containsKey(x)) {
                                exemplars.put(x, new HashMap<Point, Float>());
                            }
                            exemplars.get(x).put(y, d);
                            if (d < min) {
                                min = d;
                            }
                        }

                    } // else first try approximations
                    else {
                        // try each exemplar to point combination
                        min = Float.POSITIVE_INFINITY;
                        for (Point y : t.points) {
                            approximationWorked = false;
                            for (Point e : exemplars.keySet()) {
                                float approximation = 0;
                                // check whether distance from y to examplar was actually computed
                                if (exemplars.get(e).containsKey(y)) {
                                    if (s.points.indexOf(x) < s.points.indexOf(e)) {
                                        approximation = Math.abs(distances.get(x).get(e) - exemplars.get(e).get(y));
                                    } else {
                                        approximation = Math.abs(distances.get(e).get(x) - exemplars.get(e).get(y));
                                    }
                                }
                                if (approximation > threshold) {
                                    approximationWorked = true;
                                    break;
                                }
                            }
                            if (!approximationWorked) {
                                d = distance(x, y);
                                //update exemplars
                                if (!exemplars.containsKey(x)) {
                                    exemplars.put(x, new HashMap<Point, Float>());
                                }
                                exemplars.get(x).put(y, d);
                                if (min > d) {
                                    min = d;
                                }
                            }
                        }
                    }
                    //update maximal distances
                    //note that in case an approximation 
                    if (max < min) {
                        max = min;
                    }
                    if (max > threshold) {
                        break;
                    }
                }

                if (max <= threshold) {
                    mapping.put(t.uri, max);
                }
            }
            if (!mapping.isEmpty()) {
                map.put(s.uri, mapping);
            }
        }
        return map;
    }

    public float computeDistance(Polygon X, Polygon Y, float threshold) {
        if (X.uri.equals(Y.uri)) {
            return 0f;
        }
        float max = 0f;
        float d;
        Map<Point, Float> distances;
        float min = 0, approx;
        for (Point x : X.points) {
            distances = new HashMap<Point, Float>();
            for (Point y : Y.points) {
                if (distances.isEmpty()) {
                    min = distance(x, y);
                    distances.put(y, min);
                } else {
                    //first try examplars
                    float dist, minDist = Float.POSITIVE_INFINITY;
                    Point exemplar = null;
                    for (Point e : distances.keySet()) {
                        dist = targetIndex.getDistance(Y.uri, e, y);
                        if (dist < minDist) {
                            minDist = dist;
                            exemplar = e;
                        }
                    }
                    approx = Math.abs(distances.get(exemplar) - minDist);
                    if (approx > threshold) {
                        //no need to compute d as it is larger than the threshold anyway
                        //also no need to update min as the value would lead to the point 
                        //being discarded anyway
                        d = threshold+1;
//                        distances.put(y, d);
                        if (min > d) {
                            min = d;
                        }
                    } else 
                    if (approx < min) {
                        //approximation does not give us any information
                        d = distance(x, y);
                        distances.put(y, d);
                        if (min > d) {
                            min = d;
                        }
                    }
                }
            }
            if (max < min) {
                max = min;
            }
            if (max > threshold) {
                return max;
            }
        }

        return max;
    }

    /**
     * Needs to be replaced with the orthdromic distance
     *
     * @param x Point x
     * @param y Point y
     * @return Distance between x and y
     */
    public float distance(Point x, Point y) {
        computations++;
        return OrthodromicDistance.getDistanceInDegrees(x, y);
    }

    public String getName() {
        return "indexed";
    }
}
