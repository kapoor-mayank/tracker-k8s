/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import java.util.Date;
/*    */ import org.traccar.Context;
/*    */ import org.traccar.StringProtocolEncoder;
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
/*    */ 
/*    */ public class H02ProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   private static final String MARKER = "HQ";
/*    */   
/*    */   private Object formatCommand(Date time, String uniqueId, String type, String... params) {
/* 32 */     StringBuilder result = new StringBuilder(String.format("*%s,%s,%s,%4$tH%4$tM%4$tS", new Object[] { "HQ", uniqueId, type, time }));
/*    */     
/* 34 */     for (String param : params) {
/* 35 */       result.append(",").append(param);
/*    */     }
/*    */     
/* 38 */     result.append("#");
/*    */     
/* 40 */     return result.toString();
/*    */   }
/*    */   
/*    */   protected Object encodeCommand(Command command, Date time) {
/* 44 */     String frequency, uniqueId = getUniqueId(command.getDeviceId());
/*    */     
/* 46 */     switch (command.getType()) {
/*    */       case "alarmArm":
/* 48 */         return formatCommand(time, uniqueId, "SCF", new String[] { "0", "0" });
/*    */       case "alarmDisarm":
/* 50 */         return formatCommand(time, uniqueId, "SCF", new String[] { "1", "1" });
/*    */       case "engineStop":
/* 52 */         return formatCommand(time, uniqueId, "S20", new String[] { "1", "1" });
/*    */       case "engineResume":
/* 54 */         return formatCommand(time, uniqueId, "S20", new String[] { "1", "0" });
/*    */       case "positionPeriodic":
/* 56 */         frequency = command.getAttributes().get("frequency").toString();
/* 57 */         if (Context.getIdentityManager().lookupAttributeBoolean(command
/* 58 */             .getDeviceId(), "h02.alternative", false, true)) {
/* 59 */           return formatCommand(time, uniqueId, "D1", new String[] { frequency });
/*    */         }
/* 61 */         return formatCommand(time, uniqueId, "S71", new String[] { "22", frequency });
/*    */     } 
/*    */     
/* 64 */     return null;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 70 */     return encodeCommand(command, new Date());
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\H02ProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */