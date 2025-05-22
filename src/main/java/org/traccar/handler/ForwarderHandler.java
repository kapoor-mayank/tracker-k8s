package org.traccar.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.traccar.Context;
import org.traccar.protocol.GoSafeProtocolDecoder;
import org.traccar.protocol.Gt06ProtocolDecoder;
import org.traccar.protocol.H02ProtocolDecoder;


public class ForwarderHandler
        extends ChannelInboundHandlerAdapter {
    private Map<SocketAddress, String> deviceIdMap = new HashMap<>();
    private Map<SocketAddress, List<byte[]>> bufferMap = new HashMap<>();

    public void identify(String uniqueId, SocketAddress remoteAddress) {
        this.deviceIdMap.put(remoteAddress, uniqueId);
        if (this.bufferMap.containsKey(remoteAddress)) {
            for (byte[] data : this.bufferMap.get(remoteAddress)) {
                Context.getDataForwarder().forward(uniqueId, data);
            }
            this.bufferMap.remove(remoteAddress);
        }
    }


    private boolean requirePrefix(ChannelHandlerContext context, String protocol, Class<? extends ChannelHandler> decoder) {
        return (context.channel().pipeline().get(decoder) != null &&
                Context.getConfig().getBoolean(protocol + ".forwardPrefix"));
    }


    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketAddress remoteAddress;
        ByteBuf buffer;
        if (ctx.channel() instanceof io.netty.channel.socket.DatagramChannel) {
            DatagramPacket message = (DatagramPacket) msg;
            remoteAddress = message.recipient();
            buffer = (ByteBuf) message.content();
        } else {
            remoteAddress = ctx.channel().remoteAddress();
            buffer = (ByteBuf) msg;
        }

        byte[] data = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), data);

        if (requirePrefix(ctx, "gt06", (Class) Gt06ProtocolDecoder.class) ||
                requirePrefix(ctx, "h02", (Class) H02ProtocolDecoder.class) ||
                requirePrefix(ctx, "gosafe", (Class) GoSafeProtocolDecoder.class)) {
            byte[] prefixedData = new byte[data.length + 3];
            prefixedData[0] = 81;
            prefixedData[1] = 90;
            prefixedData[2] = 69;
            System.arraycopy(data, 0, prefixedData, 3, data.length);
            data = prefixedData;
        }

        if (this.deviceIdMap.containsKey(remoteAddress)) {
            Context.getDataForwarder().forward(this.deviceIdMap.get(remoteAddress), data);
        } else {
            if (!this.bufferMap.containsKey(remoteAddress)) {
                this.bufferMap.put(remoteAddress, (List) new ArrayList<>());
            }
            ((List<byte[]>) this.bufferMap.get(remoteAddress)).add(data);
        }
        super.channelRead(ctx, msg);
    }
}