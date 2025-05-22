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
/*     */ public class MiniFinderProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public MiniFinderProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN_FIX = (new PatternBuilder())
/*  38 */     .number("(d+)/(d+)/(d+),")
/*  39 */     .number("(d+):(d+):(d+),")
/*  40 */     .number("(-?d+.d+),")
/*  41 */     .number("(-?d+.d+),")
/*  42 */     .compile();
/*     */   
/*  44 */   private static final Pattern PATTERN_STATE = (new PatternBuilder())
/*  45 */     .number("(d+.?d*),")
/*  46 */     .number("(d+.?d*),")
/*  47 */     .number("(x+),")
/*  48 */     .number("(-?d+.d+),")
/*  49 */     .number("(d+),")
/*  50 */     .compile();
/*     */   
/*  52 */   private static final Pattern PATTERN_A = (new PatternBuilder())
/*  53 */     .text("!A,")
/*  54 */     .expression(PATTERN_FIX.pattern())
/*  55 */     .any()
/*  56 */     .compile();
/*     */   
/*  58 */   private static final Pattern PATTERN_C = (new PatternBuilder())
/*  59 */     .text("!C,")
/*  60 */     .expression(PATTERN_FIX.pattern())
/*  61 */     .expression(PATTERN_STATE.pattern())
/*  62 */     .any()
/*  63 */     .compile();
/*     */   
/*  65 */   private static final Pattern PATTERN_BD = (new PatternBuilder())
/*  66 */     .expression("![BD],")
/*  67 */     .expression(PATTERN_FIX.pattern())
/*  68 */     .expression(PATTERN_STATE.pattern())
/*  69 */     .number("(d+),")
/*  70 */     .number("(d+),")
/*  71 */     .number("(d+.?d*)")
/*  72 */     .compile();
/*     */ 
/*     */   
/*     */   private void decodeFix(Position position, Parser parser) {
/*  76 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*  77 */     position.setLatitude(parser.nextDouble(0.0D));
/*  78 */     position.setLongitude(parser.nextDouble(0.0D));
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeFlags(Position position, int flags) {
/*  83 */     position.setValid((BitUtil.to(flags, 2) > 0));
/*  84 */     if (BitUtil.check(flags, 1)) {
/*  85 */       position.set("approximate", Boolean.valueOf(true));
/*     */     }
/*     */     
/*  88 */     position.set("gps_status", Boolean.valueOf(position.getValid()));
/*     */     
/*  90 */     if (BitUtil.check(flags, 2)) {
/*  91 */       position.set("alarm", "fault");
/*     */     }
/*  93 */     if (BitUtil.check(flags, 6)) {
/*  94 */       position.set("alarm", "sos");
/*     */     }
/*  96 */     if (BitUtil.check(flags, 7)) {
/*  97 */       position.set("alarm", "overspeed");
/*     */     }
/*  99 */     if (BitUtil.check(flags, 8)) {
/* 100 */       position.set("alarm", "fallDown");
/*     */     }
/* 102 */     if (BitUtil.check(flags, 9) || BitUtil.check(flags, 10) || BitUtil.check(flags, 11)) {
/* 103 */       position.set("alarm", "geofence");
/*     */     }
/* 105 */     if (BitUtil.check(flags, 12)) {
/* 106 */       position.set("alarm", "lowBattery");
/*     */     }
/* 108 */     if (BitUtil.check(flags, 15) || BitUtil.check(flags, 14)) {
/* 109 */       position.set("alarm", "movement");
/*     */     }
/*     */     
/* 112 */     position.set("rssi", Integer.valueOf(BitUtil.between(flags, 16, 21)));
/* 113 */     position.set("charge", Boolean.valueOf(BitUtil.check(flags, 22)));
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeState(Position position, Parser parser) {
/* 118 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/*     */     
/* 120 */     position.setCourse(parser.nextDouble(0.0D));
/* 121 */     if (position.getCourse() > 360.0D) {
/* 122 */       position.setCourse(0.0D);
/*     */     }
/*     */     
/* 125 */     decodeFlags(position, parser.nextHexInt(0));
/*     */     
/* 127 */     position.setAltitude(parser.nextDouble(0.0D));
/*     */     
/* 129 */     position.set("batteryLevel", Integer.valueOf(parser.nextInt(0)));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 136 */     String sentence = (String)msg;
/*     */     
/* 138 */     if (sentence.startsWith("!1,")) {
/* 139 */       int index = sentence.indexOf(',', 3);
/* 140 */       if (index < 0) {
/* 141 */         index = sentence.length();
/*     */       }
/* 143 */       getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(3, index) });
/* 144 */       return null;
/*     */     } 
/*     */     
/* 147 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 148 */     if (deviceSession == null || !sentence.matches("![3A-D],.*")) {
/* 149 */       return null;
/*     */     }
/*     */     
/* 152 */     Position position = new Position(getProtocolName());
/* 153 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 155 */     String type = sentence.substring(1, 2);
/* 156 */     position.set("type", type);
/*     */     
/* 158 */     if (type.equals("3")) {
/*     */       
/* 160 */       getLastLocation(position, null);
/*     */       
/* 162 */       position.set("result", sentence.substring(3));
/*     */       
/* 164 */       return position;
/*     */     } 
/* 166 */     if (type.equals("B") || type.equals("D")) {
/*     */       
/* 168 */       Parser parser = new Parser(PATTERN_BD, sentence);
/* 169 */       if (!parser.matches()) {
/* 170 */         return null;
/*     */       }
/*     */       
/* 173 */       decodeFix(position, parser);
/* 174 */       decodeState(position, parser);
/*     */       
/* 176 */       position.set("sat", Integer.valueOf(parser.nextInt(0)));
/* 177 */       position.set("satVisible", Integer.valueOf(parser.nextInt(0)));
/* 178 */       position.set("hdop", Double.valueOf(parser.nextDouble(0.0D)));
/*     */       
/* 180 */       return position;
/*     */     } 
/* 182 */     if (type.equals("C")) {
/*     */       
/* 184 */       Parser parser = new Parser(PATTERN_C, sentence);
/* 185 */       if (!parser.matches()) {
/* 186 */         return null;
/*     */       }
/*     */       
/* 189 */       decodeFix(position, parser);
/* 190 */       decodeState(position, parser);
/*     */       
/* 192 */       return position;
/*     */     } 
/* 194 */     if (type.equals("A")) {
/*     */       
/* 196 */       Parser parser = new Parser(PATTERN_A, sentence);
/* 197 */       if (!parser.matches()) {
/* 198 */         return null;
/*     */       }
/*     */       
/* 201 */       decodeFix(position, parser);
/*     */       
/* 203 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 207 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MiniFinderProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */