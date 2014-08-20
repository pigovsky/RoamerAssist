package com.pigovsky.roamerassist.helpers;

import android.util.Log;

import com.nutiteq.components.MapPos;
import com.nutiteq.geometry.Marker;
import com.nutiteq.projections.Projection;
import com.nutiteq.style.MarkerStyle;
import com.nutiteq.style.StyleSet;
import com.nutiteq.ui.DefaultLabel;

import java.util.ArrayList;
import java.util.List;

/**
 * Helps process route gps points
 *
 * @author Yuriy Pigovsky
 * @version 1.01 2014/08/02
 */
public class RoutePointsHelper {
    /**
     * Size of image, which is put on marks
     */
    public static final int MARK_DRAWING_SIZE = 10;
    private static final String TAG = RoutePointsHelper.class.getSimpleName();

    /**
     * <p>This routine calculates the distance between two points (given the
     * latitude/longitude of those points). It is being used to calculate
     * the distance between two locations.</p>
     * <p/>
     * <p>Definitions: South latitudes are negative, east longitudes are positive</p>
     * <p/>
     * <p>Passed to function:
     * <ul>
     * <li>lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees)</li>
     * <li>lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees)</li>
     * <li>unit = the unit you desire for results
     * <ul>
     * <li>where: 'M' is statute miles</li>
     * <li>'K' is kilometers (default) </li>
     * <li>'N' is nautical miles</li>
     * </ul>
     * </li>
     * </ul>
     * Worldwide cities and other features databases with latitude longitude
     * are available at http://www.geodatasource.com</p>
     * <p/>
     * <p>For enquiries, please contact sales@geodatasource.com</p>
     * <p>Official Web site: http://www.geodatasource.com</p>
     * <p>GeoDataSource.com (C) All Rights Reserved 2013</p>
     *
     * @param lat1 - latitude point 1
     * @param lon1 - longitude point 1
     * @param lat2 - latitude point 2
     * @param lon2 - longitude point 2
     * @param unit - unit of measure (M, K, N)
     * @return the distance between the two points
     */
    public static final double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    /**
     * <p>This function converts decimal degrees to radians.</p>
     *
     * @param deg - the decimal to convert to radians
     * @return the decimal converted to radians
     */
    private static final double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /**
     * <p>This function converts radians to decimal degrees.</p>
     *
     * @param rad - the radian to convert
     * @return the radian converted to decimal degrees
     */
    private static final double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    /**
     * Takes list of route points and measures distance from the route start to each point
     * in this list.
     *
     * @param routePoints list of route points to compute distances from
     * @return array of doubles, which contain distances from begin of the route to its every point
     * @throws NullPointerException if <code>routePoints</code> is null
     */
    public static double[] computeDistanceToPoints(List<Point> routePoints) {
        Point previousPoint = routePoints.get(0);
        Point currentPoint;


        double routeLength = 0d;
        double[] distanceToPoints = new double[routePoints.size()];

        for (int i = 1; i < routePoints.size(); i++, previousPoint = currentPoint) {
            currentPoint = routePoints.get(i);

            double distanceFromPreviousToCurrent =
                    previousPoint.distance(currentPoint);

            routeLength += distanceFromPreviousToCurrent;
            distanceToPoints[i] = routeLength;
        }

        String message = "distanceToPoints = {\n";
        for (double d : distanceToPoints) {
            message += "\t" + d + "\n";
        }
        Log.d(TAG, message + "}");

        return distanceToPoints;
    }

    /**
     * Puts annotations with image <code>imagePath</code> on even distances <code>distanceToPutMark</code>
     * throughout route and returns them in an array.
     *
     * @param routePoints       route described by a list of its gps points
     * @param distanceToPutMark distance between subsequent marks
     * @param markerStyleSet    marker's style
     * @return array of calculated annotations
     */
    public static Marker[] calculateMarksOnRegularDistances(List<MapPos> routePoints, double[] distanceToPoints,
                                                            double distanceToPutMark, StyleSet<MarkerStyle> markerStyleSet) {
        if (routePoints == null) {
            return new Marker[0];
        }

        int k = 1;
        double previousToCurrent = distanceToPoints[1];

        int numberOfMarks = (int) Math.floor(distanceToPoints[distanceToPoints.length - 1] / distanceToPutMark);
        Marker[] markers = new Marker[numberOfMarks];
        for (int i = 1; i <= numberOfMarks; ++i) {
            double markPosition = i * distanceToPutMark;
            while (distanceToPoints[k] < markPosition) {
                k++;
                previousToCurrent = distanceToPoints[k] - distanceToPoints[k - 1];
            }
            double previousToMark = markPosition - distanceToPoints[k - 1];
            double alpha = previousToMark / previousToCurrent;

            MapPos markLocation = new MapPos(
                    routePoints.get(k - 1).x * (1d - alpha) + routePoints.get(k).x * alpha,
                    routePoints.get(k - 1).y * (1d - alpha) + routePoints.get(k).y * alpha
            );

            markers[i - 1] = new Marker(markLocation, new DefaultLabel(String.format("%d", i)),
                    markerStyleSet, null);
        }
        return markers;
    }

    static public List<Point> toPoints(Projection projection, List<MapPos> mapPoses) {
        List<Point> points = new ArrayList<Point>();
        for (MapPos mp : mapPoses) {
            points.add(new Point(projection.toWgs84(mp.x, mp.y)));
        }
        return points;
    }

    static public class Point {
        private double longitude;
        private double latitude;

        public Point() {
        }

        public Point(MapPos mapPos) {
            longitude = mapPos.x;
            latitude = mapPos.y;
        }

        public double distance(Point p2) {
            return 1000d *
                    RoutePointsHelper.distance(getLatitude(), getLongitude(), p2.getLatitude(), p2.getLongitude(), 'K');
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }
    }

}
