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
/*    */ public class KhdProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   public static final int MSG_ON_DEMAND_TRACK = 48;
/*    */   public static final int MSG_CUT_OIL = 57;
/*    */   public static final int MSG_RESUME_OIL = 56;
/*    */   public static final int MSG_CHECK_VERSION = 61;
/*    */   public static final int MSG_FACTORY_RESET = 195;
/*    */   public static final int MSG_SET_OVERSPEED = 63;
/*    */   public static final int MSG_DELETE_MILEAGE = 102;
/*    */   
/*    */   private ByteBuf encodeCommand(int command, String uniqueId, ByteBuf content) {
/* 36 */     ByteBuf buf = Unpooled.buffer();
/*    */     
/* 38 */     buf.writeByte(41);
/* 39 */     buf.writeByte(41);
/*    */     
/* 41 */     buf.writeByte(command);
/*    */     
/* 43 */     int length = 6;
/* 44 */     if (content != null) {
/* 45 */       length += content.readableBytes();
/*    */     }
/* 47 */     buf.writeShort(length);
/*    */     
/* 49 */     uniqueId = "00000000".concat(uniqueId);
/* 50 */     uniqueId = uniqueId.substring(uniqueId.length() - 8);
/* 51 */     buf.writeByte(Integer.parseInt(uniqueId.substring(0, 2)));
/* 52 */     buf.writeByte(Integer.parseInt(uniqueId.substring(2, 4)) + 128);
/* 53 */     buf.writeByte(Integer.parseInt(uniqueId.substring(4, 6)) + 128);
/* 54 */     buf.writeByte(Integer.parseInt(uniqueId.substring(6, 8)));
/*    */     
/* 56 */     if (content != null) {
/* 57 */       buf.writeBytes(content);
/*    */     }
/*    */     
/* 60 */     buf.writeByte(Checksum.xor(buf.nioBuffer()));
/* 61 */     buf.writeByte(13);
/*    */     
/* 63 */     return buf;
/*    */   }
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/*    */     ByteBuf content;
/* 69 */     String uniqueId = getUniqueId(command.getDeviceId());
/*    */     
/* 71 */     switch (command.getType()) {
/*    */       case "engineStop":
/* 73 */         return encodeCommand(57, uniqueId, (ByteBuf)null);
/*    */       case "engineResume":
/* 75 */         return encodeCommand(56, uniqueId, (ByteBuf)null);
/*    */       case "getVersion":
/* 77 */         return encodeCommand(61, uniqueId, (ByteBuf)null);
/*    */       case "factoryReset":
/* 79 */         return encodeCommand(195, uniqueId, (ByteBuf)null);
/*    */       case "setSpeedLimit":
/* 81 */         content = Unpooled.buffer();
/* 82 */         content.writeByte(Integer.parseInt(command.getString("data")));
/* 83 */         return encodeCommand(56, uniqueId, content);
/*    */       case "setOdometer":
/* 85 */         return encodeCommand(102, uniqueId, (ByteBuf)null);
/*    */       case "positionSingle":
/* 87 */         return encodeCommand(48, uniqueId, (ByteBuf)null);
/*    */     } 
/* 89 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\KhdProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */