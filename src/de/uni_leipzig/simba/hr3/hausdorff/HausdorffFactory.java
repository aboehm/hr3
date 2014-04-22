/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff;

/**
 * Generates a Hausdorff implementation
 * @author ngonga
 */
public class HausdorffFactory {
    
    public enum Type {NAIVE, INDEXED, FAST, CENTROID, SCAN}; 
    public static Hausdorff getHausdorff(Type type)
    {
        Hausdorff hausdorff;
        if (type == Type.NAIVE) {
            hausdorff = new NaiveHausdorff();
        } else if (type == Type.FAST) {
            hausdorff = new FastHausdorff();
        } else if (type == Type.INDEXED) {
            hausdorff = new IndexedHausdorff();
        } else if (type == Type.SCAN) {
            hausdorff = new ScanIndexedHausdorff();
        } else
        {
            hausdorff = new CentroidIndexedHausdorff();
        }
        return hausdorff;
    }
}
