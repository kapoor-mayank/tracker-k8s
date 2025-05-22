package org.traccar;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;


public abstract class TrackerServer
        implements TrackerConnector {
    private final boolean datagram;
    private final boolean secure;
    private final AbstractBootstrap bootstrap;
    private final int port;
    private final String address;
    private final ChannelGroup channelGroup = (ChannelGroup) new DefaultChannelGroup((EventExecutor) GlobalEventExecutor.INSTANCE);


    public boolean isDatagram() {
        return this.datagram;
    }


    public boolean isSecure() {
        return this.secure;
    }

    public TrackerServer(boolean datagram, String protocol) {
        this.datagram = datagram;

        this.secure = Context.getConfig().getBoolean(protocol + ".ssl");
        this.address = Context.getConfig().getString(protocol + ".address");
        this.port = Context.getConfig().getInteger(protocol + ".port");

        BasePipelineFactory pipelineFactory = new BasePipelineFactory(this, protocol) {
            protected void addTransportHandlers(PipelineBuilder pipeline) {
                try {
                    if (TrackerServer.this.isSecure()) {
                        SSLEngine engine = SSLContext.getDefault().createSSLEngine();
                        pipeline.addLast((ChannelHandler) new SslHandler(engine));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }


            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                TrackerServer.this.addProtocolHandlers(pipeline);
            }
        };

        if (datagram) {

            this


                    .bootstrap = ((Bootstrap) ((Bootstrap) (new Bootstrap()).group(EventLoopGroupFactory.getWorkerGroup())).channel(NioDatagramChannel.class)).handler((ChannelHandler) pipelineFactory);
        } else {

            this


                    .bootstrap = (AbstractBootstrap) ((ServerBootstrap) (new ServerBootstrap()).group(EventLoopGroupFactory.getBossGroup(), EventLoopGroupFactory.getWorkerGroup()).channel(NioServerSocketChannel.class)).childHandler((ChannelHandler) pipelineFactory);
        }
    }


    protected abstract void addProtocolHandlers(PipelineBuilder paramPipelineBuilder);

    public int getPort() {
        return this.port;
    }

    public String getAddress() {
        return this.address;
    }


    public ChannelGroup getChannelGroup() {
        return this.channelGroup;
    }


    public void start() throws Exception {
        InetSocketAddress endpoint;
        if (this.address == null) {
            endpoint = new InetSocketAddress(this.port);
        } else {
            endpoint = new InetSocketAddress(this.address, this.port);
        }

        Channel channel = this.bootstrap.bind(endpoint).syncUninterruptibly().channel();
        if (channel != null) {
            getChannelGroup().add(channel);
        }
    }


    public void stop() {
        this.channelGroup.close().awaitUninterruptibly();
    }
}
