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
/*    */ public class IntellitracFrameDecoder
/*    */   extends LineBasedFrameDecoder
/*    */ {
/*    */   private static final int MESSAGE_MINIMUM_LENGTH = 0;
/*    */   
/*    */   public IntellitracFrameDecoder(int maxFrameLength) {
/* 28 */     super(maxFrameLength);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
/* 37 */     if (buf.readableBytes() < 0) {
/* 38 */       return null;
/*    */     }
/*    */ 
/*    */     
/* 42 */     if (buf.getUnsignedShort(buf.readerIndex()) == 64248) {
/* 43 */       ByteBuf syncMessage = buf.readRetainedSlice(8);
/* 44 */       if (ctx != null && ctx.channel() != null) {
/* 45 */         ctx.channel().writeAndFlush(new NetworkMessage(syncMessage, ctx.channel().remoteAddress()));
/*    */       }
/*    */     } 
/*    */     
/* 49 */     return super.decode(ctx, buf);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\IntellitracFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */