/*    */ package org.traccar;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import io.netty.handler.codec.ByteToMessageDecoder;
/*    */ import java.util.List;
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
/*    */ public abstract class BaseFrameDecoder
/*    */   extends ByteToMessageDecoder
/*    */ {
/*    */   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
/* 29 */     Object decoded = decode(ctx, (ctx != null) ? ctx.channel() : null, in);
/* 30 */     if (decoded != null)
/* 31 */       out.add(decoded); 
/*    */   }
/*    */   
/*    */   protected abstract Object decode(ChannelHandlerContext paramChannelHandlerContext, Channel paramChannel, ByteBuf paramByteBuf) throws Exception;
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\BaseFrameDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */