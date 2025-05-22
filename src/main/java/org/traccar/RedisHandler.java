package org.traccar;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.model.Position;

public class RedisHandler
        extends BaseDataHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisHandler.class);

    private boolean duplicatePositions(Position p1, Position p2) {
        if (EqualsBuilder.reflectionEquals(p1, p2, new String[]{"id", "serverTime", "attributes"})) {
            Set<String> keys = new HashSet<>();
            keys.addAll(p1.getAttributes().keySet());
            keys.addAll(p2.getAttributes().keySet());
            for (String key : keys) {
                if (!key.equals("index") && !key.equals("sequence") &&
                        !Objects.equals(p1.getAttributes().get(key), p2.getAttributes().get(key))) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    private boolean frequentPositions(Position p1, Position p2) {
        return (p1 != null && p2 != null &&
                Math.abs(p2.getServerTime().getTime() - p1.getServerTime().getTime()) < 60000L);
    }


    protected Position handlePosition(Position position) {
        try {
            Context.getRedisManager().writePosition(position);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Redis error", (Throwable) e);
        }
        return position;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\RedisHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */