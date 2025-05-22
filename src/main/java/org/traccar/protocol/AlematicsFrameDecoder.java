/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import io.netty.handler.codec.LineBasedFrameDecoder;
/*    */ import org.traccar.NetworkMessage;
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
/*    */ public class AlematicsFrameDecoder
/*    */   extends LineBasedFrameDecoder
/*    */ {
/*    */   private static final int MESSAGE_MINIMUM_LENGTH = 2;
/*    */   
/*    */   public AlematicsFrameDecoder(int maxFrameLength) {
/* 28 */     super(maxFrameLength);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
/* 36 */     if (buf.readableBytes() < 2) {
/* 37 */       return null;
/*    */     }
/*    */     
/* 40 */     if (buf.getUnsignedShort(buf.readerIndex()) == 64248) {
/* 41 */       ByteBuf heartbeat = buf.readRetainedSlice(12);
/* 42 */       if (ctx != null && ctx.channel() != null) {
/* 43 */         ctx.channel().writeAndFlush(new NetworkMessage(heartbeat, ctx.channel().remoteAddress()));
/*    */       }
/*    */     } 
/*    */     
/* 47 */     return super.decode(ctx, buf);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AlematicsFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */