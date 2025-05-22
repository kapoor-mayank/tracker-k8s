/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ public class GlobalSatProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private String format0;
/*     */   private String format1;
/*     */   
/*     */   public GlobalSatProtocolDecoder(Protocol protocol) {
/*  39 */     super(protocol);
/*     */     
/*  41 */     this.format0 = Context.getConfig().getString(getProtocolName() + ".format0", "TSPRXAB27GHKLMnaicz*U!");
/*  42 */     this.format1 = Context.getConfig().getString(getProtocolName() + ".format1", "SARY*U!");
/*     */   }
/*     */   
/*     */   public void setFormat0(String format) {
/*  46 */     this.format0 = format;
/*     */   }
/*     */   
/*     */   public void setFormat1(String format) {
/*  50 */     this.format1 = format;
/*     */   }
/*     */   
/*     */   private Position decodeOriginal(Channel channel, SocketAddress remoteAddress, String sentence) {
/*     */     String format;
/*  55 */     if (channel != null) {
/*  56 */       channel.writeAndFlush(new NetworkMessage("ACK\r", remoteAddress));
/*     */     }
/*     */ 
/*     */     
/*  60 */     if (sentence.startsWith("GSr")) {
/*  61 */       format = this.format0;
/*  62 */     } else if (sentence.startsWith("GSh")) {
/*  63 */       format = this.format1;
/*     */     } else {
/*  65 */       return null;
/*     */     } 
/*     */ 
/*     */     
/*  69 */     if (!format.contains("B") || !format.contains("S") || (!format.contains("1") && 
/*  70 */       !format.contains("2") && !format.contains("3")) || (!format.contains("6") && 
/*  71 */       !format.contains("7") && !format.contains("8"))) {
/*  72 */       return null;
/*     */     }
/*     */     
/*  75 */     if (format.contains("*")) {
/*  76 */       format = format.substring(0, format.indexOf('*'));
/*  77 */       sentence = sentence.substring(0, sentence.indexOf('*'));
/*     */     } 
/*  79 */     String[] values = sentence.split(",");
/*     */     
/*  81 */     Position position = new Position(getProtocolName());
/*     */     
/*  83 */     int formatIndex = 0, valueIndex = 1;
/*  84 */     for (; formatIndex < format.length() && valueIndex < values.length; formatIndex++) {
/*  85 */       DeviceSession deviceSession; DateBuilder dateBuilder; double longitude, latitude; String value = values[valueIndex];
/*     */       
/*  87 */       switch (format.charAt(formatIndex)) {
/*     */         case 'S':
/*  89 */           deviceSession = getDeviceSession(channel, remoteAddress, new String[] { value });
/*  90 */           if (deviceSession == null) {
/*  91 */             return null;
/*     */           }
/*  93 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */           break;
/*     */         case 'A':
/*  96 */           if (value.isEmpty()) {
/*  97 */             position.setValid(false); break;
/*     */           } 
/*  99 */           position.setValid((Integer.parseInt(value) != 1));
/*     */           break;
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*     */         case 'B':
/* 106 */           dateBuilder = (new DateBuilder()).setDay(Integer.parseInt(value.substring(0, 2))).setMonth(Integer.parseInt(value.substring(2, 4))).setYear(Integer.parseInt(value.substring(4)));
/* 107 */           value = values[++valueIndex];
/* 108 */           dateBuilder
/* 109 */             .setHour(Integer.parseInt(value.substring(0, 2)))
/* 110 */             .setMinute(Integer.parseInt(value.substring(2, 4)))
/* 111 */             .setSecond(Integer.parseInt(value.substring(4)));
/* 112 */           position.setTime(dateBuilder.getDate());
/*     */           break;
/*     */         case 'C':
/* 115 */           valueIndex++;
/*     */           break;
/*     */         case '1':
/* 118 */           longitude = Double.parseDouble(value.substring(1));
/* 119 */           if (value.charAt(0) == 'W') {
/* 120 */             longitude = -longitude;
/*     */           }
/* 122 */           position.setLongitude(longitude);
/*     */           break;
/*     */         case '2':
/* 125 */           longitude = Double.parseDouble(value.substring(4)) / 60.0D;
/* 126 */           longitude += Integer.parseInt(value.substring(1, 4));
/* 127 */           if (value.charAt(0) == 'W') {
/* 128 */             longitude = -longitude;
/*     */           }
/* 130 */           position.setLongitude(longitude);
/*     */           break;
/*     */         case '3':
/* 133 */           position.setLongitude(Double.parseDouble(value) * 1.0E-6D);
/*     */           break;
/*     */         case '6':
/* 136 */           latitude = Double.parseDouble(value.substring(1));
/* 137 */           if (value.charAt(0) == 'S') {
/* 138 */             latitude = -latitude;
/*     */           }
/* 140 */           position.setLatitude(latitude);
/*     */           break;
/*     */         case '7':
/* 143 */           latitude = Double.parseDouble(value.substring(3)) / 60.0D;
/* 144 */           latitude += Integer.parseInt(value.substring(1, 3));
/* 145 */           if (value.charAt(0) == 'S') {
/* 146 */             latitude = -latitude;
/*     */           }
/* 148 */           position.setLatitude(latitude);
/*     */           break;
/*     */         case '8':
/* 151 */           position.setLatitude(Double.parseDouble(value) * 1.0E-6D);
/*     */           break;
/*     */         case 'G':
/* 154 */           position.setAltitude(Double.parseDouble(value));
/*     */           break;
/*     */         case 'H':
/* 157 */           position.setSpeed(Double.parseDouble(value));
/*     */           break;
/*     */         case 'I':
/* 160 */           position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(value)));
/*     */           break;
/*     */         case 'J':
/* 163 */           position.setSpeed(UnitsConverter.knotsFromMph(Double.parseDouble(value)));
/*     */           break;
/*     */         case 'K':
/* 166 */           position.setCourse(Double.parseDouble(value));
/*     */           break;
/*     */         case 'N':
/* 169 */           if (value.endsWith("mV")) {
/* 170 */             position.set("battery", 
/* 171 */                 Double.valueOf(Integer.parseInt(value.substring(0, value.length() - 2)) / 1000.0D)); break;
/*     */           } 
/* 173 */           position.set("batteryLevel", Integer.valueOf(Integer.parseInt(value)));
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 181 */       valueIndex++;
/*     */     } 
/* 183 */     return position;
/*     */   }
/*     */   
/* 186 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 187 */     .text("$")
/* 188 */     .number("(d+),")
/* 189 */     .number("d+,")
/* 190 */     .number("(d+),")
/* 191 */     .number("(dd)(dd)(dd),")
/* 192 */     .number("(dd)(dd)(dd),")
/* 193 */     .expression("([EW])")
/* 194 */     .number("(ddd)(dd.d+),")
/* 195 */     .expression("([NS])")
/* 196 */     .number("(dd)(dd.d+),")
/* 197 */     .number("(d+.?d*),")
/* 198 */     .number("(d+.?d*),")
/* 199 */     .number("(d+.?d*)?,")
/* 200 */     .number("(d+)[,*]")
/* 201 */     .number("(d+.?d*)")
/* 202 */     .compile();
/*     */ 
/*     */   
/*     */   private Position decodeAlternative(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 206 */     Parser parser = new Parser(PATTERN, sentence);
/* 207 */     if (!parser.matches()) {
/* 208 */       return null;
/*     */     }
/*     */     
/* 211 */     Position position = new Position(getProtocolName());
/*     */     
/* 213 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 214 */     if (deviceSession == null) {
/* 215 */       return null;
/*     */     }
/* 217 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 219 */     position.setValid(!parser.next().equals("1"));
/* 220 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/* 221 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 222 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 223 */     position.setAltitude(parser.nextDouble(0.0D));
/* 224 */     position.setSpeed(parser.nextDouble(0.0D));
/* 225 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 227 */     position.set("sat", Integer.valueOf(parser.nextInt(0)));
/* 228 */     position.set("hdop", parser.nextDouble());
/*     */     
/* 230 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 237 */     String sentence = (String)msg;
/*     */     
/* 239 */     if (sentence.startsWith("GS"))
/* 240 */       return decodeOriginal(channel, remoteAddress, sentence); 
/* 241 */     if (sentence.startsWith("$")) {
/* 242 */       return decodeAlternative(channel, remoteAddress, sentence);
/*     */     }
/*     */     
/* 245 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GlobalSatProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */