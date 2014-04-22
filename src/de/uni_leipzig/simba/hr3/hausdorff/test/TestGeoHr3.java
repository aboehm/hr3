/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff.test;

import de.uni_leipzig.simba.hr3.Point;
import de.uni_leipzig.simba.hr3.hausdorff.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public class TestGeoHr3 {

    public static Set<Polygon> generateTestData() {
        Point A = new Point("A", Arrays.asList(new Float[]{80f, 20f}));
        Point B = new Point("B", Arrays.asList(new Float[]{20f, 80f}));
        Point C = new Point("C", Arrays.asList(new Float[]{20f, 170f}));
        Point D = new Point("D", Arrays.asList(new Float[]{20f, -170f}));
        Point E = new Point("E", Arrays.asList(new Float[]{-80f, 20f}));
        Point F = new Point("F", Arrays.asList(new Float[]{-50f, -80f}));
        Point G = new Point("G", Arrays.asList(new Float[]{10f, 70f}));

        Polygon P1 = new Polygon("PA", Arrays.asList(new Point[]{A}));
        Polygon P2 = new Polygon("PB", Arrays.asList(new Point[]{B}));
        Polygon P3 = new Polygon("PC", Arrays.asList(new Point[]{C}));
        Polygon P4 = new Polygon("PD", Arrays.asList(new Point[]{D}));
        Polygon P5 = new Polygon("PE", Arrays.asList(new Point[]{E}));
        Polygon P6 = new Polygon("PF", Arrays.asList(new Point[]{F}));
        Polygon P7 = new Polygon("PG", Arrays.asList(new Point[]{G}));

        Set<Polygon> result = new HashSet<Polygon>(Arrays.asList(new Polygon[]{P1, P2, P3, P4, P5, P6, P7}));
        return result;
//        Point A = new Point("A", Arrays.asList(new Float[]{80f, 20f}));
//        Point A = new Point("A", Arrays.asList(new Float[]{80f, 20f}));
//        Point A = new Point("A", Arrays.asList(new Float[]{80f, 20f}));
//        Point A = new Point("A", Arrays.asList(new Float[]{80f, 20f}));
    }

    public static void testIndex() {
        GeoHR3 gh = new GeoHR3(30, 1, HausdorffFactory.Type.FAST);
        GeoIndex index = gh.assignSquares(generateTestData());
        System.out.println(index);
        for (int i = -3; i < 3; i++) {
            for (int j = -6; j < 6; j++) {
                GeoSquare gs = index.getSquare(i, j);
                //System.out.println(i+", "+j);
                if (!gs.elements.isEmpty()) {
                    gh.HR3 = true;
                    System.out.println("(" + i + ", " + j + ") => " + gh.getSquaresToCompare(i, j, index));
                    gh.HR3 = false;
                    System.out.println("(" + i + ", " + j + ") => " + gh.getSquaresToCompare(i, j, index));
                }
            }
        }
    }

    public static void main(String args[]) {
        testIndex();
    }
}
