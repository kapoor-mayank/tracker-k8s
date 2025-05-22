/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import java.util.BitSet;
/*    */ import org.traccar.BaseFrameDecoder;
/*    */ import org.traccar.BasePipelineFactory;
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
/*    */ public class NavtelecomFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 33 */     if (buf.getByte(buf.readerIndex()) == Byte.MAX_VALUE) {
/*    */       
/* 35 */       buf.skipBytes(1);
/* 36 */       return null;
/*    */     } 
/* 38 */     if (buf.getByte(buf.readerIndex()) == 64) {
/*    */       
/* 40 */       int length = buf.getUnsignedShortLE(12) + 12 + 2 + 2;
/* 41 */       if (buf.readableBytes() >= length) {
/* 42 */         return buf.readRetainedSlice(length);
/*    */       
/*    */       }
/*    */     }
/*    */     else {
/*    */       
/* 48 */       NavtelecomProtocolDecoder protocolDecoder = (NavtelecomProtocolDecoder)BasePipelineFactory.getHandler(ctx.pipeline(), NavtelecomProtocolDecoder.class);
/* 49 */       if (protocolDecoder == null) {
/* 50 */         throw new RuntimeException("Decoder not found");
/*    */       }
/*    */       
/* 53 */       String type = buf.getCharSequence(buf.readerIndex(), 2, StandardCharsets.US_ASCII).toString();
/* 54 */       BitSet bits = protocolDecoder.getBits();
/*    */       
/* 56 */       if (type.equals("~A")) {
/* 57 */         int count = buf.getUnsignedByte(buf.readerIndex() + 2);
/* 58 */         int length = 4;
/*    */         
/* 60 */         for (int i = 0; i < count; i++) {
/* 61 */           for (int j = 0; j < bits.length(); j++) {
/* 62 */             if (bits.get(j)) {
/* 63 */               length += NavtelecomProtocolDecoder.getItemLength(j + 1);
/*    */             }
/*    */           } 
/*    */         } 
/*    */         
/* 68 */         if (buf.readableBytes() >= length) {
/* 69 */           return buf.readRetainedSlice(length);
/*    */         }
/*    */       } else {
/* 72 */         throw new UnsupportedOperationException("Unsupported message type: " + type);
/*    */       } 
/*    */     } 
/*    */ 
/*    */     
/* 77 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NavtelecomFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */