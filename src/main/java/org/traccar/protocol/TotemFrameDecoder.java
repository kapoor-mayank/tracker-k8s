/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import java.nio.charset.StandardCharsets;
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
/*    */ 
/*    */ 
/*    */ public class TotemFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/*    */     int length;
/* 33 */     if (buf.readableBytes() < 10) {
/* 34 */       return null;
/*    */     }
/*    */     
/* 37 */     int beginIndex = BufferUtil.indexOf("$$", buf);
/* 38 */     if (beginIndex == -1)
/* 39 */       return null; 
/* 40 */     if (beginIndex > buf.readerIndex()) {
/* 41 */       buf.readerIndex(beginIndex);
/*    */     }
/*    */ 
/*    */ 
/*    */     
/* 46 */     if (buf.getByte(buf.readerIndex() + 2) == 48) {
/* 47 */       length = Integer.parseInt(buf.toString(buf.readerIndex() + 2, 4, StandardCharsets.US_ASCII));
/*    */     } else {
/* 49 */       length = Integer.parseInt(buf.toString(buf.readerIndex() + 2, 2, StandardCharsets.US_ASCII), 16);
/*    */     } 
/*    */     
/* 52 */     if (length <= buf.readableBytes()) {
/* 53 */       return buf.readRetainedSlice(length);
/*    */     }
/*    */     
/* 56 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TotemFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */