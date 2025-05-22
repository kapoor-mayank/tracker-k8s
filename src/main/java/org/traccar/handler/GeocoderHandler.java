package org.traccar.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.database.IdentityManager;
import org.traccar.database.StatisticsManager;
import org.traccar.geocoder.Geocoder;
import org.traccar.model.Position;


@Sharable
public class GeocoderHandler
        extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeocoderHandler.class);

    private final Geocoder geocoder;

    private final IdentityManager identityManager;
    private final StatisticsManager statisticsManager;
    private final boolean ignorePositions;
    private final boolean processInvalidPositions;
    private final int geocoderReuseDistance;

    public GeocoderHandler(Config config, Geocoder geocoder, IdentityManager identityManager, StatisticsManager statisticsManager) {
        this.geocoder = geocoder;
        this.identityManager = identityManager;
        this.statisticsManager = statisticsManager;
        this.ignorePositions = Context.getConfig().getBoolean(Keys.GEOCODER_IGNORE_POSITIONS);
        this.processInvalidPositions = config.getBoolean(Keys.GEOCODER_PROCESS_INVALID_POSITIONS);
        this.geocoderReuseDistance = config.getInteger(Keys.GEOCODER_REUSE_DISTANCE, 0);
    }


    public void channelRead(final ChannelHandlerContext ctx, Object message) {
        if (message instanceof Position && !this.ignorePositions) {
            final Position position = (Position) message;
            if (this.processInvalidPositions || position.getValid()) {
                if (this.geocoderReuseDistance != 0) {
                    Position lastPosition = this.identityManager.getLastPosition(position.getDeviceId());
                    if (lastPosition != null && lastPosition.getAddress() != null && position
                            .getDouble("distance") <= this.geocoderReuseDistance) {
                        position.setAddress(lastPosition.getAddress());
                        ctx.fireChannelRead(position);

                        return;
                    }
                }
                if (this.statisticsManager != null) {
                    this.statisticsManager.registerGeocoderRequest();
                }

                this.geocoder.getAddress(position.getLatitude(), position.getLongitude(), new Geocoder.ReverseGeocoderCallback() {
                    public void onSuccess(String address) {
                        position.setAddress(address);
                        ctx.fireChannelRead(position);
                    }


                    public void onFailure(Throwable e) {
                        GeocoderHandler.LOGGER.warn("Geocoding failed", e);
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


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\GeocoderHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */