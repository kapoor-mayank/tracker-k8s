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
/*     */ public class PortmanProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public PortmanProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN_STANDARD = (new PatternBuilder())
/*  37 */     .text("$PTMLA,")
/*  38 */     .expression("([^,]+),")
/*  39 */     .expression("([ABCL]),")
/*  40 */     .number("(dd)(dd)(dd)")
/*  41 */     .number("(dd)(dd)(dd),")
/*  42 */     .expression("([NS])")
/*  43 */     .number("(dd)(dd.d+)")
/*  44 */     .expression("([EW])")
/*  45 */     .number("(d{2,3})(dd.d+),")
/*  46 */     .number("(d+),")
/*  47 */     .number("(d+),")
/*  48 */     .number("(?:NA|C(-?d+)),")
/*  49 */     .number("(x{8}),")
/*  50 */     .number("(?:NA|(d+)),")
/*  51 */     .number("(d+),")
/*  52 */     .number("(d+),")
/*  53 */     .number("(d+.d+),")
/*  54 */     .number("(d+),")
/*  55 */     .number("(?:G(d+)|[^,]*)")
/*  56 */     .compile();
/*     */ 
/*     */   
/*     */   private Object decodeStandard(Channel channel, SocketAddress remoteAddress, String sentence) {
/*  60 */     Parser parser = new Parser(PATTERN_STANDARD, sentence);
/*  61 */     if (!parser.matches()) {
/*  62 */       return null;
/*     */     }
/*     */     
/*  65 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  66 */     if (deviceSession == null) {
/*  67 */       return null;
/*     */     }
/*     */     
/*  70 */     Position position = new Position(getProtocolName());
/*  71 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  73 */     position.setValid(!parser.next().equals("L"));
/*  74 */     position.setTime(parser.nextDateTime());
/*  75 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/*  76 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/*  77 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/*  78 */     position.setCourse(parser.nextInt().intValue());
/*     */     
/*  80 */     position.set("temp1", parser.next());
/*  81 */     position.set("status", parser.nextHexLong());
/*  82 */     position.set("driverUniqueId", parser.next());
/*     */     
/*  84 */     int event = parser.nextInt().intValue();
/*  85 */     position.set("event", Integer.valueOf(event));
/*  86 */     if (event == 253) {
/*  87 */       position.set("ignition", Boolean.valueOf(true));
/*  88 */     } else if (event == 254) {
/*  89 */       position.set("ignition", Boolean.valueOf(false));
/*     */     } 
/*     */     
/*  92 */     position.set("sat", parser.nextInt());
/*  93 */     position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/*  94 */     position.set("rssi", parser.nextInt());
/*  95 */     position.set("fuel", parser.nextInt());
/*     */     
/*  97 */     return position;
/*     */   }
/*     */   
/* 100 */   private static final Pattern PATTERN_EXTENDED = (new PatternBuilder())
/* 101 */     .text("$EXT,")
/* 102 */     .expression("([^,]+),")
/* 103 */     .expression("([ABCL]),")
/* 104 */     .number("(dd)(dd)(dd)")
/* 105 */     .number("(dd)(dd)(dd),")
/* 106 */     .expression("([NS])")
/* 107 */     .number("(dd)(dd.d+)")
/* 108 */     .expression("([EW])")
/* 109 */     .number("(d{2,3})(dd.d+),")
/* 110 */     .number("(d+),")
/* 111 */     .number("(d+),")
/* 112 */     .number("(?:NA|C(-?d+)),")
/* 113 */     .number("(?:NA|F(d+)),")
/* 114 */     .number("(d+),")
/* 115 */     .number("(d+),")
/* 116 */     .number("(d+.d+),")
/* 117 */     .number("(?:NA|(d+)),")
/* 118 */     .number("(x{8}),")
/* 119 */     .number("(d+)")
/* 120 */     .any()
/* 121 */     .compile();
/*     */ 
/*     */   
/*     */   private Object decodeExtended(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 125 */     Parser parser = new Parser(PATTERN_EXTENDED, sentence);
/* 126 */     if (!parser.matches()) {
/* 127 */       return null;
/*     */     }
/*     */     
/* 130 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 131 */     if (deviceSession == null) {
/* 132 */       return null;
/*     */     }
/*     */     
/* 135 */     Position position = new Position(getProtocolName());
/* 136 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 138 */     position.setValid(!parser.next().equals("L"));
/* 139 */     position.setTime(parser.nextDateTime());
/* 140 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 141 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 142 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/* 143 */     position.setCourse(parser.nextInt().intValue());
/*     */     
/* 145 */     position.set("temp1", parser.next());
/* 146 */     position.set("fuel", parser.nextInt());
/* 147 */     position.set("sat", parser.nextInt());
/* 148 */     position.set("rssi", parser.nextInt());
/* 149 */     position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/* 150 */     position.set("driverUniqueId", parser.next());
/* 151 */     position.set("status", parser.nextHexLong());
/* 152 */     position.set("event", parser.nextInt());
/*     */     
/* 154 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 161 */     String sentence = (String)msg;
/* 162 */     if (sentence.startsWith("$PTMLA"))
/* 163 */       return decodeStandard(channel, remoteAddress, sentence); 
/* 164 */     if (sentence.startsWith("$EXT")) {
/* 165 */       return decodeExtended(channel, remoteAddress, sentence);
/*     */     }
/* 167 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PortmanProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */