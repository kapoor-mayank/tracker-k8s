package org.traccar.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

import org.traccar.NetworkMessage;


public class NetworkMessageHandler
        extends ChannelDuplexHandler {
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (ctx.channel() instanceof io.netty.channel.socket.DatagramChannel) {
            DatagramPacket packet = (DatagramPacket) msg;
            ctx.fireChannelRead(new NetworkMessage(packet.content(), packet.sender()));
        } else if (msg instanceof ByteBuf) {
            ByteBuf buffer = (ByteBuf) msg;
            ctx.fireChannelRead(new NetworkMessage(buffer, ctx.channel().remoteAddress()));
        }
    }


    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof NetworkMessage) {
            NetworkMessage message = (NetworkMessage) msg;
            if (ctx.channel() instanceof io.netty.channel.socket.DatagramChannel) {
                InetSocketAddress recipient = (InetSocketAddress) message.getRemoteAddress();
                InetSocketAddress sender = (InetSocketAddress) ctx.channel().localAddress();
                ctx.write(new DatagramPacket((ByteBuf) message.getMessage(), recipient, sender), promise);
            } else {
                ctx.write(message.getMessage(), promise);
            }
        } else {
            ctx.write(msg, promise);
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\handler\NetworkMessageHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */