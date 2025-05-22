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
/*    */ 
/*    */ public class UlbotechFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     if (buf.readableBytes() < 2) {
/* 32 */       return null;
/*    */     }
/*    */     
/* 35 */     if (buf.getUnsignedByte(buf.readerIndex()) == 248) {
/*    */       
/* 37 */       int index = buf.indexOf(buf.readerIndex() + 1, buf.writerIndex(), (byte)-8);
/* 38 */       if (index != -1) {
/* 39 */         ByteBuf result = Unpooled.buffer(index + 1 - buf.readerIndex());
/*    */         
/* 41 */         while (buf.readerIndex() <= index) {
/* 42 */           int b = buf.readUnsignedByte();
/* 43 */           if (b == 247) {
/* 44 */             int ext = buf.readUnsignedByte();
/* 45 */             if (ext == 0) {
/* 46 */               result.writeByte(247); continue;
/* 47 */             }  if (ext == 15)
/* 48 */               result.writeByte(248); 
/*    */             continue;
/*    */           } 
/* 51 */           result.writeByte(b);
/*    */         } 
/*    */ 
/*    */         
/* 55 */         return result;
/*    */       }
/*    */     
/*    */     } else {
/*    */       
/* 60 */       int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)35);
/* 61 */       if (index != -1) {
/* 62 */         return buf.readRetainedSlice(index + 1 - buf.readerIndex());
/*    */       }
/*    */     } 
/*    */ 
/*    */     
/* 67 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\UlbotechFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */