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
/*    */ public class Tk103FrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 30 */     if (buf.readableBytes() < 2) {
/* 31 */       return null;
/*    */     }
/*    */     
/* 34 */     int frameStartIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)40);
/* 35 */     if (frameStartIndex == -1) {
/* 36 */       buf.clear();
/* 37 */       return null;
/*    */     } 
/*    */     
/*    */     int frameEndIndex, freeTextSymbolCounter;
/* 41 */     for (frameEndIndex = frameStartIndex, freeTextSymbolCounter = 0;; frameEndIndex++) {
/* 42 */       int freeTextIndex = frameEndIndex;
/* 43 */       frameEndIndex = buf.indexOf(frameEndIndex, buf.writerIndex(), (byte)41);
/* 44 */       if (frameEndIndex == -1) {
/*    */         break;
/*    */       }
/* 47 */       for (;; freeTextIndex++, freeTextSymbolCounter++) {
/* 48 */         freeTextIndex = buf.indexOf(freeTextIndex, frameEndIndex, (byte)36);
/* 49 */         if (freeTextIndex == -1 || freeTextIndex >= frameEndIndex) {
/*    */           break;
/*    */         }
/*    */       } 
/* 53 */       if (freeTextSymbolCounter % 2 == 0) {
/*    */         break;
/*    */       }
/*    */     } 
/*    */     
/* 58 */     if (frameEndIndex == -1) {
/* 59 */       while (buf.readableBytes() > 1024) {
/* 60 */         int discardUntilIndex = buf.indexOf(buf.readerIndex() + 1, buf.writerIndex(), (byte)40);
/* 61 */         if (discardUntilIndex == -1) {
/* 62 */           buf.clear(); continue;
/*    */         } 
/* 64 */         buf.readerIndex(discardUntilIndex);
/*    */       } 
/*    */       
/* 67 */       return null;
/*    */     } 
/*    */     
/* 70 */     buf.readerIndex(frameStartIndex);
/*    */     
/* 72 */     return buf.readRetainedSlice(frameEndIndex + 1 - frameStartIndex);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Tk103FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */