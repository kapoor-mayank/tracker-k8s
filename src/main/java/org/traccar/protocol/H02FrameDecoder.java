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
/*    */ public class H02FrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int MESSAGE_SHORT = 32;
/*    */   private static final int MESSAGE_LONG = 45;
/*    */   private int messageLength;
/*    */   
/*    */   public H02FrameDecoder(int messageLength) {
/* 31 */     this.messageLength = messageLength;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/*    */     int index;
/* 38 */     char marker = (char)buf.getByte(buf.readerIndex());
/*    */     
/* 40 */     while (marker != '*' && marker != '$' && marker != 'X' && buf.readableBytes() > 0) {
/* 41 */       buf.skipBytes(1);
/* 42 */       if (buf.readableBytes() > 0) {
/* 43 */         marker = (char)buf.getByte(buf.readerIndex());
/*    */       }
/*    */     } 
/*    */     
/* 47 */     switch (marker) {
/*    */ 
/*    */       
/*    */       case '*':
/* 51 */         index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)35);
/* 52 */         if (index != -1) {
/* 53 */           ByteBuf result = buf.readRetainedSlice(index + 1 - buf.readerIndex());
/* 54 */           while (buf.isReadable() && (buf
/* 55 */             .getByte(buf.readerIndex()) == 13 || buf.getByte(buf.readerIndex()) == 10)) {
/* 56 */             buf.readByte();
/*    */           }
/* 58 */           return result;
/*    */         } 
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
/* 92 */         return null;case '$': if (this.messageLength == 0) if (buf.readableBytes() == 45) { this.messageLength = 45; } else { this.messageLength = 32; }   if (buf.readableBytes() >= this.messageLength) return buf.readRetainedSlice(this.messageLength);  return null;case 'X': if (buf.readableBytes() >= 32) return buf.readRetainedSlice(32);  return null;
/*    */     } 
/*    */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\H02FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */