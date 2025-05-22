package org.traccar;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WrapperInboundHandler implements ChannelInboundHandler {
    private ChannelInboundHandler handler;
    private static final Logger LOGGER = LoggerFactory.getLogger(WrapperInboundHandler.class);
    public ChannelInboundHandler getWrappedHandler() {
        return this.handler;
    }

    @Override
    public String toString() {
        return "WrapperInboundHandler{" +
                "handler=" + handler +
                '}';
    }

    public WrapperInboundHandler(ChannelInboundHandler handler) {
        this.handler = handler;
    }

    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.handler.channelRegistered(ctx);
    }

    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        this.handler.channelUnregistered(ctx);
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.handler.channelActive(ctx);
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.handler.channelInactive(ctx);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof NetworkMessage) {
            NetworkMessage nm = (NetworkMessage)msg;
//            LOGGER.info("WrapperInbound channelRead(): NetworkMessage"+ msg);
            this.handler.channelRead(new WrapperContext(ctx, nm.getRemoteAddress()), nm.getMessage());
        } else {
//            LOGGER.info("WrapperInbound channelRead(): else"+ msg);
            this.handler.channelRead(ctx, msg);
        }
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        this.handler.channelReadComplete(ctx);
    }

    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        this.handler.userEventTriggered(ctx, evt);
    }

    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        this.handler.channelWritabilityChanged(ctx);
    }

    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.handler.handlerAdded(ctx);
    }

    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.handler.handlerRemoved(ctx);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.handler.exceptionCaught(ctx, cause);
    }
}
