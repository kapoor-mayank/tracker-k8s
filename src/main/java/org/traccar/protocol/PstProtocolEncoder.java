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
/*    */ public class PstProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   private ByteBuf encodeContent(long deviceId, int type, int data1, int data2) {
/* 28 */     ByteBuf buf = Unpooled.buffer();
/*    */     
/* 30 */     buf.writeInt((int)Long.parseLong(getUniqueId(deviceId)));
/* 31 */     buf.writeByte(6);
/*    */     
/* 33 */     buf.writeInt(1);
/* 34 */     buf.writeByte(6);
/* 35 */     buf.writeShort(type);
/* 36 */     buf.writeShort(data1);
/* 37 */     buf.writeShort(data2);
/*    */     
/* 39 */     buf.writeShort(Checksum.crc16(Checksum.CRC16_XMODEM, buf.nioBuffer()));
/*    */     
/* 41 */     return buf;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 47 */     switch (command.getType()) {
/*    */       case "engineStop":
/* 49 */         return encodeContent(command.getDeviceId(), 2, 65535, 65535);
/*    */       case "engineResume":
/* 51 */         return encodeContent(command.getDeviceId(), 1, 65535, 65535);
/*    */     } 
/* 53 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PstProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */