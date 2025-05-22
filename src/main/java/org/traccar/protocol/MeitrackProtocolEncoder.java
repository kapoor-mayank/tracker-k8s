/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import java.util.Map;
/*    */ import org.traccar.Context;
/*    */ import org.traccar.StringProtocolEncoder;
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
/*    */ public class MeitrackProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   private Object formatCommand(Command command, char dataId, String content) {
/* 28 */     String uniqueId = getUniqueId(command.getDeviceId());
/* 29 */     int length = 1 + uniqueId.length() + 1 + content.length() + 5;
/* 30 */     String result = String.format("@@%c%02d,%s,%s*", new Object[] { Character.valueOf(dataId), Integer.valueOf(length), uniqueId, content });
/* 31 */     result = result + Checksum.sum(result) + "\r\n";
/* 32 */     return result;
/*    */   }
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/*    */     int index;
/* 38 */     Map<String, Object> attributes = command.getAttributes();
/*    */     
/* 40 */     boolean alternative = Context.getIdentityManager().lookupAttributeBoolean(command
/* 41 */         .getDeviceId(), "meitrack.alternative", false, true);
/*    */     
/* 43 */     switch (command.getType()) {
/*    */       case "positionLog":
/* 45 */         return formatCommand(command, 'N', "B34," + attributes.get("frequency"));
/*    */       case "positionSingle":
/* 47 */         return formatCommand(command, 'Q', "A10");
/*    */       case "engineStop":
/* 49 */         return formatCommand(command, 'M', "C01,0,12222");
/*    */       case "engineResume":
/* 51 */         return formatCommand(command, 'M', "C01,0,02222");
/*    */       case "alarmArm":
/* 53 */         return formatCommand(command, 'M', alternative ? "B21,1" : "C01,0,22122");
/*    */       case "alarmDisarm":
/* 55 */         return formatCommand(command, 'M', alternative ? "B21,0" : "C01,0,22022");
/*    */       case "requestPhoto":
/* 57 */         index = command.getInteger("index");
/* 58 */         return formatCommand(command, 'D', "D03," + ((index > 0) ? index : 1) + ",camera_picture.jpg");
/*    */       case "sendSms":
/* 60 */         return formatCommand(command, 'f', "C02,0," + attributes
/* 61 */             .get("phone") + "," + attributes.get("message"));
/*    */     } 
/* 63 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MeitrackProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */