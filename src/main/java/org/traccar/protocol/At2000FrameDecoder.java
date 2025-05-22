/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import org.traccar.BaseFrameDecoder;
/*    */ import org.traccar.NetworkMessage;
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
/*    */ public class At2000FrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int BLOCK_LENGTH = 16;
/*    */   private static final int ACK_LENGTH = 496;
/*    */   private boolean firstPacket = true;
/*    */   private ByteBuf currentBuffer;
/*    */   private int acknowledgedBytes;
/*    */   
/*    */   private void sendResponse(Channel channel) {
/* 36 */     if (channel != null) {
/* 37 */       ByteBuf response = Unpooled.buffer(32);
/* 38 */       response.writeByte(0);
/* 39 */       response.writeMedium(1);
/* 40 */       response.writeByte(0);
/* 41 */       response.writerIndex(32);
/* 42 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*    */     } 
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/*    */     int length;
/* 50 */     if (buf.readableBytes() < 5) {
/* 51 */       return null;
/*    */     }
/*    */ 
/*    */     
/* 55 */     if (this.firstPacket) {
/* 56 */       this.firstPacket = false;
/* 57 */       length = buf.getUnsignedMediumLE(buf.readerIndex() + 2);
/*    */     } else {
/* 59 */       length = buf.getUnsignedMediumLE(buf.readerIndex() + 1);
/*    */     } 
/*    */     
/* 62 */     length += 16;
/* 63 */     if (length % 16 != 0) {
/* 64 */       length = (length / 16 + 1) * 16;
/*    */     }
/*    */     
/* 67 */     if ((buf.readableBytes() >= length || buf.readableBytes() % 496 == 0) && (buf != this.currentBuffer || buf
/* 68 */       .readableBytes() > this.acknowledgedBytes)) {
/* 69 */       sendResponse(channel);
/* 70 */       this.currentBuffer = buf;
/* 71 */       this.acknowledgedBytes = buf.readableBytes();
/*    */     } 
/*    */     
/* 74 */     if (buf.readableBytes() >= length) {
/* 75 */       return buf.readRetainedSlice(length);
/*    */     }
/*    */     
/* 78 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\At2000FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */