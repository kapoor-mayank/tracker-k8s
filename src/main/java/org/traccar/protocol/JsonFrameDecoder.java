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
/*    */ public class JsonFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 29 */     int startIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)123);
/* 30 */     if (startIndex >= 0) {
/*    */       
/* 32 */       buf.readerIndex(startIndex);
/*    */       
/* 34 */       int currentIndex = startIndex + 1;
/* 35 */       int nesting = 1;
/* 36 */       while (currentIndex < buf.writerIndex() && nesting > 0) {
/* 37 */         byte currentByte = buf.getByte(currentIndex);
/* 38 */         if (currentByte == 123) {
/* 39 */           nesting++;
/* 40 */         } else if (currentByte == 125) {
/* 41 */           nesting--;
/*    */         } 
/* 43 */         currentIndex++;
/*    */       } 
/*    */       
/* 46 */       if (nesting == 0) {
/* 47 */         return buf.readRetainedSlice(currentIndex - startIndex);
/*    */       }
/*    */     } 
/*    */ 
/*    */     
/* 52 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\JsonFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */