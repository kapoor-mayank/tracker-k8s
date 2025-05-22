package org.traccar.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePartial;
import org.traccar.BaseProtocolDecoder;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.model.Position;


@Sharable
public class TimeHandler
        extends ChannelInboundHandlerAdapter {
    private static final int DAYS_THRESHOLD = 60;
    private final Config config;
    private final boolean useServerTime;

    public TimeHandler(Config config) {
        this.config = config;
        this.useServerTime = config.getString(Keys.TIME_OVERRIDE).equalsIgnoreCase("serverTime");
    }

    public static boolean aboveThreshold(Date time) {
        return (Math.abs(Days.daysBetween((ReadablePartial) new LocalDate(), (ReadablePartial) new LocalDate(time)).getDays()) > 60);
    }


    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Position) if (this.config.getBoolean(((BaseProtocolDecoder) ctx
                .pipeline().get(BaseProtocolDecoder.class)).getProtocolName() + ".timeoverride")) {

            Position position = (Position) msg;
            if (this.useServerTime) {
                position.setDeviceTime(position.getServerTime());
            }
            position.setFixTime(position.getDeviceTime());
        }
        ctx.fireChannelRead(msg);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\TimeHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */