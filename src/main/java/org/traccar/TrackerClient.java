/*     */ package org.traccar;
/*     */ 
/*     */ import io.netty.bootstrap.Bootstrap;
/*     */ import io.netty.channel.ChannelHandler;
/*     */ import io.netty.channel.group.ChannelGroup;
/*     */ import io.netty.channel.group.DefaultChannelGroup;
/*     */ import io.netty.channel.socket.nio.NioSocketChannel;
/*     */ import io.netty.handler.ssl.SslHandler;
/*     */ import io.netty.util.concurrent.EventExecutor;
/*     */ import io.netty.util.concurrent.Future;
/*     */ import io.netty.util.concurrent.GenericFutureListener;
/*     */ import io.netty.util.concurrent.GlobalEventExecutor;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import javax.net.ssl.SSLContext;
/*     */ import javax.net.ssl.SSLEngine;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public abstract class TrackerClient
/*     */   implements TrackerConnector
/*     */ {
/*     */   private final boolean secure;
/*     */   private final long interval;
/*     */   private final Bootstrap bootstrap;
/*     */   private final int port;
/*     */   private final String address;
/*     */   private final String[] devices;
/*  42 */   private final ChannelGroup channelGroup = (ChannelGroup)new DefaultChannelGroup((EventExecutor)GlobalEventExecutor.INSTANCE);
/*     */ 
/*     */   
/*     */   public boolean isDatagram() {
/*  46 */     return false;
/*     */   }
/*     */ 
/*     */   
/*     */   public boolean isSecure() {
/*  51 */     return this.secure;
/*     */   }
/*     */ 
/*     */   
/*     */   public TrackerClient(String protocol) {
/*  56 */     this.secure = Context.getConfig().getBoolean(protocol + ".ssl");
/*  57 */     this.interval = Context.getConfig().getLong(protocol + ".interval");
/*  58 */     this.address = Context.getConfig().getString(protocol + ".address");
/*  59 */     this.port = Context.getConfig().getInteger(protocol + ".port");
/*  60 */     this.devices = Context.getConfig().getString(protocol + ".devices").split("[, ]");
/*     */     
/*  62 */     BasePipelineFactory pipelineFactory = new BasePipelineFactory(this, protocol)
/*     */       {
/*     */         protected void addTransportHandlers(PipelineBuilder pipeline) {
/*     */           try {
/*  66 */             if (TrackerClient.this.isSecure()) {
/*  67 */               SSLEngine engine = SSLContext.getDefault().createSSLEngine();
/*  68 */               engine.setUseClientMode(true);
/*  69 */               pipeline.addLast((ChannelHandler)new SslHandler(engine));
/*     */             } 
/*  71 */           } catch (Exception e) {
/*  72 */             throw new RuntimeException(e);
/*     */           } 
/*     */         }
/*     */ 
/*     */         
/*     */         protected void addProtocolHandlers(PipelineBuilder pipeline) {
/*     */           try {
/*  79 */             TrackerClient.this.addProtocolHandlers(pipeline);
/*  80 */           } catch (Exception e) {
/*  81 */             throw new RuntimeException(e);
/*     */           } 
/*     */         }
/*     */       };
/*     */     
/*  86 */     this
/*     */ 
/*     */       
/*  89 */       .bootstrap = (Bootstrap)((Bootstrap)((Bootstrap)(new Bootstrap()).group(EventLoopGroupFactory.getWorkerGroup())).channel(NioSocketChannel.class)).handler((ChannelHandler)pipelineFactory);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public String[] getDevices() {
/*  95 */     return this.devices;
/*     */   }
/*     */ 
/*     */   
/*     */   public ChannelGroup getChannelGroup() {
/* 100 */     return this.channelGroup;
/*     */   }
/*     */ 
/*     */   
/*     */   public void start() throws Exception {
/* 105 */     this.bootstrap.connect(this.address, this.port)
/* 106 */       .syncUninterruptibly().channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>()
/*     */         {
/*     */           public void operationComplete(Future<? super Void> future) {
/* 109 */             if (TrackerClient.this.interval > 0L) {
/* 110 */               GlobalEventExecutor.INSTANCE.schedule(() -> TrackerClient.this.bootstrap.connect(TrackerClient.this.address, TrackerClient.this.port).syncUninterruptibly().channel().closeFuture().addListener(this), TrackerClient.this
/*     */ 
/*     */                   
/* 113 */                   .interval, TimeUnit.SECONDS);
/*     */             }
/*     */           }
/*     */         });
/*     */   }
/*     */ 
/*     */   
/*     */   public void stop() {
/* 121 */     this.channelGroup.close().awaitUninterruptibly();
/*     */   }
/*     */   
/*     */   protected abstract void addProtocolHandlers(PipelineBuilder paramPipelineBuilder) throws Exception;
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\TrackerClient.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */