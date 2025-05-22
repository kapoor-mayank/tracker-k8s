/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import org.traccar.BaseProtocolEncoder;
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
/*    */ public class Xrb28ProtocolEncoder
/*    */   extends BaseProtocolEncoder
/*    */ {
/*    */   private String formatCommand(Command command, String content) {
/* 25 */     return String.format("ÿÿ*SCOS,OM,%s,%s#\n", new Object[] { getUniqueId(command.getDeviceId()), content });
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Channel channel, Command command) {
/* 31 */     switch (command.getType()) {
/*    */       case "custom":
/* 33 */         return formatCommand(command, command.getString("data"));
/*    */       case "positionSingle":
/* 35 */         return formatCommand(command, "D0");
/*    */       case "positionPeriodic":
/* 37 */         return formatCommand(command, "D1," + command.getInteger("frequency"));
/*    */       case "engineStop":
/*    */       case "alarmDisarm":
/* 40 */         if (channel != null) {
/* 41 */           Xrb28ProtocolDecoder decoder = (Xrb28ProtocolDecoder)channel.pipeline().get(Xrb28ProtocolDecoder.class);
/* 42 */           if (decoder != null) {
/* 43 */             decoder.setPendingCommand(command.getType());
/*    */           }
/*    */         } 
/* 46 */         return formatCommand(command, "R0,0,20,1234," + (System.currentTimeMillis() / 1000L));
/*    */     } 
/* 48 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Xrb28ProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */