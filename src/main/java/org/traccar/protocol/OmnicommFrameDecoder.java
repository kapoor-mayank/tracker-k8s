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
/*    */ public class OmnicommFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 30 */     if (buf.readableBytes() < 6) {
/* 31 */       return null;
/*    */     }
/*    */     
/* 34 */     int endIndex = buf.getUnsignedShortLE(buf.readerIndex() + 2) + buf.readerIndex() + 6;
/* 35 */     if (buf.writerIndex() < endIndex) {
/* 36 */       return null;
/*    */     }
/*    */     
/* 39 */     ByteBuf result = Unpooled.buffer();
/* 40 */     result.writeByte(buf.readUnsignedByte());
/* 41 */     while (buf.readerIndex() < endIndex) {
/* 42 */       int b = buf.readUnsignedByte();
/* 43 */       if (b == 219) {
/* 44 */         int ext = buf.readUnsignedByte();
/* 45 */         if (ext == 220) {
/* 46 */           result.writeByte(192);
/* 47 */         } else if (ext == 221) {
/* 48 */           result.writeByte(219);
/*    */         } 
/* 50 */         endIndex++; continue;
/*    */       } 
/* 52 */       result.writeByte(b);
/*    */     } 
/*    */     
/* 55 */     return result;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OmnicommFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */