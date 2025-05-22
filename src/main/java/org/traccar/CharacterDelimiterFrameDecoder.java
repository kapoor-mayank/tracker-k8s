/*    */ package org.traccar;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import io.netty.handler.codec.DelimiterBasedFrameDecoder;
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
/*    */ public class CharacterDelimiterFrameDecoder
/*    */   extends DelimiterBasedFrameDecoder
/*    */ {
/*    */   private static ByteBuf createDelimiter(char delimiter) {
/* 25 */     byte[] buf = { (byte)delimiter };
/* 26 */     return Unpooled.wrappedBuffer(buf);
/*    */   }
/*    */   
/*    */   private static ByteBuf createDelimiter(String delimiter) {
/* 30 */     byte[] buf = new byte[delimiter.length()];
/* 31 */     for (int i = 0; i < delimiter.length(); i++) {
/* 32 */       buf[i] = (byte)delimiter.charAt(i);
/*    */     }
/* 34 */     return Unpooled.wrappedBuffer(buf);
/*    */   }
/*    */   
/*    */   private static ByteBuf[] convertDelimiters(String[] delimiters) {
/* 38 */     ByteBuf[] result = new ByteBuf[delimiters.length];
/* 39 */     for (int i = 0; i < delimiters.length; i++) {
/* 40 */       result[i] = createDelimiter(delimiters[i]);
/*    */     }
/* 42 */     return result;
/*    */   }
/*    */   
/*    */   public CharacterDelimiterFrameDecoder(int maxFrameLength, char delimiter) {
/* 46 */     super(maxFrameLength, createDelimiter(delimiter));
/*    */   }
/*    */   
/*    */   public CharacterDelimiterFrameDecoder(int maxFrameLength, String delimiter) {
/* 50 */     super(maxFrameLength, createDelimiter(delimiter));
/*    */   }
/*    */   
/*    */   public CharacterDelimiterFrameDecoder(int maxFrameLength, boolean stripDelimiter, String delimiter) {
/* 54 */     super(maxFrameLength, stripDelimiter, createDelimiter(delimiter));
/*    */   }
/*    */   
/*    */   public CharacterDelimiterFrameDecoder(int maxFrameLength, String... delimiters) {
/* 58 */     super(maxFrameLength, convertDelimiters(delimiters));
/*    */   }
/*    */   
/*    */   public CharacterDelimiterFrameDecoder(int maxFrameLength, boolean stripDelimiter, String... delimiters) {
/* 62 */     super(maxFrameLength, stripDelimiter, convertDelimiters(delimiters));
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\CharacterDelimiterFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */