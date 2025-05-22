/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
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
/*     */ public class HaicomProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public HaicomProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .text("$GPRS")
/*  38 */     .number("(d+),")
/*  39 */     .expression("([^,]+),")
/*  40 */     .number("(dd)(dd)(dd),")
/*  41 */     .number("(dd)(dd)(dd),")
/*  42 */     .number("(d)")
/*  43 */     .number("(dd)(d{5})")
/*  44 */     .number("(ddd)(d{5}),")
/*  45 */     .number("(d+),")
/*  46 */     .number("(d+),")
/*  47 */     .number("(d+),")
/*  48 */     .number("(d+)?,")
/*  49 */     .number("(d+)?,")
/*  50 */     .number("(d+),")
/*  51 */     .number("(d+)")
/*  52 */     .expression("(?:[LH]{2})?")
/*  53 */     .number("#V(d+)")
/*  54 */     .any()
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
/*  66 */     Position position = new Position(getProtocolName());
/*     */     
/*  68 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  69 */     if (deviceSession == null) {
/*  70 */       return null;
/*     */     }
/*  72 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  74 */     position.set("versionFw", parser.next());
/*     */     
/*  76 */     position.setTime(parser.nextDateTime());
/*     */     
/*  78 */     int flags = parser.nextInt(0);
/*     */     
/*  80 */     position.setValid(BitUtil.check(flags, 0));
/*     */     
/*  82 */     double latitude = parser.nextDouble(0.0D) + parser.nextDouble(0.0D) / 60000.0D;
/*  83 */     if (BitUtil.check(flags, 2)) {
/*  84 */       position.setLatitude(latitude);
/*     */     } else {
/*  86 */       position.setLatitude(-latitude);
/*     */     } 
/*     */     
/*  89 */     double longitude = parser.nextDouble(0.0D) + parser.nextDouble(0.0D) / 60000.0D;
/*  90 */     if (BitUtil.check(flags, 1)) {
/*  91 */       position.setLongitude(longitude);
/*     */     } else {
/*  93 */       position.setLongitude(-longitude);
/*     */     } 
/*     */     
/*  96 */     position.setSpeed(parser.nextDouble(0.0D) / 10.0D);
/*  97 */     position.setCourse(parser.nextDouble(0.0D) / 10.0D);
/*     */     
/*  99 */     position.set("status", parser.next());
/* 100 */     position.set("gprsCount", parser.next());
/* 101 */     position.set("powersaveCountdown", parser.next());
/* 102 */     position.set("input", parser.next());
/* 103 */     position.set("output", parser.next());
/* 104 */     position.set("battery", Double.valueOf(parser.nextDouble(0.0D) * 0.1D));
/*     */     
/* 106 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\HaicomProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */