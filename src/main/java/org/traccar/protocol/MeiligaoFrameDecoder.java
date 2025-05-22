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
/*    */ public class MeiligaoFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int MESSAGE_HEADER = 4;
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 32 */     while (buf.isReadable() && buf.getUnsignedByte(buf.readerIndex()) != 36) {
/* 33 */       buf.readByte();
/*    */     }
/*    */ 
/*    */     
/* 37 */     if (buf.readableBytes() >= 4) {
/* 38 */       int length = buf.getUnsignedShort(buf.readerIndex() + 2);
/* 39 */       if (buf.readableBytes() >= length) {
/* 40 */         return buf.readRetainedSlice(length);
/*    */       }
/*    */     } 
/*    */     
/* 44 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MeiligaoFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */