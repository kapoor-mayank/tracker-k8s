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
/*    */ public class Pt502ProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */   implements StringProtocolEncoder.ValueFormatter
/*    */ {
/*    */   public String formatValue(String key, Object value) {
/* 27 */     if (key.equals("timezone")) {
/* 28 */       return String.valueOf(TimeZone.getTimeZone((String)value).getRawOffset() / 3600000);
/*    */     }
/*    */     
/* 31 */     return null;
/*    */   }
/*    */ 
/*    */   
/*    */   protected String formatCommand(Command command, String format, String... keys) {
/* 36 */     return formatCommand(command, format, this, keys);
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 42 */     switch (command.getType()) {
/*    */       case "custom":
/* 44 */         return formatCommand(command, "{%s}\r\n", new String[] { "data" });
/*    */       case "outputControl":
/* 46 */         return formatCommand(command, "#OPC{%s},{%s}\r\n", new String[] { "index", "data" });
/*    */       case "setTimezone":
/* 48 */         return formatCommand(command, "#TMZ{%s}\r\n", new String[] { "timezone" });
/*    */       case "alarmSpeed":
/* 50 */         return formatCommand(command, "#SPD{%s}\r\n", new String[] { "data" });
/*    */       case "requestPhoto":
/* 52 */         return formatCommand(command, "#PHO\r\n", new String[0]);
/*    */     } 
/* 54 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Pt502ProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */