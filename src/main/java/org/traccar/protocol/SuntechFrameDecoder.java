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
/*    */ public class SuntechFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private ByteBuf readFrame(ByteBuf buf, int delimiterIndex) {
/* 26 */     ByteBuf frame = buf.readRetainedSlice(delimiterIndex - buf.readerIndex());
/* 27 */     buf.skipBytes(1);
/* 28 */     return frame;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 35 */     if (buf.getByte(buf.readerIndex() + 1) == 0) {
/*    */       
/* 37 */       int length = 3 + buf.getShort(buf.readerIndex() + 1);
/* 38 */       if (buf.readableBytes() >= length) {
/* 39 */         return buf.readRetainedSlice(length);
/*    */       }
/*    */     }
/*    */     else {
/*    */       
/* 44 */       int delimiterIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)13);
/* 45 */       while (delimiterIndex > 0) {
/* 46 */         if (delimiterIndex + 1 < buf.writerIndex() && buf.getByte(delimiterIndex + 1) == 10) {
/* 47 */           delimiterIndex = buf.indexOf(delimiterIndex + 1, buf.writerIndex(), (byte)13); continue;
/*    */         } 
/* 49 */         return readFrame(buf, delimiterIndex);
/*    */       } 
/*    */     } 
/*    */ 
/*    */ 
/*    */     
/* 55 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SuntechFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */