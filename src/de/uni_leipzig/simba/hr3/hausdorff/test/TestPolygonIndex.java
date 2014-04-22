/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff.test;

import de.uni_leipzig.simba.hr3.MainMemoryMapping;
import de.uni_leipzig.simba.hr3.Mapping;
import de.uni_leipzig.simba.hr3.Point;
import de.uni_leipzig.simba.hr3.hausdorff.IndexedHausdorff;
import de.uni_leipzig.simba.hr3.hausdorff.Hausdorff;
import de.uni_leipzig.simba.hr3.hausdorff.Polygon;
import de.uni_leipzig.simba.hr3.Util;
import de.uni_leipzig.simba.hr3.hausdorff.*;
import de.uni_leipzig.simba.hr3.hausdorff.HausdorffFactory.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public class TestPolygonIndex {

    public static long testHausdorff(HausdorffFactory.Type type, Set<Polygon> source, Set<Polygon> target, int size, boolean deduplication, int iterations, float threshold) {

        //create hausdorff
        Hausdorff h = HausdorffFactory.getHausdorff(type);
        MainMemoryMapping result = new MainMemoryMapping();
        long begin, end, min = Long.MAX_VALUE;
        for (int i = 0; i < iterations; i++) {
            begin = System.currentTimeMillis();
            result = (MainMemoryMapping) h.run(source, target, threshold);
            end = System.currentTimeMillis();
//            System.out.println("Iteration "+i);
            min = Math.min((end - begin), min);
        }
        System.out.print(result.getNumberofMappings() + " mappings.");
        System.out.print(h.getComputations() + " computations.\t");
        if (type.equals(Type.INDEXED)) {
            System.out.print(((IndexedHausdorff) h).targetIndex.computations + "index computations.\t");
        }
        else if (type.equals(Type.CENTROID)) {
            System.out.print(((CentroidIndexedHausdorff) h).targetIndex.computations + ((CentroidIndexedHausdorff) h).sourceIndex.computations +"index computations.\t");
        }
        else if (type.equals(Type.SCAN)) {
            System.out.print(((ScanIndexedHausdorff) h).targetIndex.computations + ((ScanIndexedHausdorff) h).sourceIndex.computations +"index computations.\t");
        }
        System.out.println("Total runtime: " + min);
        return min;
    }

    public static List<Set<Polygon>> getData(String dataset, boolean deduplication, int size) {
        Set<Polygon> data = PolygonReader.readPolygons(dataset);
        Set<Polygon> source, target;
        if (deduplication) {
            if (size > 0) {
                source = Util.getSubset(data, 0, size);
                target = source;
            } else {
                source = data;
                target = data;
            }
        } else {
            if (size > 0 && data.size() >= 2 * size) {
                source = Util.getSubset(data, 0, size);
                target = Util.getSubset(data, 0, 2 * size);
                target.removeAll(source);
            } else {
                source = data;
                target = data;
            }
        }
        List<Set<Polygon>> result = new ArrayList<Set<Polygon>>();
        result.add(source);
        result.add(target);
        return result;
    }

    public static long testGeoHR3(Set<Polygon> source, Set<Polygon> target,
            HausdorffFactory.Type type, boolean HR3, int iterations,
            int granularity, float threshold) {

        //create hausdorff      
        Mapping result2 = new Mapping.NoOpMapping();
        GeoHR3 gh = new GeoHR3(threshold, granularity, type);


        long begin, end, min = Long.MAX_VALUE;
        for (int i = 0; i < iterations; i++) {
            gh = new GeoHR3(threshold, granularity, type);
            gh.HR3 = HR3;
            begin = System.currentTimeMillis();
            result2 = gh.run(source, target);
            end = System.currentTimeMillis();
            min = Math.min((end - begin), min);
        }
        try {
            System.out.print(result2.size() + " mappings\t");
        } catch (Exception e) {
            System.out.print("No size for mappings\t");
        }
        System.out.print(gh.hausdorff.getComputations() + " computations\t");
        if(gh.hausdorff instanceof CentroidIndexedHausdorff)
        System.out.print("Index computations: "+2*((IndexedHausdorff)gh.hausdorff).targetIndex.computations+"\t");
        else if(gh.hausdorff instanceof IndexedHausdorff)
        System.out.print("Index computations: "+((IndexedHausdorff)gh.hausdorff).targetIndex.computations+"\t");    
        System.out.println(min + " ms");
        return min;
    }

    public static long testGeoHR3()
    {
        Set<Polygon> p = PolygonReader.readPolygons("E:/Work/Papers/Eigene/2013/ISWC_GeoHr3/data/datasets/nuts_geometry.csv");
        return testGeoHR3(p, p, Type.INDEXED, false, 10, 1, 0.1f);
    }
    public static void testHausdorff() {

        int size = 5000;
//        
//        Set<Polygon> data = PolygonReader.readPolygons("E:/Work/Papers/Eigene/2013/ISWC_GeoHr3/data/farmers_market.csv");
        Set<Polygon> data = PolygonReader.readPolygons("E:/Work/Papers/Eigene/2013/ISWC_GeoHr3/data/dbpedia_geometry.txt");
        int granularity = 1;
        Set<Polygon> source = Util.getSubset(data, 0, size);
        Set<Polygon> target = Util.getSubset(data, 0, 2 * size);
        target.removeAll(source);
//        source = data;
//        target = data;
        int iterations = 0;

        System.out.println("Data: ");
        Util.getStats(data);
        System.out.println("Source: ");
        Util.getStats(source);
        System.out.println("Target: ");
        Util.getStats(target);

        System.out.println(source.size() + " source objects.");
        System.out.println(target.size() + " target objects.");

        float threshold = 0.01f;
        Hausdorff h;
        long begin, end;
        MainMemoryMapping result;
        for (int i = 0; i < iterations; i++) {
            h = new FastHausdorff();
            begin = System.currentTimeMillis();
            result = (MainMemoryMapping) h.run(source, target, threshold);
            end = System.currentTimeMillis();
            System.out.println("Fast: " + (end - begin) + " ms, " + result.getNumberofMappings() + " results.");
            System.out.println(h.getComputations() + " distance computations");
        }

//      
        for (int i = 0; i < iterations; i++) {

            h = new NaiveHausdorff();
            begin = System.currentTimeMillis();
            result = (MainMemoryMapping) h.run(source, target, threshold);
            end = System.currentTimeMillis();
            System.out.println("Naive: " + (end - begin) + " ms, " + result.getNumberofMappings() + " results.");
            System.out.println(h.getComputations() + " distance computations");
        }
        for (int i = 0; i < iterations; i++) {

            h = new IndexedHausdorff();
            begin = System.currentTimeMillis();
            result = (MainMemoryMapping) h.run(source, target, threshold);
            end = System.currentTimeMillis();
            System.out.println("Indexed: " + (end - begin) + " ms, " + result.getNumberofMappings() + " results.");
            System.out.println(h.getComputations() + " distance computations");
        }

//        System.exit(1);
//        try {
//            System.out.println(result.size() + " results.");
//            System.out.println(result + " results.");            
//        } catch (IOException ex) {
//            Logger.getLogger(TestPolygonIndex.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
        for (int i = 0; i < 10; i++) {
            MainMemoryMapping result2;
            GeoHR3 gh;

            gh = new GeoHR3(threshold, granularity, HausdorffFactory.Type.FAST);
            gh.HR3 = true;
            begin = System.currentTimeMillis();
            result2 = (MainMemoryMapping) gh.run(source, target);
            end = System.currentTimeMillis();
            System.out.println("GeoHr3-Hr3-Fast: " + (end - begin) + " ms, " + result2.getNumberofMappings() + " results.");

//        
            gh = new GeoHR3(threshold, granularity, HausdorffFactory.Type.FAST);
            gh.HR3 = false;
            begin = System.currentTimeMillis();
            result2 = (MainMemoryMapping) gh.run(source, target);
            end = System.currentTimeMillis();
            System.out.println("GeoHr3-NoHr3-Fast: " + (end - begin) + " ms, " + result2.getNumberofMappings() + " results.");
////
            gh = new GeoHR3(threshold, granularity, HausdorffFactory.Type.NAIVE);
            gh.HR3 = true;
            begin = System.currentTimeMillis();
            result2 = (MainMemoryMapping) gh.run(source, target);
            end = System.currentTimeMillis();
            System.out.println("GeoHr3-Hr3-Naive: " + (end - begin) + " ms, " + result2.getNumberofMappings() + " results.");
////               
            gh = new GeoHR3(threshold, granularity, HausdorffFactory.Type.NAIVE);
            gh.HR3 = false;
            begin = System.currentTimeMillis();
            result2 = (MainMemoryMapping) gh.run(source, target);
            end = System.currentTimeMillis();
            System.out.println("GeoHr3-NoHr3-Naive: " + (end - begin) + " ms, " + result2.getNumberofMappings() + " results.");
            //////                        
            gh = new GeoHR3(threshold, granularity, HausdorffFactory.Type.INDEXED);
            gh.HR3 = false;
            begin = System.currentTimeMillis();
            result = (MainMemoryMapping) gh.run(source, target);
            end = System.currentTimeMillis();
            System.out.println("GeoHr3-NoHr3-Indexed: " + (end - begin) + " ms, " + result2.getNumberofMappings() + " results.");

////        
////        
////        gh = new GeoHR3(threshold, granularity, "fast");
////        gh.HR3 = true;
////        begin = System.currentTimeMillis();        
////        result = gh.run(source, target);
////        end = System.currentTimeMillis();
////        System.out.println("GeoHr3-WithHr3-Fast: " + (end - begin) + " ms");
////        
////        
////        gh = new GeoHR3(threshold, granularity, "Indexed");
////        gh.HR3 = true;
////        begin = System.currentTimeMillis();        
////        result = gh.run(source, target);
////        end = System.currentTimeMillis();
////        System.out.println("GeoHr3-WithHr3-Indexed: " + (end - begin) + " ms");
////            System.out.println(result);

            for (String s : result.map.keySet()) {
                for (String t : result.map.get(s).keySet()) {
                    if (!result2.contains(s, t)) {
                        System.out.println("Indexed vs. Fast:\t" + s + ", " + t + ", " + result.getSimilarity(s, t));
                    }
                }
            }

            for (String s : result2.map.keySet()) {
                for (String t : result2.map.get(s).keySet()) {
                    if (!result.contains(s, t)) {
                        System.out.println("Fast vs. Indexed:\t" + s + ", " + t + ", " + result.getSimilarity(s, t));
                    }
                }
            }
        }
    }

    public static void testPolygonIndex() {
        Point a1 = new Point("a1", Arrays.asList(new Float[]{0f, 0f}));
        Point b1 = new Point("b1", Arrays.asList(new Float[]{0f, 1f}));
        Point c1 = new Point("c1", Arrays.asList(new Float[]{1f, 0f}));
        Polygon A = new Polygon("A", Arrays.asList(new Point[]{a1, b1, c1}));

        Point a2 = new Point("a2", Arrays.asList(new Float[]{0f, 0f}));
        Point b2 = new Point("b2", Arrays.asList(new Float[]{0f, 1f}));
        Point c2 = new Point("c2", Arrays.asList(new Float[]{-1f, 0f}));
        Polygon B = new Polygon("B", Arrays.asList(new Point[]{a2, b2, c2}));

        Hausdorff h = new CentroidIndexedHausdorff();
//        System.out.println(h.computeDistance(A, B, 100));
        Set<Polygon> source = new HashSet<Polygon>(Arrays.asList(new Polygon[]{A, B}));
        System.out.println("Centroid results:" + h.run(source, source, 120f));

        IndexedHausdorff fh = new IndexedHausdorff();
        System.out.println("Reference results:" + fh.run(source, source, 120f));
        System.exit(0);
    }

    public void usage() {
        System.out.println("1 - Dataset");
        System.out.println("2 - Source and target size");
        System.out.println("3 - Deduplication (t, f)");
        System.out.println("4 - # Iterations");
        System.out.println("5 - Granularity");
        System.out.println("6 - Threshold in km");
        System.out.println("7 - Experiments (distance, hr3)");
    }

    public static void testCentroidIndexedHausdorff() {
        Set<Polygon> p = PolygonReader.readPolygons("E:/Work/Papers/Eigene/2013/ISWC_GeoHr3/data/datasets/nuts_geometry.csv");
        IndexedHausdorff ih = new IndexedHausdorff();
        MainMemoryMapping m = new MainMemoryMapping(), m2 = new MainMemoryMapping();

        long begin, end;
        for (int i = 0; i < 1; i++) {
            begin = System.currentTimeMillis();
            m = (MainMemoryMapping) ih.run(p, p, 0.1f);
            end = System.currentTimeMillis();
            System.out.println("Indexed\t" + ((MainMemoryMapping) m).getNumberofMappings() + " mappings\t" + (end - begin) + "ms");
        }

        for (int i = 0; i < 1; i++) {

            ih = new CentroidIndexedHausdorff();
            begin = System.currentTimeMillis();
            m2 = (MainMemoryMapping) ih.run(p, p, 0.1f);
            end = System.currentTimeMillis();
            System.out.println("Centroid Indexed\t" + ((MainMemoryMapping) m2).getNumberofMappings() + " mappings\t" + (end - begin) + "ms");
        }

        for (int i = 0; i < 1; i++) {
            ih = new ScanIndexedHausdorff();
            begin = System.currentTimeMillis();
            m2 = (MainMemoryMapping) ih.run(p, p, 0.1f);
            end = System.currentTimeMillis();
            System.out.println("Scan dIndexed\t" + ((MainMemoryMapping) m2).getNumberofMappings() + " mappings\t" + (end - begin) + "ms");
        }
        for (String s : m.map.keySet()) {
            for (String t : m.map.get(s).keySet()) {
                if (!m2.contains(s, t)) {
                    System.out.println(s + "\t" + t + "\t" + NaiveHausdorff.distance(ih.targetIndex.polygonIndex.get(t), ih.targetIndex.polygonIndex.get(s), 0));
//                    System.out.print(s+"\t"+ih.targetIndex.polygonIndex.get(s).points);
//                    System.out.println("\t"+t+"\t"+ih.targetIndex.polygonIndex.get(t).points);
                }
            }
        }
        System.exit(0);
    }

    public static void main(String args[]) {
//        testGeoHR3();
//        System.exit(1);
//        testCentroidIndexedHausdorff();
        //        Set<Polygon> p = PolygonReader.readPolygons(args[0]);
        //        Util.getStats(p);
        //        System.exit(1);
        //        p = PolygonReader.readPolygons("E:/Work/Papers/Eigene/2013/ISWC_GeoHr3/data/dbpedia_geometry.txt");
        //        Util.getStats(p);
        if (args.length == 0) {
            System.out.println("Running default experiments");
            runExperiments(null);
        } else if (args.length >= 1) {
            String data = args[0];
            int size = Integer.parseInt(args[1]);
            boolean deduplication;
            if (args[2].toLowerCase().startsWith("t")) {
                deduplication = true;
            } else {
                deduplication = false;
            }
            int iterations = Integer.parseInt(args[3]);
            int granularity = Integer.parseInt(args[4]);
            float threshold = Float.parseFloat(args[5]);
            if (args[6].toLowerCase().startsWith("h")) {
                runExperiments(data, iterations, size, deduplication, threshold, granularity);
            } else if (args[6].toLowerCase().startsWith("n")) {
                System.out.println("=Indexed experiments=");

                runIndexedExperiments(data, iterations, size, deduplication, threshold, granularity);
            } else {
                runHausdorffExperiments(data, iterations, size, deduplication, threshold, granularity);
            }
        }
    }

    public static void runExperiments(String data) {
        int iterations = 2;
        if (data == null) {
            data = "E:/Work/Papers/Eigene/2013/ISWC_GeoHr3/data/nuts_geometry.csv";
        }
        int size = 10000;
        boolean deduplication = true;
        float threshold = 0.1f;
        int granularity = 4;
        boolean HR3 = true;
        List<Set<Polygon>> dataset = getData(data, deduplication, size);
        Util.getStats(dataset.get(0));
//    System.out.print("Naive: "); testHausdorff(HausdorffFactory.Type.NAIVE, dataset.get(0), dataset.get(1), size, deduplication, iterations, threshold);
//    System.out.print("Fast: "); testHausdorff(HausdorffFactory.Type.FAST, dataset.get(0), dataset.get(1), size, deduplication, iterations, threshold);
        System.out.print("Indexed: ");
        testHausdorff(HausdorffFactory.Type.INDEXED, dataset.get(0), dataset.get(1), size, deduplication, iterations, threshold);

//        List<Set<Polygon>> dataset = getData(data, deduplication, size);
//        System.out.println("Naive-HR3: " + testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.NAIVE, HR3, iterations, granularity, threshold));
//        System.out.println("Naive-NoHR3: " + testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.NAIVE, !HR3, iterations, granularity, threshold));
//        System.out.println("Fast-HR3: " + testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.FAST, HR3, iterations, granularity, threshold));
//        System.out.println("Fast-NoHR3: " + testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.FAST, !HR3, iterations, granularity, threshold));
//        System.out.println("Indexed-HR3: " + testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.INDEXED, HR3, iterations, granularity, threshold));
//        System.out.println("Indexed-NoHR3: " + testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.INDEXED, !HR3, iterations, granularity, threshold));
    }

    public static void runExperiments(String data, int iterations, int size, boolean deduplication, float threshold, int granularity) {
        if (data == null) {
            data = "E:/Work/Papers/Eigene/2013/ISWC_GeoHr3/data/dbpedia_geometry.txt";
        }
        boolean HR3 = true;
//        System.out.println("Naive: " + testHausdorff(HausdorffFactory.Type.NAIVE, data, size, deduplication, iterations, threshold));
//        System.out.println("Fast: " + testHausdorff(HausdorffFactory.Type.FAST, data, size, deduplication, iterations, threshold));
//        System.out.println("Indexed: " + testHausdorff(HausdorffFactory.Type.INDEXED, data, size, deduplication, iterations, threshold));
        List<Set<Polygon>> dataset = getData(data, deduplication, size);

        System.out.println("===================");
        Util.getStats(dataset.get(0));
        Util.getStats(dataset.get(1));
        System.out.print("Naive-HR3\t");
        testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.NAIVE, HR3, iterations, granularity, threshold);
        System.out.print("Naive-NoHR3\t");
        testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.NAIVE, !HR3, iterations, granularity, threshold);
        System.out.print("Bound-HR3\t");
        testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.FAST, HR3, iterations, granularity, threshold);
        System.out.print("Bound-NoHR3\t");
        testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.FAST, !HR3, iterations, granularity, threshold);
        System.out.print("Indexed-HR3\t");
        testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.INDEXED, HR3, iterations, granularity, threshold);
        System.out.print("Indexed-NoHR3\t");
        testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.INDEXED, !HR3, iterations, granularity, threshold);
        System.out.print("Centroid-HR3\t");
        testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.CENTROID, HR3, iterations, granularity, threshold);
        System.out.print("Centroid-NoHR3\t");
        testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.CENTROID, !HR3, iterations, granularity, threshold);
    }

    public static void runIndexedExperiments(String data, int iterations, int size, boolean deduplication, float threshold, int granularity) {
        if (data == null) {
            data = "E:/Work/Papers/Eigene/2013/ISWC_GeoHr3/data/dbpedia_geometry.txt";
        }
        System.out.println("=Running indexed exp.=");

        boolean HR3 = true;
//        System.out.println("Naive: " + testHausdorff(HausdorffFactory.Type.NAIVE, data, size, deduplication, iterations, threshold));
//        System.out.println("Fast: " + testHausdorff(HausdorffFactory.Type.FAST, data, size, deduplication, iterations, threshold));
//        System.out.println("Indexed: " + testHausdorff(HausdorffFactory.Type.INDEXED, data, size, deduplication, iterations, threshold));
        List<Set<Polygon>> dataset = getData(data, deduplication, size);

        System.out.println("===================");
        Util.getStats(dataset.get(0));
        Util.getStats(dataset.get(1));
//        System.out.print("Indexed-HR3\t");
//        testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.INDEXED, HR3, iterations, granularity, threshold);
        System.out.print("Indexed-NoHR3\t");
        testGeoHR3(dataset.get(0), dataset.get(1), HausdorffFactory.Type.INDEXED, !HR3, iterations, granularity, threshold);
    }

    public static void runHausdorffExperiments(String data, int iterations, int size, boolean deduplication, float threshold, int granularity) {
        List<Set<Polygon>> dataset = getData(data, deduplication, size);

        System.out.println("===================");
        Util.getStats(dataset.get(0));
        Util.getStats(dataset.get(1));
        System.out.print("Naive: ");
        testHausdorff(HausdorffFactory.Type.NAIVE, dataset.get(0), dataset.get(1), size, deduplication, iterations, threshold);
        System.out.print("Fast: ");
        testHausdorff(HausdorffFactory.Type.FAST, dataset.get(0), dataset.get(1), size, deduplication, iterations, threshold);
        System.out.print("Indexed: ");
        testHausdorff(HausdorffFactory.Type.INDEXED, dataset.get(0), dataset.get(1), size, deduplication, iterations, threshold);
        System.out.print("Centroid: ");
        testHausdorff(HausdorffFactory.Type.CENTROID, dataset.get(0), dataset.get(1), size, deduplication, iterations, threshold);
        System.out.print("Scan: ");
        testHausdorff(HausdorffFactory.Type.SCAN, dataset.get(0), dataset.get(1), size, deduplication, iterations, threshold);

    }
}
