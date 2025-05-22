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
/*     */ public class VtfmsProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*  32 */   private static final String[] DIRECTIONS = new String[] { "N", "NE", "E", "SE", "S", "SW", "W", "NW" };
/*     */   
/*     */   public VtfmsProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*  38 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  39 */     .text("(")
/*  40 */     .number("(d{15}),")
/*  41 */     .number("[0-9A-Z]{3}dd,")
/*  42 */     .number("(dd),")
/*  43 */     .number("[^,]*,")
/*  44 */     .number("(d+)?,")
/*  45 */     .number("(?:d+)?,")
/*  46 */     .number("(d+)?,")
/*  47 */     .number("[^,]*,")
/*  48 */     .expression("([AV]),")
/*  49 */     .number("(dd)(dd)(dd),")
/*  50 */     .number("(dd)(dd)(dd),")
/*  51 */     .number("(-?d+.d+),")
/*  52 */     .number("(-?d+.d+),")
/*  53 */     .number("(?:(d+)|([NESW]{1,2})),")
/*  54 */     .number("(d+),")
/*  55 */     .number("(d+),")
/*  56 */     .number("(d+),")
/*  57 */     .expression("[KNT],")
/*  58 */     .number("(d+),")
/*  59 */     .expression("([01]),")
/*  60 */     .number("(d+.d+),")
/*  61 */     .number("[^,]*,")
/*  62 */     .number("(d+)?,")
/*  63 */     .number("(d+.d+)?,")
/*  64 */     .number("[^,]*,")
/*  65 */     .number("(d+.d+)?,")
/*  66 */     .expression("([01]),")
/*  67 */     .expression("([01]),")
/*  68 */     .expression("([01]),")
/*  69 */     .expression("([01]),")
/*  70 */     .expression("([01]),")
/*  71 */     .expression("([01]),")
/*  72 */     .expression("([01]),")
/*  73 */     .number("[^,]*,")
/*  74 */     .number("[^,]*")
/*  75 */     .text(")")
/*  76 */     .number("ddd")
/*  77 */     .compile();
/*     */   
/*     */   private String decodeAlarm(int value) {
/*  80 */     switch (value) {
/*     */       case 10:
/*  82 */         return "overspeed";
/*     */       case 14:
/*  84 */         return "powerCut";
/*     */       case 15:
/*  86 */         return "powerRestored";
/*     */       case 32:
/*  88 */         return "hardBraking";
/*     */       case 33:
/*  90 */         return "hardAcceleration";
/*     */     } 
/*  92 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private double convertToDegrees(double value) {
/*  97 */     double degrees = Math.floor(value / 100.0D);
/*  98 */     return degrees + (value - degrees * 100.0D) / 60.0D;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 105 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 106 */     if (!parser.matches()) {
/* 107 */       return null;
/*     */     }
/*     */     
/* 110 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 111 */     if (deviceSession == null) {
/* 112 */       return null;
/*     */     }
/*     */     
/* 115 */     Position position = new Position(getProtocolName());
/* 116 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 118 */     position.set("alarm", decodeAlarm(parser.nextInt().intValue()));
/* 119 */     position.set("rssi", parser.nextInt());
/* 120 */     position.set("sat", parser.nextInt());
/*     */     
/* 122 */     position.setValid(parser.next().equals("A"));
/* 123 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));
/*     */     
/* 125 */     double latitude = parser.nextDouble().doubleValue();
/* 126 */     double longitude = parser.nextDouble().doubleValue();
/* 127 */     if (Math.abs(latitude) > 90.0D || Math.abs(longitude) > 180.0D) {
/* 128 */       position.setLatitude(convertToDegrees(latitude));
/* 129 */       position.setLongitude(convertToDegrees(longitude));
/*     */     } else {
/* 131 */       position.setLatitude(latitude);
/* 132 */       position.setLongitude(longitude);
/*     */     } 
/*     */     
/* 135 */     position.setCourse(parser.nextDouble(0.0D));
/* 136 */     if (parser.hasNext()) {
/* 137 */       String direction = parser.next();
/* 138 */       for (int i = 0; i < DIRECTIONS.length; i++) {
/* 139 */         if (direction.equals(DIRECTIONS[i])) {
/* 140 */           position.setCourse(i * 45.0D);
/*     */           
/*     */           break;
/*     */         } 
/*     */       } 
/*     */     } 
/* 146 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/*     */     
/* 148 */     position.set("hours", Long.valueOf(UnitsConverter.msFromHours(parser.nextInt().intValue())));
/* 149 */     position.set("idleHours", parser.nextInt());
/* 150 */     position.set("odometer", Integer.valueOf(parser.nextInt().intValue() * 100));
/* 151 */     position.set("charge", Boolean.valueOf(parser.next().equals("1")));
/* 152 */     position.set("power", parser.nextDouble());
/* 153 */     position.set("fuel", parser.nextInt());
/* 154 */     position.set("adc1", parser.nextDouble());
/* 155 */     position.set("adc2", parser.nextDouble());
/* 156 */     position.set("in1", parser.nextInt());
/* 157 */     position.set("in2", parser.nextInt());
/* 158 */     position.set("in3", parser.nextInt());
/* 159 */     position.set("in4", parser.nextInt());
/* 160 */     position.set("out1", parser.nextInt());
/* 161 */     position.set("out2", parser.nextInt());
/* 162 */     position.set("out3", parser.nextInt());
/*     */     
/* 164 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\VtfmsProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */