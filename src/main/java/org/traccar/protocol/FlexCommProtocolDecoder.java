/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
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
/*     */ public class FlexCommProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public FlexCommProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */   
/*  39 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  40 */     .text("7E")
/*  41 */     .number("(dd)")
/*  42 */     .number("(d{15})")
/*  43 */     .number("(dddd)(dd)(dd)")
/*  44 */     .number("(dd)(dd)(dd)")
/*  45 */     .expression("([01])")
/*  46 */     .number("(d{9})")
/*  47 */     .number("(d{10})")
/*  48 */     .number("(d{4})")
/*  49 */     .number("(ddd)")
/*  50 */     .number("(ddd)")
/*  51 */     .number("(dd)")
/*  52 */     .number("(dd)")
/*  53 */     .number("(dd)")
/*  54 */     .number("(ddd)")
/*  55 */     .number("(ddd)")
/*  56 */     .number("(x{6})")
/*  57 */     .number("(x{6})")
/*  58 */     .expression("([01])([01])([01])")
/*  59 */     .expression("([01])([01])")
/*  60 */     .number("(ddd)")
/*  61 */     .number("(d{4})")
/*  62 */     .number("(ddd)")
/*  63 */     .number("(ddd)")
/*  64 */     .any()
/*  65 */     .compile();
/*     */   
/*     */   private static double parseSignedValue(Parser parser, int decimalPoints) {
/*  68 */     String stringValue = parser.next();
/*  69 */     boolean negative = (stringValue.charAt(0) == '1');
/*  70 */     double value = Integer.parseInt(stringValue.substring(1)) * Math.pow(10.0D, -decimalPoints);
/*  71 */     return negative ? -value : value;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  78 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  79 */     if (!parser.matches()) {
/*  80 */       return null;
/*     */     }
/*     */     
/*  83 */     Position position = new Position(getProtocolName());
/*     */     
/*  85 */     position.set("status", parser.nextInt());
/*     */     
/*  87 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  88 */     if (deviceSession == null) {
/*  89 */       return null;
/*     */     }
/*  91 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  93 */     position.setTime(parser.nextDateTime());
/*  94 */     position.setValid(parser.next().equals("1"));
/*  95 */     position.setLatitude(parseSignedValue(parser, 6));
/*  96 */     position.setLongitude(parseSignedValue(parser, 6));
/*  97 */     position.setAltitude(parseSignedValue(parser, 0));
/*  98 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/*  99 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 101 */     position.set("satVisible", parser.nextInt());
/* 102 */     position.set("sat", parser.nextInt());
/* 103 */     position.set("rssi", parser.nextInt());
/*     */     
/* 105 */     position.setNetwork(new Network(CellTower.from(parser
/* 106 */             .nextInt().intValue(), parser.nextInt().intValue(), parser.nextHexInt().intValue(), parser.nextHexInt().intValue())));
/*     */     int i;
/* 108 */     for (i = 1; i <= 3; i++) {
/* 109 */       position.set("in" + i, parser.nextInt());
/*     */     }
/*     */     
/* 112 */     for (i = 1; i <= 2; i++) {
/* 113 */       position.set("out" + i, parser.nextInt());
/*     */     }
/*     */     
/* 116 */     position.set("fuel", parser.nextInt());
/* 117 */     position.set("temp1", Double.valueOf(parseSignedValue(parser, 0)));
/* 118 */     position.set("batteryLevel", parser.nextInt());
/* 119 */     position.set("power", Double.valueOf(parser.nextInt().intValue() * 0.1D));
/*     */     
/* 121 */     if (channel != null) {
/* 122 */       channel.writeAndFlush(new NetworkMessage("{01}", remoteAddress));
/*     */     }
/*     */     
/* 125 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FlexCommProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */