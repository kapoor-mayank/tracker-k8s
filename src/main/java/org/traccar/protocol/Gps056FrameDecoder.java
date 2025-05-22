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
/*    */ public class Gps056FrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int MESSAGE_HEADER = 4;
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 33 */     if (buf.readableBytes() >= 4) {
/* 34 */       int length = Integer.parseInt(buf.toString(2, 2, StandardCharsets.US_ASCII)) + 5;
/* 35 */       if (buf.readableBytes() >= length) {
/* 36 */         ByteBuf frame = buf.readRetainedSlice(length);
/* 37 */         while (buf.isReadable() && buf.getUnsignedByte(buf.readerIndex()) != 36) {
/* 38 */           buf.readByte();
/*    */         }
/* 40 */         return frame;
/*    */       } 
/*    */     } 
/*    */     
/* 44 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gps056FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */