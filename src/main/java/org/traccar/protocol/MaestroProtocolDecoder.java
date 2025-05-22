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
/*     */ public class MaestroProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public MaestroProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .text("@")
/*  38 */     .number("(d+),")
/*  39 */     .number("d+,")
/*  40 */     .expression("[^,]+,")
/*  41 */     .expression("([01]),")
/*  42 */     .number("(d+.d+),")
/*  43 */     .number("(d+),")
/*  44 */     .expression("([01]),")
/*  45 */     .expression("([01]),")
/*  46 */     .number("(dd)/(dd)/(dd),")
/*  47 */     .number("(dd):(dd):(dd),")
/*  48 */     .number("(-?d+.d+),")
/*  49 */     .number("(-?d+.d+),")
/*  50 */     .number("(d+.?d*),")
/*  51 */     .number("(d+.?d*),")
/*  52 */     .number("(d+.?d*),")
/*  53 */     .number("(d+),")
/*  54 */     .number("(d+.?d*),")
/*  55 */     .number("(d+.?d*)")
/*  56 */     .number(",(d+)").optional()
/*  57 */     .any()
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
/*  69 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  70 */     if (deviceSession == null) {
/*  71 */       return null;
/*     */     }
/*     */     
/*  74 */     Position position = new Position(getProtocolName());
/*  75 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  77 */     position.setValid((parser.nextInt(0) == 1));
/*     */     
/*  79 */     position.set("battery", Double.valueOf(parser.nextDouble(0.0D)));
/*  80 */     position.set("rssi", Integer.valueOf(parser.nextInt(0)));
/*  81 */     position.set("charge", Boolean.valueOf((parser.nextInt(0) == 1)));
/*  82 */     position.set("ignition", Boolean.valueOf((parser.nextInt(0) == 1)));
/*     */     
/*  84 */     position.setTime(parser.nextDateTime());
/*     */     
/*  86 */     position.setLatitude(parser.nextDouble(0.0D));
/*  87 */     position.setLongitude(parser.nextDouble(0.0D));
/*  88 */     position.setAltitude(parser.nextDouble(0.0D));
/*  89 */     position.setSpeed(UnitsConverter.knotsFromMph(parser.nextDouble(0.0D)));
/*  90 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/*  92 */     position.set("sat", Integer.valueOf(parser.nextInt(0)));
/*  93 */     position.set("hdop", Double.valueOf(parser.nextDouble(0.0D)));
/*  94 */     position.set("odometer", Double.valueOf(parser.nextDouble(0.0D) * 1609.34D));
/*     */     
/*  96 */     if (parser.hasNext()) {
/*  97 */       position.set("adc1", Integer.valueOf(parser.nextInt(0)));
/*     */     }
/*     */     
/* 100 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MaestroProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */