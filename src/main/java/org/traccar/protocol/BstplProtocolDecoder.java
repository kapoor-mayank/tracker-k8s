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
/*     */ public class BstplProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public BstplProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .text("BSTPL$")
/*  38 */     .number("(d),")
/*  39 */     .expression("([^,]+),")
/*  40 */     .expression("([AV]),")
/*  41 */     .number("(dd)(dd)(dd),")
/*  42 */     .number("(dd)(dd)(dd),")
/*  43 */     .number("(d+.d+),([0NS]),")
/*  44 */     .number("(d+.d+),([0EW]),")
/*  45 */     .number("(d+),")
/*  46 */     .number("(d+),")
/*  47 */     .number("(d+),")
/*  48 */     .number("(d+),")
/*  49 */     .number("([01]),")
/*  50 */     .number("(d+),")
/*  51 */     .number("([01]),")
/*  52 */     .number("([01]),")
/*  53 */     .number("([01]),")
/*  54 */     .number("([01]),")
/*  55 */     .number("(d+.d+),")
/*  56 */     .number("d+,")
/*  57 */     .number("(d+.d+),")
/*  58 */     .expression("([^,]+),")
/*  59 */     .number("([^,]+),")
/*  60 */     .number("(d+.d+)")
/*  61 */     .compile();
/*     */   
/*     */   private String decodeAlarm(int value) {
/*  64 */     switch (value) {
/*     */       case 4:
/*  66 */         return "lowBattery";
/*     */       case 5:
/*  68 */         return "hardAcceleration";
/*     */       case 6:
/*  70 */         return "hardBraking";
/*     */       case 7:
/*  72 */         return "overspeed";
/*     */       case 9:
/*  74 */         return "sos";
/*     */     } 
/*  76 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  83 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  84 */     if (!parser.matches()) {
/*  85 */       return null;
/*     */     }
/*     */     
/*  88 */     int type = parser.nextInt().intValue();
/*     */     
/*  90 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  91 */     if (deviceSession == null) {
/*  92 */       return null;
/*     */     }
/*     */     
/*  95 */     Position position = new Position(getProtocolName());
/*  96 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  98 */     position.set("alarm", decodeAlarm(type));
/*     */     
/* 100 */     position.setValid(parser.next().equals("A"));
/* 101 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/* 102 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 103 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 104 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/*     */     
/* 106 */     position.set("odometer", Long.valueOf(parser.nextInt().intValue() * 1000L));
/*     */     
/* 108 */     position.setCourse(parser.nextInt().intValue());
/*     */     
/* 110 */     position.set("sat", parser.nextInt());
/*     */     
/* 112 */     boolean boxOpen = (parser.nextInt().intValue() > 0);
/* 113 */     if (type == 8 && boxOpen) {
/* 114 */       position.set("alarm", "tampering");
/*     */     }
/* 116 */     position.set("boxOpen", Boolean.valueOf(boxOpen));
/*     */     
/* 118 */     position.set("rssi", parser.nextInt());
/*     */     
/* 120 */     boolean charge = (parser.nextInt().intValue() > 0);
/* 121 */     if (type == 3) {
/* 122 */       position.set("alarm", charge ? "powerRestored" : "powerCut");
/*     */     }
/* 124 */     position.set("charge", Boolean.valueOf(charge));
/*     */     
/* 126 */     position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/* 127 */     position.set("engine", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/* 128 */     position.set("blocked", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/* 129 */     position.set("adc1", parser.nextDouble());
/* 130 */     position.set("battery", parser.nextDouble());
/* 131 */     position.set("iccid", parser.next());
/* 132 */     position.set("power", parser.nextDouble());
/*     */     
/* 134 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\BstplProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */