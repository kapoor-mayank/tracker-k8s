/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ public class EasyTrackPlusProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public EasyTrackPlusProtocolDecoder(Protocol protocol) {
/*  20 */     super(protocol);
/*     */   }
/*     */   
/*  23 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  24 */     .text("*").expression("..,")
/*  25 */     .number("(d+),")
/*  26 */     .expression("([^,]{2}),")
/*  27 */     .expression("([AV]),")
/*  28 */     .number("(xx)(xx)(xx),")
/*  29 */     .number("(xx)(xx)(xx),")
/*  30 */     .number("(x)(x{7}),")
/*  31 */     .number("(x)(x{7}),")
/*  32 */     .number("(x{4}),")
/*  33 */     .number("(x{4}),")
/*  34 */     .number("(x{8}),")
/*  35 */     .number("(x+),")
/*  36 */     .number("(d+),")
/*  37 */     .number("(x+),?")
/*  38 */     .number("(x+),?")
/*  39 */     .number("(x+),?")
/*  40 */     .number("(d+),")
/*  41 */     .number("(d+),")
/*  42 */     .number("(d+),")
/*  43 */     .number("(d+),")
/*  44 */     .number("(d+)")
/*  45 */     .any()
/*  46 */     .compile();
/*     */   
/*     */   private String decodeAlarm(long status) {
/*  49 */     if ((status & 0x2000000L) != 0L) {
/*  50 */       return "geofenceEnter";
/*     */     }
/*  52 */     if ((status & 0x4000000L) != 0L) {
/*  53 */       return "geofenceExit";
/*     */     }
/*  55 */     if ((status & 0x8000000L) != 0L) {
/*  56 */       return "lowBattery";
/*     */     }
/*  58 */     if ((status & 0x20000000L) != 0L) {
/*  59 */       return "vibration";
/*     */     }
/*  61 */     if ((status & 0xFFFFFFFF80000000L) != 0L) {
/*  62 */       return "overspeed";
/*     */     }
/*  64 */     if ((status & 0x10000L) != 0L) {
/*  65 */       return "sos";
/*     */     }
/*  67 */     if ((status & 0x40000L) != 0L) {
/*  68 */       return "powerCut";
/*     */     }
/*  70 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  77 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  78 */     if (!parser.matches()) {
/*  79 */       return null;
/*     */     }
/*     */     
/*  82 */     Position position = new Position(getProtocolName());
/*     */     
/*  84 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  85 */     if (deviceSession == null) {
/*  86 */       return null;
/*     */     }
/*  88 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  90 */     position.set("command", parser.next());
/*     */     
/*  92 */     position.setValid(parser.next().equals("A"));
/*     */ 
/*     */ 
/*     */     
/*  96 */     DateBuilder dateBuilder = (new DateBuilder()).setDate(parser.nextHexInt(0), parser.nextHexInt(0), parser.nextHexInt(0)).setTime(parser.nextHexInt(0), parser.nextHexInt(0), parser.nextHexInt(0));
/*  97 */     position.setTime(dateBuilder.getDate());
/*     */     
/*  99 */     if (BitUtil.check(parser.nextHexInt(0), 3)) {
/* 100 */       position.setLatitude(-parser.nextHexInt(0) / 600000.0D);
/*     */     } else {
/* 102 */       position.setLatitude(parser.nextHexInt(0) / 600000.0D);
/*     */     } 
/*     */     
/* 105 */     if (BitUtil.check(parser.nextHexInt(0), 3)) {
/* 106 */       position.setLongitude(-parser.nextHexInt(0) / 600000.0D);
/*     */     } else {
/* 108 */       position.setLongitude(parser.nextHexInt(0) / 600000.0D);
/*     */     } 
/*     */     
/* 111 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextHexInt(0) / 100.0D));
/* 112 */     position.setCourse(parser.nextHexInt(0) / 100.0D);
/*     */     
/* 114 */     long status = parser.nextHexLong().longValue();
/* 115 */     position.set("alarm", decodeAlarm(status));
/* 116 */     position.set("ignition", Boolean.valueOf(BitUtil.check(status, 23)));
/* 117 */     position.set("status", Long.valueOf(status));
/*     */     
/* 119 */     position.set("signal", parser.next());
/* 120 */     position.set("power", Double.valueOf(parser.nextDouble(0.0D)));
/* 121 */     position.set("oil", Integer.valueOf(parser.nextHexInt(0)));
/* 122 */     position.set("odometer", Integer.valueOf(parser.nextHexInt(0) * 100));
/* 123 */     position.setAltitude(parser.nextDouble(0.0D));
/* 124 */     position.set("sat", parser.next());
/* 125 */     position.set("GPSdata", parser.next());
/* 126 */     position.set("driverUniqueId", parser.next());
/* 127 */     position.set("Temperature", parser.next());
/* 128 */     position.set("voltage", parser.next());
/* 129 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EasyTrackPlusProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */