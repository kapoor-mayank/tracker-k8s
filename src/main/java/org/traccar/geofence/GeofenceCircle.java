package org.traccar.geofence;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.traccar.helper.DistanceCalculator;


public class GeofenceCircle
        extends GeofenceGeometry {
    private double centerLatitude;
    private double centerLongitude;
    private double radius;

    public GeofenceCircle() {
    }

    public GeofenceCircle(String wkt) throws ParseException {
        fromWkt(wkt);
    }

    public GeofenceCircle(double latitude, double longitude, double radius) {
        this.centerLatitude = latitude;
        this.centerLongitude = longitude;
        this.radius = radius;
    }

    public double distanceFromCenter(double latitude, double longitude) {
        return DistanceCalculator.distance(this.centerLatitude, this.centerLongitude, latitude, longitude);
    }


    public boolean containsPoint(double latitude, double longitude) {
        return (distanceFromCenter(latitude, longitude) <= this.radius);
    }


    public String toWkt() {
        String wkt = "";
        wkt = "CIRCLE (";
        wkt = wkt + String.valueOf(this.centerLatitude);
        wkt = wkt + " ";
        wkt = wkt + String.valueOf(this.centerLongitude);
        wkt = wkt + ", ";
        DecimalFormat format = new DecimalFormat("0.#");
        wkt = wkt + format.format(this.radius);
        wkt = wkt + ")";
        return wkt;
    }


    public void fromWkt(String wkt) throws ParseException {
        if (!wkt.startsWith("CIRCLE")) {
            throw new ParseException("Mismatch geometry type", 0);
        }
        String content = wkt.substring(wkt.indexOf("(") + 1, wkt.indexOf(")"));
        if (content == null || content.equals("")) {
            throw new ParseException("No content", 0);
        }
        String[] commaTokens = content.split(",");
        if (commaTokens.length != 2) {
            throw new ParseException("Not valid content", 0);
        }
        String[] tokens = commaTokens[0].split("\\s");
        if (tokens.length != 2) {
            throw new ParseException("Too much or less coordinates", 0);
        }
        try {
            this.centerLatitude = Double.parseDouble(tokens[0]);
        } catch (NumberFormatException e) {
            throw new ParseException(tokens[0] + " is not a double", 0);
        }
        try {
            this.centerLongitude = Double.parseDouble(tokens[1]);
        } catch (NumberFormatException e) {
            throw new ParseException(tokens[1] + " is not a double", 0);
        }
        try {
            this.radius = Double.parseDouble(commaTokens[1]);
        } catch (NumberFormatException e) {
            throw new ParseException(commaTokens[1] + " is not a double", 0);
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geofence\GeofenceCircle.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */