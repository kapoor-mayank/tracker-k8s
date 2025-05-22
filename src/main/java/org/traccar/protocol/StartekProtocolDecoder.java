/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
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
/*     */ public class StartekProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public StartekProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */   
/*  39 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  40 */     .text("&&")
/*  41 */     .expression(".")
/*  42 */     .number("d+,")
/*  43 */     .number("(d+),")
/*  44 */     .number("(xxx),")
/*  45 */     .expression("(.+)")
/*  46 */     .number("xx")
/*  47 */     .text("\r\n")
/*  48 */     .compile();
/*     */   
/*  50 */   private static final Pattern PATTERN_POSITION = (new PatternBuilder())
/*  51 */     .number("(d+),")
/*  52 */     .expression("([^,]+)?,")
/*  53 */     .number("(dd)(dd)(dd)")
/*  54 */     .number("(dd)(dd)(dd),")
/*  55 */     .expression("([AV]),")
/*  56 */     .number("(-?d+.d+),")
/*  57 */     .number("(-?d+.d+),")
/*  58 */     .number("(d+),")
/*  59 */     .number("(d+.d+),")
/*  60 */     .number("(d+),")
/*  61 */     .number("(d+),")
/*  62 */     .number("(-?d+),")
/*  63 */     .number("(d+),")
/*  64 */     .number("(d+)|")
/*  65 */     .number("(d+)|")
/*  66 */     .number("(x+)|")
/*  67 */     .number("(x+),")
/*  68 */     .number("(d+),")
/*  69 */     .number("(x+),")
/*  70 */     .number("(x+),")
/*  71 */     .number("(x+),")
/*  72 */     .number("(x+)|")
/*  73 */     .number("(x+)")
/*  74 */     .expression("([^,]+)?")
/*  75 */     .groupBegin()
/*  76 */     .number(",d+")
/*  77 */     .expression(",([^,]+)?")
/*  78 */     .groupBegin()
/*  79 */     .expression(",([^,]+)?")
/*  80 */     .groupBegin()
/*  81 */     .text(",")
/*  82 */     .groupBegin()
/*  83 */     .number("(d+)?|")
/*  84 */     .number("(d+)?|")
/*  85 */     .number("(d+)?|")
/*  86 */     .number("(d+)?|")
/*  87 */     .number("(d+)?|")
/*  88 */     .number("(d+)?|")
/*  89 */     .number("(d+)?|")
/*  90 */     .number("(d+)?|")
/*  91 */     .number("(d+)[%L]").optional()
/*  92 */     .groupEnd("?")
/*  93 */     .number(",(d+)").optional()
/*  94 */     .groupEnd("?")
/*  95 */     .groupEnd("?")
/*  96 */     .groupEnd("?")
/*  97 */     .any()
/*  98 */     .compile();
/*     */   
/*     */   private String decodeAlarm(int value) {
/* 101 */     switch (value) {
/*     */       case 1:
/* 103 */         return "sos";
/*     */       case 5:
/*     */       case 6:
/* 106 */         return "door";
/*     */       case 39:
/* 108 */         return "hardAcceleration";
/*     */       case 40:
/* 110 */         return "hardBraking";
/*     */       case 41:
/* 112 */         return "hardCornering";
/*     */     } 
/* 114 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 122 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 123 */     if (!parser.matches()) {
/* 124 */       return null;
/*     */     }
/*     */     
/* 127 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 128 */     if (deviceSession == null) {
/* 129 */       return null;
/*     */     }
/*     */     
/* 132 */     String type = parser.next();
/* 133 */     String content = parser.next();
/* 134 */     switch (type) {
/*     */       case "000":
/* 136 */         return decodePosition(deviceSession, content);
/*     */       case "710":
/* 138 */         return decodeSerial(deviceSession, content);
/*     */     } 
/* 140 */     Position position = new Position(getProtocolName());
/* 141 */     position.setDeviceId(deviceSession.getDeviceId());
/* 142 */     getLastLocation(position, null);
/* 143 */     position.set("type", type);
/* 144 */     position.set("result", content);
/* 145 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodePosition(DeviceSession deviceSession, String content) {
/* 151 */     Parser parser = new Parser(PATTERN_POSITION, content);
/* 152 */     if (!parser.matches()) {
/* 153 */       return null;
/*     */     }
/*     */     
/* 156 */     Position position = new Position(getProtocolName());
/* 157 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 159 */     int event = parser.nextInt().intValue();
/* 160 */     String eventData = parser.next();
/* 161 */     position.set("event", Integer.valueOf(event));
/* 162 */     if (event == 53) {
/* 163 */       position.set("driverUniqueId", eventData);
/*     */     } else {
/* 165 */       position.set("alarm", decodeAlarm(event));
/*     */     } 
/*     */     
/* 168 */     position.setTime(parser.nextDateTime());
/* 169 */     position.setValid(parser.next().equals("A"));
/* 170 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 171 */     position.setLongitude(parser.nextDouble().doubleValue());
/*     */     
/* 173 */     position.set("sat", parser.nextInt());
/* 174 */     position.set("hdop", parser.nextDouble());
/*     */     
/* 176 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/* 177 */     position.setCourse(parser.nextInt().intValue());
/* 178 */     position.setAltitude(parser.nextInt().intValue());
/*     */     
/* 180 */     position.set("odometer", parser.nextLong());
/*     */     
/* 182 */     position.setNetwork(new Network(CellTower.from(parser
/* 183 */             .nextInt().intValue(), parser.nextInt().intValue(), parser.nextHexInt().intValue(), parser.nextHexInt().intValue(), parser.nextInt().intValue())));
/*     */     
/* 185 */     position.set("status", parser.nextHexInt());
/*     */     
/* 187 */     int input = parser.nextHexInt().intValue();
/* 188 */     int output = parser.nextHexInt().intValue();
/* 189 */     position.set("ignition", Boolean.valueOf(BitUtil.check(input, 1)));
/* 190 */     position.set("door", Boolean.valueOf(BitUtil.check(input, 2))); int i;
/* 191 */     for (i = 1; i <= 4; i++) {
/* 192 */       position.set("in" + i, Boolean.valueOf(BitUtil.check(input, i - 1)));
/*     */     }
/* 194 */     for (i = 1; i <= 4; i++) {
/* 195 */       position.set("out" + i, Boolean.valueOf(BitUtil.check(output, i - 1)));
/*     */     }
/*     */     
/* 198 */     position.set("power", Double.valueOf(parser.nextHexInt().intValue() * 0.01D));
/* 199 */     position.set("battery", Double.valueOf(parser.nextHexInt().intValue() * 0.01D));
/*     */     
/* 201 */     if (parser.hasNext()) {
/* 202 */       String[] adc = parser.next().split("\\|");
/* 203 */       for (int j = 1; j < adc.length; j++) {
/* 204 */         position.set("adc" + (j + 1), Double.valueOf(Integer.parseInt(adc[j], 16) * 0.01D));
/*     */       }
/*     */     } 
/*     */     
/* 208 */     if (parser.hasNext()) {
/* 209 */       String[] fuels = parser.next().split("\\|");
/* 210 */       for (String fuel : fuels) {
/* 211 */         int index = Integer.parseInt(fuel.substring(0, 2));
/* 212 */         int value = Integer.parseInt(fuel.substring(2), 16);
/* 213 */         position.set("fuel" + index, Double.valueOf(value * 0.1D));
/*     */       } 
/*     */     } 
/*     */     
/* 217 */     if (parser.hasNext()) {
/* 218 */       String[] temperatures = parser.next().split("\\|");
/* 219 */       for (String temperature : temperatures) {
/* 220 */         int index = Integer.parseInt(temperature.substring(0, 2));
/* 221 */         int value = Integer.parseInt(temperature.substring(2), 16);
/* 222 */         double convertedValue = BitUtil.to(value, 15);
/* 223 */         if (BitUtil.check(value, 15)) {
/* 224 */           convertedValue = -convertedValue;
/*     */         }
/* 226 */         position.set("temp" + index, Double.valueOf(convertedValue * 0.1D));
/*     */       } 
/*     */     } 
/*     */     
/* 230 */     if (parser.hasNext(9)) {
/* 231 */       position.set("rpm", parser.nextInt());
/* 232 */       position.set("engineLoad", parser.nextInt());
/* 233 */       position.set("airFlow", parser.nextInt());
/* 234 */       position.set("airPressure", parser.nextInt());
/* 235 */       if (parser.hasNext()) {
/* 236 */         position.set("airTemp", Integer.valueOf(parser.nextInt().intValue() - 40));
/*     */       }
/* 238 */       position.set("throttle", parser.nextInt());
/* 239 */       if (parser.hasNext()) {
/* 240 */         position.set("coolantTemp", Integer.valueOf(parser.nextInt().intValue() - 40));
/*     */       }
/* 242 */       if (parser.hasNext()) {
/* 243 */         position.set("fuelConsumption", Double.valueOf(parser.nextInt().intValue() * 0.1D));
/*     */       }
/* 245 */       position.set("fuel", parser.nextInt());
/*     */     } 
/*     */     
/* 248 */     if (parser.hasNext()) {
/* 249 */       position.set("hours", parser.nextInt());
/*     */     }
/*     */     
/* 252 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Object decodeSerial(DeviceSession deviceSession, String content) {
/* 257 */     Position position = new Position(getProtocolName());
/* 258 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 260 */     getLastLocation(position, null);
/*     */     
/* 262 */     String[] frames = content.split("\r\n");
/*     */     
/* 264 */     for (String frame : frames) {
/* 265 */       int ignition; String[] values = frame.split(",");
/* 266 */       int index = 0;
/* 267 */       String type = values[index++];
/* 268 */       switch (type) {
/*     */         case "T1":
/* 270 */           index++;
/* 271 */           position.set("rpm", Double.valueOf(Double.parseDouble(values[index++])));
/* 272 */           index++;
/* 273 */           position.set("fuel", Double.valueOf(Double.parseDouble(values[index++])));
/* 274 */           index += 4;
/* 275 */           index++;
/* 276 */           position.set("coolantTemp", Integer.valueOf(Integer.parseInt(values[index++])));
/* 277 */           index++;
/* 278 */           position.set("torque", Integer.valueOf(Integer.parseInt(values[index++])));
/* 279 */           index++;
/* 280 */           position.set("power", Double.valueOf(Double.parseDouble(values[index++])));
/* 281 */           index++;
/* 282 */           position.set("oilTemp", Double.valueOf(Double.parseDouble(values[index++])));
/* 283 */           index++;
/* 284 */           position.set("throttle", Double.valueOf(Double.parseDouble(values[index++])));
/* 285 */           index++;
/* 286 */           index++;
/* 287 */           index++;
/* 288 */           index++;
/* 289 */           position.set("oilPressure", Integer.valueOf(Integer.parseInt(values[index++])));
/* 290 */           index++;
/* 291 */           index++;
/* 292 */           ignition = Integer.parseInt(values[index++]);
/* 293 */           if (ignition < 2) {
/* 294 */             position.set("ignition", Boolean.valueOf((ignition > 0)));
/*     */           }
/* 296 */           index++;
/* 297 */           position.set("catalystLevel", Double.valueOf(Double.parseDouble(values[index++])));
/* 298 */           index++;
/*     */           break;
/*     */         case "T2":
/* 301 */           position.set("odometer", Double.valueOf(Double.parseDouble(values[index++]) * 1000.0D));
/* 302 */           index++;
/* 303 */           index++;
/* 304 */           index++;
/* 305 */           index++;
/* 306 */           index++;
/* 307 */           index++;
/* 308 */           index++;
/* 309 */           index++;
/* 310 */           index++;
/* 311 */           index++;
/* 312 */           index++;
/* 313 */           index++;
/* 314 */           index++;
/* 315 */           index++;
/* 316 */           index++;
/* 317 */           position.set("hours", Integer.valueOf(Integer.parseInt(values[index++])));
/* 318 */           index++;
/* 319 */           position.set("fuelConsumption", Double.valueOf(Double.parseDouble(values[index++])));
/* 320 */           index++;
/* 321 */           position.set("fuelUsed", Double.valueOf(Double.parseDouble(values[index++])));
/* 322 */           index++;
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */     
/*     */     } 
/* 329 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\StartekProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */