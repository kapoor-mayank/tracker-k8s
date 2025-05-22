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
/*    */ 
/*    */ public class Vt200FrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)41) + 1;
/* 32 */     if (endIndex > 0) {
/*    */       
/* 34 */       ByteBuf frame = Unpooled.buffer();
/*    */       
/* 36 */       while (buf.readerIndex() < endIndex) {
/* 37 */         int b = buf.readByte();
/* 38 */         if (b == 61) {
/* 39 */           frame.writeByte(buf.readByte() ^ 0x3D); continue;
/*    */         } 
/* 41 */         frame.writeByte(b);
/*    */       } 
/*    */ 
/*    */       
/* 45 */       return frame;
/*    */     } 
/*    */ 
/*    */     
/* 49 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Vt200FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */