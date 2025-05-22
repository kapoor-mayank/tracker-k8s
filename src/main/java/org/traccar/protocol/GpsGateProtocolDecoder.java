/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.Checksum;
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
/*     */ public class GpsGateProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public GpsGateProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*  38 */   private static final Pattern PATTERN_GPRMC = (new PatternBuilder())
/*  39 */     .text("$GPRMC,")
/*  40 */     .number("(dd)(dd)(dd).?d*,")
/*  41 */     .expression("([AV]),")
/*  42 */     .number("(dd)(dd.d+),")
/*  43 */     .expression("([NS]),")
/*  44 */     .number("(ddd)(dd.d+),")
/*  45 */     .expression("([EW]),")
/*  46 */     .number("(d+.d+)?,")
/*  47 */     .number("(d+.d+)?,")
/*  48 */     .number("(dd)(dd)(dd)")
/*  49 */     .any()
/*  50 */     .compile();
/*     */   
/*  52 */   private static final Pattern PATTERN_FRCMD = (new PatternBuilder())
/*  53 */     .text("$FRCMD,")
/*  54 */     .number("(d+),")
/*  55 */     .expression("[^,]*,")
/*  56 */     .expression("[^,]*,")
/*  57 */     .number("(d+)(dd.d+),")
/*  58 */     .expression("([NS]),")
/*  59 */     .number("(d+)(dd.d+),")
/*  60 */     .expression("([EW]),")
/*  61 */     .number("(d+.?d*),")
/*  62 */     .number("(d+.?d*),")
/*  63 */     .number("(d+.?d*)?,")
/*  64 */     .number("(dd)(dd)(dd),")
/*  65 */     .number("(dd)(dd)(dd).?d*,")
/*  66 */     .expression("([01])")
/*  67 */     .any()
/*  68 */     .compile();
/*     */   
/*     */   private void send(Channel channel, SocketAddress remoteAddress, String message) {
/*  71 */     if (channel != null) {
/*  72 */       channel.writeAndFlush(new NetworkMessage(message + Checksum.nmea(message) + "\r\n", remoteAddress));
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  80 */     String sentence = (String)msg;
/*     */     
/*  82 */     if (sentence.startsWith("$FRLIN,")) {
/*     */       
/*  84 */       int beginIndex = sentence.indexOf(',', 7);
/*  85 */       if (beginIndex != -1) {
/*  86 */         beginIndex++;
/*  87 */         int endIndex = sentence.indexOf(',', beginIndex);
/*  88 */         if (endIndex != -1) {
/*  89 */           String imei = sentence.substring(beginIndex, endIndex);
/*  90 */           DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  91 */           if (deviceSession != null) {
/*  92 */             if (channel != null) {
/*  93 */               send(channel, remoteAddress, "$FRSES," + channel.id().asShortText());
/*     */             }
/*     */           } else {
/*  96 */             send(channel, remoteAddress, "$FRERR,AuthError,Unknown device");
/*     */           } 
/*     */         } else {
/*  99 */           send(channel, remoteAddress, "$FRERR,AuthError,Parse error");
/*     */         } 
/*     */       } else {
/* 102 */         send(channel, remoteAddress, "$FRERR,AuthError,Parse error");
/*     */       }
/*     */     
/* 105 */     } else if (sentence.startsWith("$FRVER,")) {
/*     */       
/* 107 */       send(channel, remoteAddress, "$FRVER,1,0,GpsGate Server 1.0");
/*     */     } else {
/* 109 */       if (sentence.startsWith("$GPRMC,")) {
/*     */         
/* 111 */         DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 112 */         if (deviceSession == null) {
/* 113 */           return null;
/*     */         }
/*     */         
/* 116 */         Parser parser = new Parser(PATTERN_GPRMC, sentence);
/* 117 */         if (!parser.matches()) {
/* 118 */           return null;
/*     */         }
/*     */         
/* 121 */         Position position = new Position(getProtocolName());
/* 122 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */         
/* 125 */         DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */         
/* 127 */         position.setValid(parser.next().equals("A"));
/* 128 */         position.setLatitude(parser.nextCoordinate());
/* 129 */         position.setLongitude(parser.nextCoordinate());
/* 130 */         position.setSpeed(parser.nextDouble(0.0D));
/* 131 */         position.setCourse(parser.nextDouble(0.0D));
/*     */         
/* 133 */         dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 134 */         position.setTime(dateBuilder.getDate());
/*     */         
/* 136 */         return position;
/*     */       } 
/* 138 */       if (sentence.startsWith("$FRCMD,")) {
/*     */         
/* 140 */         Parser parser = new Parser(PATTERN_FRCMD, sentence);
/* 141 */         if (!parser.matches()) {
/* 142 */           return null;
/*     */         }
/*     */         
/* 145 */         Position position = new Position(getProtocolName());
/*     */         
/* 147 */         DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 148 */         if (deviceSession == null) {
/* 149 */           return null;
/*     */         }
/* 151 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 153 */         position.setLatitude(parser.nextCoordinate());
/* 154 */         position.setLongitude(parser.nextCoordinate());
/* 155 */         position.setAltitude(parser.nextDouble(0.0D));
/* 156 */         position.setSpeed(parser.nextDouble(0.0D));
/* 157 */         position.setCourse(parser.nextDouble(0.0D));
/*     */         
/* 159 */         position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */         
/* 161 */         position.setValid(parser.next().equals("1"));
/*     */         
/* 163 */         return position;
/*     */       } 
/*     */     } 
/*     */     
/* 167 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GpsGateProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */