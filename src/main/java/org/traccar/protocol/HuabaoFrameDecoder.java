/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
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
/*    */ public class HuabaoFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 30 */     if (buf.readableBytes() < 2) {
/* 31 */       return null;
/*    */     }
/*    */     
/* 34 */     if (buf.getByte(buf.readerIndex()) == 40) {
/*    */       
/* 36 */       int index = buf.indexOf(buf.readerIndex() + 1, buf.writerIndex(), (byte)41);
/* 37 */       if (index >= 0) {
/* 38 */         return buf.readRetainedSlice(index + 1);
/*    */       }
/*    */     }
/*    */     else {
/*    */       
/* 43 */       int index = buf.indexOf(buf.readerIndex() + 1, buf.writerIndex(), (byte)126);
/* 44 */       if (index >= 0) {
/* 45 */         ByteBuf result = Unpooled.buffer(index + 1 - buf.readerIndex());
/*    */         
/* 47 */         while (buf.readerIndex() <= index) {
/* 48 */           int b = buf.readUnsignedByte();
/* 49 */           if (b == 125) {
/* 50 */             int ext = buf.readUnsignedByte();
/* 51 */             if (ext == 1) {
/* 52 */               result.writeByte(125); continue;
/* 53 */             }  if (ext == 2)
/* 54 */               result.writeByte(126); 
/*    */             continue;
/*    */           } 
/* 57 */           result.writeByte(b);
/*    */         } 
/*    */ 
/*    */         
/* 61 */         return result;
/*    */       } 
/*    */     } 
/*    */ 
/*    */     
/* 66 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\HuabaoFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */