/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import java.util.TimeZone;
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
/*    */ public class CityeasyProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   private ByteBuf encodeContent(int type, ByteBuf content) {
/* 30 */     ByteBuf buf = Unpooled.buffer();
/*    */     
/* 32 */     buf.writeByte(83);
/* 33 */     buf.writeByte(83);
/* 34 */     buf.writeShort(6 + content.readableBytes() + 4 + 2 + 2);
/* 35 */     buf.writeShort(type);
/* 36 */     buf.writeBytes(content);
/* 37 */     buf.writeInt(11);
/* 38 */     buf.writeShort(Checksum.crc16(Checksum.CRC16_KERMIT, buf.nioBuffer()));
/* 39 */     buf.writeByte(13);
/* 40 */     buf.writeByte(10);
/*    */     
/* 42 */     return buf;
/*    */   }
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/*    */     int timezone;
/* 48 */     ByteBuf content = Unpooled.buffer();
/*    */     
/* 50 */     switch (command.getType()) {
/*    */       case "positionSingle":
/* 52 */         return encodeContent(4, content);
/*    */       case "positionPeriodic":
/* 54 */         content.writeShort(command.getInteger("frequency"));
/* 55 */         return encodeContent(5, content);
/*    */       case "positionStop":
/* 57 */         content.writeShort(0);
/* 58 */         return encodeContent(5, content);
/*    */       case "setTimezone":
/* 60 */         timezone = TimeZone.getTimeZone(command.getString("timezone")).getRawOffset() / 60000;
/* 61 */         if (timezone < 0) {
/* 62 */           content.writeByte(1);
/*    */         } else {
/* 64 */           content.writeByte(0);
/*    */         } 
/* 66 */         content.writeShort(Math.abs(timezone));
/* 67 */         return encodeContent(8, content);
/*    */     } 
/* 69 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CityeasyProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */