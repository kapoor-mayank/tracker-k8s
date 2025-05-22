/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import java.text.ParseException;
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
/*    */ public class Jt600FrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     if (buf.readableBytes() < 10) {
/* 32 */       return null;
/*    */     }
/*    */     
/* 35 */     char type = (char)buf.getByte(buf.readerIndex());
/*    */     
/* 37 */     if (type == '$') {
/* 38 */       boolean longFormat = Jt600ProtocolDecoder.isLongFormat(buf);
/* 39 */       int length = buf.getUnsignedShort(buf.readerIndex() + (longFormat ? 8 : 7)) + 10;
/* 40 */       if (length <= buf.readableBytes()) {
/* 41 */         return buf.readRetainedSlice(length);
/*    */       }
/* 43 */     } else if (type == '(') {
/* 44 */       int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)41);
/* 45 */       if (endIndex != -1) {
/* 46 */         return buf.readRetainedSlice(endIndex + 1);
/*    */       }
/*    */     } else {
/* 49 */       throw new ParseException(null, 0);
/*    */     } 
/*    */     
/* 52 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Jt600FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */