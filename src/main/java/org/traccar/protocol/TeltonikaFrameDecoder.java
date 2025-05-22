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
/*    */ 
/*    */ public class TeltonikaFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int MESSAGE_MINIMUM_LENGTH = 12;
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 33 */     if (buf.readableBytes() < 12) {
/* 34 */       return null;
/*    */     }
/*    */ 
/*    */     
/* 38 */     int length = buf.getUnsignedShort(buf.readerIndex());
/* 39 */     if (length > 0) {
/* 40 */       if (buf.readableBytes() >= length + 2) {
/* 41 */         return buf.readRetainedSlice(length + 2);
/*    */       }
/*    */     } else {
/* 44 */       int dataLength = buf.getInt(buf.readerIndex() + 4);
/* 45 */       if (buf.readableBytes() >= dataLength + 12) {
/* 46 */         return buf.readRetainedSlice(dataLength + 12);
/*    */       }
/*    */     } 
/*    */     
/* 50 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TeltonikaFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */