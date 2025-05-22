/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
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
/*     */ public class XirgoProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private Boolean newFormat;
/*     */   private String form;
/*     */   
/*     */   public XirgoProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
/*  39 */     this.form = Context.getConfig().getString(getProtocolName() + ".form");
/*     */   }
/*     */   
/*     */   public void setForm(String form) {
/*  43 */     this.form = form;
/*     */   }
/*     */   
/*  46 */   private static final Pattern PATTERN_OLD = (new PatternBuilder())
/*  47 */     .text("$$")
/*  48 */     .number("(d+),")
/*  49 */     .number("(d+),")
/*  50 */     .number("(dddd)/(dd)/(dd),")
/*  51 */     .number("(dd):(dd):(dd),")
/*  52 */     .number("(-?d+.?d*),")
/*  53 */     .number("(-?d+.?d*),")
/*  54 */     .number("(-?d+.?d*),")
/*  55 */     .number("(d+.?d*),")
/*  56 */     .number("(d+.?d*),")
/*  57 */     .number("(d+),")
/*  58 */     .number("(d+.?d*),")
/*  59 */     .number("(d+.d+),")
/*  60 */     .number("(d+),")
/*  61 */     .number("(d+.?d*),")
/*  62 */     .number("(d+),")
/*  63 */     .any()
/*  64 */     .compile();
/*     */   
/*  66 */   private static final Pattern PATTERN_NEW = (new PatternBuilder())
/*  67 */     .text("$$")
/*  68 */     .number("(d+),")
/*  69 */     .number("(d+),")
/*  70 */     .number("(dddd)/(dd)/(dd),")
/*  71 */     .number("(dd):(dd):(dd),")
/*  72 */     .number("(-?d+.?d*),")
/*  73 */     .number("(-?d+.?d*),")
/*  74 */     .number("(-?d+.?d*),")
/*  75 */     .number("(d+.?d*),")
/*  76 */     .number("d+.?d*,")
/*  77 */     .number("d+.?d*,")
/*  78 */     .number("d+,")
/*  79 */     .number("(d+.?d*),")
/*  80 */     .number("(d+),")
/*  81 */     .number("(d+.?d*),")
/*  82 */     .number("(d+.?d*),")
/*  83 */     .number("(d+.?d*),")
/*  84 */     .number("(d+.d+),")
/*  85 */     .number("(d+),")
/*  86 */     .number("(d+),")
/*  87 */     .groupBegin()
/*  88 */     .number("d,")
/*  89 */     .expression("([01])")
/*  90 */     .expression("([01])")
/*  91 */     .expression("([01])")
/*  92 */     .expression("([01]),")
/*  93 */     .number("(d+.?d*),")
/*  94 */     .number("(d+.?d*),")
/*  95 */     .number("d+,")
/*  96 */     .number("(d+),")
/*  97 */     .number("(d+),")
/*  98 */     .number("(d+),")
/*  99 */     .number("(-?d+),")
/* 100 */     .number("(d+),")
/* 101 */     .number("(d+),")
/* 102 */     .number("(-?d+)")
/* 103 */     .groupEnd("?")
/* 104 */     .any()
/* 105 */     .compile();
/*     */ 
/*     */   
/*     */   private void decodeEvent(Position position, int event) {
/* 109 */     position.set("event", Integer.valueOf(event));
/*     */     
/* 111 */     switch (event) {
/*     */       case 4001:
/*     */       case 4003:
/*     */       case 6011:
/*     */       case 6013:
/* 116 */         position.set("ignition", Boolean.valueOf(true));
/*     */         break;
/*     */       case 4002:
/*     */       case 4004:
/*     */       case 6012:
/*     */       case 6014:
/* 122 */         position.set("ignition", Boolean.valueOf(false));
/*     */         break;
/*     */       case 4005:
/* 125 */         position.set("charge", Boolean.valueOf(false));
/*     */         break;
/*     */       case 6001:
/* 128 */         position.set("alarm", "directionChange");
/*     */         break;
/*     */       case 6002:
/* 131 */         position.set("alarm", "overspeed");
/*     */         break;
/*     */       case 6005:
/* 134 */         position.set("alarm", "mileage");
/*     */         break;
/*     */       case 6006:
/* 137 */         position.set("alarm", "hardAcceleration");
/*     */         break;
/*     */       case 6007:
/* 140 */         position.set("alarm", "hardBraking");
/*     */         break;
/*     */       case 6008:
/* 143 */         position.set("alarm", "lowPower");
/*     */         break;
/*     */       case 6009:
/* 146 */         position.set("alarm", "powerCut");
/*     */         break;
/*     */       case 6010:
/* 149 */         position.set("alarm", "powerRestored");
/*     */         break;
/*     */       case 6016:
/* 152 */         position.set("alarm", "idle");
/*     */         break;
/*     */       case 6017:
/* 155 */         position.set("alarm", "tow");
/*     */         break;
/*     */       case 6018:
/* 158 */         position.set("alarm", "towStop");
/*     */         break;
/*     */       case 6019:
/* 161 */         position.set("in2", Boolean.valueOf(true));
/*     */         break;
/*     */       case 6020:
/* 164 */         position.set("in2", Boolean.valueOf(false));
/*     */         break;
/*     */       case 6030:
/*     */       case 6071:
/* 168 */         position.set("motion", Boolean.valueOf(true));
/*     */         break;
/*     */       case 6031:
/* 171 */         position.set("motion", Boolean.valueOf(false));
/*     */         break;
/*     */       case 6032:
/* 174 */         position.set("alarm", "parking");
/*     */         break;
/*     */       case 6073:
/* 177 */         position.set("alarm", "periodicAlarm");
/*     */         break;
/*     */       case 6076:
/* 180 */         position.set("alarm", "voltage");
/*     */         break;
/*     */       case 6079:
/* 183 */         position.set("in1", Boolean.valueOf(true));
/*     */         break;
/*     */       case 6090:
/* 186 */         position.set("alarm", "removing");
/*     */         break;
/*     */       case 6091:
/* 189 */         position.set("alarm", "lowBattery");
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
/* 200 */     String sentence = (String)msg;
/* 201 */     if (this.form != null) {
/* 202 */       return decodeCustom(channel, remoteAddress, sentence);
/*     */     }
/* 204 */     return decodeFixed(channel, remoteAddress, sentence);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeCustom(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 212 */     String[] keys = this.form.split(",");
/* 213 */     String[] values = sentence.replace("$$", "").replace("##", "").split(",");
/*     */     
/* 215 */     Position position = new Position(getProtocolName());
/* 216 */     DateBuilder dateBuilder = new DateBuilder();
/*     */     
/* 218 */     for (int i = 0; i < Math.min(keys.length, values.length); i++) {
/* 219 */       DeviceSession deviceSession; String[] date; String[] time; int ignition; switch (keys[i]) {
/*     */         case "UID":
/*     */         case "IM":
/* 222 */           deviceSession = getDeviceSession(channel, remoteAddress, new String[] { values[i] });
/* 223 */           if (deviceSession != null) {
/* 224 */             position.setDeviceId(deviceSession.getDeviceId());
/*     */           }
/*     */           break;
/*     */         case "EV":
/* 228 */           decodeEvent(position, Integer.parseInt(values[i]));
/*     */           break;
/*     */         case "D":
/* 231 */           date = values[i].split("/");
/* 232 */           dateBuilder.setMonth(Integer.parseInt(date[0]));
/* 233 */           dateBuilder.setDay(Integer.parseInt(date[1]));
/* 234 */           dateBuilder.setYear(Integer.parseInt(date[2]));
/*     */           break;
/*     */         case "T":
/* 237 */           time = values[i].split(":");
/* 238 */           dateBuilder.setHour(Integer.parseInt(time[0]));
/* 239 */           dateBuilder.setMinute(Integer.parseInt(time[1]));
/* 240 */           dateBuilder.setSecond(Integer.parseInt(time[2]));
/*     */           break;
/*     */         case "LT":
/* 243 */           position.setLatitude(Double.parseDouble(values[i]));
/*     */           break;
/*     */         case "LN":
/* 246 */           position.setLongitude(Double.parseDouble(values[i]));
/*     */           break;
/*     */         case "AL":
/* 249 */           position.setAltitude(Integer.parseInt(values[i]));
/*     */           break;
/*     */         case "GSPT":
/* 252 */           position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[i])));
/*     */           break;
/*     */         case "HD":
/* 255 */           if (values[i].contains(".")) {
/* 256 */             position.setCourse(Double.parseDouble(values[i])); break;
/*     */           } 
/* 258 */           position.setCourse(Integer.parseInt(values[i]) * 0.1D);
/*     */           break;
/*     */         
/*     */         case "SV":
/* 262 */           position.set("sat", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "BV":
/* 265 */           position.set("battery", Double.valueOf(Double.parseDouble(values[i])));
/*     */           break;
/*     */         case "CQ":
/* 268 */           position.set("rssi", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "MI":
/* 271 */           position.set("odometer", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */         case "GS":
/* 274 */           position.setValid((Integer.parseInt(values[i]) == 3));
/*     */           break;
/*     */         case "SI":
/* 277 */           position.set("iccid", values[i]);
/*     */           break;
/*     */         case "IG":
/* 280 */           ignition = Integer.parseInt(values[i]);
/* 281 */           if (ignition > 0) {
/* 282 */             position.set("ignition", Boolean.valueOf((ignition == 1)));
/*     */           }
/*     */           break;
/*     */         case "OT":
/* 286 */           position.set("output", Integer.valueOf(Integer.parseInt(values[i])));
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */     
/*     */     } 
/* 293 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 295 */     return (position.getDeviceId() > 0L) ? position : null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeFixed(Channel channel, SocketAddress remoteAddress, String sentence) {
/*     */     Parser parser;
/* 302 */     if (this.newFormat == null) {
/* 303 */       parser = new Parser(PATTERN_NEW, sentence);
/* 304 */       if (parser.matches()) {
/* 305 */         this.newFormat = Boolean.valueOf(true);
/*     */       } else {
/* 307 */         parser = new Parser(PATTERN_OLD, sentence);
/* 308 */         if (parser.matches()) {
/* 309 */           this.newFormat = Boolean.valueOf(false);
/*     */         } else {
/* 311 */           return null;
/*     */         } 
/*     */       } 
/*     */     } else {
/* 315 */       if (this.newFormat.booleanValue()) {
/* 316 */         parser = new Parser(PATTERN_NEW, sentence);
/*     */       } else {
/* 318 */         parser = new Parser(PATTERN_OLD, sentence);
/*     */       } 
/* 320 */       if (!parser.matches()) {
/* 321 */         return null;
/*     */       }
/*     */     } 
/*     */     
/* 325 */     Position position = new Position(getProtocolName());
/*     */     
/* 327 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 328 */     if (deviceSession == null) {
/* 329 */       return null;
/*     */     }
/* 331 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 333 */     decodeEvent(position, parser.nextInt().intValue());
/*     */     
/* 335 */     position.setTime(parser.nextDateTime());
/*     */     
/* 337 */     position.setLatitude(parser.nextDouble(0.0D));
/* 338 */     position.setLongitude(parser.nextDouble(0.0D));
/* 339 */     position.setAltitude(parser.nextDouble(0.0D));
/* 340 */     position.setSpeed(UnitsConverter.knotsFromMph(parser.nextDouble(0.0D)));
/* 341 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 343 */     position.set("sat", parser.nextInt());
/* 344 */     position.set("hdop", parser.nextDouble());
/*     */     
/* 346 */     if (this.newFormat.booleanValue()) {
/* 347 */       position.set("odometer", Double.valueOf(UnitsConverter.metersFromMiles(parser.nextDouble(0.0D))));
/* 348 */       position.set("fuelConsumption", parser.next());
/*     */     } 
/*     */     
/* 351 */     position.set("battery", Double.valueOf(parser.nextDouble(0.0D)));
/* 352 */     position.set("rssi", parser.nextDouble());
/*     */     
/* 354 */     if (!this.newFormat.booleanValue()) {
/* 355 */       position.set("odometer", Double.valueOf(UnitsConverter.metersFromMiles(parser.nextDouble(0.0D))));
/*     */     }
/*     */     
/* 358 */     position.setValid((parser.nextInt(0) == 1));
/*     */     
/* 360 */     if (this.newFormat.booleanValue() && parser.hasNext(13)) {
/* 361 */       position.set("in1", parser.nextInt());
/* 362 */       position.set("in2", parser.nextInt());
/* 363 */       position.set("in3", parser.nextInt());
/* 364 */       position.set("out1", parser.nextInt());
/* 365 */       position.set("adc1", parser.nextDouble());
/* 366 */       position.set("fuel", parser.nextDouble());
/* 367 */       position.set("hours", Long.valueOf(UnitsConverter.msFromHours(parser.nextInt().intValue())));
/* 368 */       position.set("oilPressure", parser.nextInt());
/* 369 */       position.set("oilLevel", parser.nextInt());
/* 370 */       position.set("oilTemp", parser.nextInt());
/* 371 */       position.set("coolantPressure", parser.nextInt());
/* 372 */       position.set("coolantLevel", parser.nextInt());
/* 373 */       position.set("coolantTemp", parser.nextInt());
/*     */     } 
/*     */     
/* 376 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\XirgoProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */