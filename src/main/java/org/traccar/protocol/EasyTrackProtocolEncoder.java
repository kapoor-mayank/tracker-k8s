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
/*    */ public class EasyTrackProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/* 26 */     switch (command.getType()) {
/*    */       case "engineStop":
/* 28 */         return formatCommand(command, "*ET,{%s},FD,Y1#", new String[] { "uniqueId" });
/*    */       case "engineResume":
/* 30 */         return formatCommand(command, "*ET,{%s},FD,Y2#", new String[] { "uniqueId" });
/*    */       case "alarmArm":
/* 32 */         return formatCommand(command, "*ET,{%s},FD,F1#", new String[] { "uniqueId" });
/*    */       case "alarmDisarm":
/* 34 */         return formatCommand(command, "*ET,{%s},FD,F2#", new String[] { "uniqueId" });
/*    */     } 
/* 36 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EasyTrackProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */