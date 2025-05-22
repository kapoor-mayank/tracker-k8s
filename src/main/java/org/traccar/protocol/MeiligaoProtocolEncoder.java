/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import java.util.TimeZone;
/*    */ import org.traccar.BaseProtocolEncoder;
/*    */ import org.traccar.helper.Checksum;
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
/*    */ public class MeiligaoProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   private ByteBuf encodeContent(long deviceId, int type, ByteBuf content) {
/* 32 */     ByteBuf buf = Unpooled.buffer();
/*    */     
/* 34 */     buf.writeByte(64);
/* 35 */     buf.writeByte(64);
/*    */     
/* 37 */     buf.writeShort(13 + content.readableBytes() + 2 + 2);
/*    */     
/* 39 */     buf.writeBytes(DataConverter.parseHex((getUniqueId(deviceId) + "FFFFFFFFFFFFFF").substring(0, 14)));
/*    */     
/* 41 */     buf.writeShort(type);
/*    */     
/* 43 */     buf.writeBytes(content);
/*    */     
/* 45 */     buf.writeShort(Checksum.crc16(Checksum.CRC16_CCITT_FALSE, buf.nioBuffer()));
/*    */     
/* 47 */     buf.writeByte(13);
/* 48 */     buf.writeByte(10);
/*    */     
/* 50 */     return buf;
/*    */   }
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/*    */     int offset;
/* 56 */     ByteBuf content = Unpooled.buffer();
/*    */     
/* 58 */     switch (command.getType()) {
/*    */       case "positionSingle":
/* 60 */         return encodeContent(command.getDeviceId(), 16641, content);
/*    */       case "positionPeriodic":
/* 62 */         content.writeShort(command.getInteger("frequency") / 10);
/* 63 */         return encodeContent(command.getDeviceId(), 16642, content);
/*    */       case "engineStop":
/* 65 */         content.writeByte(1);
/* 66 */         content.writeByte(2);
/* 67 */         content.writeByte(2);
/* 68 */         content.writeByte(2);
/* 69 */         content.writeByte(2);
/* 70 */         return encodeContent(command.getDeviceId(), 16660, content);
/*    */       case "engineResume":
/* 72 */         content.writeByte(0);
/* 73 */         content.writeByte(2);
/* 74 */         content.writeByte(2);
/* 75 */         content.writeByte(2);
/* 76 */         content.writeByte(2);
/* 77 */         return encodeContent(command.getDeviceId(), 16660, content);
/*    */       case "movementAlarm":
/* 79 */         content.writeShort(command.getInteger("radius"));
/* 80 */         return encodeContent(command.getDeviceId(), 16646, content);
/*    */       case "setTimezone":
/* 82 */         offset = TimeZone.getTimeZone(command.getString("timezone")).getRawOffset() / 60000;
/* 83 */         content.writeBytes(String.valueOf(offset).getBytes(StandardCharsets.US_ASCII));
/* 84 */         return encodeContent(command.getDeviceId(), 16690, content);
/*    */       case "requestPhoto":
/* 86 */         return encodeContent(command.getDeviceId(), 16721, content);
/*    */       case "rebootDevice":
/* 88 */         return encodeContent(command.getDeviceId(), 18690, content);
/*    */     } 
/* 90 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MeiligaoProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */