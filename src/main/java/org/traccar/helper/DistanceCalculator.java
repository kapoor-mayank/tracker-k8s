package org.traccar.helper;


public final class DistanceCalculator {
    private static final double EQUATORIAL_EARTH_RADIUS = 6378.137D;
    private static final double DEG_TO_RAD = 0.017453292519943295D;

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double dlong = (lon2 - lon1) * 0.017453292519943295D;
        double dlat = (lat2 - lat1) * 0.017453292519943295D;

        double a = Math.pow(Math.sin(dlat / 2.0D), 2.0D) + Math.cos(lat1 * 0.017453292519943295D) * Math.cos(lat2 * 0.017453292519943295D) * Math.pow(Math.sin(dlong / 2.0D), 2.0D);
        double c = 2.0D * Math.atan2(Math.sqrt(a), Math.sqrt(1.0D - a));
        double d = 6378.137D * c;
        return d * 1000.0D;
    }


    public static double distanceToLine(double pointLat, double pointLon, double lat1, double lon1, double lat2, double lon2) {
        double d0 = distance(pointLat, pointLon, lat1, lon1);
        double d1 = distance(lat1, lon1, lat2, lon2);
        double d2 = distance(lat2, lon2, pointLat, pointLon);
        if (Math.pow(d0, 2.0D) > Math.pow(d1, 2.0D) + Math.pow(d2, 2.0D)) {
            return d2;
        }
        if (Math.pow(d2, 2.0D) > Math.pow(d1, 2.0D) + Math.pow(d0, 2.0D)) {
            return d0;
        }
        double halfP = (d0 + d1 + d2) * 0.5D;
        double area = Math.sqrt(halfP * (halfP - d0) * (halfP - d1) * (halfP - d2));
        return 2.0D * area / d1;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\DistanceCalculator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */