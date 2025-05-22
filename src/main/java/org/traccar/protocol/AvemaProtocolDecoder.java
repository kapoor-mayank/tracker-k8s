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
/*     */ public class AvemaProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public AvemaProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*  38 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  39 */     .number("(d+),")
/*  40 */     .number("(dddd)(dd)(dd)")
/*  41 */     .number("(dd)(dd)(dd),")
/*  42 */     .number("(-?d+.d+),")
/*  43 */     .number("(-?d+.d+),")
/*  44 */     .number("(d+),")
/*  45 */     .number("(d+),")
/*  46 */     .number("(-?d+),")
/*  47 */     .number("(d+),")
/*  48 */     .number("(d+),")
/*  49 */     .number("(d+.d+),")
/*  50 */     .number("(d+),")
/*  51 */     .number("(d+.d+)V,")
/*  52 */     .number("(d+.d+)V,")
/*  53 */     .number("(d+),")
/*  54 */     .number("(d),")
/*  55 */     .number("(d+),")
/*  56 */     .number("d,")
/*  57 */     .number("(ddd)")
/*  58 */     .number("(dd),")
/*  59 */     .number("(x+),")
/*  60 */     .number("(x+),")
/*  61 */     .number("([^,]+)?")
/*  62 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  68 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  69 */     if (!parser.matches()) {
/*  70 */       return null;
/*     */     }
/*     */     
/*  73 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  74 */     if (deviceSession == null) {
/*  75 */       return null;
/*     */     }
/*     */     
/*  78 */     Position position = new Position(getProtocolName());
/*  79 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  81 */     position.setValid(true);
/*  82 */     position.setTime(parser.nextDateTime());
/*  83 */     position.setLongitude(parser.nextDouble().doubleValue());
/*  84 */     position.setLatitude(parser.nextDouble().doubleValue());
/*  85 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/*  86 */     position.setCourse(parser.nextInt().intValue());
/*  87 */     position.setAltitude(parser.nextInt().intValue());
/*     */     
/*  89 */     position.set("sat", parser.nextInt());
/*  90 */     position.set("event", parser.nextInt());
/*  91 */     position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/*  92 */     position.set("input", parser.nextInt());
/*  93 */     position.set("adc1", parser.nextDouble());
/*  94 */     position.set("adc2", parser.nextDouble());
/*  95 */     position.set("output", parser.nextInt());
/*  96 */     position.set("roaming", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/*     */     
/*  98 */     int rssi = parser.nextInt().intValue();
/*  99 */     position.setNetwork(new Network(CellTower.from(parser
/* 100 */             .nextInt().intValue(), parser.nextInt().intValue(), parser.nextHexInt().intValue(), parser.nextHexInt().intValue(), rssi)));
/*     */     
/* 102 */     position.set("driverUniqueId", parser.next());
/*     */     
/* 104 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AvemaProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */