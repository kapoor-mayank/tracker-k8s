/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
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
/*     */ public class EasyTrackProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public EasyTrackProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */   
/*  39 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  40 */     .text("*").expression("..,")
/*  41 */     .number("(d+),")
/*  42 */     .expression("([^,]{2}),")
/*  43 */     .expression("([AV]),")
/*  44 */     .number("(xx)(xx)(xx),")
/*  45 */     .number("(xx)(xx)(xx),")
/*  46 */     .number("(x)(x{7}),")
/*  47 */     .number("(x)(x{7}),")
/*  48 */     .number("(x{4}),")
/*  49 */     .number("(x{4}),")
/*  50 */     .number("(x{8}),")
/*  51 */     .number("(x+),")
/*  52 */     .number("(d+),")
/*  53 */     .number("(x+),")
/*  54 */     .number("(x+)")
/*  55 */     .groupBegin()
/*  56 */     .number(",(x+)")
/*  57 */     .groupBegin()
/*  58 */     .number(",d+")
/*  59 */     .number(",(d*)")
/*  60 */     .number(",(x+)")
/*  61 */     .number(",(d+.d+)")
/*  62 */     .number(",(d+)")
/*  63 */     .groupEnd("?")
/*  64 */     .groupEnd("?")
/*  65 */     .any()
/*  66 */     .compile();
/*     */   
/*  68 */   private static final Pattern PATTERN_OBD = (new PatternBuilder())
/*  69 */     .text("*").expression("..,")
/*  70 */     .number("(d+),")
/*  71 */     .text("OB,")
/*  72 */     .number("(xx)(xx)(xx),")
/*  73 */     .number("(xx)(xx)(xx),")
/*  74 */     .text("BD$")
/*  75 */     .number("V(d+.d);")
/*  76 */     .number("R(d+);")
/*  77 */     .number("S(d+);")
/*  78 */     .number("P(d+.d);")
/*  79 */     .number("O(d+.d);")
/*  80 */     .number("C(d+);")
/*  81 */     .number("L(d+.d);")
/*  82 */     .number("[XY][MH]d+.d+;")
/*  83 */     .number("Md+.?d*;")
/*  84 */     .number("F(d+.d+);")
/*  85 */     .number("T(d+);")
/*  86 */     .any()
/*  87 */     .compile();
/*     */   
/*     */   private String decodeAlarm(long status) {
/*  90 */     if ((status & 0x2000000L) != 0L) {
/*  91 */       return "geofenceEnter";
/*     */     }
/*  93 */     if ((status & 0x4000000L) != 0L) {
/*  94 */       return "geofenceExit";
/*     */     }
/*  96 */     if ((status & 0x8000000L) != 0L) {
/*  97 */       return "lowBattery";
/*     */     }
/*  99 */     if ((status & 0x20000000L) != 0L) {
/* 100 */       return "vibration";
/*     */     }
/* 102 */     if ((status & 0x80000000L) != 0L) {
/* 103 */       return "overspeed";
/*     */     }
/* 105 */     if ((status & 0x10000L) != 0L) {
/* 106 */       return "sos";
/*     */     }
/* 108 */     if ((status & 0x40000L) != 0L) {
/* 109 */       return "powerCut";
/*     */     }
/* 111 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 118 */     String sentence = (String)msg;
/* 119 */     String type = sentence.substring(20, 22);
/*     */     
/* 121 */     if ((type.equals("TX") || type.equals("MQ")) && channel != null) {
/* 122 */       channel.writeAndFlush(new NetworkMessage(sentence + "#", remoteAddress));
/*     */     }
/*     */     
/* 125 */     if (type.equals("OB")) {
/* 126 */       return decodeObd(channel, remoteAddress, sentence);
/*     */     }
/* 128 */     return decodeLocation(channel, remoteAddress, sentence);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeLocation(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 134 */     Parser parser = new Parser(PATTERN, sentence);
/* 135 */     if (!parser.matches()) {
/* 136 */       return null;
/*     */     }
/*     */     
/* 139 */     Position position = new Position(getProtocolName());
/*     */     
/* 141 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 142 */     if (deviceSession == null) {
/* 143 */       return null;
/*     */     }
/* 145 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 147 */     position.set("command", parser.next());
/*     */     
/* 149 */     position.setValid(parser.next().equals("A"));
/*     */ 
/*     */ 
/*     */     
/* 153 */     DateBuilder dateBuilder = (new DateBuilder()).setDate(parser.nextHexInt().intValue(), parser.nextHexInt().intValue(), parser.nextHexInt().intValue()).setTime(parser.nextHexInt().intValue(), parser.nextHexInt().intValue(), parser.nextHexInt().intValue());
/* 154 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 156 */     if (BitUtil.check(parser.nextHexInt().intValue(), 3)) {
/* 157 */       position.setLatitude(-parser.nextHexInt().intValue() / 600000.0D);
/*     */     } else {
/* 159 */       position.setLatitude(parser.nextHexInt().intValue() / 600000.0D);
/*     */     } 
/*     */     
/* 162 */     if (BitUtil.check(parser.nextHexInt().intValue(), 3)) {
/* 163 */       position.setLongitude(-parser.nextHexInt().intValue() / 600000.0D);
/*     */     } else {
/* 165 */       position.setLongitude(parser.nextHexInt().intValue() / 600000.0D);
/*     */     } 
/*     */     
/* 168 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextHexInt().intValue() / 100.0D));
/* 169 */     double course = parser.nextHexInt().intValue() * 0.01D;
/* 170 */     if (course < 360.0D) {
/* 171 */       position.setCourse(course);
/*     */     }
/*     */     
/* 174 */     long status = parser.nextHexLong().longValue();
/* 175 */     position.set("alarm", decodeAlarm(status));
/* 176 */     position.set("blocked", Boolean.valueOf(((status & 0x80000L) > 0L)));
/* 177 */     position.set("ignition", Boolean.valueOf(((status & 0x800000L) > 0L)));
/* 178 */     position.set("status", Long.valueOf(status));
/*     */     
/* 180 */     position.set("rssi", parser.nextHexInt());
/* 181 */     position.set("power", parser.nextDouble());
/* 182 */     position.set("fuel", parser.nextHexInt());
/* 183 */     position.set("odometer", Integer.valueOf(parser.nextHexInt().intValue() * 100));
/*     */     
/* 185 */     position.setAltitude(parser.nextDouble(0.0D));
/*     */     
/* 187 */     if (parser.hasNext(4)) {
/* 188 */       position.set("driverUniqueId", parser.next());
/* 189 */       position.set("temp1", Double.valueOf(parser.nextHexInt().intValue() * 0.01D));
/* 190 */       position.set("adc1", parser.nextDouble());
/* 191 */       position.set("sat", parser.nextInt());
/*     */     } 
/*     */     
/* 194 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeObd(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 199 */     Parser parser = new Parser(PATTERN_OBD, sentence);
/* 200 */     if (!parser.matches()) {
/* 201 */       return null;
/*     */     }
/*     */     
/* 204 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 205 */     if (deviceSession == null) {
/* 206 */       return null;
/*     */     }
/*     */     
/* 209 */     Position position = new Position(getProtocolName());
/* 210 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 215 */     DateBuilder dateBuilder = (new DateBuilder()).setDate(parser.nextHexInt().intValue(), parser.nextHexInt().intValue(), parser.nextHexInt().intValue()).setTime(parser.nextHexInt().intValue(), parser.nextHexInt().intValue(), parser.nextHexInt().intValue());
/* 216 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 218 */     position.set("battery", parser.nextDouble());
/* 219 */     position.set("rpm", parser.nextInt());
/* 220 */     position.set("obdSpeed", parser.nextInt());
/* 221 */     position.set("throttle", parser.nextDouble());
/* 222 */     position.set("engineLoad", parser.nextDouble());
/* 223 */     position.set("coolantTemp", parser.nextInt());
/* 224 */     position.set("fuel", parser.nextDouble());
/* 225 */     position.set("fuelConsumption", parser.nextDouble());
/* 226 */     position.set("hours", parser.nextInt());
/*     */     
/* 228 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EasyTrackProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */