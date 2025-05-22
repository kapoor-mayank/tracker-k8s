/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import org.traccar.BaseProtocolEncoder;
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
/*    */ public class CellocatorProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   public static ByteBuf encodeContent(int type, int uniqueId, int packetNumber, ByteBuf content) {
/* 27 */     ByteBuf buf = Unpooled.buffer();
/* 28 */     buf.writeByte(77);
/* 29 */     buf.writeByte(67);
/* 30 */     buf.writeByte(71);
/* 31 */     buf.writeByte(80);
/* 32 */     buf.writeByte(type);
/* 33 */     buf.writeIntLE(uniqueId);
/* 34 */     buf.writeByte(packetNumber);
/* 35 */     buf.writeIntLE(0);
/* 36 */     buf.writeBytes(content);
/*    */     
/* 38 */     byte checksum = 0;
/* 39 */     for (int i = 4; i < buf.writerIndex(); i++) {
/* 40 */       checksum = (byte)(checksum + buf.getByte(i));
/*    */     }
/* 42 */     buf.writeByte(checksum);
/*    */     
/* 44 */     return buf;
/*    */   }
/*    */ 
/*    */   
/*    */   private ByteBuf encodeCommand(long deviceId, int command, int data1, int data2) {
/* 49 */     ByteBuf content = Unpooled.buffer();
/* 50 */     content.writeByte(command);
/* 51 */     content.writeByte(command);
/* 52 */     content.writeByte(data1);
/* 53 */     content.writeByte(data1);
/* 54 */     content.writeByte(data2);
/* 55 */     content.writeByte(data2);
/* 56 */     content.writeIntLE(0);
/*    */     
/* 58 */     ByteBuf buf = encodeContent(0, Integer.parseInt(getUniqueId(deviceId)), 0, content);
/* 59 */     content.release();
/*    */     
/* 61 */     return buf;
/*    */   }
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/*    */     int data;
/* 67 */     switch (command.getType()) {
/*    */       
/*    */       case "outputControl":
/* 70 */         data = Integer.parseInt(command.getString("data")) << 4 + command.getInteger("index");
/* 71 */         return encodeCommand(command.getDeviceId(), 3, data, 0);
/*    */     } 
/* 73 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CellocatorProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */