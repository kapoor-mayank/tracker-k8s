package org.traccar.handler;

import io.netty.channel.ChannelHandler.Sharable;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.traccar.BaseDataHandler;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.database.IdentityManager;
import org.traccar.helper.DistanceCalculator;
import org.traccar.model.Position;


@Sharable
public class DistanceHandler
        extends BaseDataHandler {
    private final IdentityManager identityManager;
    private final boolean filter;
    private final int coordinatesMinError;
    private final int coordinatesMaxError;

    public DistanceHandler(Config config, IdentityManager identityManager) {
        this.identityManager = identityManager;
        this.filter = config.getBoolean(Keys.COORDINATES_FILTER);
        this.coordinatesMinError = config.getInteger(Keys.COORDINATES_MIN_ERROR);
        this.coordinatesMaxError = config.getInteger(Keys.COORDINATES_MAX_ERROR);
    }


    protected Position handlePosition(Position position) {
        double distance = 0.0D;
        if (position.getAttributes().containsKey("distance")) {
            distance = position.getDouble("distance");
        }
        double totalDistance = 0.0D;

        Position last = (this.identityManager != null) ? this.identityManager.getLastPosition(position.getDeviceId()) : null;
        if (last != null) {
            totalDistance = last.getDouble("totalDistance");
            if (!position.getAttributes().containsKey("distance")) {
                distance = DistanceCalculator.distance(position
                        .getLatitude(), position.getLongitude(), last
                        .getLatitude(), last.getLongitude());
                distance = BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
            }
            if (this.filter && last.getValid() && last.getLatitude() != 0.0D && last.getLongitude() != 0.0D) {
                boolean satisfiesMin = (this.coordinatesMinError == 0 || distance > this.coordinatesMinError);

                boolean satisfiesMax = (this.coordinatesMaxError == 0 || distance < this.coordinatesMaxError || position.getValid());
                if (!satisfiesMin || !satisfiesMax) {
                    position.setLatitude(last.getLatitude());
                    position.setLongitude(last.getLongitude());
                    distance = 0.0D;
                }
            }
        }
        position.set("distance", Double.valueOf(distance));
        totalDistance = BigDecimal.valueOf(totalDistance + distance).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        position.set("totalDistance", Double.valueOf(totalDistance));

        return position;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\DistanceHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */