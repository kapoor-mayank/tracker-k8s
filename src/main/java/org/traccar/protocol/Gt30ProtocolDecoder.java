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
/*     */ public class Gt30ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public Gt30ProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .text("$$")
/*  38 */     .number("x{4}")
/*  39 */     .expression("(.{14})")
/*  40 */     .number("x{4}")
/*  41 */     .expression("(.)?")
/*  42 */     .number("(dd)(dd)(dd).(ddd),")
/*  43 */     .expression("([AV]),")
/*  44 */     .number("(d+)(dd.d+),")
/*  45 */     .expression("([NS]),")
/*  46 */     .number("(d+)(dd.d+),")
/*  47 */     .expression("([EW]),")
/*  48 */     .number("(d+.d+)?,")
/*  49 */     .number("(d+.d+)?,")
/*  50 */     .number("(dd)(dd)(dd)")
/*  51 */     .expression("[^\\|]*")
/*  52 */     .number("|(d+.d+)")
/*  53 */     .number("|(-?d+)")
/*  54 */     .number("x{4}")
/*  55 */     .compile();
/*     */   
/*     */   private String decodeAlarm(int value) {
/*  58 */     switch (value) {
/*     */       case 1:
/*     */       case 2:
/*     */       case 3:
/*  62 */         return "sos";
/*     */       case 16:
/*  64 */         return "lowBattery";
/*     */       case 17:
/*  66 */         return "overspeed";
/*     */       case 18:
/*  68 */         return "geofence";
/*     */     } 
/*  70 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  78 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  79 */     if (!parser.matches()) {
/*  80 */       return null;
/*     */     }
/*     */     
/*  83 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next().trim() });
/*  84 */     if (deviceSession == null) {
/*  85 */       return null;
/*     */     }
/*     */     
/*  88 */     Position position = new Position(getProtocolName());
/*  89 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  91 */     if (parser.hasNext()) {
/*  92 */       position.set("alarm", decodeAlarm(parser.next().charAt(0)));
/*     */     }
/*     */ 
/*     */     
/*  96 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/*  98 */     position.setValid(parser.next().equals("A"));
/*  99 */     position.setLatitude(parser.nextCoordinate());
/* 100 */     position.setLongitude(parser.nextCoordinate());
/* 101 */     position.setSpeed(parser.nextDouble(0.0D));
/* 102 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 104 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 105 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 107 */     position.set("hdop", parser.nextDouble());
/*     */     
/* 109 */     position.setAltitude(parser.nextDouble(0.0D));
/*     */     
/* 111 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gt30ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */