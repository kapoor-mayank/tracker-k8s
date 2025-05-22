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
/*     */ public class TechtoCruzProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TechtoCruzProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .text("$$A")
/*  38 */     .number("d+,")
/*  39 */     .number("(d+),")
/*  40 */     .number("(dd)(dd)(dd)")
/*  41 */     .number("(dd)(dd)(dd),")
/*  42 */     .expression("([AV]),")
/*  43 */     .expression("([^,]+),")
/*  44 */     .expression("([^,]+),")
/*  45 */     .number("(d+.d+),")
/*  46 */     .number("(d+),")
/*  47 */     .number("(-?d+.d+),[NS],")
/*  48 */     .number("(-?d+.d+),[WE],")
/*  49 */     .number("(-?d+.d+),")
/*  50 */     .number("(d+.d+),")
/*  51 */     .number("(d+),")
/*  52 */     .number("(d+),")
/*  53 */     .number("(d+.d+),")
/*  54 */     .number("(d+.d+),")
/*  55 */     .number("([01]),")
/*  56 */     .number("([01]),")
/*  57 */     .number("[01],")
/*  58 */     .number("([01]),")
/*  59 */     .number("([01]),")
/*  60 */     .any()
/*  61 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  67 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  68 */     if (!parser.matches()) {
/*  69 */       return null;
/*     */     }
/*     */     
/*  72 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  73 */     if (deviceSession == null) {
/*  74 */       return null;
/*     */     }
/*     */     
/*  77 */     Position position = new Position(getProtocolName());
/*  78 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  80 */     position.setTime(parser.nextDateTime());
/*  81 */     position.setValid(parser.next().equals("A"));
/*     */     
/*  83 */     position.set("manufacturer", parser.next());
/*  84 */     position.set("registration", parser.next());
/*     */     
/*  86 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/*     */     
/*  88 */     position.set("odometer", parser.nextInt());
/*     */     
/*  90 */     position.setLatitude(parser.nextDouble().doubleValue());
/*  91 */     position.setLongitude(parser.nextDouble().doubleValue());
/*  92 */     position.setAltitude(parser.nextDouble().doubleValue());
/*  93 */     position.setCourse(parser.nextDouble().doubleValue());
/*     */     
/*  95 */     position.set("sat", parser.nextInt());
/*  96 */     position.set("rssi", parser.nextInt());
/*  97 */     position.set("power", parser.nextDouble());
/*  98 */     position.set("battery", parser.nextDouble());
/*  99 */     position.set("charge", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/* 100 */     position.set("speedSignalStatus", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/* 101 */     position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/*     */     
/* 103 */     if (parser.nextInt().intValue() > 0) {
/* 104 */       position.set("alarm", "overspeed");
/*     */     }
/*     */     
/* 107 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TechtoCruzProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */