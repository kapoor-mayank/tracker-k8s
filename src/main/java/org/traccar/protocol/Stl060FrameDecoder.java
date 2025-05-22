/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import org.traccar.CharacterDelimiterFrameDecoder;
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
/*    */ public class Stl060FrameDecoder
/*    */   extends CharacterDelimiterFrameDecoder
/*    */ {
/*    */   public Stl060FrameDecoder(int maxFrameLength) {
/* 25 */     super(maxFrameLength, '#');
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
/* 31 */     ByteBuf result = (ByteBuf)super.decode(ctx, buf);
/*    */     
/* 33 */     if (result != null) {
/*    */       
/* 35 */       int index = result.indexOf(result.readerIndex(), result.writerIndex(), (byte)36);
/* 36 */       if (index == -1) {
/* 37 */         return result;
/*    */       }
/* 39 */       result.skipBytes(index);
/* 40 */       return result.readRetainedSlice(result.readableBytes());
/*    */     } 
/*    */ 
/*    */ 
/*    */     
/* 45 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Stl060FrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */