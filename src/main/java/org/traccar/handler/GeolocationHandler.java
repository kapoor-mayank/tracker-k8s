package org.traccar.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.database.StatisticsManager;
import org.traccar.geolocation.GeolocationProvider;
import org.traccar.model.Position;


@Sharable
public class GeolocationHandler
        extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeolocationHandler.class);

    private final GeolocationProvider geolocationProvider;

    private final StatisticsManager statisticsManager;
    private final boolean processInvalidPositions;

    public GeolocationHandler(Config config, GeolocationProvider geolocationProvider, StatisticsManager statisticsManager) {
        this.geolocationProvider = geolocationProvider;
        this.statisticsManager = statisticsManager;
        this.processInvalidPositions = config.getBoolean(Keys.GEOLOCATION_PROCESS_INVALID_POSITIONS);
    }


    public void channelRead(final ChannelHandlerContext ctx, Object message) {
        if (message instanceof Position) {
            final Position position = (Position) message;
            if ((position.getOutdated() || (this.processInvalidPositions && !position.getValid())) && position
                    .getNetwork() != null) {
                if (this.statisticsManager != null) {
                    this.statisticsManager.registerGeolocationRequest();
                }

                this.geolocationProvider.getLocation(position.getNetwork(), new GeolocationProvider.LocationProviderCallback() {
                    public void onSuccess(double latitude, double longitude, double accuracy) {
                        position.set("approximate", Boolean.valueOf(true));
                        position.setValid(true);
                        position.setFixTime(position.getDeviceTime());
                        position.setLatitude(latitude);
                        position.setLongitude(longitude);
                        position.setAccuracy(accuracy);
                        position.setAltitude(0.0D);
                        position.setSpeed(0.0D);
                        position.setCourse(0.0D);
                        position.set("rssi", Integer.valueOf(0));
                        ctx.fireChannelRead(position);
                    }


                    public void onFailure(Throwable e) {
                        GeolocationHandler.LOGGER.warn("Geolocation network error", e);
                        ctx.fireChannelRead(position);
                    }
                });
            } else {
                ctx.fireChannelRead(position);
            }
        } else {
            ctx.fireChannelRead(message);
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\GeolocationHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */