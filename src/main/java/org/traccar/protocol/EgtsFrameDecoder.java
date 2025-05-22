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
/*    */ public class EgtsFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 29 */     if (buf.readableBytes() < 10) {
/* 30 */       return null;
/*    */     }
/*    */     
/* 33 */     int headerLength = buf.getUnsignedByte(buf.readerIndex() + 3);
/* 34 */     int frameLength = buf.getUnsignedShortLE(buf.readerIndex() + 5);
/*    */     
/* 36 */     int length = headerLength + frameLength + ((frameLength > 0) ? 2 : 0);
/*    */     
/* 38 */     if (buf.readableBytes() >= length) {
/* 39 */       return buf.readRetainedSlice(length);
/*    */     }
/*    */     
/* 42 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EgtsFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */