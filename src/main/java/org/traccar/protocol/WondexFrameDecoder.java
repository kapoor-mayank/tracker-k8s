/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import org.traccar.BaseFrameDecoder;
/*    */ import org.traccar.NetworkMessage;
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
/*    */ public class WondexFrameDecoder
/*    */   extends BaseFrameDecoder
/*    */ {
/*    */   private static final int KEEP_ALIVE_LENGTH = 8;
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
/* 34 */     if (buf.readableBytes() < 8) {
/* 35 */       return null;
/*    */     }
/*    */     
/* 38 */     if (buf.getUnsignedByte(buf.readerIndex()) == 208) {
/*    */ 
/*    */       
/* 41 */       ByteBuf frame = buf.readRetainedSlice(8);
/* 42 */       if (channel != null) {
/* 43 */         frame.retain();
/* 44 */         channel.writeAndFlush(new NetworkMessage(frame, channel.remoteAddress()));
/*    */       } 
/* 46 */       return frame;
/*    */     } 
/*    */ 
/*    */     
/* 50 */     int index = BufferUtil.indexOf("\r\n", buf);
/* 51 */     if (index != -1) {
/* 52 */       ByteBuf frame = buf.readRetainedSlice(index - buf.readerIndex());
/* 53 */       buf.skipBytes(2);
/* 54 */       return frame;
/*    */     } 
/*    */ 
/*    */ 
/*    */     
/* 59 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WondexFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */