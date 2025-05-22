package org.traccar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.text.ParseException;

import org.traccar.Context;
import org.traccar.database.QueryIgnore;
import org.traccar.geofence.GeofenceCircle;
import org.traccar.geofence.GeofenceGeometry;
import org.traccar.geofence.GeofencePolygon;
import org.traccar.geofence.GeofencePolyline;


public class Geofence
        extends ScheduledModel {
    public static final String TYPE_GEOFENCE_CILCLE = "geofenceCircle";
    public static final String TYPE_GEOFENCE_POLYGON = "geofencePolygon";
    public static final String TYPE_GEOFENCE_POLYLINE = "geofencePolyline";
    private String name;
    private String description;
    private String area;
    private GeofenceGeometry geometry;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getArea() {
        return this.area;
    }


    public void setArea(String area) throws ParseException {
        if (area.startsWith("CIRCLE")) {
            this.geometry = (GeofenceGeometry) new GeofenceCircle(area);
        } else if (area.startsWith("POLYGON")) {
            this.geometry = (GeofenceGeometry) new GeofencePolygon(area);
        } else if (area.startsWith("LINESTRING")) {
            double distance = getDouble("polylineDistance");
            this
                    .geometry = (GeofenceGeometry) new GeofencePolyline(area, (distance > 0.0D) ? distance : Context.getConfig().getDouble("geofence.polylineDistance", 25.0D));
        } else {
            throw new ParseException("Unknown geometry type", 0);
        }

        this.area = area;
    }


    @QueryIgnore
    @JsonIgnore
    public GeofenceGeometry getGeometry() {
        return this.geometry;
    }

    @QueryIgnore
    @JsonIgnore
    public void setGeometry(GeofenceGeometry geometry) {
        this.area = geometry.toWkt();
        this.geometry = geometry;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\Geofence.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */