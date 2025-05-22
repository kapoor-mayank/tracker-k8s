/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class StartekProtocolEncoder
/*    */   extends StringProtocolEncoder
/*    */ {
/*    */   protected String formatCommand(Command command, String format, String... keys) {
/* 30 */     String uniqueId = getUniqueId(command.getDeviceId());
/* 31 */     String payload = super.formatCommand(command, format, keys);
/* 32 */     int length = 1 + uniqueId.length() + 1 + payload.length();
/* 33 */     String sentence = "$$:" + length + "," + uniqueId + "," + payload;
/* 34 */     return sentence + Checksum.sum(sentence) + "\r\n";
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Channel channel, Command command) {
/* 40 */     switch (command.getType()) {
/*    */       case "custom":
/* 42 */         return formatCommand(command, "{%s}", new String[] { "data" });
/*    */       case "outputControl":
/* 44 */         return formatCommand(command, "900,{%s},{%s}", new String[] { "index", "data" });
/*    */       case "engineStop":
/* 46 */         return formatCommand(command, "900,1,1", new String[0]);
/*    */       case "engineResume":
/* 48 */         return formatCommand(command, "900,1,0", new String[0]);
/*    */     } 
/* 50 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\StartekProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */