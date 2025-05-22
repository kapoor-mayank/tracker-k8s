/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
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
/*    */ public class WatchFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 30 */     int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)93) + 1;
/* 31 */     if (endIndex > 0) {
/* 32 */       ByteBuf frame = Unpooled.buffer();
/* 33 */       while (buf.readerIndex() < endIndex) {
/* 34 */         byte b1 = buf.readByte();
/* 35 */         if (b1 == 125) {
/* 36 */           byte b2 = buf.readByte();
/* 37 */           switch (b2) {
/*    */             case 1:
/* 39 */               frame.writeByte(125);
/*    */               continue;
/*    */             case 2:
/* 42 */               frame.writeByte(91);
/*    */               continue;
/*    */             case 3:
/* 45 */               frame.writeByte(93);
/*    */               continue;
/*    */             case 4:
/* 48 */               frame.writeByte(44);
/*    */               continue;
/*    */             case 5:
/* 51 */               frame.writeByte(42);
/*    */               continue;
/*    */           } 
/* 54 */           throw new IllegalArgumentException(String.format("unexpected byte at %d: 0x%02x", new Object[] {
/* 55 */                   Integer.valueOf(buf.readerIndex() - 1), Byte.valueOf(b2)
/*    */                 }));
/*    */         } 
/* 58 */         frame.writeByte(b1);
/*    */       } 
/*    */       
/* 61 */       return frame;
/*    */     } 
/*    */     
/* 64 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WatchFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */