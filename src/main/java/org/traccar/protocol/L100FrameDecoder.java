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
/*    */ public class L100FrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 31 */     if (buf.readableBytes() < 10) {
/* 32 */       return null;
/*    */     }
/*    */     
/* 35 */     if (buf.getCharSequence(buf.readerIndex(), 4, StandardCharsets.US_ASCII).toString().equals("ATL,")) {
/* 36 */       return decodeNew(buf);
/*    */     }
/* 38 */     return decodeOld(buf);
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   private Object decodeOld(ByteBuf buf) {
/* 44 */     int index, header = buf.getByte(buf.readerIndex());
/* 45 */     boolean obd = (header == 76 || header == 72);
/*    */ 
/*    */     
/* 48 */     if (obd) {
/* 49 */       index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)42);
/*    */     } else {
/* 51 */       index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)2);
/* 52 */       if (index < 0) {
/* 53 */         index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)4);
/* 54 */         if (index < 0) {
/* 55 */           return null;
/*    */         }
/*    */       } 
/*    */     } 
/*    */     
/* 60 */     index += 2;
/*    */     
/* 62 */     if (buf.writerIndex() >= index) {
/* 63 */       if (!obd) {
/* 64 */         buf.skipBytes(2);
/*    */       }
/* 66 */       ByteBuf frame = buf.readRetainedSlice(index - buf.readerIndex() - 2);
/* 67 */       buf.skipBytes(2);
/* 68 */       return frame;
/*    */     } 
/*    */     
/* 71 */     return null;
/*    */   }
/*    */ 
/*    */   
/*    */   private Object decodeNew(ByteBuf buf) {
/* 76 */     int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)64);
/* 77 */     if (index < 0) {
/* 78 */       return null;
/*    */     }
/*    */     
/* 81 */     if (buf.writerIndex() >= index + 1) {
/* 82 */       ByteBuf frame = buf.readRetainedSlice(index - buf.readerIndex());
/* 83 */       buf.skipBytes(1);
/* 84 */       return frame;
/*    */     } 
/*    */     
/* 87 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\L100FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */