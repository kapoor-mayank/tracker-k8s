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
/*    */ public class T57FrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     if (buf.readableBytes() < 10) {
/* 32 */       return null;
/*    */     }
/*    */     
/* 35 */     String type = buf.toString(buf.readerIndex() + 5, 2, StandardCharsets.US_ASCII);
/* 36 */     int count = type.equals("F3") ? 12 : 14;
/*    */     
/* 38 */     int index = 0;
/* 39 */     while (index >= 0 && count > 0) {
/* 40 */       index = buf.indexOf(index + 1, buf.writerIndex(), (byte)35);
/* 41 */       if (index > 0) {
/* 42 */         count--;
/*    */       }
/*    */     } 
/*    */     
/* 46 */     return (index > 0) ? buf.readRetainedSlice(index + 1 - buf.readerIndex()) : null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\T57FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */