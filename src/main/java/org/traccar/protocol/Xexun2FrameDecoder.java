/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import org.traccar.BaseFrameDecoder;
/*    */ import org.traccar.helper.BufferUtil;
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
/*    */ public class Xexun2FrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/*    */     int index;
/* 31 */     if (buf.readableBytes() < 5) {
/* 32 */       return null;
/*    */     }
/*    */     
/* 35 */     ByteBuf flag = Unpooled.wrappedBuffer(new byte[] { -6, -81 });
/*    */     
/*    */     try {
/* 38 */       index = BufferUtil.indexOf(flag, buf, buf.readerIndex() + 2, buf.writerIndex());
/*    */     } finally {
/* 40 */       flag.release();
/*    */     } 
/*    */     
/* 43 */     if (index >= 0) {
/* 44 */       ByteBuf result = Unpooled.buffer(index + 2 - buf.readerIndex());
/*    */       
/* 46 */       while (buf.readerIndex() < index + 2) {
/* 47 */         int b = buf.readUnsignedByte();
/* 48 */         if (b == 251 && buf.isReadable() && buf.getUnsignedByte(buf.readerIndex()) == 191) {
/* 49 */           buf.readUnsignedByte();
/* 50 */           int ext = buf.readUnsignedByte();
/* 51 */           if (ext == 1) {
/* 52 */             result.writeByte(250);
/* 53 */             result.writeByte(175); continue;
/* 54 */           }  if (ext == 2) {
/* 55 */             result.writeByte(251);
/* 56 */             result.writeByte(191);
/*    */           }  continue;
/*    */         } 
/* 59 */         result.writeByte(b);
/*    */       } 
/*    */ 
/*    */       
/* 63 */       return result;
/*    */     } 
/*    */     
/* 66 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Xexun2FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */