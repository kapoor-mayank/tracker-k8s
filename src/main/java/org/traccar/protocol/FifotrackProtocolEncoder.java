/*    */ package org.traccar.protocol;
/*    */ 
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
/*    */ public class FifotrackProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   private Object formatCommand(Command command, String content) {
/* 25 */     String uniqueId = getUniqueId(command.getDeviceId());
/* 26 */     int length = 1 + uniqueId.length() + 3 + content.length();
/* 27 */     String result = String.format("##%02d,%s,1,%s*", new Object[] { Integer.valueOf(length), uniqueId, content });
/* 28 */     result = result + Checksum.sum(result) + "\r\n";
/* 29 */     return result;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 35 */     switch (command.getType()) {
/*    */       case "custom":
/* 37 */         return formatCommand(command, command.getString("data"));
/*    */       case "requestPhoto":
/* 39 */         return formatCommand(command, "D05,3");
/*    */     } 
/* 41 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FifotrackProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */