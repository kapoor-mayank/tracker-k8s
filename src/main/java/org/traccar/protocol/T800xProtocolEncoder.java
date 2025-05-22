/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import io.netty.channel.Channel;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import org.traccar.BaseProtocolEncoder;
/*    */ import org.traccar.helper.DataConverter;
/*    */ import org.traccar.model.Command;
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
/*    */ public class T800xProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   public static final int MODE_SETTING = 1;
/*    */   public static final int MODE_BROADCAST = 2;
/*    */   public static final int MODE_FORWARD = 3;
/*    */   
/*    */   private ByteBuf encodeContent(Command command, short header, String content) {
/* 36 */     ByteBuf buf = Unpooled.buffer();
/*    */     
/* 38 */     buf.writeShort(header);
/* 39 */     buf.writeByte(129);
/* 40 */     buf.writeShort(16 + content.length());
/* 41 */     buf.writeShort(1);
/* 42 */     buf.writeBytes(DataConverter.parseHex("0" + getUniqueId(command.getDeviceId())));
/* 43 */     buf.writeByte(1);
/* 44 */     buf.writeBytes(content.getBytes(StandardCharsets.US_ASCII));
/*    */     
/* 46 */     return buf;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Channel channel, Command command) {
/* 52 */     short header = 8995;
/* 53 */     if (channel != null) {
/* 54 */       header = ((T800xProtocolDecoder)channel.pipeline().get(T800xProtocolDecoder.class)).getHeader();
/*    */     }
/*    */     
/* 57 */     switch (command.getType()) {
/*    */       case "custom":
/* 59 */         return encodeContent(command, header, command.getString("data"));
/*    */     } 
/* 61 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\T800xProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */