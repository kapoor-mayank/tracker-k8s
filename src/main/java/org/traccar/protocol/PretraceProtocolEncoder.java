/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import org.traccar.BaseProtocolEncoder;
/*    */ import org.traccar.Context;
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
/*    */ public class PretraceProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   private String formatCommand(String uniqueId, String data) {
/* 26 */     String content = uniqueId + data;
/* 27 */     return String.format("(%s^%02X)", new Object[] { content, Integer.valueOf(Checksum.xor(content)) });
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 33 */     String uniqueId = Context.getIdentityManager().getById(command.getDeviceId()).getUniqueId();
/*    */     
/* 35 */     switch (command.getType()) {
/*    */       case "custom":
/* 37 */         return formatCommand(uniqueId, command.getString("data"));
/*    */       case "positionPeriodic":
/* 39 */         return formatCommand(uniqueId, 
/* 40 */             String.format("D221%1$d,%1$d,,", new Object[] { Integer.valueOf(command.getInteger("frequency")) }));
/*    */     } 
/* 42 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PretraceProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */