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
/*     */ public class SanavProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public SanavProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  38 */     .expression("imei[:=]")
/*  39 */     .number("(d+)")
/*  40 */     .expression("&?rmc[:=]")
/*  41 */     .text("$GPRMC,")
/*  42 */     .number("(dd)(dd)(dd).d+,")
/*  43 */     .expression("([AV]),")
/*  44 */     .number("(d+)(dd.d+),")
/*  45 */     .expression("([NS]),")
/*  46 */     .number("(d+)(dd.d+),")
/*  47 */     .expression("([EW]),")
/*  48 */     .number("(d+.d+),")
/*  49 */     .number("(d+.d+)?,")
/*  50 */     .number("(dd)(dd)(dd),")
/*  51 */     .groupBegin()
/*  52 */     .expression("[^*]*")
/*  53 */     .text("*")
/*  54 */     .number("xx,")
/*  55 */     .expression("[^,]+,")
/*  56 */     .number("(d+),")
/*  57 */     .groupEnd("?")
/*  58 */     .any()
/*  59 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  65 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  66 */     if (!parser.matches()) {
/*  67 */       return null;
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
/*     */     
/*  79 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*     */     
/*  81 */     position.setValid(parser.next().equals("A"));
/*  82 */     position.setLatitude(parser.nextCoordinate());
/*  83 */     position.setLongitude(parser.nextCoordinate());
/*  84 */     position.setSpeed(parser.nextDouble().doubleValue());
/*  85 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/*  87 */     dateBuilder.setDateReverse(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*  88 */     position.setTime(dateBuilder.getDate());
/*     */     
/*  90 */     if (parser.hasNext()) {
/*  91 */       int io = parser.nextHexInt().intValue();
/*  92 */       for (int i = 0; i < 5; i++) {
/*  93 */         position.set("in" + (i + 1), Boolean.valueOf(BitUtil.check(io, i)));
/*     */       }
/*  95 */       position.set("ignition", Boolean.valueOf(BitUtil.check(io, 5)));
/*  96 */       position.set("out1", Boolean.valueOf(BitUtil.check(io, 6)));
/*  97 */       position.set("out2", Boolean.valueOf(BitUtil.check(io, 7)));
/*  98 */       position.set("charge", Boolean.valueOf(BitUtil.check(io, 8)));
/*  99 */       if (!BitUtil.check(io, 9)) {
/* 100 */         position.set("alarm", "lowBattery");
/*     */       }
/*     */     } 
/*     */     
/* 104 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SanavProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */