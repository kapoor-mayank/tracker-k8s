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
/*    */ public class XexunFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     if (buf.readableBytes() < 80) {
/* 32 */       return null;
/*    */     }
/*    */     
/* 35 */     int beginIndex = BufferUtil.indexOf("GPRMC", buf);
/* 36 */     if (beginIndex == -1) {
/* 37 */       beginIndex = BufferUtil.indexOf("GNRMC", buf);
/* 38 */       if (beginIndex == -1) {
/* 39 */         return null;
/*    */       }
/*    */     } 
/*    */     
/* 43 */     int identifierIndex = BufferUtil.indexOf("imei:", buf, beginIndex, buf.writerIndex());
/* 44 */     if (identifierIndex == -1) {
/* 45 */       return null;
/*    */     }
/*    */     
/* 48 */     int endIndex = buf.indexOf(identifierIndex, buf.writerIndex(), (byte)44);
/* 49 */     if (endIndex == -1) {
/* 50 */       return null;
/*    */     }
/*    */     
/* 53 */     buf.skipBytes(beginIndex - buf.readerIndex());
/*    */     
/* 55 */     return buf.readRetainedSlice(endIndex - beginIndex + 1);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\XexunFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */