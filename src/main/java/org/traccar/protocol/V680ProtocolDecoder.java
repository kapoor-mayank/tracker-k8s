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
/*     */ public class V680ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public V680ProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .groupBegin()
/*  38 */     .number("#(d+)#")
/*  39 */     .expression("([^#]*)#")
/*  40 */     .groupEnd("?")
/*  41 */     .number("(d+)#")
/*  42 */     .expression("([^#]+)#")
/*  43 */     .expression("([^#]+)#")
/*  44 */     .number("(d+)#")
/*  45 */     .expression("([^#]+)?#?")
/*  46 */     .expression("(?:[^#]+#)?")
/*  47 */     .number("(d+.d+),([EW]),")
/*  48 */     .number("(d+.d+),([NS]),")
/*  49 */     .number("(d+.d+),")
/*  50 */     .number("(d+.?d*)?#")
/*  51 */     .number("(dd)(dd)(dd)#")
/*  52 */     .number("(dd)(dd)(dd)")
/*  53 */     .any()
/*  54 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  60 */     String sentence = (String)msg;
/*  61 */     sentence = sentence.trim();
/*     */     
/*  63 */     if (sentence.length() == 16) {
/*     */       
/*  65 */       getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(1, sentence.length()) });
/*     */     } else {
/*     */       DeviceSession deviceSession;
/*     */       
/*  69 */       Parser parser = new Parser(PATTERN, sentence);
/*  70 */       if (!parser.matches()) {
/*  71 */         return null;
/*     */       }
/*     */       
/*  74 */       Position position = new Position(getProtocolName());
/*     */ 
/*     */       
/*  77 */       if (parser.hasNext()) {
/*  78 */         deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*     */       } else {
/*  80 */         deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*     */       } 
/*  82 */       if (deviceSession == null) {
/*  83 */         return null;
/*     */       }
/*  85 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  87 */       position.set("user", parser.next());
/*  88 */       position.setValid((parser.nextInt(0) > 0));
/*  89 */       position.set("password", parser.next());
/*  90 */       position.set("event", parser.next());
/*  91 */       position.set("packet", parser.next());
/*  92 */       position.set("lbsData", parser.next());
/*     */       
/*  94 */       double lon = parser.nextDouble(0.0D);
/*  95 */       boolean west = parser.next().equals("W");
/*  96 */       double lat = parser.nextDouble(0.0D);
/*  97 */       boolean south = parser.next().equals("S");
/*     */       
/*  99 */       if (lat > 90.0D || lon > 180.0D) {
/* 100 */         int lonDegrees = (int)(lon * 0.01D);
/* 101 */         lon = (lon - (lonDegrees * 100)) / 60.0D;
/* 102 */         lon += lonDegrees;
/*     */         
/* 104 */         int latDegrees = (int)(lat * 0.01D);
/* 105 */         lat = (lat - (latDegrees * 100)) / 60.0D;
/* 106 */         lat += latDegrees;
/*     */       } 
/*     */       
/* 109 */       position.setLongitude(west ? -lon : lon);
/* 110 */       position.setLatitude(south ? -lat : lat);
/*     */       
/* 112 */       position.setSpeed(parser.nextDouble(0.0D));
/* 113 */       position.setCourse(parser.nextDouble(0.0D));
/*     */       
/* 115 */       int day = parser.nextInt(0);
/* 116 */       int month = parser.nextInt(0);
/* 117 */       if (day == 0 && month == 0) {
/* 118 */         return null;
/*     */       }
/*     */ 
/*     */ 
/*     */       
/* 123 */       DateBuilder dateBuilder = (new DateBuilder()).setDate(parser.nextInt(0), month, day).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 124 */       position.setTime(dateBuilder.getDate());
/*     */       
/* 126 */       return position;
/*     */     } 
/*     */     
/* 129 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\V680ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */