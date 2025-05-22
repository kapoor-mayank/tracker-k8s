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
/*     */ public class KenjiProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public KenjiProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  38 */     .text(">")
/*  39 */     .number("C(d{6}),")
/*  40 */     .number("M(x{6}),")
/*  41 */     .number("O(x{4}),")
/*  42 */     .number("I(x{4}),")
/*  43 */     .number("D(dd)(dd)(dd),")
/*  44 */     .expression("([AV]),")
/*  45 */     .number("([NS])(dd)(dd.d+),")
/*  46 */     .number("([EW])(ddd)(dd.d+),")
/*  47 */     .number("T(d+.d+),")
/*  48 */     .number("H(d+.d+),")
/*  49 */     .number("Y(dd)(dd)(dd),")
/*  50 */     .number("G(d+)")
/*  51 */     .any()
/*  52 */     .compile();
/*     */   
/*     */   private String decodeAlarm(int value) {
/*  55 */     if (BitUtil.check(value, 2)) {
/*  56 */       return "sos";
/*     */     }
/*  58 */     if (BitUtil.check(value, 4)) {
/*  59 */       return "lowBattery";
/*     */     }
/*  61 */     if (BitUtil.check(value, 6)) {
/*  62 */       return "movement";
/*     */     }
/*  64 */     if (BitUtil.check(value, 1) || BitUtil.check(value, 10) || BitUtil.check(value, 11)) {
/*  65 */       return "vibration";
/*     */     }
/*     */     
/*  68 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  75 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  76 */     if (!parser.matches()) {
/*  77 */       return null;
/*     */     }
/*     */     
/*  80 */     Position position = new Position(getProtocolName());
/*     */     
/*  82 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  83 */     if (deviceSession == null) {
/*  84 */       return null;
/*     */     }
/*  86 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  88 */     position.set("alarm", decodeAlarm(parser.nextHexInt(0)));
/*  89 */     position.set("output", Integer.valueOf(parser.nextHexInt(0)));
/*  90 */     position.set("input", Integer.valueOf(parser.nextHexInt(0)));
/*     */ 
/*     */     
/*  93 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/*  95 */     position.setValid(parser.next().equals("A"));
/*     */     
/*  97 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/*  98 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/*  99 */     position.setSpeed(parser.nextDouble(0.0D));
/* 100 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 102 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 103 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 105 */     position.set("sat", Integer.valueOf(parser.nextInt(0)));
/*     */     
/* 107 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\KenjiProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */