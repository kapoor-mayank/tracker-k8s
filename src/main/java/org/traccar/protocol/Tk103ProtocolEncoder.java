/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import org.traccar.Context;
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
/*     */ public class Tk103ProtocolEncoder
/*     */   extends StringProtocolEncoder
/*     */ {
/*     */   private final boolean forceAlternative;
/*     */   
/*     */   public Tk103ProtocolEncoder() {
/*  28 */     this.forceAlternative = false;
/*     */   }
/*     */   
/*     */   public Tk103ProtocolEncoder(boolean forceAlternative) {
/*  32 */     this.forceAlternative = forceAlternative;
/*     */   }
/*     */   
/*     */   private String formatAlt(Command command, String format, String... keys) {
/*  36 */     return formatCommand(command, "[begin]sms2," + format + ",[end]", keys);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object encodeCommand(Command command) {
/*  42 */     boolean alternative = (this.forceAlternative || Context.getIdentityManager().lookupAttributeBoolean(command
/*  43 */         .getDeviceId(), "tk103.alternative", false, true));
/*     */     
/*  45 */     initDevicePassword(command, "123456");
/*     */     
/*  47 */     if (alternative) {
/*  48 */       switch (command.getType()) {
/*     */         case "custom":
/*  50 */           return formatAlt(command, "{%s}", new String[] { "data" });
/*     */         case "getVersion":
/*  52 */           return formatAlt(command, "*about*", new String[0]);
/*     */         case "powerOff":
/*  54 */           return formatAlt(command, "*turnoff*", new String[0]);
/*     */         case "rebootDevice":
/*  56 */           return formatAlt(command, "88888888", new String[0]);
/*     */         case "positionSingle":
/*  58 */           return formatAlt(command, "*getposl*", new String[0]);
/*     */         case "positionPeriodic":
/*  60 */           return formatAlt(command, "*routetrack*99*", new String[0]);
/*     */         case "positionStop":
/*  62 */           return formatAlt(command, "*routetrackoff*", new String[0]);
/*     */         case "getDeviceStatus":
/*  64 */           return formatAlt(command, "*status*", new String[0]);
/*     */         case "deviceIdentification":
/*  66 */           return formatAlt(command, "999999", new String[0]);
/*     */         case "modeDeepSleep":
/*  68 */           return formatAlt(command, command.getBoolean("enable") ? "*sleep*2*" : "*sleepoff*", new String[0]);
/*     */         case "modePowerSaving":
/*  70 */           return formatAlt(command, command.getBoolean("enable") ? "*sleepv*" : "*sleepoff*", new String[0]);
/*     */         case "alarmSos":
/*  72 */           return formatAlt(command, command.getBoolean("enable") ? "*soson*" : "*sosoff*", new String[0]);
/*     */         case "setConnection":
/*  74 */           return formatAlt(command, "*setip*%s*{%s}*", new String[] { command
/*  75 */                 .getString("server").replace(".", "*"), "port" });
/*     */         case "sosNumber":
/*  77 */           return formatAlt(command, "*master*{%s}*{%s}*", new String[] { "devicePassword", "phone" });
/*     */       } 
/*  79 */       return null;
/*     */     } 
/*     */     
/*  82 */     switch (command.getType()) {
/*     */       case "custom":
/*  84 */         return formatCommand(command, "({%s}{%s})", new String[] { "uniqueId", "data" });
/*     */       case "getVersion":
/*  86 */         return formatCommand(command, "({%s}AP07)", new String[] { "uniqueId" });
/*     */       case "rebootDevice":
/*  88 */         return formatCommand(command, "({%s}AT00)", new String[] { "uniqueId" });
/*     */       case "setOdometer":
/*  90 */         return formatCommand(command, "({%s}AX01)", new String[] { "uniqueId" });
/*     */       case "positionSingle":
/*  92 */         return formatCommand(command, "({%s}AP00)", new String[] { "uniqueId" });
/*     */       case "positionPeriodic":
/*  94 */         return formatCommand(command, "({%s}AR00%s0000)", new String[] { "uniqueId", 
/*  95 */               String.format("%04X", new Object[] { Integer.valueOf(command.getInteger("frequency")) }) });
/*     */       case "positionStop":
/*  97 */         return formatCommand(command, "({%s}AR0000000000)", new String[] { "uniqueId" });
/*     */       case "engineStop":
/*  99 */         return formatCommand(command, "({%s}AV010)", new String[] { "uniqueId" });
/*     */       case "engineResume":
/* 101 */         return formatCommand(command, "({%s}AV011)", new String[] { "uniqueId" });
/*     */       case "outputControl":
/* 103 */         return formatCommand(command, "({%s}AV00{%s})", new String[] { "uniqueId", "data" });
/*     */     } 
/* 105 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Tk103ProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */