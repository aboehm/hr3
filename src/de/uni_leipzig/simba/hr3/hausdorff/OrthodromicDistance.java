/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_leipzig.simba.hr3.hausdorff;

import de.uni_leipzig.simba.hr3.Point;

/**
 *
 * @author ngonga
 */
public class OrthodromicDistance {

    public static float R = 6378f;

    public static float getDistanceInDegrees(Point x, Point y) {
        return getDistanceInDegrees(x.coordinates.get(0), x.coordinates.get(1), y.coordinates.get(0), y.coordinates.get(1));
    }

    public static float getDistanceInDegrees(float lat1, float long1, float lat2, float long2) {
        float la1 = (float) Math.toRadians(lat1);
        float lo1 = (float) Math.toRadians(long1);
        float la2 = (float) Math.toRadians(lat2);
        float lo2 = (float) Math.toRadians(long2);
        return getDistance(la1, lo1, la2, lo2);
    }

    /**
     * Computes the distance between two points on earth
     *
     * @param lat1 Latitude of first point
     * @param long1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param long2 Longitude of second point
     * @return Distance between both points
     */
    public static float getDistance(float lat1, float long1, float lat2, float long2) {
        float dLat = lat2 - lat1;
        float dLon = long2 - long1;
        float sinLat = (float) Math.sin(dLat / 2);
        float sinLon = (float) Math.sin(dLon / 2);

        float a = (float) (sinLat * sinLat
                + sinLon * sinLon * Math.cos(lat1) * Math.cos(lat2));
        float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        return R * c;
    }

    public static void main(String args[]) {
        System.out.println(getDistanceInDegrees(16.9275f, 81.6736f, 16.92f, 81.67f));
    }
}
