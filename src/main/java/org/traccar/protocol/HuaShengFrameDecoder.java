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
/*    */ public class HuaShengFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 30 */     if (buf.readableBytes() < 2) {
/* 31 */       return null;
/*    */     }
/*    */     
/* 34 */     int index = buf.indexOf(buf.readerIndex() + 1, buf.writerIndex(), (byte)-64);
/* 35 */     if (index != -1) {
/* 36 */       ByteBuf result = Unpooled.buffer(index + 1 - buf.readerIndex());
/*    */       
/* 38 */       while (buf.readerIndex() <= index) {
/* 39 */         int b = buf.readUnsignedByte();
/* 40 */         if (b == 219) {
/* 41 */           int ext = buf.readUnsignedByte();
/* 42 */           if (ext == 220) {
/* 43 */             result.writeByte(192); continue;
/* 44 */           }  if (ext == 221)
/* 45 */             result.writeByte(219); 
/*    */           continue;
/*    */         } 
/* 48 */         result.writeByte(b);
/*    */       } 
/*    */ 
/*    */       
/* 52 */       return result;
/*    */     } 
/*    */     
/* 55 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\HuaShengFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */