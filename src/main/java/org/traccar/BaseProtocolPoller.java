/*    */ package org.traccar;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelDuplexHandler;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import io.netty.util.concurrent.Future;
/*    */ import java.net.SocketAddress;
/*    */ import java.util.concurrent.TimeUnit;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public abstract class BaseProtocolPoller
/*    */   extends ChannelDuplexHandler
/*    */ {
/*    */   private final long interval;
/*    */   private Future<?> timeout;
/*    */   
/*    */   public BaseProtocolPoller(Protocol protocol) {
/* 33 */     this.interval = Context.getConfig().getLong(protocol.getName() + ".interval");
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public void channelActive(ChannelHandlerContext ctx) throws Exception {
/* 40 */     super.channelActive(ctx);
/* 41 */     if (this.interval > 0L) {
/* 42 */       this.timeout = (Future<?>)ctx.executor().scheduleAtFixedRate(() -> sendRequest(ctx.channel(), ctx.channel().remoteAddress()), 0L, this.interval, TimeUnit.SECONDS);
/*    */     }
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public void channelInactive(ChannelHandlerContext ctx) throws Exception {
/* 49 */     super.channelInactive(ctx);
/* 50 */     if (this.timeout != null) {
/* 51 */       this.timeout.cancel(false);
/* 52 */       this.timeout = null;
/*    */     } 
/*    */   }
/*    */   
/*    */   protected abstract void sendRequest(Channel paramChannel, SocketAddress paramSocketAddress);
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\BaseProtocolPoller.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */