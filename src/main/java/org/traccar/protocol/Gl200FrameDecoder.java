/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import java.util.Arrays;
/*    */ import java.util.HashSet;
/*    */ import java.util.Set;
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
/*    */ public class Gl200FrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int MINIMUM_LENGTH = 11;
/* 33 */   private static final Set<String> BINARY_HEADERS = new HashSet<>(
/* 34 */       Arrays.asList(new String[] { "+RSP", "+BSP", "+EVT", "+BVT", "+INF", "+BNF", "+HBD", "+CRD", "+BRD", "+LGN" }));
/*    */   
/*    */   public static boolean isBinary(ByteBuf buf) {
/* 37 */     String header = buf.toString(buf.readerIndex(), 4, StandardCharsets.US_ASCII);
/* 38 */     if (header.equals("+ACK")) {
/* 39 */       return (buf.getByte(buf.readerIndex() + header.length()) != 58);
/*    */     }
/* 41 */     return BINARY_HEADERS.contains(header);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 49 */     if (buf.readableBytes() < 11) {
/* 50 */       return null;
/*    */     }
/*    */     
/* 53 */     if (isBinary(buf)) {
/*    */       int length;
/*    */       
/* 56 */       switch (buf.toString(buf.readerIndex(), 4, StandardCharsets.US_ASCII)) {
/*    */         case "+ACK":
/* 58 */           length = buf.getUnsignedByte(buf.readerIndex() + 6);
/*    */           break;
/*    */         case "+INF":
/*    */         case "+BNF":
/* 62 */           length = buf.getUnsignedShort(buf.readerIndex() + 7);
/*    */           break;
/*    */         case "+HBD":
/* 65 */           length = buf.getUnsignedByte(buf.readerIndex() + 5);
/*    */           break;
/*    */         case "+CRD":
/*    */         case "+BRD":
/*    */         case "+LGN":
/* 70 */           length = buf.getUnsignedShort(buf.readerIndex() + 6);
/*    */           break;
/*    */         default:
/* 73 */           length = buf.getUnsignedShort(buf.readerIndex() + 9);
/*    */           break;
/*    */       } 
/*    */       
/* 77 */       if (buf.readableBytes() >= length) {
/* 78 */         return buf.readRetainedSlice(length);
/*    */       }
/*    */     }
/*    */     else {
/*    */       
/* 83 */       int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)36);
/* 84 */       if (endIndex < 0) {
/* 85 */         endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)0);
/*    */       }
/* 87 */       if (endIndex > 0) {
/* 88 */         ByteBuf frame = buf.readRetainedSlice(endIndex - buf.readerIndex());
/* 89 */         buf.readByte();
/* 90 */         return frame;
/*    */       } 
/*    */     } 
/*    */ 
/*    */     
/* 95 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gl200FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */