package org.traccar;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.helper.DataConverter;
import org.traccar.model.Position;
import org.traccar.protocol.DmtProtocolDecoder;

public abstract class ExtendedObjectDecoder extends ChannelInboundHandlerAdapter {
    private long sequence;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedObjectDecoder.class);
    private void saveOriginal(Object decodedMessage, Object originalMessage) {
        Position position = (Position)decodedMessage;
        position.set("sequence", Long.valueOf(++this.sequence));
        if (decodedMessage instanceof Position && Context.getConfig().getBoolean("database.saveOriginal"))
            if (originalMessage instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf)originalMessage;
                position.set("raw", ByteBufUtil.hexDump(buf));
            } else if (originalMessage instanceof String) {
                position.set("raw", DataConverter.printHex(((String)originalMessage)
                        .getBytes(StandardCharsets.US_ASCII)));
            }
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NetworkMessage networkMessage = (NetworkMessage) msg;
        Object originalMessage = networkMessage.getMessage();
//        LOGGER.info("Inside channelRead() ExtendedObjectDecoder: ",networkMessage, originalMessage);
        try {
            Object decodedMessage = decode(ctx.channel(), networkMessage.getRemoteAddress(), originalMessage);
            onMessageEvent(ctx.channel(), networkMessage.getRemoteAddress(), originalMessage, decodedMessage);

            if (decodedMessage == null) {
                decodedMessage = handleEmptyMessage(ctx.channel(), networkMessage.getRemoteAddress(), originalMessage);
            }

            if (decodedMessage != null) {
                // Check if decodedMessage is a collection
                if (decodedMessage instanceof Collection<?>) {
                    for (Object o : (Collection<?>) decodedMessage) {
                        saveOriginal(o, originalMessage);
                        ctx.fireChannelRead(o);
                    }
                } else {
                    // For non-collection decoded messages
                    saveOriginal(decodedMessage, originalMessage);
                    ctx.fireChannelRead(decodedMessage);
                }
            }
        } finally {
            // Always release the original message after processing
            ReferenceCountUtil.release(originalMessage);
        }
    }


    protected void onMessageEvent(Channel channel, SocketAddress remoteAddress, Object originalMessage, Object decodedMessage) {}

    protected Object handleEmptyMessage(Channel channel, SocketAddress remoteAddress, Object msg) {
        return null;
    }

    protected abstract Object decode(Channel paramChannel, SocketAddress paramSocketAddress, Object paramObject) throws Exception;
}
