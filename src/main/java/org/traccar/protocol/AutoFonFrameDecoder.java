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
/*    */ public class AutoFonFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/*    */     int length;
/* 31 */     if (buf.readableBytes() < 12) {
/* 32 */       return null;
/*    */     }
/*    */ 
/*    */     
/* 36 */     switch (buf.getUnsignedByte(buf.readerIndex())) {
/*    */       case 16:
/* 38 */         length = 12;
/*    */         break;
/*    */       case 17:
/* 41 */         length = 78;
/*    */         break;
/*    */       case 18:
/* 44 */         length = 257;
/*    */         break;
/*    */       case 65:
/* 47 */         length = 19;
/*    */         break;
/*    */       case 2:
/* 50 */         length = 34;
/*    */         break;
/*    */       default:
/* 53 */         length = 0;
/*    */         break;
/*    */     } 
/*    */ 
/*    */     
/* 58 */     if (length != 0 && buf.readableBytes() >= length) {
/* 59 */       return buf.readRetainedSlice(length);
/*    */     }
/*    */     
/* 62 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AutoFonFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */