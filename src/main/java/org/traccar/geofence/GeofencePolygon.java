package org.traccar.geofence;

import java.text.ParseException;
import java.util.ArrayList;

public class GeofencePolygon extends GeofenceGeometry {
    private ArrayList<GeofenceGeometry.Coordinate> coordinates;
    private double[] constant;
    private double[] multiple;
    private boolean needNormalize = false;

    public GeofencePolygon() {}

    public GeofencePolygon(String wkt) throws ParseException {
        fromWkt(wkt);
    }

    private void precalc() {
        if (this.coordinates == null)
            return;

        int polyCorners = this.coordinates.size();
        int j = polyCorners - 1;
        this.constant = new double[polyCorners];
        this.multiple = new double[polyCorners];

        boolean hasNegative = false;
        boolean hasPositive = false;

        // Check for longitude normalization need
        for (GeofenceGeometry.Coordinate coordinate : coordinates) {
            if (coordinate.getLon() > 90.0D) {
                hasPositive = true;
            } else if (coordinate.getLon() < -90.0D) {
                hasNegative = true;
            }
        }
        this.needNormalize = (hasPositive && hasNegative);

        for (int i = 0; i < polyCorners; j = i++) {
            if (normalizeLon(coordinates.get(j).getLon()) == normalizeLon(coordinates.get(i).getLon())) {
                this.constant[i] = coordinates.get(i).getLat();
                this.multiple[i] = 0.0D;
            } else {
                this.constant[i] = (coordinates.get(i).getLat() - normalizeLon(coordinates.get(i).getLon()) *
                        coordinates.get(j).getLat() / (normalizeLon(coordinates.get(j).getLon()) - normalizeLon(coordinates.get(i).getLon())) +
                        normalizeLon(coordinates.get(i).getLon()) * coordinates.get(i).getLat() /
                                (normalizeLon(coordinates.get(j).getLon()) - normalizeLon(coordinates.get(i).getLon())));

                this.multiple[i] = (coordinates.get(j).getLat() - coordinates.get(i).getLat()) /
                        (normalizeLon(coordinates.get(j).getLon()) - normalizeLon(coordinates.get(i).getLon()));
            }
        }
    }

    private double normalizeLon(double lon) {
        if (this.needNormalize && lon < -90.0D)
            return lon + 360.0D;
        return lon;
    }

    public boolean containsPoint(double latitude, double longitude) {
        int polyCorners = this.coordinates.size();
        int j = polyCorners - 1;
        double longitudeNorm = normalizeLon(longitude);
        boolean oddNodes = false;

        for (int i = 0; i < polyCorners; j = i++) {
            if ((normalizeLon(coordinates.get(i).getLon()) < longitudeNorm &&
                    normalizeLon(coordinates.get(j).getLon()) >= longitudeNorm) ||
                    (normalizeLon(coordinates.get(j).getLon()) < longitudeNorm &&
                            normalizeLon(coordinates.get(i).getLon()) >= longitudeNorm)) {
                if (longitudeNorm * this.multiple[i] + this.constant[i] < latitude) {
                    oddNodes = !oddNodes; // Toggle the state
                }
            }
        }
        return oddNodes;
    }

    public String toWkt() {
        StringBuilder buf = new StringBuilder();
        buf.append("POLYGON ((");
        for (GeofenceGeometry.Coordinate coordinate : this.coordinates) {
            buf.append(String.format("%.6f", coordinate.getLat())); // Format with precision
            buf.append(" ");
            buf.append(String.format("%.6f", coordinate.getLon())); // Format with precision
            buf.append(", ");
        }
        return buf.substring(0, buf.length() - 2) + "))";
    }

    public void fromWkt(String wkt) throws ParseException {
        if (this.coordinates == null) {
            this.coordinates = new ArrayList<>();
        } else {
            this.coordinates.clear();
        }

        if (!wkt.startsWith("POLYGON"))
            throw new ParseException("Mismatch geometry type", 0);

        String content = wkt.substring(wkt.indexOf("((") + 2, wkt.indexOf("))"));
        if (content.isEmpty())
            throw new ParseException("No content", 0);

        String[] commaTokens = content.split(",");
        if (commaTokens.length < 3)
            throw new ParseException("Not valid content", 0);

        for (String commaToken : commaTokens) {
            String[] tokens = commaToken.trim().split("\\s");
            if (tokens.length != 2)
                throw new ParseException("Here must be two coordinates: " + commaToken, 0);

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
        precalc();
    }
}
