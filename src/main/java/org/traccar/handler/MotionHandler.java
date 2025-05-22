package org.traccar.handler;

import io.netty.channel.ChannelHandler.Sharable;
import org.traccar.BaseDataHandler;
import org.traccar.model.Position;


@Sharable
public class MotionHandler
        extends BaseDataHandler {
    private double speedThreshold;

    public MotionHandler(double speedThreshold) {
        this.speedThreshold = speedThreshold;
    }


    protected Position handlePosition(Position position) {
        if (!position.getAttributes().containsKey("motion")) {
            position.set("motion", Boolean.valueOf((position.getSpeed() > this.speedThreshold)));
        }
        if (position.getProtocol().equals("jt600")) {
            boolean motion = position.getBoolean("motion");
            double distance = position.getDouble("distance");
            position.set("ignition", Boolean.valueOf((motion || (distance > 0.0D && distance < 1000.0D))));
        }
        return position;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\MotionHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */