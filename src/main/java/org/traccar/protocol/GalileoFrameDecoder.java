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
/*    */ public class GalileoFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int MESSAGE_MINIMUM_LENGTH = 5;
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     if (buf.readableBytes() < 5) {
/* 32 */       return null;
/*    */     }
/*    */     
/* 35 */     int length = buf.getUnsignedShortLE(buf.readerIndex() + 1) & 0x7FFF;
/* 36 */     if (buf.readableBytes() >= length + 5) {
/* 37 */       return buf.readRetainedSlice(length + 5);
/*    */     }
/*    */     
/* 40 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GalileoFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */