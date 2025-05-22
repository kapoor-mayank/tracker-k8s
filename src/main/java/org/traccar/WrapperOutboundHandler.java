package org.traccar;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import java.net.SocketAddress;

public class WrapperOutboundHandler implements ChannelOutboundHandler {
    private ChannelOutboundHandler handler;

    public ChannelOutboundHandler getWrappedHandler() {
        return this.handler;
    }

    public WrapperOutboundHandler(ChannelOutboundHandler handler) {
        this.handler = handler;
    }

    @Override
    public String toString() {
        return "WrapperOutboundHandler{" +
                "handler=" + handler +
                '}';
    }

    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        this.handler.bind(ctx, localAddress, promise);
    }

    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        this.handler.connect(ctx, remoteAddress, localAddress, promise);
    }

    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.handler.disconnect(ctx, promise);
    }

    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.handler.close(ctx, promise);
    }

    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.handler.deregister(ctx, promise);
    }

    public void read(ChannelHandlerContext ctx) throws Exception {
        this.handler.read(ctx);
    }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof NetworkMessage) {
            NetworkMessage nm = (NetworkMessage)msg;
            this.handler.write(new WrapperContext(ctx, nm.getRemoteAddress()), nm.getMessage(), promise);
        } else {
            this.handler.write(ctx, msg, promise);
        }
    }

    public void flush(ChannelHandlerContext ctx) throws Exception {
        this.handler.flush(ctx);
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
