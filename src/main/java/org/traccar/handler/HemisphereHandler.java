package org.traccar.handler;

import io.netty.channel.ChannelHandler.Sharable;
import org.traccar.BaseDataHandler;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.model.Position;


@Sharable
public class HemisphereHandler
        extends BaseDataHandler {
    private int latitudeFactor;
    private int longitudeFactor;

    public HemisphereHandler(Config config) {
        String latitudeHemisphere = config.getString(Keys.LOCATION_LATITUDE_HEMISPHERE);
        if (latitudeHemisphere != null) {
            if (latitudeHemisphere.equalsIgnoreCase("N")) {
                this.latitudeFactor = 1;
            } else if (latitudeHemisphere.equalsIgnoreCase("S")) {
                this.latitudeFactor = -1;
            }
        }
        String longitudeHemisphere = config.getString(Keys.LOCATION_LATITUDE_HEMISPHERE);
        if (longitudeHemisphere != null) {
            if (longitudeHemisphere.equalsIgnoreCase("E")) {
                this.longitudeFactor = 1;
            } else if (longitudeHemisphere.equalsIgnoreCase("W")) {
                this.longitudeFactor = -1;
            }
        }
    }


    protected Position handlePosition(Position position) {
        if (this.latitudeFactor != 0) {
            position.setLatitude(Math.abs(position.getLatitude()) * this.latitudeFactor);
        }
        if (this.longitudeFactor != 0) {
            position.setLongitude(Math.abs(position.getLongitude()) * this.longitudeFactor);
        }
        return position;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\HemisphereHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */