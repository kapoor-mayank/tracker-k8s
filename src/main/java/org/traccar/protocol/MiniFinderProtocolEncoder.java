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
/*    */ public class MiniFinderProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */   implements StringProtocolEncoder.ValueFormatter
/*    */ {
/*    */   public String formatValue(String key, Object value) {
/* 27 */     switch (key) {
/*    */       case "enable":
/* 29 */         return ((Boolean)value).booleanValue() ? "1" : "0";
/*    */       case "timezone":
/* 31 */         return String.format("%+03d", new Object[] { Integer.valueOf(TimeZone.getTimeZone((String)value).getRawOffset() / 3600000) });
/*    */       case "index":
/* 33 */         switch (((Number)value).intValue()) {
/*    */           case 0:
/* 35 */             return "A";
/*    */           case 1:
/* 37 */             return "B";
/*    */           case 2:
/* 39 */             return "C";
/*    */         } 
/* 41 */         return null;
/*    */     } 
/*    */     
/* 44 */     return null;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 51 */     initDevicePassword(command, "123456");
/*    */     
/* 53 */     switch (command.getType()) {
/*    */       case "setTimezone":
/* 55 */         return formatCommand(command, "{%s}L{%s}", this, new String[] { "devicePassword", "timezone" });
/*    */       case "voiceMonitoring":
/* 57 */         return formatCommand(command, "{%s}P{%s}", this, new String[] { "devicePassword", "enable" });
/*    */       case "alarmSpeed":
/* 59 */         return formatCommand(command, "{%s}J1{%s}", new String[] { "devicePassword", "data" });
/*    */       case "movementAlarm":
/* 61 */         return formatCommand(command, "{%s}R1{%s}", new String[] { "devicePassword", "radius" });
/*    */       case "alarmVibration":
/* 63 */         return formatCommand(command, "{%s}W1,{%s}", new String[] { "devicePassword", "data" });
/*    */       case "setAgps":
/* 65 */         return formatCommand(command, "{%s}AGPS{%s}", this, new String[] { "devicePassword", "enable" });
/*    */       case "alarmFall":
/* 67 */         return formatCommand(command, "{%s}F{%s}", this, new String[] { "devicePassword", "enable" });
/*    */       case "modePowerSaving":
/* 69 */         return formatCommand(command, "{%s}SP{%s}", this, new String[] { "devicePassword", "enable" });
/*    */       case "modeDeepSleep":
/* 71 */         return formatCommand(command, "{%s}DS{%s}", this, new String[] { "devicePassword", "enable" });
/*    */       case "sosNumber":
/* 73 */         return formatCommand(command, "{%s}{%s}1,{%s}", this, new String[] { "devicePassword", "index", "phone" });
/*    */       
/*    */       case "setIndicator":
/* 76 */         return formatCommand(command, "{%s}LED{%s}", new String[] { "devicePassword", "data" });
/*    */     } 
/* 78 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MiniFinderProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */