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
/*    */ public class CellocatorFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int MESSAGE_MINIMUM_LENGTH = 15;
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     if (buf.readableBytes() < 15) {
/* 32 */       return null;
/*    */     }
/*    */     
/* 35 */     int length = 0;
/* 36 */     int type = buf.getUnsignedByte(4);
/* 37 */     switch (type) {
/*    */       case 0:
/* 39 */         length = 70;
/*    */         break;
/*    */       case 3:
/* 42 */         length = 31;
/*    */         break;
/*    */       case 7:
/* 45 */         length = 70;
/*    */         break;
/*    */       case 8:
/* 48 */         if (buf.readableBytes() >= 19) {
/* 49 */           length = 19 + buf.getUnsignedShortLE(buf.readerIndex() + 16);
/*    */         }
/*    */         break;
/*    */       case 9:
/* 53 */         length = 15 + buf.getUnsignedByte(buf.readerIndex() + 13);
/*    */         break;
/*    */       case 11:
/* 56 */         length = 16 + buf.getUnsignedShortLE(buf.readerIndex() + 13);
/*    */         break;
/*    */     } 
/*    */ 
/*    */ 
/*    */     
/* 62 */     if (length > 0 && buf.readableBytes() >= length) {
/* 63 */       return buf.readRetainedSlice(length);
/*    */     }
/*    */     
/* 66 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CellocatorFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */