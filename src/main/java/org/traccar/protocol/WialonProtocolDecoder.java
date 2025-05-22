/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.regex.Matcher;
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
/*     */ public class WialonProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public WialonProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
/*     */   }
/*     */   
/*  41 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  42 */     .number("(?:NA|(dd)(dd)(dd));")
/*  43 */     .number("(?:NA|(dd)(dd)(dd));")
/*  44 */     .number("(?:NA|(dd)(dd.d+));")
/*  45 */     .expression("(?:NA|([NS]));")
/*  46 */     .number("(?:NA|(ddd)(dd.d+));")
/*  47 */     .expression("(?:NA|([EW]));")
/*  48 */     .number("(?:NA|(d+.?d*))?;")
/*  49 */     .number("(?:NA|(d+.?d*))?;")
/*  50 */     .number("(?:NA|(-?d+.?d*));")
/*  51 */     .number("(?:NA|(d+))")
/*  52 */     .groupBegin().text(";")
/*  53 */     .number("(?:NA|(d+.?d*));")
/*  54 */     .number("(?:NA|(d+));")
/*  55 */     .number("(?:NA|(d+));")
/*  56 */     .expression("(?:NA|([^;]*));")
/*  57 */     .expression("(?:NA|([^;]*));")
/*  58 */     .expression("(?:NA|([^;]*))")
/*  59 */     .groupEnd("?")
/*  60 */     .any()
/*  61 */     .compile();
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, String prefix, Integer number) {
/*  64 */     if (channel != null) {
/*  65 */       StringBuilder response = new StringBuilder(prefix);
/*  66 */       if (number != null) {
/*  67 */         response.append(number);
/*     */       }
/*  69 */       response.append("\r\n");
/*  70 */       channel.writeAndFlush(new NetworkMessage(response.toString(), remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodePosition(Channel channel, SocketAddress remoteAddress, String substring) {
/*  76 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  77 */     if (deviceSession == null) {
/*  78 */       return null;
/*     */     }
/*     */     
/*  81 */     Parser parser = new Parser(PATTERN, substring);
/*  82 */     if (!parser.matches()) {
/*  83 */       return null;
/*     */     }
/*     */     
/*  86 */     Position position = new Position(getProtocolName());
/*  87 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  89 */     if (parser.hasNext(6)) {
/*  90 */       position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     } else {
/*  92 */       position.setTime(new Date());
/*     */     } 
/*     */     
/*  95 */     if (parser.hasNext(9)) {
/*  96 */       position.setLatitude(parser.nextCoordinate());
/*  97 */       position.setLongitude(parser.nextCoordinate());
/*  98 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/*  99 */       position.setCourse(parser.nextDouble(0.0D));
/* 100 */       position.setAltitude(parser.nextDouble(0.0D));
/*     */     } else {
/* 102 */       getLastLocation(position, position.getDeviceTime());
/*     */     } 
/*     */     
/* 105 */     if (parser.hasNext()) {
/* 106 */       int satellites = parser.nextInt(0);
/* 107 */       position.setValid((satellites >= 3));
/* 108 */       position.set("sat", Integer.valueOf(satellites));
/*     */     } 
/*     */     
/* 111 */     position.set("hdop", parser.nextDouble());
/* 112 */     position.set("input", parser.next());
/* 113 */     position.set("output", parser.next());
/*     */     
/* 115 */     if (parser.hasNext()) {
/* 116 */       String[] values = parser.next().split(",");
/* 117 */       for (int i = 0; i < values.length; i++) {
/* 118 */         position.set("adc" + (i + 1), values[i]);
/*     */       }
/*     */     } 
/*     */     
/* 122 */     position.set("driverUniqueId", parser.next());
/*     */     
/* 124 */     if (parser.hasNext()) {
/* 125 */       String[] values = parser.next().split(",");
/* 126 */       for (String param : values) {
/* 127 */         Matcher paramParser = Pattern.compile("(.*):[1-3]:(.*)").matcher(param);
/* 128 */         if (paramParser.matches()) {
/*     */           try {
/* 130 */             position.set(paramParser.group(1).toLowerCase(), Double.valueOf(Double.parseDouble(paramParser.group(2))));
/* 131 */           } catch (NumberFormatException e) {
/* 132 */             position.set(paramParser.group(1).toLowerCase(), paramParser.group(2));
/*     */           } 
/*     */         }
/*     */       } 
/*     */     } 
/*     */     
/* 138 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 145 */     String sentence = (String)msg;
/*     */     
/* 147 */     if (sentence.startsWith("#L#")) {
/*     */       
/* 149 */       String[] values = sentence.substring(3).split(";");
/*     */       
/* 151 */       String imei = (values[0].indexOf('.') >= 0) ? values[1] : values[0];
/* 152 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 153 */       if (deviceSession != null) {
/* 154 */         sendResponse(channel, remoteAddress, "#AL#", Integer.valueOf(1));
/*     */       }
/*     */     }
/* 157 */     else if (sentence.startsWith("#P#")) {
/*     */       
/* 159 */       sendResponse(channel, remoteAddress, "#AP#", (Integer)null);
/*     */     }
/* 161 */     else if (sentence.startsWith("#SD#") || sentence.startsWith("#D#")) {
/*     */       
/* 163 */       Position position = decodePosition(channel, remoteAddress, sentence
/* 164 */           .substring(sentence.indexOf('#', 1) + 1));
/*     */       
/* 166 */       if (position != null) {
/* 167 */         sendResponse(channel, remoteAddress, "#AD#", Integer.valueOf(1));
/* 168 */         return position;
/*     */       }
/*     */     
/* 171 */     } else if (sentence.startsWith("#B#")) {
/*     */       
/* 173 */       String[] messages = sentence.substring(sentence.indexOf('#', 1) + 1).split("\\|");
/* 174 */       List<Position> positions = new LinkedList<>();
/*     */       
/* 176 */       for (String message : messages) {
/* 177 */         Position position = decodePosition(channel, remoteAddress, message);
/* 178 */         if (position != null) {
/* 179 */           position.set("archive", Boolean.valueOf(true));
/* 180 */           positions.add(position);
/*     */         } 
/*     */       } 
/*     */       
/* 184 */       sendResponse(channel, remoteAddress, "#AB#", Integer.valueOf(messages.length));
/* 185 */       if (!positions.isEmpty()) {
/* 186 */         return positions;
/*     */       }
/*     */     }
/* 189 */     else if (sentence.startsWith("#M#")) {
/* 190 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 191 */       if (deviceSession != null) {
/* 192 */         Position position = new Position(getProtocolName());
/* 193 */         position.setDeviceId(deviceSession.getDeviceId());
/* 194 */         getLastLocation(position, new Date());
/* 195 */         position.setValid(false);
/* 196 */         position.set("result", sentence.substring(sentence.indexOf('#', 1) + 1));
/* 197 */         sendResponse(channel, remoteAddress, "#AM#", Integer.valueOf(1));
/* 198 */         return position;
/*     */       } 
/*     */     } 
/*     */     
/* 202 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WialonProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */