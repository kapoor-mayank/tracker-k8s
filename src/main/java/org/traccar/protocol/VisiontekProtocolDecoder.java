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
/*     */ public class VisiontekProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public VisiontekProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .text("$1,")
/*  38 */     .expression("([^,]+),")
/*  39 */     .number("(d+),").optional()
/*  40 */     .number("(dd),(dd),(dd),")
/*  41 */     .number("(dd),(dd),(dd),")
/*  42 */     .groupBegin()
/*  43 */     .number("(dd)(dd).?(d+)([NS]),")
/*  44 */     .number("(ddd)(dd).?(d+)([EW]),")
/*  45 */     .or()
/*  46 */     .number("(dd.d+)([NS]),")
/*  47 */     .number("(ddd.d+)([EW]),")
/*  48 */     .groupEnd()
/*  49 */     .number("(d+.?d+),")
/*  50 */     .number("(d+),")
/*  51 */     .groupBegin()
/*  52 */     .number("(d+),")
/*  53 */     .number("(d+),")
/*  54 */     .number("(d+),")
/*  55 */     .number("([01]),")
/*  56 */     .number("([01]),")
/*  57 */     .number("([01]),")
/*  58 */     .number("([01]),")
/*  59 */     .number("([01]),")
/*  60 */     .number("(d+),")
/*  61 */     .or()
/*  62 */     .number("(d+.d),")
/*  63 */     .number("(d+),")
/*  64 */     .number("(d+),")
/*  65 */     .number("([01],[01],[01],[01]),")
/*  66 */     .number("([01],[01],[01],[01]),")
/*  67 */     .number("(d+.?d*),")
/*  68 */     .number("(d+.?d*),")
/*  69 */     .groupEnd("?")
/*  70 */     .any()
/*  71 */     .expression("([AV])")
/*  72 */     .number(",(d{10})").optional()
/*  73 */     .any()
/*  74 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  80 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  81 */     if (!parser.matches()) {
/*  82 */       return null;
/*     */     }
/*     */     
/*  85 */     Position position = new Position(getProtocolName());
/*     */     
/*  87 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next(), parser.next() });
/*  88 */     if (deviceSession == null) {
/*  89 */       return null;
/*     */     }
/*  91 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  93 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/*  95 */     if (parser.hasNext(8)) {
/*  96 */       position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN_HEM));
/*  97 */       position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN_HEM));
/*     */     } 
/*  99 */     if (parser.hasNext(4)) {
/* 100 */       position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 101 */       position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/*     */     } 
/*     */     
/* 104 */     position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(parser
/* 105 */             .next().replace(".", "")) / 10.0D));
/*     */     
/* 107 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 109 */     if (parser.hasNext(9)) {
/* 110 */       position.setAltitude(parser.nextDouble(0.0D));
/* 111 */       position.set("sat", parser.nextInt());
/* 112 */       position.set("odometer", Integer.valueOf(parser.nextInt(0) * 1000));
/* 113 */       position.set("ignition", Boolean.valueOf(parser.next().equals("1")));
/* 114 */       position.set("io1", parser.next());
/* 115 */       position.set("io2", parser.next());
/* 116 */       position.set("immobilizer", parser.next());
/* 117 */       position.set("charge", Boolean.valueOf(parser.next().equals("1")));
/* 118 */       position.set("rssi", parser.nextDouble());
/*     */     } 
/*     */     
/* 121 */     if (parser.hasNext(7)) {
/* 122 */       position.set("hdop", parser.nextDouble());
/* 123 */       position.setAltitude(parser.nextDouble(0.0D));
/* 124 */       position.set("odometer", Integer.valueOf(parser.nextInt(0) * 1000));
/* 125 */       position.set("input", parser.next());
/* 126 */       position.set("output", parser.next());
/* 127 */       position.set("adc1", parser.next());
/* 128 */       position.set("adc2", parser.next());
/*     */     } 
/*     */     
/* 131 */     position.setValid(parser.next().equals("A"));
/*     */     
/* 133 */     position.set("driverUniqueId", parser.next());
/*     */     
/* 135 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\VisiontekProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */