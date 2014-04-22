/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff;

import de.uni_leipzig.simba.hr3.MainMemoryMapping;
import de.uni_leipzig.simba.hr3.Point;
import java.util.*;

/**
 *
 * @author ngonga
 */
public class ScanIndexedHausdorff extends CentroidIndexedHausdorff {
    
    @Override
    public float computeDistance(Polygon s, Polygon t, float threshold) {
        MainMemoryMapping knownDistances = new MainMemoryMapping();
        Map<Integer, List<Integer>> toCompute = initToCompute(s, t, threshold, knownDistances);
//        if (verbose) {
//            System.out.println("Initial to compute: " + toCompute);
//            System.out.println("Known distances: " + knownDistances);
//        }
        boolean checkTermination;
//        int count = 0;
        while (!toCompute.isEmpty()) {
            checkTermination = checkTermination(s, knownDistances, toCompute, threshold);
            if (checkTermination) {
                return (threshold + 1);
            } else {
                toCompute = updateToCompute(s, t, threshold, knownDistances, toCompute);
            }
//            if (verbose) {
//                System.out.println(count + ". ToCompute: " + toCompute);
//                System.out.println(count + ". Known distances: " + knownDistances);
//                count++;
//            }
        }
        //approximated as much as we could. Have to go through known distances;

        for (int i = 0; i < s.points.size(); i++) {
            if (!knownDistances.map.containsKey(i + "")) {
                //means that all min distances for this point are larger than the threshold
                return threshold + 1;
            }
        }
        //if all points from s are in here then we can compute the real value
        float max = -1;
        float min;

        for (String sIdx : knownDistances.map.keySet()) {
            min = Float.MAX_VALUE;
            for (String tIdx : knownDistances.map.get(sIdx).keySet()) {
                min = Math.min(min, (float) knownDistances.getSimilarity(sIdx, tIdx));
            }
            max = Math.max(max, min);
        }
        if (max == -1) {
            return threshold + 1;
        }
        return max;
    }

    /**
     * Checks whether the map contains the pair (source, target)
     *
     * @param map
     * @param source
     * @param target
     * @return
     */
    public boolean contains(Map<Integer, List<Integer>> map, int source, int target) {
        if (!map.containsKey(source)) {
            return false;
        } else if (!map.get(source).contains(target)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks whether it is known for the source point with index sourceIndex
     * that all distances to points in p are above the threshold
     *
     * @param sourceIndex
     * @param knownDistances
     * @param toCompute
     * @param threshold
     * @return -1 if it is unknown, the known distance if it is known and
     * (threshold + 1) if the distance is known to be beyond the threshold
     */
    public float getCurrentApproximation(int sourceIndex, MainMemoryMapping knownDistances,
            Map<Integer, List<Integer>> toCompute, float threshold) {
        if (toCompute.containsKey(sourceIndex)) {
            return -1f; //distance is unknown
        } else {
            if (knownDistances.map.containsKey(sourceIndex + "")) {
                HashMap<String, Double> distances = knownDistances.map.get(sourceIndex + "");
                float d, min = Float.MAX_VALUE;
                for (String key : distances.keySet()) {
                    d = distances.get(key).floatValue();
                    min = Math.min(d, min);
                }
                return min;
            } else {
                return threshold + 1f;
            }
        }
    }

    /**
     * Checks whether a distance computation should be terminated
     *
     * @param s Source polygon
     * @param knownDistances
     * @param toCompute
     * @param threshold
     * @return true if computation should be terminated
     */
    public boolean checkTermination(Polygon s, MainMemoryMapping knownDistances, Map<Integer, List<Integer>> toCompute, float threshold) {
        for (int i = 0; i < s.points.size(); i++) {
            if (getCurrentApproximation(i, knownDistances, toCompute, threshold) > threshold) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns distances that are still to be computed
     *
     * @param s
     * @param t
     * @param threshold
     * @param sourceReference
     * @param targetReference
     * @param toCompute
     * @return
     */
    public Map<Integer, List<Integer>> initToCompute(Polygon s, Polygon t, float threshold, MainMemoryMapping knownDistances) {
        //1. compute first distance
        Map<Integer, List<Integer>> toCompute = new HashMap<Integer, List<Integer>>();
        float approx, d = distance(s.points.get(0), t.points.get(0));
        if (d <= threshold) {
            knownDistances.add(0 + "", 0 + "", d);
        }
        //2. approximate distance from s0 to all other points
        for (int j = 1; j < t.points.size(); j++) {
            approx = d - targetIndex.getDistance(t.uri, t.points.get(0), t.points.get(j));
            //do not compute values larger than the threshold
            if (approx <= threshold) {
                if (!toCompute.containsKey(0)) {
                    toCompute.put(0, new ArrayList<Integer>());
                }
                toCompute.get(0).add(j);
            }
        }

        //3. Repeat 2. for t0, i.e., approximate distance from t0 to all other points
        for (int i = 1; i < s.points.size(); i++) {
            approx = d - targetIndex.getDistance(s.uri, s.points.get(0), s.points.get(i));
            if (approx <= threshold) {
                //remove from toCompute if in there
                if (!toCompute.containsKey(i)) {
                    toCompute.put(i, new ArrayList<Integer>());
                }
                toCompute.get(i).add(0);
            }
        }

        //4. now approximate distance from s1 ... sn to t1 ... tm
        for (int i = 1; i < s.points.size(); i++) {
            approx = d - (sourceIndex.getDistance(s.uri, s.points.get(0), s.points.get(i)));
            for (int j = 1; j < t.points.size(); j++) {
                if (approx - targetIndex.getDistance(t.uri, t.points.get(0), t.points.get(j)) <= threshold) {
                    if (!toCompute.containsKey(i)) {
                        toCompute.put(i, new ArrayList<Integer>());
                    }
                    toCompute.get(i).add(j);
                }
            }
        }
        return toCompute;
    }

    /**
     * Returns distances that are still to be computed
     *
     * @param s
     * @param t
     * @param threshold
     * @param sourceReference
     * @param targetReference
     * @param toCompute
     * @return
     */
    public Map<Integer, List<Integer>> updateToCompute(Polygon s, Polygon t, float threshold, MainMemoryMapping knownDistances,
            Map<Integer, List<Integer>> toCompute) {
        //1. compute first distance
        int sIndex, tIndex;
        Map.Entry<Integer, List<Integer>> entries = toCompute.entrySet().iterator().next();
        sIndex = entries.getKey();
        tIndex = entries.getValue().get(0);
        float approx, d = distance(s.points.get(sIndex), t.points.get(tIndex));
//        if (verbose) {
//            System.out.println("d(" + sIndex + ", " + tIndex + ") = " + d+" while threshold = "+threshold);
//        }
        if (d <= threshold) {
            knownDistances.add(sIndex + "", tIndex + "", d);
//            if (verbose) {
//                System.out.println("Adding d(" + sIndex + ", " + tIndex + ") to known distances");
//            }
        }

        toCompute.get(sIndex).remove(0);
        if (toCompute.get(sIndex).isEmpty()) {
            toCompute.remove(sIndex);
        }

        List<List<Integer>> toDelete = new ArrayList<List<Integer>>();
        // compute appoximations based on toCompute list
        for (int sIdx : toCompute.keySet()) {
            for (int tIdx : toCompute.get(sIdx)) {
                if (sIdx == sIndex) {
                    approx = Math.abs(d - targetIndex.getDistance(t.uri, t.points.get(tIdx), t.points.get(tIndex)));
                } else if (tIdx == tIndex) {
                    approx = Math.abs(d - sourceIndex.getDistance(s.uri, s.points.get(sIdx), s.points.get(sIndex)));
                } else {
                    approx = d - targetIndex.getDistance(t.uri, t.points.get(tIdx), t.points.get(tIndex)) 
                            - sourceIndex.getDistance(s.uri, s.points.get(sIdx), s.points.get(sIndex));
                }
//                if (verbose) {
//                    System.out.println("approx d(A" + sIdx + ", B" + tIdx + ") = " + approx);
//                }

                if (approx > threshold) {
                    List<Integer> entry = new ArrayList<Integer>();
                    entry.add(sIdx);
                    entry.add(tIdx);
                    toDelete.add(entry);
                }
            }
        }
//        if (verbose) {
//            System.out.println("To delete :" + toDelete);
//        }
        for (List<Integer> entry : toDelete) {
            if (toCompute.containsKey(entry.get(0))) {
                toCompute.get(entry.get(0)).remove(entry.get(1));
                if (toCompute.get(entry.get(0)).isEmpty()) {
                    toCompute.remove(entry.get(0));
                }
            }
        }
        return toCompute;
    }

    public static void test() {
        Point a1 = new Point("a1", Arrays.asList(new Float[]{1f, 1f}));
        Point b1 = new Point("b1", Arrays.asList(new Float[]{1f, 2f}));
        Point c1 = new Point("c1", Arrays.asList(new Float[]{2f, 1f}));
        Polygon A = new Polygon("A", Arrays.asList(new Point[]{a1, b1, c1}));

        Point a2 = new Point("a2", Arrays.asList(new Float[]{3f, 1f}));
        Point b2 = new Point("b2", Arrays.asList(new Float[]{3f, 2f}));
        Point c2 = new Point("c2", Arrays.asList(new Float[]{2f, 2f}));
        Polygon B = new Polygon("B", Arrays.asList(new Point[]{a2, b2, c2}));

        Set<Polygon> testSet = new HashSet<Polygon>();
        testSet.add(A);
        testSet.add(B);

        ScanIndexedHausdorff c = new ScanIndexedHausdorff();
//        System.out.println(c.run(testSet, testSet, 0.1f));
    }

    public static void main(String args[]) {
        test();
    }
}
