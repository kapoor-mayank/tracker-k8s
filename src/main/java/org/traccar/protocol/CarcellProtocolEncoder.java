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
/*    */ public class CarcellProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected Object encodeCommand(Command command) {
/* 26 */     switch (command.getType()) {
/*    */       case "engineStop":
/* 28 */         return formatCommand(command, "$SRVCMD,{%s},BA#\r\n", new String[] { "uniqueId" });
/*    */       case "engineResume":
/* 30 */         return formatCommand(command, "$SRVCMD,{%s},BD#\r\n", new String[] { "uniqueId" });
/*    */     } 
/* 32 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CarcellProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */