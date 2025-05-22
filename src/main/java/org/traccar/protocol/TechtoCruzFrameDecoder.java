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
/*    */ public class TechtoCruzFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     int lengthStart = buf.readerIndex() + 3;
/* 32 */     int lengthEnd = buf.indexOf(lengthStart, buf.writerIndex(), (byte)44);
/* 33 */     if (lengthEnd > 0) {
/*    */       
/* 35 */       int length = lengthStart + Integer.parseInt(buf.toString(lengthStart, lengthEnd - lengthStart, StandardCharsets.US_ASCII));
/* 36 */       if (buf.readableBytes() >= length) {
/* 37 */         return buf.readRetainedSlice(length);
/*    */       }
/*    */     } 
/*    */     
/* 41 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TechtoCruzFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */