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
/*    */ 
/*    */ 
/*    */ public class NavigilFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int MESSAGE_HEADER = 20;
/*    */   private static final long PREAMBLE = 611841526L;
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 33 */     if (buf.readableBytes() < 20) {
/* 34 */       return null;
/*    */     }
/*    */ 
/*    */     
/* 38 */     boolean hasPreamble = false;
/* 39 */     if (buf.getUnsignedIntLE(buf.readerIndex()) == 611841526L) {
/* 40 */       hasPreamble = true;
/*    */     }
/*    */ 
/*    */     
/* 44 */     int length = buf.getUnsignedShortLE(buf.readerIndex() + 6);
/* 45 */     if (buf.readableBytes() >= length) {
/* 46 */       if (hasPreamble) {
/* 47 */         buf.readUnsignedIntLE();
/* 48 */         length -= 4;
/*    */       } 
/* 50 */       return buf.readRetainedSlice(length);
/*    */     } 
/*    */     
/* 53 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NavigilFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */