package org.traccar;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundInvoker;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class WrapperContext implements ChannelHandlerContext {
    private ChannelHandlerContext context;

    private SocketAddress remoteAddress;
    private static final Logger LOGGER = LoggerFactory.getLogger(WrapperContext.class);
    public WrapperContext(ChannelHandlerContext context, SocketAddress remoteAddress) {
        this.context = context;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public String toString() {
        return "WrapperContext{" +
                "context=" + context +
                ", remoteAddress=" + remoteAddress +
                '}';
    }

    public Channel channel() {
        return this.context.channel();
    }

    public EventExecutor executor() {
        return this.context.executor();
    }

    public String name() {
        return this.context.name();
    }

    public ChannelHandler handler() {
        return this.context.handler();
    }

    public boolean isRemoved() {
        return this.context.isRemoved();
    }

    public ChannelHandlerContext fireChannelRegistered() {
        return this.context.fireChannelRegistered();
    }

    public ChannelHandlerContext fireChannelUnregistered() {
        return this.context.fireChannelUnregistered();
    }

    public ChannelHandlerContext fireChannelActive() {
        return this.context.fireChannelActive();
    }

    public ChannelHandlerContext fireChannelInactive() {
        return this.context.fireChannelInactive();
    }

    public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
        return this.context.fireExceptionCaught(cause);
    }

    public ChannelHandlerContext fireUserEventTriggered(Object evt) {
        return this.context.fireUserEventTriggered(evt);
    }

    public ChannelHandlerContext fireChannelRead(Object msg) {
        if (!(msg instanceof NetworkMessage))
            msg = new NetworkMessage(msg, this.remoteAddress);
        return this.context.fireChannelRead(msg);
    }

    public ChannelHandlerContext fireChannelReadComplete() {
        return this.context.fireChannelReadComplete();
    }

    public ChannelHandlerContext fireChannelWritabilityChanged() {
        return this.context.fireChannelWritabilityChanged();
    }

    public ChannelFuture bind(SocketAddress localAddress) {
        return this.context.bind(localAddress);
    }

    public ChannelFuture connect(SocketAddress remoteAddress) {
        return this.context.connect(remoteAddress);
    }

    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return this.context.connect(remoteAddress, localAddress);
    }

    public ChannelFuture disconnect() {
        return this.context.disconnect();
    }

    public ChannelFuture close() {
        return this.context.close();
    }

    public ChannelFuture deregister() {
        return this.context.deregister();
    }

    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return this.context.bind(localAddress, promise);
    }

    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return this.context.connect(remoteAddress, promise);
    }

    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return this.context.connect(remoteAddress, localAddress, promise);
    }

    public ChannelFuture disconnect(ChannelPromise promise) {
        return this.context.disconnect(promise);
    }

    public ChannelFuture close(ChannelPromise promise) {
        return this.context.close(promise);
    }

    public ChannelFuture deregister(ChannelPromise promise) {
        return this.context.deregister(promise);
    }

    public ChannelHandlerContext read() {
        return this.context.read();
    }

    public ChannelFuture write(Object msg) {
        return this.context.write(msg);
    }

    public ChannelFuture write(Object msg, ChannelPromise promise) {
        if (!(msg instanceof NetworkMessage))
            msg = new NetworkMessage(msg, this.remoteAddress);
        return this.context.write(msg, promise);
    }

    public ChannelHandlerContext flush() {
        return this.context.flush();
    }

    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return this.context.writeAndFlush(msg, promise);
    }

    public ChannelFuture writeAndFlush(Object msg) {
        return this.context.writeAndFlush(msg);
    }

    public ChannelPromise newPromise() {
        return this.context.newPromise();
    }

    public ChannelProgressivePromise newProgressivePromise() {
        return this.context.newProgressivePromise();
    }

    public ChannelFuture newSucceededFuture() {
        return this.context.newSucceededFuture();
    }

    public ChannelFuture newFailedFuture(Throwable cause) {
        return this.context.newFailedFuture(cause);
    }

    public ChannelPromise voidPromise() {
        return this.context.voidPromise();
    }

    public ChannelPipeline pipeline() {
        return this.context.pipeline();
    }

    public ByteBufAllocator alloc() {
        return this.context.alloc();
    }

    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return this.context.attr(key);
    }

    public <T> boolean hasAttr(AttributeKey<T> key) {
        return this.context.hasAttr(key);
    }
}
