/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
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
/*     */ public class YwtProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public YwtProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  38 */     .expression("%(..),")
/*  39 */     .number("(d+):")
/*  40 */     .number("d+,")
/*  41 */     .number("(dd)(dd)(dd)")
/*  42 */     .number("(dd)(dd)(dd),")
/*  43 */     .expression("([EW])")
/*  44 */     .number("(ddd.d{6}),")
/*  45 */     .expression("([NS])")
/*  46 */     .number("(dd.d{6}),")
/*  47 */     .number("(d+)?,")
/*  48 */     .number("(d+),")
/*  49 */     .number("(d+),")
/*  50 */     .number("(d+),")
/*  51 */     .expression("([^,]+),")
/*  52 */     .expression("([-0-9a-fA-F]+)")
/*  53 */     .any()
/*  54 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  60 */     String sentence = (String)msg;
/*     */ 
/*     */     
/*  63 */     if (sentence.startsWith("%SN") && channel != null) {
/*  64 */       int start = sentence.indexOf(':');
/*  65 */       int end = start;
/*  66 */       for (int i = 0; i < 4; i++) {
/*  67 */         end = sentence.indexOf(',', end + 1);
/*     */       }
/*  69 */       if (end == -1) {
/*  70 */         end = sentence.length();
/*     */       }
/*     */       
/*  73 */       channel.writeAndFlush(new NetworkMessage("%AT+SN=" + sentence.substring(start, end), remoteAddress));
/*  74 */       return null;
/*     */     } 
/*     */     
/*  77 */     Parser parser = new Parser(PATTERN, sentence);
/*  78 */     if (!parser.matches()) {
/*  79 */       return null;
/*     */     }
/*     */     
/*  82 */     Position position = new Position(getProtocolName());
/*     */     
/*  84 */     String type = parser.next();
/*     */     
/*  86 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  87 */     if (deviceSession == null) {
/*  88 */       return null;
/*     */     }
/*  90 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  92 */     position.setTime(parser.nextDateTime());
/*     */     
/*  94 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
/*  95 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
/*  96 */     position.setAltitude(parser.nextDouble(0.0D));
/*  97 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/*  98 */     position.setCourse(parser.nextDouble().doubleValue());
/*     */     
/* 100 */     int satellites = parser.nextInt().intValue();
/* 101 */     position.setValid((satellites != 0));
/* 102 */     position.set("sat", Integer.valueOf(satellites));
/*     */     
/* 104 */     String reportId = parser.next();
/*     */     
/* 106 */     position.set("status", parser.next());
/*     */ 
/*     */     
/* 109 */     if ((type.equals("KP") || type.equals("EP")) && channel != null) {
/* 110 */       channel.writeAndFlush(new NetworkMessage("%AT+" + type + "=" + reportId + "\r\n", remoteAddress));
/*     */     }
/*     */     
/* 113 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\YwtProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */