/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import java.nio.charset.StandardCharsets;
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
/*    */ public class FifotrackFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     if (buf.readableBytes() < 10) {
/* 32 */       return null;
/*    */     }
/*    */     
/* 35 */     int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)44);
/* 36 */     if (index != -1) {
/* 37 */       int length = index - buf.readerIndex() + 3 + Integer.parseInt(buf
/* 38 */           .toString(buf.readerIndex() + 2, index - buf.readerIndex() - 2, StandardCharsets.US_ASCII));
/* 39 */       if (buf.readableBytes() >= length + 2) {
/* 40 */         ByteBuf frame = buf.readRetainedSlice(length);
/* 41 */         buf.skipBytes(2);
/* 42 */         return frame;
/*    */       } 
/*    */     } 
/*    */     
/* 46 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FifotrackFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */