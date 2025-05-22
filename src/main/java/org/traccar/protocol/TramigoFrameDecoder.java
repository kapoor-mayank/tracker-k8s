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
/*    */ public class TramigoFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/*    */     int length;
/* 28 */     if (buf.readableBytes() < 20) {
/* 29 */       return null;
/*    */     }
/*    */ 
/*    */     
/* 33 */     if (buf.getUnsignedByte(buf.readerIndex()) == 128) {
/* 34 */       length = buf.getUnsignedShortLE(buf.readerIndex() + 6);
/*    */     } else {
/* 36 */       length = buf.getUnsignedShort(buf.readerIndex() + 6);
/*    */     } 
/*    */     
/* 39 */     if (length >= buf.readableBytes()) {
/* 40 */       return buf.readRetainedSlice(length);
/*    */     }
/*    */     
/* 43 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TramigoFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */