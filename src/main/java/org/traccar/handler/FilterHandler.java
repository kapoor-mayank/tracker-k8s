package org.traccar.handler;

import io.netty.channel.ChannelHandler.Sharable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.BaseDataHandler;
import org.traccar.Context;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Position;


@Sharable
public class FilterHandler
        extends BaseDataHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterHandler.class);

    private boolean filterInvalid;
    private boolean filterZero;
    private boolean filterDuplicate;
    private long filterFuture;
    private boolean filterApproximate;
    private int filterAccuracy;
    private boolean filterStatic;
    private int filterDistance;
    private int filterMaxDistance;
    private int filterMaxSpeed;
    private long filterMinPeriod;
    private long filterMinSatellites;
    private long filterMinAltitude;
    private long skipLimit;
    private boolean skipAttributes;

    public FilterHandler(Config config) {
        this.filterInvalid = config.getBoolean(Keys.FILTER_INVALID);
        this.filterZero = config.getBoolean(Keys.FILTER_ZERO);
        this.filterDuplicate = config.getBoolean(Keys.FILTER_DUPLICATE);
        this.filterFuture = config.getLong(Keys.FILTER_FUTURE) * 1000L;
        this.filterAccuracy = config.getInteger(Keys.FILTER_ACCURACY);
        this.filterApproximate = config.getBoolean(Keys.FILTER_APPROXIMATE);
        this.filterStatic = config.getBoolean(Keys.FILTER_STATIC);
        this.filterDistance = config.getInteger(Keys.FILTER_DISTANCE);
        this.filterMaxDistance = config.getInteger("filter.maxDistance");
        this.filterMaxSpeed = config.getInteger(Keys.FILTER_MAX_SPEED);
        this.filterMinPeriod = (config.getInteger(Keys.FILTER_MIN_PERIOD) * 1000);
        this.filterMinSatellites = config.getInteger("filter.minSatellites");
        this.filterMinAltitude = config.getInteger("filter.minAltitude");
        this.skipLimit = config.getLong(Keys.FILTER_SKIP_LIMIT) * 1000L;
        this.skipAttributes = config.getBoolean(Keys.FILTER_SKIP_ATTRIBUTES_ENABLE);
    }

    private boolean filterInvalid(Position position) {
        return (this.filterInvalid && (!position.getValid() || position
                .getLatitude() > 90.0D || position.getLongitude() > 180.0D || position
                .getLatitude() < -90.0D || position.getLongitude() < -180.0D));
    }

    private boolean filterZero(Position position) {
        return (this.filterZero && position.getLatitude() < 1.0D && position.getLongitude() < 1.0D);
    }

    private boolean filterDuplicate(Position position, Position last) {
        if (this.filterDuplicate && last != null && position.getFixTime().equals(last.getFixTime())) {
            for (String key : position.getAttributes().keySet()) {
                if (!last.getAttributes().containsKey(key)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean filterFuture(Position position) {
        return (this.filterFuture != 0L && position.getFixTime().getTime() > System.currentTimeMillis() + this.filterFuture);
    }

    private boolean filterAccuracy(Position position) {
        return (this.filterAccuracy != 0 && position.getAccuracy() > this.filterAccuracy);
    }

    private boolean filterApproximate(Position position) {
        return (this.filterApproximate && position.getBoolean("approximate"));
    }

    private boolean filterStatic(Position position) {
        return (this.filterStatic && position.getSpeed() == 0.0D);
    }

    private boolean filterDistance(Position position, Position last) {
        double distance = position.getDouble("distance");
        return (last != null && ((this.filterDistance != 0 && distance < this.filterDistance) || (this.filterMaxDistance != 0 && distance > this.filterMaxDistance)));
    }


    private boolean filterMaxSpeed(Position position, Position last) {
        if (this.filterMaxSpeed != 0 && last != null) {
            double distance = position.getDouble("distance");
            double time = (position.getFixTime().getTime() - last.getFixTime().getTime());
            return (UnitsConverter.knotsFromMps(distance / time / 1000.0D) > this.filterMaxSpeed);
        }
        return false;
    }

    private boolean filterMinPeriod(Position position, Position last) {
        if (this.filterMinPeriod != 0L && last != null) {
            long time = position.getFixTime().getTime() - last.getFixTime().getTime();
            return (time > 0L && time < this.filterMinPeriod);
        }
        return false;
    }

    private boolean filterMinSatellites(Position position) {
        if (this.filterMinSatellites != 0L && position.getAttributes().containsKey("sat")) {
            return (position.getInteger("sat") < this.filterMinSatellites);
        }
        return false;
    }

    private boolean filterMinAltitude(Position position) {
        if (this.filterMinAltitude != 0L) {
            return (position.getAltitude() < this.filterMinAltitude);
        }
        return false;
    }

    private boolean skipLimit(Position position, Position last) {
        if (this.skipLimit != 0L && last != null) {
            return (position.getServerTime().getTime() - last.getServerTime().getTime() > this.skipLimit);
        }
        return false;
    }

    private boolean skipAttributes(Position position) {
        if (this.skipAttributes) {
            String attributesString = Context.getIdentityManager().lookupAttributeString(position
                    .getDeviceId(), "filter.skipAttributes", "", true);
            for (String attribute : attributesString.split("[ ,]")) {
                if (position.getAttributes().containsKey(attribute)) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean filter(Position position) {
        StringBuilder filterType = new StringBuilder();

        Position last = null;
        if (Context.getIdentityManager() != null) {
            last = Context.getIdentityManager().getLastPosition(position.getDeviceId());
        }

        if (filterInvalid(position)) {
            filterType.append("Invalid ");
        }
        if (filterZero(position)) {
            filterType.append("Zero ");
        }
        if (filterDuplicate(position, last) && !skipLimit(position, last) && !skipAttributes(position)) {
            filterType.append("Duplicate ");
        }
        if (filterFuture(position)) {
            filterType.append("Future ");
        }
        if (filterAccuracy(position)) {
            filterType.append("Accuracy ");
        }
        if (filterApproximate(position)) {
            filterType.append("Approximate ");
        }
        if (filterStatic(position) && !skipLimit(position, last) && !skipAttributes(position)) {
            filterType.append("Static ");
        }
        if (filterDistance(position, last) && !skipLimit(position, last) && !skipAttributes(position)) {
            filterType.append("Distance ");
        }
        if (filterMaxSpeed(position, last)) {
            filterType.append("MaxSpeed ");
        }
        if (filterMinPeriod(position, last)) {
            filterType.append("MinPeriod ");
        }
        if (filterMinSatellites(position)) {
            filterType.append("MinSatellites ");
        }
        if (filterMinAltitude(position)) {
            filterType.append("MinAltitude ");
        }

        if (filterType.length() > 0) {

            StringBuilder message = new StringBuilder();
            message.append("Position filtered by ");
            message.append(filterType.toString());
            message.append("filters from device: ");
            message.append(Context.getIdentityManager().getById(position.getDeviceId()).getUniqueId());

            LOGGER.info(message.toString());
            return true;
        }

        return false;
    }


    protected Position handlePosition(Position position) {
        if (filter(position)) {
            return null;
        }
        return position;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\FilterHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */