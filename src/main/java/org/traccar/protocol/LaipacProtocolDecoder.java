/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
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
/*     */ public class LaipacProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public LaipacProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */   
/*  40 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  41 */     .text("$AVRMC,")
/*  42 */     .expression("([^,]+),")
/*  43 */     .number("(dd)(dd)(dd),")
/*  44 */     .expression("([AVRPavrp]),")
/*  45 */     .number("(dd)(dd.d+),")
/*  46 */     .expression("([NS]),")
/*  47 */     .number("(ddd)(dd.d+),")
/*  48 */     .number("([EW]),")
/*  49 */     .number("(d+.d+),")
/*  50 */     .number("(d+.d+),")
/*  51 */     .number("(dd)(dd)(dd),")
/*  52 */     .expression("([abZXTSMHFE86430]),")
/*  53 */     .expression("([\\d.]+),")
/*  54 */     .number("(d+),")
/*  55 */     .number("(d),")
/*  56 */     .number("(d+),")
/*  57 */     .number("(d+)")
/*  58 */     .number(",(xxxx)")
/*  59 */     .number("(xxxx),")
/*  60 */     .number("(ddd)")
/*  61 */     .number("(ddd)")
/*  62 */     .optional(4)
/*  63 */     .text("*")
/*  64 */     .number("(xx)")
/*  65 */     .compile();
/*     */   
/*     */   private String decodeAlarm(String event) {
/*  68 */     switch (event) {
/*     */       case "Z":
/*  70 */         return "lowBattery";
/*     */       case "X":
/*  72 */         return "geofenceEnter";
/*     */       case "T":
/*  74 */         return "tampering";
/*     */       case "H":
/*  76 */         return "powerOff";
/*     */       case "8":
/*  78 */         return "shock";
/*     */       case "7":
/*     */       case "4":
/*  81 */         return "geofenceExit";
/*     */       case "6":
/*  83 */         return "overspeed";
/*     */       case "3":
/*  85 */         return "sos";
/*     */     } 
/*  87 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  95 */     String sentence = (String)msg;
/*     */     
/*  97 */     if (sentence.startsWith("$ECHK") && channel != null) {
/*  98 */       channel.writeAndFlush(new NetworkMessage(sentence + "\r\n", remoteAddress));
/*  99 */       return null;
/*     */     } 
/*     */     
/* 102 */     Parser parser = new Parser(PATTERN, sentence);
/* 103 */     if (!parser.matches()) {
/* 104 */       return null;
/*     */     }
/*     */     
/* 107 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 108 */     if (deviceSession == null) {
/* 109 */       return null;
/*     */     }
/*     */     
/* 112 */     Position position = new Position(getProtocolName());
/* 113 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */     
/* 116 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 118 */     String status = parser.next();
/* 119 */     String upperCaseStatus = status.toUpperCase();
/* 120 */     position.setValid((upperCaseStatus.equals("A") || upperCaseStatus.equals("R") || upperCaseStatus.equals("P")));
/* 121 */     position.set("status", status);
/*     */     
/* 123 */     position.setLatitude(parser.nextCoordinate());
/* 124 */     position.setLongitude(parser.nextCoordinate());
/* 125 */     position.setSpeed(parser.nextDouble(0.0D));
/* 126 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 128 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 129 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 131 */     String event = parser.next();
/* 132 */     position.set("alarm", decodeAlarm(event));
/* 133 */     position.set("event", event);
/* 134 */     position.set("battery", Double.valueOf(Double.parseDouble(parser.next().replaceAll("\\.", "")) * 0.001D));
/* 135 */     position.set("odometer", parser.nextDouble());
/* 136 */     position.set("gps", parser.nextInt());
/* 137 */     position.set("adc1", Double.valueOf(parser.nextDouble().doubleValue() * 0.001D));
/* 138 */     position.set("adc2", Double.valueOf(parser.nextDouble().doubleValue() * 0.001D));
/*     */     
/* 140 */     Integer lac = parser.nextHexInt();
/* 141 */     Integer cid = parser.nextHexInt();
/* 142 */     Integer mcc = parser.nextInt();
/* 143 */     Integer mnc = parser.nextInt();
/* 144 */     if (lac != null && cid != null && mcc != null && mnc != null) {
/* 145 */       position.setNetwork(new Network(CellTower.from(mcc.intValue(), mnc.intValue(), lac.intValue(), cid.intValue())));
/*     */     }
/*     */     
/* 148 */     String checksum = parser.next();
/*     */     
/* 150 */     if (channel != null) {
/* 151 */       if (event.equals("3")) {
/* 152 */         channel.writeAndFlush(new NetworkMessage("$AVCFG,00000000,d*31\r\n", remoteAddress));
/* 153 */       } else if (event.equals("X") || event.equals("4")) {
/* 154 */         channel.writeAndFlush(new NetworkMessage("$AVCFG,00000000,x*2D\r\n", remoteAddress));
/* 155 */       } else if (event.equals("Z")) {
/* 156 */         channel.writeAndFlush(new NetworkMessage("$AVCFG,00000000,z*2F\r\n", remoteAddress));
/* 157 */       } else if (Character.isLowerCase(status.charAt(0))) {
/* 158 */         String response = "$EAVACK," + event + "," + checksum;
/* 159 */         response = response + Checksum.nmea(response) + "\r\n";
/* 160 */         channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */       } 
/*     */     }
/*     */     
/* 164 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\LaipacProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */