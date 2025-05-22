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
/*    */ public class MeitrackFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     if (buf.readableBytes() < 10) {
/* 32 */       return null;
/*    */     }
/*    */     
/* 35 */     int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)44);
/* 36 */     if (index != -1) {
/* 37 */       int length = index - buf.readerIndex() + Integer.parseInt(buf
/* 38 */           .toString(buf.readerIndex() + 3, index - buf.readerIndex() - 3, StandardCharsets.US_ASCII));
/* 39 */       if (buf.readableBytes() >= length) {
/* 40 */         return buf.readRetainedSlice(length);
/*    */       }
/*    */     } 
/*    */     
/* 44 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MeitrackFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */