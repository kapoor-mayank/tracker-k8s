/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Date;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.DateUtil;
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
/*     */ public class TaipProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TaipProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
/*     */   }
/*     */   
/*  41 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  42 */     .groupBegin()
/*  43 */     .expression("R[EP]V")
/*  44 */     .groupBegin()
/*  45 */     .number("(dd)")
/*  46 */     .number("(dddd)")
/*  47 */     .number("(d)")
/*  48 */     .groupEnd("?")
/*  49 */     .number("(d{5})")
/*  50 */     .or()
/*  51 */     .expression("(?:RGP|RCQ|RCV|RBR|RUS00),?")
/*  52 */     .number("(dd)?")
/*  53 */     .number("(dd)(dd)(dd)")
/*  54 */     .number("(dd)(dd)(dd)")
/*  55 */     .groupEnd()
/*  56 */     .groupBegin()
/*  57 */     .number("([-+]dd)(d{5})")
/*  58 */     .number("([-+]ddd)(d{5})")
/*  59 */     .or()
/*  60 */     .number("([-+])(dd)(dd.dddd)")
/*  61 */     .number("([-+])(ddd)(dd.dddd)")
/*  62 */     .groupEnd()
/*  63 */     .number("(ddd)")
/*  64 */     .number("(ddd)")
/*  65 */     .groupBegin()
/*  66 */     .number("([023])")
/*  67 */     .number("xx")
/*  68 */     .number("(xx)")
/*  69 */     .groupBegin()
/*  70 */     .number(",d+")
/*  71 */     .number(",(d+)")
/*  72 */     .number(",(d{4})(d{4})")
/*  73 */     .number(",(d+)")
/*  74 */     .groupBegin()
/*  75 */     .number(",([-+]?d+.?d*)")
/*  76 */     .number(",([-+]?d+.?d*)")
/*  77 */     .groupEnd("?")
/*  78 */     .number(",(xx)")
/*  79 */     .or()
/*  80 */     .number("(dd)")
/*  81 */     .number("(dd)")
/*  82 */     .groupEnd()
/*  83 */     .or()
/*  84 */     .groupBegin()
/*  85 */     .number("(xx)")
/*  86 */     .number("(xx)")
/*  87 */     .number("(ddd)")
/*  88 */     .number("(x{8})")
/*  89 */     .number("[01]")
/*  90 */     .groupBegin()
/*  91 */     .number("([023])")
/*  92 */     .number("(dd)")
/*  93 */     .number("dd")
/*  94 */     .number("xxxx")
/*  95 */     .number("[01]")
/*  96 */     .number("[0-5]")
/*  97 */     .number("(dd)")
/*  98 */     .number("([-+]dddd)")
/*  99 */     .number("xx")
/* 100 */     .number("([-+]dddd)")
/* 101 */     .number("xx")
/* 102 */     .groupEnd("?")
/* 103 */     .groupEnd("?")
/* 104 */     .groupEnd()
/* 105 */     .any()
/* 106 */     .compile();
/*     */ 
/*     */ 
/*     */   
/*     */   private Date getTime(long week, long day, long seconds) {
/* 111 */     DateBuilder dateBuilder = (new DateBuilder()).setDate(1980, 1, 6).addMillis(((week * 7L + day) * 24L * 60L * 60L + seconds) * 1000L);
/* 112 */     return dateBuilder.getDate();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Date getTime(long seconds) {
/* 118 */     DateBuilder dateBuilder = (new DateBuilder(new Date())).setTime(0, 0, 0, 0).addMillis(seconds * 1000L);
/* 119 */     return DateUtil.correctDay(dateBuilder.getDate());
/*     */   }
/*     */   
/*     */   private String decodeAlarm(int value) {
/* 123 */     switch (value) {
/*     */       case 1:
/* 125 */         return "sos";
/*     */       case 2:
/* 127 */         return "powerCut";
/*     */     } 
/* 129 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private String decodeAlarm2(int value) {
/* 134 */     switch (value) {
/*     */       case 22:
/* 136 */         return "hardAcceleration";
/*     */       case 23:
/* 138 */         return "hardBraking";
/*     */       case 24:
/* 140 */         return "accident";
/*     */       case 26:
/*     */       case 28:
/* 143 */         return "hardCornering";
/*     */     } 
/* 145 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 153 */     String sentence = (String)msg;
/*     */     
/* 155 */     int beginIndex = sentence.indexOf('>');
/* 156 */     if (beginIndex != -1) {
/* 157 */       sentence = sentence.substring(beginIndex + 1);
/*     */     }
/*     */     
/* 160 */     Parser parser = new Parser(PATTERN, sentence);
/* 161 */     if (!parser.matches()) {
/* 162 */       return null;
/*     */     }
/*     */     
/* 165 */     Position position = new Position(getProtocolName());
/*     */     
/* 167 */     Boolean valid = null;
/* 168 */     Integer event = null;
/*     */     
/* 170 */     if (parser.hasNext(3)) {
/* 171 */       event = parser.nextInt();
/* 172 */       position.setTime(getTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0)));
/* 173 */     } else if (parser.hasNext()) {
/* 174 */       position.setTime(getTime(parser.nextInt(0)));
/*     */     } 
/*     */     
/* 177 */     if (parser.hasNext()) {
/* 178 */       event = parser.nextInt();
/*     */     }
/*     */     
/* 181 */     if (parser.hasNext(6)) {
/* 182 */       position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     }
/*     */     
/* 185 */     if (parser.hasNext(4)) {
/* 186 */       position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_DEG));
/* 187 */       position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_DEG));
/*     */     } 
/* 189 */     if (parser.hasNext(6)) {
/* 190 */       position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 191 */       position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/*     */     } 
/*     */     
/* 194 */     position.setSpeed(convertSpeed(parser.nextDouble(0.0D), "mph"));
/* 195 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 197 */     if (parser.hasNext(2)) {
/* 198 */       valid = Boolean.valueOf((parser.nextInt().intValue() > 0));
/* 199 */       int input = parser.nextHexInt().intValue();
/* 200 */       position.set("ignition", Boolean.valueOf(BitUtil.check(input, 7)));
/* 201 */       position.set("input", Integer.valueOf(input));
/*     */     } 
/*     */     
/* 204 */     if (parser.hasNext(7)) {
/* 205 */       position.set("odometer", parser.nextInt());
/* 206 */       position.set("power", Double.valueOf(parser.nextInt().intValue() * 0.01D));
/* 207 */       position.set("battery", Double.valueOf(parser.nextInt().intValue() * 0.01D));
/* 208 */       position.set("rpm", parser.nextInt());
/* 209 */       position.set("temp1", parser.nextDouble());
/* 210 */       position.set("temp2", parser.nextDouble());
/* 211 */       event = parser.nextHexInt();
/*     */     } 
/*     */     
/* 214 */     if (parser.hasNext(2)) {
/* 215 */       event = parser.nextInt();
/* 216 */       position.set("hdop", parser.nextInt());
/*     */     } 
/*     */     
/* 219 */     if (parser.hasNext(4)) {
/* 220 */       position.set("input", Integer.valueOf(parser.nextHexInt(0)));
/* 221 */       position.set("sat", Integer.valueOf(parser.nextHexInt(0)));
/* 222 */       position.set("battery", Integer.valueOf(parser.nextInt(0)));
/* 223 */       position.set("odometer", Long.valueOf(parser.nextLong(16, 0L)));
/*     */     } 
/*     */     
/* 226 */     if (parser.hasNext(4)) {
/* 227 */       valid = Boolean.valueOf((parser.nextInt().intValue() > 0));
/* 228 */       position.set("pdop", parser.nextInt());
/* 229 */       position.set("rssi", parser.nextInt());
/* 230 */       position.set("temp1", Double.valueOf(parser.nextInt().intValue() * 0.01D));
/* 231 */       position.set("temp2", Double.valueOf(parser.nextInt().intValue() * 0.01D));
/*     */     } 
/*     */     
/* 234 */     position.setValid((valid == null || valid.booleanValue()));
/*     */     
/* 236 */     if (event != null) {
/* 237 */       position.set("event", event);
/* 238 */       if (sentence.charAt(5) == ',') {
/* 239 */         position.set("alarm", decodeAlarm2(event.intValue()));
/*     */       } else {
/* 241 */         position.set("alarm", decodeAlarm(event.intValue()));
/*     */       } 
/*     */     } 
/*     */     
/* 245 */     String[] attributes = null;
/* 246 */     beginIndex = sentence.indexOf(';');
/* 247 */     if (beginIndex != -1) {
/* 248 */       int endIndex = sentence.indexOf('<', beginIndex);
/* 249 */       if (endIndex == -1) {
/* 250 */         endIndex = sentence.length();
/*     */       }
/* 252 */       attributes = sentence.substring(beginIndex, endIndex).split(";");
/*     */     } 
/*     */     
/* 255 */     return decodeAttributes(channel, remoteAddress, position, attributes);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeAttributes(Channel channel, SocketAddress remoteAddress, Position position, String[] attributes) {
/* 261 */     String uniqueId = null;
/* 262 */     DeviceSession deviceSession = null;
/* 263 */     String messageIndex = null;
/*     */     
/* 265 */     if (attributes != null) {
/* 266 */       for (String attribute : attributes) {
/* 267 */         int index = attribute.indexOf('=');
/* 268 */         if (index != -1) {
/* 269 */           String key = attribute.substring(0, index).toLowerCase();
/* 270 */           String value = attribute.substring(index + 1);
/* 271 */           switch (key) {
/*     */             case "id":
/* 273 */               uniqueId = value;
/* 274 */               deviceSession = getDeviceSession(channel, remoteAddress, new String[] { value });
/* 275 */               if (deviceSession != null) {
/* 276 */                 position.setDeviceId(deviceSession.getDeviceId());
/*     */               }
/*     */               break;
/*     */             case "io":
/* 280 */               position.set("ignition", Boolean.valueOf(BitUtil.check((value.charAt(0) - 48), 0)));
/* 281 */               position.set("charge", Boolean.valueOf(BitUtil.check((value.charAt(0) - 48), 1)));
/* 282 */               position.set("output", Integer.valueOf(value.charAt(1) - 48));
/* 283 */               position.set("input", Integer.valueOf(value.charAt(2) - 48));
/*     */               break;
/*     */             case "ix":
/* 286 */               position.set("io1", value);
/*     */               break;
/*     */             case "ad":
/* 289 */               position.set("adc1", Integer.valueOf(Integer.parseInt(value)));
/*     */               break;
/*     */             case "sv":
/* 292 */               position.set("sat", Integer.valueOf(Integer.parseInt(value)));
/*     */               break;
/*     */             case "bl":
/* 295 */               position.set("battery", Double.valueOf(Integer.parseInt(value) * 0.001D));
/*     */               break;
/*     */             case "vo":
/* 298 */               position.set("odometer", Long.valueOf(Long.parseLong(value)));
/*     */               break;
/*     */             default:
/* 301 */               position.set(key, value);
/*     */               break;
/*     */           } 
/* 304 */         } else if (attribute.startsWith("#")) {
/* 305 */           messageIndex = attribute;
/*     */         } 
/*     */       } 
/*     */     }
/*     */     
/* 310 */     if (deviceSession != null) {
/* 311 */       if (channel != null) {
/* 312 */         if (messageIndex != null) {
/*     */           String response;
/* 314 */           if (messageIndex.startsWith("#IP")) {
/* 315 */             response = ">SAK;ID=" + uniqueId + ";" + messageIndex + "<";
/*     */           } else {
/* 317 */             response = ">ACK;ID=" + uniqueId + ";" + messageIndex + ";*";
/* 318 */             response = response + String.format("%02X", new Object[] { Integer.valueOf(Checksum.xor(response)) }) + "<";
/*     */           } 
/* 320 */           channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */         } else {
/* 322 */           channel.writeAndFlush(new NetworkMessage(uniqueId, remoteAddress));
/*     */         } 
/*     */       }
/* 325 */       return position;
/*     */     } 
/*     */     
/* 328 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TaipProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */