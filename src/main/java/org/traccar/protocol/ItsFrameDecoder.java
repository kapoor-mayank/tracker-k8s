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
/*    */ public class ItsFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int MINIMUM_LENGTH = 20;
/*    */   
/*    */   private ByteBuf readFrame(ByteBuf buf, int delimiterIndex, int skip) {
/* 29 */     int headerIndex = buf.indexOf(buf.readerIndex() + 1, buf.writerIndex(), (byte)36);
/* 30 */     if (headerIndex > 0 && headerIndex < delimiterIndex) {
/* 31 */       return buf.readRetainedSlice(headerIndex - buf.readerIndex());
/*    */     }
/* 33 */     ByteBuf frame = buf.readRetainedSlice(delimiterIndex - buf.readerIndex());
/* 34 */     buf.skipBytes(skip);
/* 35 */     return frame;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 43 */     while (buf.isReadable() && buf.getByte(buf.readerIndex()) != 36) {
/* 44 */       buf.skipBytes(1);
/*    */     }
/*    */     
/* 47 */     int delimiterIndex = BufferUtil.indexOf("\r\n", buf);
/* 48 */     if (delimiterIndex > 20) {
/* 49 */       return readFrame(buf, delimiterIndex, 2);
/*    */     }
/* 51 */     delimiterIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)42);
/* 52 */     if (delimiterIndex > 20) {
/* 53 */       if (buf.writerIndex() > delimiterIndex + 1 && buf.getByte(delimiterIndex + 1) == 42) {
/* 54 */         delimiterIndex++;
/*    */       }
/* 56 */       if (buf.getByte(delimiterIndex - 2) == 44) {
/* 57 */         return readFrame(buf, delimiterIndex - 1, 2);
/*    */       }
/* 59 */       return readFrame(buf, delimiterIndex, 1);
/*    */     } 
/*    */ 
/*    */ 
/*    */     
/* 64 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ItsFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */