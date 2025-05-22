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
/*    */ public class EskyFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 29 */     int startIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)69);
/* 30 */     if (startIndex >= 0) {
/* 31 */       buf.readerIndex(startIndex);
/* 32 */       int endIndex = buf.indexOf(buf.readerIndex() + 1, buf.writerIndex(), (byte)69);
/* 33 */       if (endIndex > 0) {
/* 34 */         return buf.readRetainedSlice(endIndex - buf.readerIndex());
/*    */       }
/* 36 */       return buf.readRetainedSlice(buf.readableBytes());
/*    */     } 
/*    */ 
/*    */     
/* 40 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EskyFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */