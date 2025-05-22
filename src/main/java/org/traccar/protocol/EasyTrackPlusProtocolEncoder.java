/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import org.traccar.StringProtocolEncoder;
/*    */ import org.traccar.model.Command;
/*    */ 
/*    */ public class EasyTrackPlusProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */   implements StringProtocolEncoder.ValueFormatter
/*    */ {
/*    */   public String formatValue(String key, Object value) {
/* 11 */     if (key.equals("frequency")) {
/* 12 */       long frequency = ((Number)value).longValue();
/* 13 */       if (frequency / 60L / 60L > 0L)
/* 14 */         return String.format("%02dh", new Object[] { Long.valueOf(frequency / 60L / 60L) }); 
/* 15 */       if (frequency / 60L > 0L) {
/* 16 */         return String.format("%02dm", new Object[] { Long.valueOf(frequency / 60L) });
/*    */       }
/* 18 */       return String.format("%02ds", new Object[] { Long.valueOf(frequency) });
/*    */     } 
/*    */ 
/*    */     
/* 22 */     return null;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 28 */     switch (command.getType()) {
/*    */       case "custom":
/* 30 */         return formatCommand(command, "*ET,%s,%s", new String[] { "uniqueId", "data" });
/*    */       case "engineStop":
/* 32 */         return formatCommand(command, "*ET,%s,FD,Y1#", new String[] { "uniqueId" });
/*    */       case "engineResume":
/* 34 */         return formatCommand(command, "*ET,%s,FD,Y2#", new String[] { "uniqueId" });
/*    */     } 
/* 36 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EasyTrackPlusProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */