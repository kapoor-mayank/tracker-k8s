/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import io.netty.channel.ChannelHandler.Sharable;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import io.netty.handler.codec.MessageToMessageEncoder;
/*    */ import java.util.List;
/*    */ import org.traccar.Context;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.config.Keys;
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
/*    */ @Sharable
/*    */ public class TaipPrefixEncoder
/*    */   extends MessageToMessageEncoder<ByteBuf>
/*    */ {
/*    */   private final Protocol protocol;
/*    */   
/*    */   public TaipPrefixEncoder(Protocol protocol) {
/* 35 */     this.protocol = protocol;
/*    */   }
/*    */ 
/*    */   
/*    */   protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
/* 40 */     if (Context.getConfig().getBoolean(Keys.PROTOCOL_PREFIX.withPrefix(this.protocol.getName()))) {
/* 41 */       out.add(Unpooled.wrappedBuffer(new ByteBuf[] { Unpooled.wrappedBuffer(new byte[] { 32, 32, 6, 0 }), msg.retain() }));
/*    */     } else {
/* 43 */       out.add(msg.retain());
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TaipPrefixEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */