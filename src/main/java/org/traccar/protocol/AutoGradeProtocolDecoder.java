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
/*     */ public class AutoGradeProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public AutoGradeProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  38 */     .text("(")
/*  39 */     .number("d{12}")
/*  40 */     .number("(d{15})")
/*  41 */     .number("(dd)(dd)(dd)")
/*  42 */     .expression("([AV])")
/*  43 */     .number("(d+)(dd.d+)([NS])")
/*  44 */     .number("(d+)(dd.d+)([EW])")
/*  45 */     .number("([d.]{5})")
/*  46 */     .number("(dd)(dd)(dd)")
/*  47 */     .number("([d.]{6})")
/*  48 */     .expression("(.)")
/*  49 */     .number("A(xxxx)")
/*  50 */     .number("B(xxxx)")
/*  51 */     .number("C(xxxx)")
/*  52 */     .number("D(xxxx)")
/*  53 */     .number("E(xxxx)")
/*  54 */     .number("K(xxxx)")
/*  55 */     .number("L(xxxx)")
/*  56 */     .number("M(xxxx)")
/*  57 */     .number("N(xxxx)")
/*  58 */     .number("O(xxxx)")
/*  59 */     .any()
/*  60 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  66 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  67 */     if (!parser.matches()) {
/*  68 */       return null;
/*     */     }
/*     */     
/*  71 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  72 */     if (deviceSession == null) {
/*  73 */       return null;
/*     */     }
/*     */     
/*  76 */     Position position = new Position(getProtocolName());
/*  77 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */     
/*  80 */     DateBuilder dateBuilder = (new DateBuilder()).setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/*  82 */     position.setValid(parser.next().equals("A"));
/*  83 */     position.setLatitude(parser.nextCoordinate());
/*  84 */     position.setLongitude(parser.nextCoordinate());
/*  85 */     position.setSpeed(parser.nextDouble(0.0D));
/*     */     
/*  87 */     dateBuilder.setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*  88 */     position.setTime(dateBuilder.getDate());
/*     */     
/*  90 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/*  92 */     int status = parser.next().charAt(0);
/*  93 */     position.set("status", Integer.valueOf(status));
/*  94 */     position.set("ignition", Boolean.valueOf(BitUtil.check(status, 0)));
/*     */     int i;
/*  96 */     for (i = 1; i <= 5; i++) {
/*  97 */       position.set("adc" + i, parser.next());
/*     */     }
/*     */     
/* 100 */     for (i = 1; i <= 5; i++) {
/* 101 */       position.set("can" + i, parser.next());
/*     */     }
/*     */     
/* 104 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AutoGradeProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */