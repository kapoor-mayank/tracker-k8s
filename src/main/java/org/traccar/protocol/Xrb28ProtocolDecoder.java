/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.model.Position;
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
/*     */ public class Xrb28ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private String pendingCommand;
/*     */   
/*     */   public void setPendingCommand(String pendingCommand) {
/*  37 */     this.pendingCommand = pendingCommand;
/*     */   }
/*     */   
/*     */   public Xrb28ProtocolDecoder(Protocol protocol) {
/*  41 */     super(protocol);
/*     */   }
/*     */   
/*  44 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  45 */     .text("*")
/*  46 */     .expression("....,")
/*  47 */     .expression("..,")
/*  48 */     .number("d{15},")
/*  49 */     .expression("..,")
/*  50 */     .number("0,")
/*  51 */     .number("(dd)(dd)(dd).d+,")
/*  52 */     .expression("([AV]),")
/*  53 */     .number("(dd)(dd.d+),")
/*  54 */     .expression("([NS]),")
/*  55 */     .number("(d{2,3})(dd.d+),")
/*  56 */     .expression("([EW]),")
/*  57 */     .number("(d+),")
/*  58 */     .number("(d+.d+),")
/*  59 */     .number("(dd)(dd)(dd),")
/*  60 */     .number("(-?d+.?d*),")
/*  61 */     .expression(".,")
/*  62 */     .expression(".#")
/*  63 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  69 */     String sentence = (String)msg;
/*     */     
/*  71 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(9, 24) });
/*  72 */     if (deviceSession == null) {
/*  73 */       return null;
/*     */     }
/*     */     
/*  76 */     String type = sentence.substring(25, 27);
/*  77 */     if (channel != null) {
/*  78 */       if (type.matches("L0|L1|W0|E1")) {
/*  79 */         channel.write(new NetworkMessage("ÿÿ*SCOS" + sentence
/*  80 */               .substring(5, 27) + "#\n", remoteAddress));
/*     */       }
/*  82 */       else if (type.equals("R0") && this.pendingCommand != null) {
/*  83 */         String command = this.pendingCommand.equals("alarmArm") ? "L1," : "L0,";
/*  84 */         channel.write(new NetworkMessage("ÿÿ*SCOS" + sentence
/*  85 */               .substring(5, 25) + command + sentence.substring(30) + "\n", remoteAddress));
/*     */         
/*  87 */         this.pendingCommand = null;
/*     */       } 
/*     */     }
/*     */     
/*  91 */     if (!type.startsWith("D")) {
/*     */       
/*  93 */       Position position1 = new Position(getProtocolName());
/*  94 */       position1.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  96 */       getLastLocation(position1, null);
/*     */       
/*  98 */       String payload = sentence.substring(25, sentence.length() - 1);
/*     */       
/* 100 */       int index = 0;
/* 101 */       String[] values = payload.substring(3).split(",");
/*     */       
/* 103 */       switch (type) {
/*     */         case "Q0":
/* 105 */           position1.set("battery", Double.valueOf(Integer.parseInt(values[index++]) * 0.01D));
/* 106 */           position1.set("batteryLevel", Integer.valueOf(Integer.parseInt(values[index++])));
/* 107 */           position1.set("rssi", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */           break;
/*     */         case "H0":
/* 110 */           position1.set("blocked", Boolean.valueOf((Integer.parseInt(values[index++]) > 0)));
/* 111 */           position1.set("battery", Double.valueOf(Integer.parseInt(values[index++]) * 0.01D));
/* 112 */           position1.set("rssi", Integer.valueOf(Integer.parseInt(values[index++])));
/* 113 */           position1.set("batteryLevel", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */           break;
/*     */         case "W0":
/* 116 */           switch (Integer.parseInt(values[index++])) {
/*     */             case 1:
/* 118 */               position1.set("alarm", "movement");
/*     */               break;
/*     */             case 2:
/* 121 */               position1.set("alarm", "fallDown");
/*     */               break;
/*     */             case 3:
/* 124 */               position1.set("alarm", "lowBattery");
/*     */               break;
/*     */           } 
/*     */           
/*     */           break;
/*     */         
/*     */         case "E0":
/* 131 */           position1.set("alarm", "fault");
/* 132 */           position1.set("error", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */           break;
/*     */         case "S1":
/* 135 */           position1.set("event", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */           break;
/*     */         case "R0":
/*     */         case "L0":
/*     */         case "L1":
/*     */         case "S4":
/*     */         case "S5":
/*     */         case "S6":
/*     */         case "S7":
/*     */         case "V0":
/*     */         case "G0":
/*     */         case "K0":
/*     */         case "I0":
/*     */         case "M0":
/* 149 */           position1.set("result", payload);
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 155 */       return !position1.getAttributes().isEmpty() ? position1 : null;
/*     */     } 
/*     */ 
/*     */     
/* 159 */     Parser parser = new Parser(PATTERN, sentence);
/* 160 */     if (!parser.matches()) {
/* 161 */       return null;
/*     */     }
/*     */     
/* 164 */     Position position = new Position(getProtocolName());
/* 165 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */     
/* 168 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*     */     
/* 170 */     position.setValid(parser.next().equals("A"));
/* 171 */     position.setLatitude(parser.nextCoordinate());
/* 172 */     position.setLongitude(parser.nextCoordinate());
/*     */     
/* 174 */     position.set("sat", parser.nextInt());
/* 175 */     position.set("hdop", parser.nextDouble());
/*     */     
/* 177 */     dateBuilder.setDateReverse(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/* 178 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 180 */     position.setAltitude(parser.nextDouble().doubleValue());
/*     */     
/* 182 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Xrb28ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */