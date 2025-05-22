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
/*    */ public class NvsFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/*    */     int length;
/* 29 */     if (buf.readableBytes() < 6) {
/* 30 */       return null;
/*    */     }
/*    */ 
/*    */     
/* 34 */     if (buf.getUnsignedByte(buf.readerIndex()) == 0) {
/* 35 */       length = 2 + buf.getUnsignedShort(buf.readerIndex());
/*    */     } else {
/* 37 */       length = 6 + buf.getUnsignedShort(buf.readerIndex() + 4) + 2;
/*    */     } 
/*    */     
/* 40 */     if (buf.readableBytes() >= length) {
/* 41 */       return buf.readRetainedSlice(length);
/*    */     }
/*    */     
/* 44 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NvsFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */