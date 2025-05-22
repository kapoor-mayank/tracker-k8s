/*    */ package org.traccar;
/*    */ 
/*    */ import java.util.Map;
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
/*    */ 
/*    */ 
/*    */ public abstract class StringProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   protected String formatCommand(Command command, String format, ValueFormatter valueFormatter, String... keys) {
/* 30 */     String result = String.format(format, (Object[])keys);
/*    */     
/* 32 */     result = result.replaceAll("\\{uniqueId}", getUniqueId(command.getDeviceId()));
/* 33 */     for (Map.Entry<String, Object> entry : (Iterable<Map.Entry<String, Object>>)command.getAttributes().entrySet()) {
/* 34 */       String value = null;
/* 35 */       if (valueFormatter != null) {
/* 36 */         value = valueFormatter.formatValue(entry.getKey(), entry.getValue());
/*    */       }
/* 38 */       if (value == null) {
/* 39 */         value = entry.getValue().toString();
/*    */       }
/* 41 */       result = result.replaceAll("\\{" + (String)entry.getKey() + "}", value);
/*    */     } 
/*    */     
/* 44 */     return result;
/*    */   }
/*    */   
/*    */   protected String formatCommand(Command command, String format, String... keys) {
/* 48 */     return formatCommand(command, format, (ValueFormatter)null, keys);
/*    */   }
/*    */   
/*    */   public static interface ValueFormatter {
/*    */     String formatValue(String param1String, Object param1Object);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\StringProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */