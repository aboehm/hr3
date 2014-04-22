/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author ngonga
 */
public class HR3 {

    static float DEFAULT_THRESHOLD = 4f;
    static int DEFAULT_GRANULARITY = 8;
    protected int granularity;
    protected float threshold;
    public static Logger logger = Logger.getLogger(HR3.class.getName());

    public HR3(float threshold, int granularity) {
        this.threshold = threshold;
        this.granularity = granularity;

        System.out.println("threshold \u03B8: " + threshold);
        System.out.println("granularity \u03B1: " + granularity);
    }

    /**
     * Indexes a set of points
     *
     * @param inputData
     * @return Index for the set of points
     */
    public Index assignCubes(Set<Point> inputData) {
        float delta = threshold / (float) granularity;
        Index index = new Index();
        for (Point p : inputData) {
            List<Integer> cubeIndex = new ArrayList<Integer>();
            for (float f : p.coordinates) {
                cubeIndex.add((int) Math.floor(f / delta));
            }
            index.addPointToCube(p, cubeIndex);
        }
        return index;
    }

    public static int power(int a, int b) {
        int result = 1;
        while (b > 0) {
            if (b % 2 != 0) {
                result *= a;
                b--;
            }
            a *= a;
            b /= 2;
        }
        return result;
    }
    
    public static float power(float a, int b) {
        float result = 1f;
        while (b > 0) {
            if (b % 2 != 0) {
                result *= a;
                b--;
            }
            a *= a;
            b /= 2;
        }
        return result;
    }

    /**
     * Returns the cubes to be compared to a cube with a given index
     *
     * @param cubeId
     * @return
     */
    public static List<List<Integer>> getCubesToCompare(List<Integer> cubeId, int granularity) {
        int dim = cubeId.size();
        if (dim == 0) {
            return new ArrayList<List<Integer>>();
        }
        List<List<Integer>> result = new ArrayList<List<Integer>>();
        List<List<Integer>> hr3result = new ArrayList<List<Integer>>();
        result.add(cubeId);

        List<List<Integer>> toAdd;
        List<Integer> id;

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < power(2 * granularity + 1, i); j++) {
                id = result.get(j);
                toAdd = new ArrayList<List<Integer>>();
                for (int k = 0; k < 2 * granularity; k++) {
                    toAdd.add(new ArrayList<Integer>());
                }
                for (int k = 0; k < dim; k++) {
                    if (k != i) {
                        for (int l = 0; l < 2 * granularity; l++) {
                            toAdd.get(l).add(id.get(k));
                        }
                    } else {
                        for (int l = 0; l < granularity; l++) {
                            toAdd.get(l).add(id.get(k) - (l + 1));
                        }
                        for (int l = 0; l < granularity; l++) {
                            toAdd.get(l + granularity).add(id.get(k) + l + 1);
                        }
                    }
                }
                //Merge results
                for (int l = 0; l < 2 * granularity; l++) {
                    result.add(toAdd.get(l));
                }
            }
        }

        //now run hr3 check 
        int alphaPower = power(granularity, dim);
        List<Integer> block;
        int hr3Index;
        int index;
        for (int i = 0; i < result.size(); i++) {
            hr3Index = 0;
            block = result.get(i);
            for (int j = 0; j < dim; j++) {
                if (block.get(j) == cubeId.get(j)) {
                    hr3Index = 0;
                    break;
                } else {
                    index = (Math.abs(cubeId.get(j) - block.get(j)) - 1);
                    hr3Index = hr3Index + power(index, dim);
                }
            }
            if (hr3Index < alphaPower) {
                hr3result.add(block);
            }
        }

        return hr3result;
    }

    /**
     * Computes the Euclidean distance between two points
     *
     * @param a
     * @param b
     * @return Distance between a and b
     */
    public static float computeDistance(Point a, Point b) {
        if (a.coordinates.size() != b.coordinates.size()) {
            logger.info("Size mismatch for " + a.coordinates + " and " + b.coordinates);
            return 0f;
        } else {
            int n = a.coordinates.size();
            float d = 0f;
            for (int i = 0; i < n; i++) {
                d = d + (float) power(a.coordinates.get(i) - b.coordinates.get(i), 2);
            }
            return (float) Math.sqrt(d);
        }
    }

    public void run(Set<Point> sourceData, Set<Point> targetData, Mapping mapping) {
        Index source = assignCubes(sourceData);
        Index target = assignCubes(targetData);
        System.out.println("|source cubes|= " + source.cubes.keySet().size());
        System.out.println("|target cubes|= " + target.cubes.keySet().size());

        float d;
        for (List<Integer> cubeIndex1 : source.cubes.keySet()) {
            Hypercube h1 = source.getCube(cubeIndex1);
            List<List<Integer>> cubes = getCubesToCompare(cubeIndex1, granularity);
            for (List<Integer> cubeIndex2 : cubes) {
                Hypercube h2 = target.getCube(cubeIndex2);
                // only run if the hypercube actually exists
                if (h2 != null) {
                    for (Point a : h1.elements) {
                        for (Point b : h2.elements) {
                            d = computeDistance(a, b);
                            if (d <= threshold) {
                                mapping.add(a.label, b.label, d);
                            }
                        }
                    }
                }
            }
        }
    }

    public void deduplicate(Set<Point> data, Mapping mapping) {
        Index index = assignCubes(data);
        System.out.println("|source cubes|= " + index.cubes.keySet().size());

        float d;

        try {
            for (List<Integer> cubeIndex1 : index.cubes.keySet()) {
                Hypercube h1 = index.getCube(cubeIndex1);
                List<List<Integer>> cubes = getCubesToCompare(cubeIndex1, granularity);
                for (List<Integer> cubeIndex2 : cubes) {
                    Hypercube h2 = index.getCube(cubeIndex2);
                    if (h2 != null) {
                        int cmp = compareCubes(cubeIndex1, cubeIndex2);
                        if (cmp < 0) {
                            for (Point a : h1.elements) {
                                for (Point b : h2.elements) {
                                    d = computeDistance(a, b);
                                    if (d <= threshold) {
                                        mapping.addSingleSource(a.label, b.label, d);
                                    }
                                }
                            }
                        } else if (cmp == 0) {
                            List<Point> points = new ArrayList<Point>(h1.elements);
                            for (int i = 0; i < points.size() - 1; i++) {
                                Point p1 = points.get(i);
                                for (int j = i + 1; j < points.size(); j++) {
                                    Point p2 = points.get(j);
                                    d = computeDistance(p1, p2);
                                    if (d <= threshold) {
                                        mapping.addSingleSource(p1.label, p2.label, d);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int compareCubes(final List<Integer> cubeId1, final List<Integer> cubeId2) {
        for (int i = 0; i < cubeId1.size(); i++) {
            int index1 = cubeId1.get(i);
            int index2 = cubeId2.get(i);

            if (index1 != index2) {
                return index1 - index2;
            }
        }

        return 0;
    }

    public void runBruteForce(Set<Point> source, Mapping mapping) {
        List<Point> points = new ArrayList<Point>(source);
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                Point p2 = points.get(j);
                float d = computeDistance(p1, p2);
                if (d <= threshold) {
                    mapping.addSingleSource(p1.label, p2.label, d);
                }
            }
        }
    }

    public void runBruteForce(Set<Point> source, Set<Point> target, Mapping mapping) {
        for (Point a : source) {
            for (Point b : target) {
                float d = computeDistance(a, b);
                if (d <= threshold) {
                    mapping.add(a.label, b.label, d);
                }
            }
        }
    }
    
}
