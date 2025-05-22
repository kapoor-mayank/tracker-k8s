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
/*    */ public class BceFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int HANDSHAKE_LENGTH = 7;
/*    */   private boolean header = true;
/*    */   
/*    */   private static byte checksum(ByteBuf buf, int end) {
/* 30 */     byte result = 0;
/* 31 */     for (int i = 0; i < end; i++) {
/* 32 */       result = (byte)(result + buf.getByte(buf.readerIndex() + i));
/*    */     }
/* 34 */     return result;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 41 */     if (this.header && buf.readableBytes() >= 7) {
/* 42 */       buf.skipBytes(7);
/* 43 */       this.header = false;
/*    */     } 
/*    */     
/* 46 */     int end = 8;
/*    */     
/* 48 */     while (buf.readableBytes() >= end + 2 + 1 + 1 + 1) {
/* 49 */       end += buf.getUnsignedShortLE(buf.readerIndex() + end) + 2;
/*    */       
/* 51 */       if (buf.readableBytes() > end && checksum(buf, end) == buf.getByte(buf.readerIndex() + end)) {
/* 52 */         return buf.readRetainedSlice(end + 1);
/*    */       }
/*    */     } 
/*    */     
/* 56 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\BceFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */