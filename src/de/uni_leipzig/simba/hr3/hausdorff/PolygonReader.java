/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff;

import de.uni_leipzig.simba.hr3.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 *
 * @author ngonga
 */
public class PolygonReader {

    public static boolean keepPolygons = true;

    public static Set<Polygon> readPolygons(String file) {
        return readPolygons(file, -1);
    }

    public static Set<Polygon> readPolygons(String file, int numberOfEntries) {
        Map<String, Polygon> result = new HashMap<String, Polygon>();
        String s, split[];
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            s = buf.readLine();
            while (s != null) {
                while (s.contains("  ")) {
                    s = s.replaceAll(Pattern.quote("  "), " ");
                }
                s = s.replaceAll(Pattern.quote(" "), "\t");
                split = s.split("\t");
                if (split.length % 2 != 1) {
                    System.err.println("Error: "+split.length+" => "+s);
                } else {
                    if (!result.containsKey(split[0])) {
                        result.put(split[0], new Polygon(split[0]));
                    }
                    //data is stored as long, lat
                    for (int i = 1; i < split.length; i = i + 2) {
                        result.get(split[0]).add(new Point("", Arrays.asList(new Float[]{Float.parseFloat(split[i+1]), Float.parseFloat(split[i])})));
                    }
                }
                if (result.keySet().size() == numberOfEntries) {
                    break;
                }
                s = buf.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set<Polygon> r = new HashSet<Polygon>();
        if (keepPolygons) {
            for (Polygon p : result.values()) {
//                if (p.points.size() > 2) {
                r.add(p);
//                }
            }
            return r;
        } else {
            return new HashSet<Polygon>(result.values());
        }
    }

    public static void main(String args[]) {
        Set<Polygon> result = readPolygons("E:/Work/Papers/Eigene/2013/ISWC_GeoHr3/data/example_lgd.txt");
        for (Polygon p: result)
        {
            System.out.println(p+"\t"+p.points.size());
        }
        
    }
}
