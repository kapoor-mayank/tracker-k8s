package org.traccar.helper;


public final class UnitsConverter {
    private static final double KNOTS_TO_KPH_RATIO = 0.539957D;
    private static final double KNOTS_TO_MPH_RATIO = 0.868976D;
    private static final double KNOTS_TO_MPS_RATIO = 1.94384D;
    private static final double KNOTS_TO_CPS_RATIO = 0.0194384449D;
    private static final double METERS_TO_FEET_RATIO = 0.3048D;
    private static final double METERS_TO_MILE_RATIO = 1609.34D;
    private static final long MILLISECONDS_TO_HOURS_RATIO = 3600000L;
    private static final long MILLISECONDS_TO_MINUTES_RATIO = 60000L;

    public static double knotsFromKph(double value) {
        return value * 0.539957D;
    }

    public static double kphFromKnots(double value) {
        return value / 0.539957D;
    }

    public static double knotsFromMph(double value) {
        return value * 0.868976D;
    }

    public static double mphFromKnots(double value) {
        return value / 0.868976D;
    }

    public static double knotsFromMps(double value) {
        return value * 1.94384D;
    }

    public static double mpsFromKnots(double value) {
        return value / 1.94384D;
    }

    public static double knotsFromCps(double value) {
        return value * 0.0194384449D;
    }

    public static double feetFromMeters(double value) {
        return value / 0.3048D;
    }

    public static double metersFromFeet(double value) {
        return value * 0.3048D;
    }

    public static double milesFromMeters(double value) {
        return value / 1609.34D;
    }

    public static double metersFromMiles(double value) {
        return value * 1609.34D;
    }

    public static long msFromHours(long value) {
        return value * 3600000L;
    }

    public static long msFromHours(double value) {
        return (long) (value * 3600000.0D);
    }

    public static long msFromMinutes(long value) {
        return value * 60000L;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\UnitsConverter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */