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
/*    */ public class OrionFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 29 */     int length = 6;
/*    */     
/* 31 */     if (buf.readableBytes() >= length) {
/*    */       
/* 33 */       int type = buf.getUnsignedByte(buf.readerIndex() + 2) & 0xF;
/*    */       
/* 35 */       if (type == 0 && buf.readableBytes() >= length + 5) {
/*    */         
/* 37 */         int index = buf.readerIndex() + 3;
/* 38 */         int count = buf.getUnsignedByte(index) & 0xF;
/* 39 */         index += 5;
/* 40 */         length += 5;
/*    */         
/* 42 */         for (int i = 0; i < count; i++) {
/* 43 */           if (buf.readableBytes() < length) {
/* 44 */             return null;
/*    */           }
/* 46 */           int logLength = buf.getUnsignedByte(index + 1);
/* 47 */           index += logLength;
/* 48 */           length += logLength;
/*    */         } 
/*    */         
/* 51 */         if (buf.readableBytes() >= length) {
/* 52 */           return buf.readRetainedSlice(length);
/*    */         }
/*    */       }
/* 55 */       else if (type == 3 && buf.readableBytes() >= length + 12) {
/*    */         
/* 57 */         length += buf.getUnsignedShortLE(buf.readerIndex() + 8);
/* 58 */         if (buf.readableBytes() >= length) {
/* 59 */           return buf.readRetainedSlice(length);
/*    */         }
/*    */       } 
/*    */     } 
/*    */ 
/*    */     
/* 65 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OrionFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */