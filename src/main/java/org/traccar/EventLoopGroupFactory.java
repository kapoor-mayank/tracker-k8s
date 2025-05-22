/*    */ package org.traccar;
/*    */ 
/*    */ import io.netty.channel.EventLoopGroup;
/*    */ import io.netty.channel.nio.NioEventLoopGroup;
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
/*    */ public final class EventLoopGroupFactory
/*    */ {
/* 23 */   private static EventLoopGroup bossGroup = (EventLoopGroup)new NioEventLoopGroup();
/* 24 */   private static EventLoopGroup workerGroup = (EventLoopGroup)new NioEventLoopGroup();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static EventLoopGroup getBossGroup() {
/* 30 */     return bossGroup;
/*    */   }
/*    */   
/*    */   public static EventLoopGroup getWorkerGroup() {
/* 34 */     return workerGroup;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\EventLoopGroupFactory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */