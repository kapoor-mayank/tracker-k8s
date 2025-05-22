/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import java.util.TimeZone;
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
/*    */ public class Jt600ProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/*    */     int offset;
/* 28 */     switch (command.getType()) {
/*    */       case "engineStop":
/* 30 */         return "(S07,0)";
/*    */       case "engineResume":
/* 32 */         return "(S07,1)";
/*    */       case "setTimezone":
/* 34 */         offset = TimeZone.getTimeZone(command.getString("timezone")).getRawOffset() / 60000;
/* 35 */         return "(S09,1," + offset + ")";
/*    */       case "rebootDevice":
/* 37 */         return "(S17)";
/*    */     } 
/* 39 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Jt600ProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */