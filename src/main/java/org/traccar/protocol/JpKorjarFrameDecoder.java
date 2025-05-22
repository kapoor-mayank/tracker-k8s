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
/*    */ public class JpKorjarFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 30 */     if (buf.readableBytes() < 80) {
/* 31 */       return null;
/*    */     }
/*    */     
/* 34 */     int spaceIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)32);
/* 35 */     if (spaceIndex == -1) {
/* 36 */       return null;
/*    */     }
/*    */     
/* 39 */     int endIndex = buf.indexOf(spaceIndex, buf.writerIndex(), (byte)44);
/* 40 */     if (endIndex == -1) {
/* 41 */       return null;
/*    */     }
/*    */     
/* 44 */     return buf.readRetainedSlice(endIndex + 1);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\JpKorjarFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */