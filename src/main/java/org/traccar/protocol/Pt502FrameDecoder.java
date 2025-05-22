/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import java.nio.charset.StandardCharsets;
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
/*    */ public class Pt502FrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int BINARY_HEADER = 5;
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 33 */     if (buf.readableBytes() < 10) {
/* 34 */       return null;
/*    */     }
/*    */     
/* 37 */     if (buf.getUnsignedByte(buf.readerIndex()) == 191 && buf
/* 38 */       .toString(buf.readerIndex() + 5, 4, StandardCharsets.US_ASCII).equals("$PHD")) {
/*    */       
/* 40 */       int length = buf.getUnsignedShortLE(buf.readerIndex() + 3);
/* 41 */       if (buf.readableBytes() >= length) {
/* 42 */         buf.skipBytes(5);
/* 43 */         ByteBuf result = buf.readRetainedSlice(length - 5 - 2);
/* 44 */         buf.skipBytes(2);
/* 45 */         return result;
/*    */       }
/*    */     
/*    */     } else {
/*    */       
/* 50 */       if (buf.getUnsignedByte(buf.readerIndex()) == 191) {
/* 51 */         buf.skipBytes(5);
/*    */       }
/*    */       
/* 54 */       int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)13);
/* 55 */       if (index < 0) {
/* 56 */         index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)10);
/*    */       }
/*    */       
/* 59 */       if (index > 0) {
/* 60 */         ByteBuf result = buf.readRetainedSlice(index - buf.readerIndex());
/* 61 */         while (buf.isReadable() && (buf
/* 62 */           .getByte(buf.readerIndex()) == 13 || buf.getByte(buf.readerIndex()) == 10)) {
/* 63 */           buf.skipBytes(1);
/*    */         }
/* 65 */         return result;
/*    */       } 
/*    */     } 
/*    */ 
/*    */     
/* 70 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Pt502FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */