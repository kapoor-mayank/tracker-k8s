package org.traccar.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;

import org.traccar.model.Position;


@Sharable
public class RemoteAddressHandler
        extends ChannelInboundHandlerAdapter {
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String hostAddress = (remoteAddress != null) ? remoteAddress.getAddress().getHostAddress() : null;

        if (msg instanceof Position) {
            Position position = (Position) msg;
            position.set("ip", hostAddress);
        }

        ctx.fireChannelRead(msg);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\RemoteAddressHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */