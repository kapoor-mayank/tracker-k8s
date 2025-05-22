/*    */ package org.traccar.protocol;
/*    */ 
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
/*    */ public class SviasProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/* 26 */     switch (command.getType()) {
/*    */       case "custom":
/* 28 */         return formatCommand(command, "{%s}", new String[] { "data" });
/*    */       case "positionSingle":
/* 30 */         return formatCommand(command, "AT+STR=1*", new String[0]);
/*    */       case "setOdometer":
/* 32 */         return formatCommand(command, "AT+ODT={%s}*", new String[] { "data" });
/*    */       case "engineStop":
/* 34 */         return formatCommand(command, "AT+OUT=1,1*", new String[0]);
/*    */       case "engineResume":
/* 36 */         return formatCommand(command, "AT+OUT=1,0*", new String[0]);
/*    */       case "alarmArm":
/* 38 */         return formatCommand(command, "AT+OUT=2,1*", new String[0]);
/*    */       case "alarmDisarm":
/* 40 */         return formatCommand(command, "AT+OUT=2,0*", new String[0]);
/*    */       case "alarmRemove":
/* 42 */         return formatCommand(command, "AT+PNC=600*", new String[0]);
/*    */     } 
/* 44 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SviasProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */