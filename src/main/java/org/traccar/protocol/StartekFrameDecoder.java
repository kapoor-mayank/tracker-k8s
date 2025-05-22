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
/*    */ public class StartekFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     if (buf.readableBytes() < 10) {
/* 32 */       return null;
/*    */     }
/*    */     
/* 35 */     int lengthIndex = buf.readerIndex() + 3;
/* 36 */     int dividerIndex = buf.indexOf(lengthIndex, buf.writerIndex(), (byte)44);
/* 37 */     if (dividerIndex > 0) {
/* 38 */       int lengthOffset = dividerIndex - buf.readerIndex() + 4;
/* 39 */       int length = lengthOffset + Integer.parseInt(buf.getCharSequence(lengthIndex, dividerIndex - lengthIndex, StandardCharsets.US_ASCII)
/* 40 */           .toString());
/* 41 */       if (buf.readableBytes() >= length) {
/* 42 */         return buf.readRetainedSlice(length);
/*    */       }
/*    */     } 
/*    */     
/* 46 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\StartekFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */