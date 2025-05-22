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
/*    */ public class TelicFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 30 */     if (buf.readableBytes() < 4) {
/* 31 */       return null;
/*    */     }
/*    */     
/* 34 */     long length = buf.getUnsignedIntLE(buf.readerIndex());
/*    */     
/* 36 */     if (length < 1024L) {
/* 37 */       if (buf.readableBytes() >= length + 4L) {
/* 38 */         buf.readUnsignedIntLE();
/* 39 */         return buf.readRetainedSlice((int)length);
/*    */       } 
/*    */     } else {
/* 42 */       int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)0);
/* 43 */       if (endIndex >= 0) {
/* 44 */         ByteBuf frame = buf.readRetainedSlice(endIndex - buf.readerIndex());
/* 45 */         buf.readByte();
/* 46 */         if (frame.readableBytes() > 0) {
/* 47 */           return frame;
/*    */         }
/*    */       } 
/*    */     } 
/*    */     
/* 52 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TelicFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */