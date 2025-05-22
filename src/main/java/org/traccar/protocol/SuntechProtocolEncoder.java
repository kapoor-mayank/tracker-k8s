/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import org.traccar.BasePipelineFactory;
/*     */ import org.traccar.StringProtocolEncoder;
/*     */ import org.traccar.model.Command;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class SuntechProtocolEncoder
/*     */   extends StringProtocolEncoder
/*     */ {
/*     */   protected Object encodeCommand(Channel channel, Command command) {
/*  28 */     boolean universal = false;
/*  29 */     String prefix = "SA200";
/*  30 */     if (channel != null) {
/*     */       
/*  32 */       SuntechProtocolDecoder protocolDecoder = (SuntechProtocolDecoder)BasePipelineFactory.getHandler(channel.pipeline(), SuntechProtocolDecoder.class);
/*  33 */       if (protocolDecoder != null) {
/*  34 */         universal = protocolDecoder.getUniversal();
/*  35 */         String decoderPrefix = protocolDecoder.getPrefix();
/*  36 */         if (decoderPrefix != null && decoderPrefix.length() > 5) {
/*  37 */           prefix = decoderPrefix.substring(0, decoderPrefix.length() - 3);
/*     */         }
/*     */       } 
/*     */     } 
/*     */     
/*  42 */     if (universal) {
/*  43 */       return encodeUniversalCommand(channel, command);
/*     */     }
/*  45 */     return encodeLegacyCommand(channel, prefix, command);
/*     */   }
/*     */ 
/*     */   
/*     */   protected Object encodeUniversalCommand(Channel channel, Command command) {
/*  50 */     switch (command.getType()) {
/*     */       case "rebootDevice":
/*  52 */         return formatCommand(command, "CMD;{%s};03;03\r", new String[] { "uniqueId" });
/*     */       case "positionSingle":
/*  54 */         return formatCommand(command, "CMD;{%s};03;01\r", new String[] { "uniqueId" });
/*     */       case "outputControl":
/*  56 */         if (command.getAttributes().get("data").equals("1")) {
/*  57 */           switch (command.getString("index")) {
/*     */             case "1":
/*  59 */               return formatCommand(command, "CMD;{%s};04;01\r", new String[] { "uniqueId" });
/*     */             case "2":
/*  61 */               return formatCommand(command, "CMD;{%s};04;03\r", new String[] { "uniqueId" });
/*     */             case "3":
/*  63 */               return formatCommand(command, "CMD;{%s};04;09\r", new String[] { "uniqueId" });
/*     */           } 
/*  65 */           return null;
/*     */         } 
/*     */         
/*  68 */         switch (command.getString("index")) {
/*     */           case "1":
/*  70 */             return formatCommand(command, "CMD;{%s};04;02\r", new String[] { "uniqueId" });
/*     */           case "2":
/*  72 */             return formatCommand(command, "CMD;{%s};04;04\r", new String[] { "uniqueId" });
/*     */           case "3":
/*  74 */             return formatCommand(command, "CMD;{%s};04;10\r", new String[] { "uniqueId" });
/*     */         } 
/*  76 */         return null;
/*     */ 
/*     */       
/*     */       case "engineStop":
/*  80 */         return formatCommand(command, "CMD;{%s};04;01\r", new String[] { "uniqueId" });
/*     */       case "engineResume":
/*  82 */         return formatCommand(command, "CMD;{%s};02;02\r", new String[] { "uniqueId" });
/*     */       case "alarmArm":
/*  84 */         return formatCommand(command, "CMD;{%s};04;03\r", new String[] { "uniqueId" });
/*     */       case "alarmDisarm":
/*  86 */         return formatCommand(command, "CMD;{%s};04;04\r", new String[] { "uniqueId" });
/*     */     } 
/*  88 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   protected Object encodeLegacyCommand(Channel channel, String prefix, Command command) {
/*  93 */     switch (command.getType()) {
/*     */       case "rebootDevice":
/*  95 */         return formatCommand(command, prefix + "CMD;{%s};02;Reboot\r", new String[] { "uniqueId" });
/*     */       case "positionSingle":
/*  97 */         return formatCommand(command, prefix + "CMD;{%s};02;\r", new String[] { "uniqueId" });
/*     */       case "outputControl":
/*  99 */         if (command.getAttributes().containsKey("data")) {
/* 100 */           if (command.getAttributes().get("data").equals("1")) {
/* 101 */             return formatCommand(command, prefix + "CMD;{%s};02;Enable%s\r", new String[] { "uniqueId", "index" });
/*     */           }
/*     */           
/* 104 */           return formatCommand(command, prefix + "CMD;{%s};02;Disable%s\r", new String[] { "uniqueId", "index" });
/*     */         } 
/*     */ 
/*     */       
/*     */       case "engineStop":
/* 109 */         return formatCommand(command, prefix + "CMD;{%s};02;Enable1\r", new String[] { "uniqueId" });
/*     */       case "engineResume":
/* 111 */         return formatCommand(command, prefix + "CMD;{%s};02;Disable1\r", new String[] { "uniqueId" });
/*     */       case "alarmArm":
/* 113 */         return formatCommand(command, prefix + "CMD;{%s};02;Enable2\r", new String[] { "uniqueId" });
/*     */       case "alarmDisarm":
/* 115 */         return formatCommand(command, prefix + "CMD;{%s};02;Disable2\r", new String[] { "uniqueId" });
/*     */     } 
/* 117 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SuntechProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */