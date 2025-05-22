/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
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
/*    */ public class ArnaviFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int HEADER_LENGTH = 10;
/*    */   private static final int PACKET_WRAPPER_LENGTH = 8;
/*    */   private static final int RESULT_TYPE = 253;
/*    */   private static final byte PACKAGE_END_SIGN = 93;
/*    */   private boolean firstPacket = true;
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 37 */     if (buf.readableBytes() < 4) {
/* 38 */       return null;
/*    */     }
/*    */     
/* 41 */     if (buf.getByte(buf.readerIndex()) == 36) {
/*    */       
/* 43 */       int index = BufferUtil.indexOf("\r\n", buf);
/* 44 */       if (index > 0) {
/* 45 */         ByteBuf frame = buf.readRetainedSlice(index - buf.readerIndex());
/* 46 */         buf.skipBytes(2);
/* 47 */         return frame;
/*    */       } 
/*    */     } else {
/*    */       int length;
/*    */ 
/*    */       
/* 53 */       if (this.firstPacket) {
/* 54 */         this.firstPacket = false;
/* 55 */         length = 10;
/*    */       } else {
/* 57 */         int type = buf.getUnsignedByte(1);
/* 58 */         if (type == 253) {
/* 59 */           length = 4;
/*    */         } else {
/* 61 */           int index = 2;
/* 62 */           while (index + 8 < buf.readableBytes() && buf
/* 63 */             .getByte(index) != 93) {
/* 64 */             index += 8 + buf.getUnsignedShortLE(index + 1);
/*    */           }
/* 66 */           if (buf.getByte(index) != 93) {
/* 67 */             return null;
/*    */           }
/* 69 */           length = index + 1;
/*    */         } 
/*    */       } 
/*    */       
/* 73 */       if (buf.readableBytes() >= length) {
/* 74 */         return buf.readRetainedSlice(length);
/*    */       }
/*    */     } 
/*    */ 
/*    */     
/* 79 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ArnaviFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */