/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ public class MilesmateProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public MilesmateProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*  38 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  39 */     .text("ApiString={")
/*  40 */     .number("A:(d+),")
/*  41 */     .number("B:(d+.d+),")
/*  42 */     .number("C:(d+.d+),")
/*  43 */     .number("D:(dd)(dd)(dd),")
/*  44 */     .number("E:(dd)(dd.d+)([NS]),")
/*  45 */     .number("F:(ddd)(dd.d+)([EW]),")
/*  46 */     .number("G:(d+.d+),")
/*  47 */     .number("H:(dd)(dd)(dd),")
/*  48 */     .expression("I:[GL],")
/*  49 */     .number("J:(d{8}),")
/*  50 */     .number("K:(d{7})")
/*  51 */     .expression("([AV]),")
/*  52 */     .number("L:d{4},")
/*  53 */     .number("M:(d+.d+)")
/*  54 */     .text("}")
/*  55 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  61 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  62 */     if (!parser.matches()) {
/*  63 */       return null;
/*     */     }
/*     */     
/*  66 */     if (channel != null) {
/*  67 */       channel.writeAndFlush(new NetworkMessage("+##Received OK\n", remoteAddress));
/*     */     }
/*     */     
/*  70 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  71 */     if (deviceSession == null) {
/*  72 */       return null;
/*     */     }
/*     */     
/*  75 */     Position position = new Position(getProtocolName());
/*  76 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  78 */     position.set("battery", parser.nextDouble());
/*  79 */     position.set("adc1", parser.nextDouble());
/*     */ 
/*     */     
/*  82 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*     */     
/*  84 */     position.setLatitude(parser.nextCoordinate());
/*  85 */     position.setLongitude(parser.nextCoordinate());
/*  86 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/*     */     
/*  88 */     dateBuilder.setDateReverse(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*  89 */     position.setTime(dateBuilder.getDate());
/*     */     
/*  91 */     String flags = parser.next();
/*  92 */     position.set("ignition", Boolean.valueOf((flags.charAt(0) == '1')));
/*  93 */     position.set("alarm", (flags.charAt(1) == '1') ? "sos" : null);
/*  94 */     position.set("charge", Boolean.valueOf((flags.charAt(5) == '1')));
/*  95 */     position.set("alarm", (flags.charAt(7) == '1') ? "overspeed" : null);
/*     */     
/*  97 */     flags = parser.next();
/*  98 */     position.set("blocked", Boolean.valueOf((flags.charAt(0) == '1')));
/*  99 */     position.set("alarm", (flags.charAt(1) == '1') ? "tow" : null);
/*     */     
/* 101 */     position.setValid(parser.next().equals("A"));
/*     */     
/* 103 */     position.setCourse(parser.nextDouble().doubleValue());
/*     */     
/* 105 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MilesmateProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */