/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
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
/*     */ 
/*     */ public class CarTrackProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public CarTrackProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  38 */     .text("$$")
/*  39 */     .number("(d+)")
/*  40 */     .text("?").expression("*")
/*  41 */     .text("&A")
/*  42 */     .number("(dddd)")
/*  43 */     .text("&B")
/*  44 */     .number("(dd)(dd)(dd).(ddd),")
/*  45 */     .expression("([AV]),")
/*  46 */     .number("(dd)(dd.dddd),")
/*  47 */     .expression("([NS]),")
/*  48 */     .number("(ddd)(dd.dddd),")
/*  49 */     .expression("([EW]),")
/*  50 */     .number("(d+.d*)?,")
/*  51 */     .number("(d+.d*)?,")
/*  52 */     .number("(dd)(dd)(dd)")
/*  53 */     .any()
/*  54 */     .expression("&C([^&]*)")
/*  55 */     .expression("&D([^&]*)")
/*  56 */     .expression("&E([^&]*)")
/*  57 */     .expression("&Y([^&]*)").optional()
/*  58 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  64 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  65 */     if (!parser.matches()) {
/*  66 */       return null;
/*     */     }
/*     */     
/*  69 */     Position position = new Position(getProtocolName());
/*     */     
/*  71 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  72 */     if (deviceSession == null) {
/*  73 */       return null;
/*     */     }
/*  75 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  77 */     position.set("command", parser.next());
/*     */ 
/*     */     
/*  80 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/*  82 */     position.setValid(parser.next().equals("A"));
/*  83 */     position.setLatitude(parser.nextCoordinate());
/*  84 */     position.setLongitude(parser.nextCoordinate());
/*  85 */     position.setSpeed(parser.nextDouble(0.0D));
/*  86 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/*  88 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*  89 */     position.setTime(dateBuilder.getDate());
/*     */     
/*  91 */     position.set("io1", parser.next());
/*     */     
/*  93 */     String odometer = parser.next();
/*  94 */     odometer = odometer.replace(":", "A");
/*  95 */     odometer = odometer.replace(";", "B");
/*  96 */     odometer = odometer.replace("<", "C");
/*  97 */     odometer = odometer.replace("=", "D");
/*  98 */     odometer = odometer.replace(">", "E");
/*  99 */     odometer = odometer.replace("?", "F");
/* 100 */     position.set("odometer", Integer.valueOf(Integer.parseInt(odometer, 16)));
/*     */     
/* 102 */     parser.next();
/* 103 */     position.set("adc1", parser.next());
/*     */     
/* 105 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CarTrackProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */