package org.traccar.geofence;

import java.text.ParseException;
import java.util.ArrayList;

import org.traccar.helper.DistanceCalculator;


public class GeofencePolyline
        extends GeofenceGeometry {
    private ArrayList<GeofenceGeometry.Coordinate> coordinates;
    private double distance;

    public GeofencePolyline() {
    }

    public GeofencePolyline(String wkt, double distance) throws ParseException {
        fromWkt(wkt);
        this.distance = distance;
    }


    public boolean containsPoint(double latitude, double longitude) {
        for (int i = 1; i < this.coordinates.size(); i++) {
            if (DistanceCalculator.distanceToLine(latitude, longitude, ((GeofenceGeometry.Coordinate) this.coordinates
                    .get(i - 1)).getLat(), ((GeofenceGeometry.Coordinate) this.coordinates.get(i - 1)).getLon(), ((GeofenceGeometry.Coordinate) this.coordinates
                    .get(i)).getLat(), ((GeofenceGeometry.Coordinate) this.coordinates.get(i)).getLon()) <= this.distance) {
                return true;
            }
        }
        return false;
    }


    public String toWkt() {
        StringBuilder buf = new StringBuilder();
        buf.append("LINESTRING (");
        for (GeofenceGeometry.Coordinate coordinate : this.coordinates) {
            buf.append(String.valueOf(coordinate.getLat()));
            buf.append(" ");
            buf.append(String.valueOf(coordinate.getLon()));
            buf.append(", ");
        }
        return buf.substring(0, buf.length() - 2) + ")";
    }


    public void fromWkt(String wkt) throws ParseException {
        if (this.coordinates == null) {
            this.coordinates = new ArrayList<>();
        } else {
            this.coordinates.clear();
        }

        if (!wkt.startsWith("LINESTRING")) {
            throw new ParseException("Mismatch geometry type", 0);
        }
        String content = wkt.substring(wkt.indexOf("(") + 1, wkt.indexOf(")"));
        if (content.isEmpty()) {
            throw new ParseException("No content", 0);
        }
        String[] commaTokens = content.split(",");
        if (commaTokens.length < 2) {
            throw new ParseException("Not valid content", 0);
        }

        for (String commaToken : commaTokens) {
            String[] tokens = commaToken.trim().split("\\s");
            if (tokens.length != 2) {
                throw new ParseException("Here must be two coordinates: " + commaToken, 0);
            }
            GeofenceGeometry.Coordinate coordinate = new GeofenceGeometry.Coordinate();
            try {
                coordinate.setLat(Double.parseDouble(tokens[0]));
            } catch (NumberFormatException e) {
                throw new ParseException(tokens[0] + " is not a double", 0);
            }
            try {
                coordinate.setLon(Double.parseDouble(tokens[1]));
            } catch (NumberFormatException e) {
                throw new ParseException(tokens[1] + " is not a double", 0);
            }
            this.coordinates.add(coordinate);
        }
    }


    public void setDistance(double distance) {
        this.distance = distance;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geofence\GeofencePolyline.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */