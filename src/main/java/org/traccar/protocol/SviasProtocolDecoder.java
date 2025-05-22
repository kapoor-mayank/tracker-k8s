/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
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
/*     */ public class SviasProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public SviasProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*  38 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  39 */     .text("[")
/*  40 */     .number("d{4},")
/*  41 */     .number("d{4},")
/*  42 */     .number("d+,")
/*  43 */     .number("(d+),")
/*  44 */     .number("d+,")
/*  45 */     .number("(d+)(dd)(dd),")
/*  46 */     .number("(d+)(dd)(dd),")
/*  47 */     .number("(-?)(d+)(dd)(d{5}),")
/*  48 */     .number("(-?)(d+)(dd)(d{5}),")
/*  49 */     .number("(d+),")
/*  50 */     .number("(d+),")
/*  51 */     .number("(d+),")
/*  52 */     .number("(d+),")
/*  53 */     .number("(d+),")
/*  54 */     .number("(d),")
/*  55 */     .number("(d),")
/*  56 */     .number("(d+),")
/*  57 */     .number("(d+),")
/*  58 */     .number("(d+),")
/*  59 */     .any()
/*  60 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  66 */     if (channel != null) {
/*  67 */       channel.writeAndFlush(new NetworkMessage("@", remoteAddress));
/*     */     }
/*     */     
/*  70 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  71 */     if (!parser.matches()) {
/*  72 */       return null;
/*     */     }
/*     */     
/*  75 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  76 */     if (deviceSession == null) {
/*  77 */       return null;
/*     */     }
/*     */     
/*  80 */     Position position = new Position(getProtocolName());
/*  81 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  83 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*  84 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_MIN));
/*  85 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_MIN));
/*  86 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue() * 0.01D));
/*  87 */     position.setCourse(parser.nextDouble().doubleValue() * 0.01D);
/*     */     
/*  89 */     position.set("odometer", Integer.valueOf(parser.nextInt().intValue() * 100));
/*     */     
/*  91 */     int input = parser.nextInt().intValue();
/*  92 */     int output = parser.nextInt().intValue();
/*     */     
/*  94 */     position.set("alarm", BitUtil.check(input, 0) ? "sos" : null);
/*  95 */     position.set("ignition", Boolean.valueOf(BitUtil.check(input, 4)));
/*  96 */     position.setValid(BitUtil.check(output, 0));
/*     */     
/*  98 */     position.set("power", Double.valueOf(parser.nextInt().intValue() * 0.001D));
/*  99 */     position.set("batteryLevel", parser.nextInt());
/* 100 */     position.set("rssi", parser.nextInt());
/*     */     
/* 102 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SviasProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */