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
/*    */ public class PstFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 30 */     while (buf.isReadable() && buf.getByte(buf.readerIndex()) == 40) {
/* 31 */       buf.skipBytes(1);
/*    */     }
/*    */     
/* 34 */     int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)41);
/* 35 */     if (endIndex > 0) {
/* 36 */       ByteBuf result = Unpooled.buffer(endIndex - buf.readerIndex());
/* 37 */       while (buf.readerIndex() < endIndex) {
/* 38 */         int b = buf.readUnsignedByte();
/* 39 */         if (b == 39) {
/* 40 */           b = buf.readUnsignedByte() ^ 0x40;
/*    */         }
/* 42 */         result.writeByte(b);
/*    */       } 
/* 44 */       buf.skipBytes(1);
/* 45 */       return result;
/*    */     } 
/*    */     
/* 48 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PstFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */