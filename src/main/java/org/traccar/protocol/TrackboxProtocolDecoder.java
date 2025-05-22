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
/*     */ public class TrackboxProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TrackboxProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  38 */     .number("(dd)(dd)(dd).(ddd),")
/*  39 */     .number("(dd)(dd.dddd)([NS]),")
/*  40 */     .number("(ddd)(dd.dddd)([EW]),")
/*  41 */     .number("(d+.d),")
/*  42 */     .number("(-?d+.?d*),")
/*  43 */     .number("(d),")
/*  44 */     .number("(d+.d+),")
/*  45 */     .number("d+.d+,")
/*  46 */     .number("(d+.d+),")
/*  47 */     .number("(dd)(dd)(dd),")
/*  48 */     .number("(d+)")
/*  49 */     .compile();
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress) {
/*  52 */     if (channel != null) {
/*  53 */       channel.writeAndFlush(new NetworkMessage("=OK=\r\n", remoteAddress));
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  61 */     String sentence = (String)msg;
/*     */     
/*  63 */     if (sentence.startsWith("a=connect")) {
/*  64 */       String id = sentence.substring(sentence.indexOf("i=") + 2);
/*  65 */       if (getDeviceSession(channel, remoteAddress, new String[] { id }) != null) {
/*  66 */         sendResponse(channel, remoteAddress);
/*     */       }
/*  68 */       return null;
/*     */     } 
/*     */     
/*  71 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  72 */     if (deviceSession == null) {
/*  73 */       return null;
/*     */     }
/*     */     
/*  76 */     Parser parser = new Parser(PATTERN, sentence);
/*  77 */     if (!parser.matches()) {
/*  78 */       return null;
/*     */     }
/*  80 */     sendResponse(channel, remoteAddress);
/*     */     
/*  82 */     Position position = new Position(getProtocolName());
/*  83 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */     
/*  86 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/*  88 */     position.setLatitude(parser.nextCoordinate());
/*  89 */     position.setLongitude(parser.nextCoordinate());
/*     */     
/*  91 */     position.set("hdop", parser.nextDouble());
/*     */     
/*  93 */     position.setAltitude(parser.nextDouble(0.0D));
/*     */     
/*  95 */     int fix = parser.nextInt(0);
/*  96 */     position.set("gps", Integer.valueOf(fix));
/*  97 */     position.setValid((fix > 0));
/*     */     
/*  99 */     position.setCourse(parser.nextDouble(0.0D));
/* 100 */     position.setSpeed(parser.nextDouble(0.0D));
/*     */     
/* 102 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 103 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 105 */     position.set("sat", parser.nextInt());
/*     */     
/* 107 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TrackboxProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */