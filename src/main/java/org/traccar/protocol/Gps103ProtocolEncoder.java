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
/*    */ public class Gps103ProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */   implements StringProtocolEncoder.ValueFormatter
/*    */ {
/*    */   public String formatValue(String key, Object value) {
/* 26 */     if (key.equals("frequency")) {
/* 27 */       long frequency = ((Number)value).longValue();
/* 28 */       if (frequency / 60L / 60L > 0L)
/* 29 */         return String.format("%02dh", new Object[] { Long.valueOf(frequency / 60L / 60L) }); 
/* 30 */       if (frequency / 60L > 0L) {
/* 31 */         return String.format("%02dm", new Object[] { Long.valueOf(frequency / 60L) });
/*    */       }
/* 33 */       return String.format("%02ds", new Object[] { Long.valueOf(frequency) });
/*    */     } 
/*    */ 
/*    */     
/* 37 */     return null;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 43 */     switch (command.getType()) {
/*    */       case "custom":
/* 45 */         return formatCommand(command, "**,imei:{%s},{%s}", new String[] { "uniqueId", "data" });
/*    */       case "positionStop":
/* 47 */         return formatCommand(command, "**,imei:{%s},A", new String[] { "uniqueId" });
/*    */       case "positionSingle":
/* 49 */         return formatCommand(command, "**,imei:{%s},B", new String[] { "uniqueId" });
/*    */       case "positionPeriodic":
/* 51 */         return formatCommand(command, "**,imei:{%s},C,{%s}", this, new String[] { "uniqueId", "frequency" });
/*    */       
/*    */       case "engineStop":
/* 54 */         return formatCommand(command, "**,imei:{%s},J", new String[] { "uniqueId" });
/*    */       case "engineResume":
/* 56 */         return formatCommand(command, "**,imei:{%s},K", new String[] { "uniqueId" });
/*    */       case "customEngineStop":
/* 58 */         return formatCommand(command, "**,imei:{%s},109", new String[] { "uniqueId" });
/*    */       case "customEngineResume":
/* 60 */         return formatCommand(command, "**,imei:{%s},110", new String[] { "uniqueId" });
/*    */       case "alarmArm":
/* 62 */         return formatCommand(command, "**,imei:{%s},L", new String[] { "uniqueId" });
/*    */       case "alarmDisarm":
/* 64 */         return formatCommand(command, "**,imei:{%s},M", new String[] { "uniqueId" });
/*    */       case "requestPhoto":
/* 66 */         return formatCommand(command, "**,imei:{%s},160", new String[] { "uniqueId" });
/*    */     } 
/* 68 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gps103ProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */