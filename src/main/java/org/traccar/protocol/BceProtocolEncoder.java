/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import org.traccar.BaseProtocolEncoder;
/*    */ import org.traccar.helper.Checksum;
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
/*    */ public class BceProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/* 29 */     if (command.getType().equals("outputControl")) {
/* 30 */       ByteBuf buf = Unpooled.buffer();
/*    */       
/* 32 */       buf.writeLongLE(Long.parseLong(getUniqueId(command.getDeviceId())));
/* 33 */       buf.writeShortLE(6);
/* 34 */       buf.writeByte(65);
/* 35 */       buf.writeByte((command.getInteger("index") == 1) ? 10 : 11);
/* 36 */       buf.writeByte(255);
/* 37 */       buf.writeByte(0);
/* 38 */       buf.writeShortLE((Integer.parseInt(command.getString("data")) > 0) ? 85 : 0);
/* 39 */       buf.writeByte(Checksum.sum(buf.nioBuffer()));
/*    */       
/* 41 */       return buf;
/*    */     } 
/* 43 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\BceProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */