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
/*    */ public class TmgFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private boolean isLetter(byte c) {
/* 27 */     return (c >= 97 && c <= 122);
/*    */   }
/*    */   
/*    */   private int findHeader(ByteBuf buffer) {
/* 31 */     int guessedIndex = buffer.indexOf(buffer.readerIndex(), buffer.writerIndex(), (byte)36);
/* 32 */     while (guessedIndex != -1 && buffer.writerIndex() - guessedIndex >= 5) {
/* 33 */       if (buffer.getByte(guessedIndex + 4) == 44 && 
/* 34 */         isLetter(buffer.getByte(guessedIndex + 1)) && 
/* 35 */         isLetter(buffer.getByte(guessedIndex + 2)) && 
/* 36 */         isLetter(buffer.getByte(guessedIndex + 3))) {
/* 37 */         return guessedIndex;
/*    */       }
/* 39 */       guessedIndex = buffer.indexOf(guessedIndex, buffer.writerIndex(), (byte)36);
/*    */     } 
/* 41 */     return -1;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 48 */     int beginIndex = findHeader(buf);
/*    */     
/* 50 */     if (beginIndex >= 0) {
/*    */       
/* 52 */       buf.readerIndex(beginIndex);
/*    */       
/* 54 */       int endIndex = buf.indexOf(beginIndex, buf.writerIndex(), (byte)10);
/*    */       
/* 56 */       if (endIndex >= 0) {
/* 57 */         ByteBuf frame = buf.readRetainedSlice(endIndex - beginIndex);
/* 58 */         buf.readByte();
/* 59 */         return frame;
/*    */       } 
/*    */     } 
/*    */ 
/*    */     
/* 64 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TmgFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */