package org.traccar.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.traccar.TrackerConnector;


public class OpenChannelHandler
        extends ChannelDuplexHandler {
    private final TrackerConnector connector;

    public OpenChannelHandler(TrackerConnector connector) {
        this.connector = connector;
    }


    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.connector.getChannelGroup().add(ctx.channel());
    }


    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        this.connector.getChannelGroup().remove(ctx.channel());
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\OpenChannelHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */