/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.Network;
/*     */ import org.traccar.model.Position;
/*     */ import org.traccar.model.WifiAccessPoint;
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
/*     */ public class Tlt2hProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private final boolean custom;
/*  40 */   private Boolean charge = null;
/*     */   
/*     */   public Tlt2hProtocolDecoder(Protocol protocol, boolean custom) {
/*  43 */     super(protocol);
/*  44 */     this.custom = custom;
/*     */   }
/*     */   
/*  47 */   private static final Pattern PATTERN_HEADER = (new PatternBuilder())
/*  48 */     .number("#(d+)")
/*  49 */     .expression("#[^#]*")
/*  50 */     .number("#d*")
/*  51 */     .groupBegin()
/*  52 */     .number("#([01])")
/*  53 */     .number("#(d+)")
/*  54 */     .number("#(d+)")
/*  55 */     .number("#(d+)")
/*  56 */     .number("#(d+)")
/*  57 */     .groupEnd("?")
/*  58 */     .expression("#([^#]+)")
/*  59 */     .number("#d+")
/*  60 */     .compile();
/*     */   
/*  62 */   private static final Pattern PATTERN_POSITION = (new PatternBuilder())
/*  63 */     .text("#")
/*  64 */     .number("(?:(dd)|x*)")
/*  65 */     .text("$GPRMC,")
/*  66 */     .number("(dd)(dd)(dd).d+,")
/*  67 */     .expression("([AVL]),")
/*  68 */     .number("(d+)(dd.d+),")
/*  69 */     .expression("([NS]),")
/*  70 */     .number("(d+)(dd.d+),")
/*  71 */     .number("([EW]),")
/*  72 */     .number("(d+.?d*)?,")
/*  73 */     .number("(d+.?d*)?,")
/*  74 */     .number("(dd)(dd)(dd)")
/*  75 */     .any()
/*  76 */     .compile();
/*     */   
/*  78 */   private static final Pattern PATTERN_WIFI = (new PatternBuilder())
/*  79 */     .text("#")
/*  80 */     .number("(?:(dd)|x+)")
/*  81 */     .text("$WIFI,")
/*  82 */     .number("(dd)(dd)(dd).d+,")
/*  83 */     .expression("[AVL],")
/*  84 */     .expression("(.*)")
/*  85 */     .number("(dd)(dd)(dd)")
/*  86 */     .text("*")
/*  87 */     .number("xx")
/*  88 */     .compile();
/*     */   
/*     */   private void decodeStatus(Position position, String status) {
/*  91 */     if (this.custom) {
/*  92 */       if (status.equals("DEF")) {
/*  93 */         this.charge = Boolean.valueOf(false);
/*     */       }
/*  95 */       position.set("charge", this.charge);
/*     */     } 
/*  97 */     switch (status) {
/*     */       case "AUTOSTART":
/*     */       case "AUTO":
/* 100 */         if (!this.custom) {
/* 101 */           position.set("ignition", Boolean.valueOf(true));
/*     */         }
/*     */         break;
/*     */       case "AUTOSTOP":
/*     */       case "AUTOLOW":
/* 106 */         if (!this.custom) {
/* 107 */           position.set("ignition", Boolean.valueOf(false));
/*     */         }
/*     */         break;
/*     */       case "TOWED":
/* 111 */         position.set("alarm", "tow");
/*     */         break;
/*     */       case "SOS":
/* 114 */         position.set("alarm", "sos");
/*     */         break;
/*     */       case "DEF":
/* 117 */         position.set("alarm", "powerCut");
/*     */         break;
/*     */       case "BLP":
/* 120 */         position.set("alarm", "lowBattery");
/*     */         break;
/*     */       case "CLP":
/* 123 */         position.set("alarm", "lowPower");
/*     */         break;
/*     */       case "OS":
/* 126 */         position.set("alarm", "geofenceExit");
/*     */         break;
/*     */       case "RS":
/* 129 */         position.set("alarm", "geofenceEnter");
/*     */         break;
/*     */       case "OVERSPEED":
/* 132 */         position.set("alarm", "overspeed");
/*     */         break;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 143 */     String sentence = (String)msg;
/* 144 */     sentence = sentence.trim();
/*     */     
/* 146 */     String header = sentence.substring(0, sentence.indexOf('\r'));
/* 147 */     Parser parser = new Parser(PATTERN_HEADER, header);
/* 148 */     if (!parser.matches()) {
/* 149 */       return null;
/*     */     }
/*     */     
/* 152 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 153 */     if (deviceSession == null) {
/* 154 */       return null;
/*     */     }
/*     */     
/* 157 */     Position last = Context.getIdentityManager().getLastPosition(deviceSession.getDeviceId());
/* 158 */     if (last != null && 
/* 159 */       last.getAttributes().containsKey("charge")) {
/* 160 */       this.charge = Boolean.valueOf(last.getBoolean("charge"));
/*     */     }
/*     */ 
/*     */     
/* 164 */     Boolean door = null;
/* 165 */     Double adc = null;
/* 166 */     Double power = null;
/* 167 */     Double battery = null;
/* 168 */     Double temperature = null;
/* 169 */     if (parser.hasNext(5)) {
/* 170 */       door = Boolean.valueOf((parser.nextInt().intValue() == 1));
/* 171 */       adc = Double.valueOf(parser.nextInt().intValue() * 0.1D);
/* 172 */       power = Double.valueOf(parser.nextInt().intValue() * 0.1D);
/* 173 */       battery = Double.valueOf(parser.nextInt().intValue() * 0.1D);
/* 174 */       temperature = Double.valueOf(parser.nextInt().intValue() * 0.1D);
/*     */     } 
/*     */     
/* 177 */     String status = parser.next();
/*     */     
/* 179 */     String[] messages = sentence.substring(sentence.indexOf('\n') + 1).split("\r\n");
/* 180 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 182 */     for (String message : messages) {
/* 183 */       Position position = new Position(getProtocolName());
/* 184 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 186 */       if (message.contains("$GPRMC")) {
/*     */         
/* 188 */         parser = new Parser(PATTERN_POSITION, message);
/* 189 */         if (parser.matches()) {
/*     */           
/* 191 */           if (parser.hasNext()) {
/* 192 */             position.set("battery", Double.valueOf(parser.nextInt().intValue() * 0.1D));
/*     */           }
/*     */ 
/*     */           
/* 196 */           DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*     */           
/* 198 */           position.setValid(parser.next().equals("A"));
/* 199 */           position.setLatitude(parser.nextCoordinate());
/* 200 */           position.setLongitude(parser.nextCoordinate());
/* 201 */           double speed = parser.nextDouble(0.0D);
/* 202 */           if (this.custom) {
/* 203 */             if (UnitsConverter.kphFromKnots(speed) > 10.0D) {
/* 204 */               position.set("ignition", Boolean.valueOf(true));
/* 205 */               this.charge = Boolean.valueOf(true);
/*     */             } else {
/* 207 */               position.set("ignition", Boolean.valueOf(false));
/*     */             } 
/*     */           }
/* 210 */           position.setSpeed(speed);
/* 211 */           position.setCourse(parser.nextDouble(0.0D));
/*     */           
/* 213 */           dateBuilder.setDateReverse(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/* 214 */           position.setTime(dateBuilder.getDate());
/*     */         }
/*     */         else {
/*     */           
/*     */           continue;
/*     */         } 
/* 220 */       } else if (message.contains("$WIFI")) {
/*     */         
/* 222 */         parser = new Parser(PATTERN_WIFI, message);
/* 223 */         if (parser.matches())
/*     */         {
/* 225 */           position.set("battery", Double.valueOf(parser.nextInt().intValue() * 0.1D));
/*     */ 
/*     */           
/* 228 */           DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*     */           
/* 230 */           String[] values = parser.next().split(",");
/* 231 */           Network network = new Network();
/* 232 */           for (int i = 0; i < values.length / 2; i++) {
/* 233 */             String mac = values[i * 2 + 1].replaceAll("(..)", "$1:").substring(0, 17);
/* 234 */             network.addWifiAccessPoint(WifiAccessPoint.from(mac, Integer.parseInt(values[i * 2])));
/*     */           } 
/* 236 */           position.setNetwork(network);
/*     */           
/* 238 */           dateBuilder.setDateReverse(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*     */           
/* 240 */           getLastLocation(position, dateBuilder.getDate());
/*     */         }
/*     */       
/*     */       } else {
/*     */         
/* 245 */         getLastLocation(position, null);
/*     */       } 
/*     */ 
/*     */       
/* 249 */       position.set("door", door);
/* 250 */       position.set("adc1", adc);
/* 251 */       position.set("power", power);
/* 252 */       position.set("battery", battery);
/* 253 */       position.set("temp1", temperature);
/* 254 */       decodeStatus(position, status);
/*     */       
/* 256 */       positions.add(position);
/*     */       continue;
/*     */     } 
/* 259 */     return positions.isEmpty() ? null : positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Tlt2hProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */