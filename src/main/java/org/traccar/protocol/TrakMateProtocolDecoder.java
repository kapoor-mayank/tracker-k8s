/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
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
/*     */ public class TrakMateProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TrakMateProtocolDecoder(Protocol protocol) {
/*  32 */     super(protocol);
/*     */   }
/*     */   
/*  35 */   private static final Pattern PATTERN_SRT = (new PatternBuilder())
/*  36 */     .text("^TMSRT|")
/*  37 */     .expression("([^ ]+)|")
/*  38 */     .number("(d+.d+)|")
/*  39 */     .number("(d+.d+)|")
/*  40 */     .number("(dd)(dd)(dd)|")
/*  41 */     .number("(dd)(dd)(dd)|")
/*  42 */     .number("(d+.d+)|")
/*  43 */     .number("(d+.d+)|")
/*  44 */     .any()
/*  45 */     .compile();
/*     */   
/*  47 */   private static final Pattern PATTERN_PER = (new PatternBuilder())
/*  48 */     .text("^TM")
/*  49 */     .expression("...|")
/*  50 */     .expression("([^ ]+)|")
/*  51 */     .number("(d+)|")
/*  52 */     .number("(d+.d+)|")
/*  53 */     .number("(d+.d+)|")
/*  54 */     .number("(dd)(dd)(dd)|")
/*  55 */     .number("(dd)(dd)(dd)|")
/*  56 */     .number("(d+.d+)|")
/*  57 */     .number("(d+.d+)|")
/*  58 */     .number("(d+)|").optional()
/*  59 */     .number("([01])|")
/*  60 */     .groupBegin()
/*  61 */     .number("(d+)|")
/*  62 */     .number("(d+)|")
/*  63 */     .number("(d+.d+)|")
/*  64 */     .number("(d+.d+)|")
/*  65 */     .or()
/*  66 */     .number("-?d+ -?d+ -?d+|")
/*  67 */     .number("([01])|")
/*  68 */     .groupEnd()
/*  69 */     .number("(d+.d+)|")
/*  70 */     .number("(d+.d+)|")
/*  71 */     .number("(d+.d+)|").optional()
/*  72 */     .number("([01])|")
/*  73 */     .number("([01])|")
/*  74 */     .number("([01])|")
/*  75 */     .any()
/*  76 */     .compile();
/*     */   
/*  78 */   private static final Pattern PATTERN_ALT = (new PatternBuilder())
/*  79 */     .text("^TMALT|")
/*  80 */     .expression("([^ ]+)|")
/*  81 */     .number("(d+)|")
/*  82 */     .number("(d+)|")
/*  83 */     .number("(d+)|")
/*  84 */     .number("(d+.d+)|")
/*  85 */     .number("(d+.d+)|")
/*  86 */     .number("(dd)(dd)(dd)|")
/*  87 */     .number("(dd)(dd)(dd)|")
/*  88 */     .number("(d+.d+)|")
/*  89 */     .number("(d+.d+)|")
/*  90 */     .any()
/*  91 */     .compile();
/*     */   
/*     */   private String decodeAlarm(int value) {
/*  94 */     switch (value) {
/*     */       case 1:
/*  96 */         return "sos";
/*     */       case 3:
/*  98 */         return "geofence";
/*     */       case 4:
/* 100 */         return "powerCut";
/*     */     } 
/* 102 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeSrt(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 108 */     Parser parser = new Parser(PATTERN_SRT, sentence);
/* 109 */     if (!parser.matches()) {
/* 110 */       return null;
/*     */     }
/*     */     
/* 113 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 114 */     if (deviceSession == null) {
/* 115 */       return null;
/*     */     }
/*     */     
/* 118 */     Position position = new Position(getProtocolName());
/* 119 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 121 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 122 */     position.setLongitude(parser.nextDouble().doubleValue());
/*     */     
/* 124 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));
/*     */     
/* 126 */     position.set("versionFw", parser.next());
/* 127 */     position.set("versionHw", parser.next());
/*     */     
/* 129 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Object decodeAlt(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 134 */     Parser parser = new Parser(PATTERN_ALT, sentence);
/* 135 */     if (!parser.matches()) {
/* 136 */       return null;
/*     */     }
/*     */     
/* 139 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 140 */     if (deviceSession == null) {
/* 141 */       return null;
/*     */     }
/*     */     
/* 144 */     Position position = new Position(getProtocolName());
/* 145 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 147 */     parser.next();
/* 148 */     position.set("alarm", decodeAlarm(parser.nextInt().intValue()));
/* 149 */     parser.next();
/*     */     
/* 151 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 152 */     position.setLongitude(parser.nextDouble().doubleValue());
/*     */     
/* 154 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));
/*     */     
/* 156 */     position.setSpeed(parser.nextDouble().doubleValue());
/* 157 */     position.setCourse(parser.nextDouble().doubleValue());
/*     */     
/* 159 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Object decodePer(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 164 */     Parser parser = new Parser(PATTERN_PER, (String)msg);
/* 165 */     if (!parser.matches()) {
/* 166 */       return null;
/*     */     }
/*     */     
/* 169 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 170 */     if (deviceSession == null) {
/* 171 */       return null;
/*     */     }
/*     */     
/* 174 */     Position position = new Position(getProtocolName());
/* 175 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 177 */     parser.next();
/*     */     
/* 179 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 180 */     position.setLongitude(parser.nextDouble().doubleValue());
/*     */     
/* 182 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));
/*     */     
/* 184 */     position.setSpeed(parser.nextDouble().doubleValue());
/* 185 */     position.setCourse(parser.nextDouble().doubleValue());
/*     */     
/* 187 */     position.set("sat", parser.nextInt());
/* 188 */     position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/*     */     
/* 190 */     if (parser.hasNext(4)) {
/* 191 */       position.set("dop1", parser.nextInt());
/* 192 */       position.set("dop2", parser.nextInt());
/* 193 */       position.set("adc1", parser.nextDouble());
/* 194 */       position.set("battery", parser.nextDouble());
/*     */     } 
/*     */     
/* 197 */     if (parser.hasNext()) {
/* 198 */       position.set("motion", Boolean.valueOf((parser.nextInt(0) > 0)));
/*     */     }
/*     */     
/* 201 */     position.set("power", parser.nextDouble());
/* 202 */     position.set("odometer", parser.nextDouble());
/* 203 */     position.set("pulseOdometer", parser.nextDouble());
/* 204 */     position.set("status", parser.nextInt());
/*     */     
/* 206 */     position.setValid((parser.nextInt().intValue() > 0));
/*     */     
/* 208 */     position.set("archive", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/*     */     
/* 210 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 216 */     String sentence = (String)msg;
/* 217 */     int typeIndex = sentence.indexOf("^TM");
/* 218 */     if (typeIndex < 0) {
/* 219 */       return null;
/*     */     }
/*     */     
/* 222 */     String type = sentence.substring(typeIndex + 3, typeIndex + 6);
/* 223 */     switch (type) {
/*     */       case "ALT":
/* 225 */         return decodeAlt(channel, remoteAddress, sentence);
/*     */       case "SRT":
/* 227 */         return decodeSrt(channel, remoteAddress, sentence);
/*     */     } 
/* 229 */     return decodePer(channel, remoteAddress, sentence);
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TrakMateProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */