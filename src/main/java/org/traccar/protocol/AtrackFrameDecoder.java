/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import java.nio.charset.StandardCharsets;
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
/*    */ public class AtrackFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int KEEPALIVE_LENGTH = 12;
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 34 */     if (buf.readableBytes() >= 2)
/*    */     {
/* 36 */       if (buf.getUnsignedShort(buf.readerIndex()) == 65026) {
/*    */         
/* 38 */         if (buf.readableBytes() >= 12) {
/* 39 */           return buf.readRetainedSlice(12);
/*    */         }
/*    */       }
/* 42 */       else if (buf.getUnsignedByte(buf.readerIndex()) == 64 && buf.getByte(buf.readerIndex() + 2) != 44) {
/*    */         
/* 44 */         if (buf.readableBytes() > 6) {
/* 45 */           int length = buf.getUnsignedShort(buf.readerIndex() + 4) + 4 + 2;
/* 46 */           if (buf.readableBytes() >= length) {
/* 47 */             return buf.readRetainedSlice(length);
/*    */           }
/*    */         }
/*    */       
/*    */       } else {
/*    */         
/* 53 */         int lengthStart = buf.indexOf(buf.readerIndex() + 3, buf.writerIndex(), (byte)44) + 1;
/* 54 */         if (lengthStart > 0) {
/* 55 */           int lengthEnd = buf.indexOf(lengthStart, buf.writerIndex(), (byte)44);
/* 56 */           if (lengthEnd > 0) {
/* 57 */             int length = lengthEnd + Integer.parseInt(buf.toString(lengthStart, lengthEnd - lengthStart, StandardCharsets.US_ASCII));
/*    */             
/* 59 */             if (buf.readableBytes() > length && buf.getByte(buf.readerIndex() + length) == 10) {
/* 60 */               length++;
/*    */             }
/* 62 */             if (buf.readableBytes() >= length) {
/* 63 */               return buf.readRetainedSlice(length);
/*    */             }
/*    */           } 
/*    */         } else {
/* 67 */           int endIndex = BufferUtil.indexOf("\r\n", buf);
/* 68 */           if (endIndex > 0) {
/* 69 */             return buf.readRetainedSlice(endIndex - buf.readerIndex() + 2);
/*    */           }
/*    */         } 
/*    */       } 
/*    */     }
/*    */ 
/*    */ 
/*    */     
/* 77 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AtrackFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */