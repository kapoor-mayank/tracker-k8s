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
/*    */ public class AplicomFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 30 */     while (buf.isReadable() && Character.isDigit(buf.getByte(buf.readerIndex()))) {
/* 31 */       buf.readByte();
/*    */     }
/*    */ 
/*    */     
/* 35 */     if (buf.readableBytes() < 11) {
/* 36 */       return null;
/*    */     }
/*    */ 
/*    */     
/* 40 */     int version = buf.getUnsignedByte(buf.readerIndex() + 1);
/* 41 */     int offset = 5;
/* 42 */     if ((version & 0x80) != 0) {
/* 43 */       offset += 4;
/*    */     }
/*    */ 
/*    */     
/* 47 */     int length = buf.getUnsignedShort(buf.readerIndex() + offset);
/* 48 */     offset += 2;
/* 49 */     if ((version & 0x40) != 0) {
/* 50 */       offset += 3;
/*    */     }
/* 52 */     length += offset;
/*    */ 
/*    */     
/* 55 */     if (buf.readableBytes() >= length) {
/* 56 */       return buf.readRetainedSlice(length);
/*    */     }
/*    */     
/* 59 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AplicomFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */