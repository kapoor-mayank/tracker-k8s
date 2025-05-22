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
/*     */ public class TelicProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TelicProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .number("dddd")
/*  38 */     .number("(d{6}|d{15})")
/*  39 */     .number("(d{1,2}),")
/*  40 */     .number("d{12},")
/*  41 */     .number("d+,")
/*  42 */     .number("(dd)(dd)(dd)")
/*  43 */     .number("(dd)(dd)(dd),")
/*  44 */     .groupBegin()
/*  45 */     .number("(ddd)(dd)(dddd),")
/*  46 */     .number("(dd)(dd)(dddd),")
/*  47 */     .or()
/*  48 */     .number("(-?d+),")
/*  49 */     .number("(-?d+),")
/*  50 */     .groupEnd()
/*  51 */     .number("(d),")
/*  52 */     .number("(d+),")
/*  53 */     .number("(d+),")
/*  54 */     .number("(d+)?,")
/*  55 */     .expression("(?:[^,]*,){7}")
/*  56 */     .number("(d+),")
/*  57 */     .any()
/*  58 */     .compile();
/*     */ 
/*     */   
/*     */   private String decodeAlarm(int eventId) {
/*  62 */     switch (eventId) {
/*     */       case 1:
/*  64 */         return "powerOn";
/*     */       case 2:
/*  66 */         return "sos";
/*     */       case 5:
/*  68 */         return "powerOff";
/*     */       case 7:
/*  70 */         return "geofenceEnter";
/*     */       case 8:
/*  72 */         return "geofenceExit";
/*     */       case 22:
/*  74 */         return "lowBattery";
/*     */       case 25:
/*  76 */         return "movement";
/*     */     } 
/*  78 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  86 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  87 */     if (!parser.matches()) {
/*  88 */       return null;
/*     */     }
/*     */     
/*  91 */     Position position = new Position(getProtocolName());
/*     */     
/*  93 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  94 */     if (deviceSession == null) {
/*  95 */       return null;
/*     */     }
/*  97 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  99 */     int event = parser.nextInt(0);
/* 100 */     position.set("event", Integer.valueOf(event));
/*     */     
/* 102 */     position.set("alarm", decodeAlarm(event));
/*     */     
/* 104 */     if (event == 11) {
/* 105 */       position.set("ignition", Boolean.valueOf(true));
/* 106 */     } else if (event == 12) {
/* 107 */       position.set("ignition", Boolean.valueOf(false));
/*     */     } 
/*     */     
/* 110 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 112 */     if (parser.hasNext(6)) {
/* 113 */       position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN));
/* 114 */       position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN));
/*     */     } 
/*     */     
/* 117 */     if (parser.hasNext(2)) {
/* 118 */       position.setLongitude(parser.nextDouble(0.0D) / 10000.0D);
/* 119 */       position.setLatitude(parser.nextDouble(0.0D) / 10000.0D);
/*     */     } 
/*     */     
/* 122 */     position.setValid((parser.nextInt(0) != 1));
/* 123 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/* 124 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 126 */     if (parser.hasNext()) {
/* 127 */       position.set("sat", Integer.valueOf(parser.nextInt(0)));
/*     */     }
/*     */     
/* 130 */     position.set("battery", Integer.valueOf(parser.nextInt(0)));
/*     */     
/* 132 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TelicProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */