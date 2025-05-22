/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import org.traccar.BaseFrameDecoder;
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
/*    */ public class DualcamFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int MESSAGE_MINIMUM_LENGTH = 4;
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/*    */     int length;
/* 31 */     if (buf.readableBytes() < 4) {
/* 32 */       return null;
/*    */     }
/*    */ 
/*    */     
/* 36 */     if (buf.getUnsignedShort(buf.readerIndex()) == 0) {
/* 37 */       length = 16;
/*    */     } else {
/* 39 */       length = 4 + buf.getUnsignedShort(buf.readerIndex() + 2);
/*    */     } 
/*    */     
/* 42 */     if (buf.readableBytes() >= length) {
/* 43 */       return buf.readRetainedSlice(length);
/*    */     }
/*    */     
/* 46 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\DualcamFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */