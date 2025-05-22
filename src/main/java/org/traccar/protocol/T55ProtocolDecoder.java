/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.channels.DatagramChannel;
import java.util.Date;
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
/*     */ public class T55ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private static final Pattern PATTERN_GPRMC = (new PatternBuilder()).text("$GPRMC,").number("(dd)(dd)(dd).?d*,").expression("([AV]),").number("(dd)(dd.d+),").expression("([NS]),").number("(d{2,3})(dd.d+),").expression("([EW]),").number("(d+.?d*)?,").number("(d+.?d*)?,").number("(dd)(dd)(dd),").expression("[^*]+").text("*").expression("[^,]+").number(",(d+)").number(",(d+)").expression(",([01])").number(",(d+)").number(",(d+)").optional(7).number("((?:,d+)+)?").any().compile();
/*     */   private static final Pattern PATTERN_GPGGA = (new PatternBuilder()).text("$GPGGA,").number("(dd)(dd)(dd).?d*,").number("(d+)(dd.d+),").expression("([NS]),").number("(d+)(dd.d+),").expression("([EW]),").any().compile();
/*     */   
/*     */   public T55ProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 127 */     this.position = null;
/*     */   }
/*     */   private static final Pattern PATTERN_GPRMA = (new PatternBuilder()).text("$GPRMA,").expression("([AV]),").number("(dd)(dd.d+),").expression("([NS]),").number("(ddd)(dd.d+),").expression("([EW]),,,").number("(d+.?d*)?,").number("(d+.?d*)?,").any().compile();
/*     */   
/*     */   private Position decodeGprmc(DeviceSession deviceSession, String sentence, SocketAddress remoteAddress, Channel channel) {
/* 132 */     if (deviceSession != null && channel != null && !(channel instanceof DatagramChannel)) {
/* 133 */       if (Context.getIdentityManager().lookupAttributeBoolean(deviceSession
/* 134 */           .getDeviceId(), getProtocolName() + ".ack", false, true)) {
/* 135 */         channel.writeAndFlush(new NetworkMessage("OK1\r\n", remoteAddress));
/*     */       }
/*     */     }
/* 138 */     Parser parser = new Parser(PATTERN_GPRMC, sentence);
/* 139 */     if (!parser.matches()) {
/* 140 */       return null;
/*     */     }
/*     */     
/* 143 */     Position position = new Position(getProtocolName());
/*     */     
/* 145 */     if (deviceSession != null) {
/* 146 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */     }
/*     */ 
/*     */     
/* 150 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 152 */     position.setValid(parser.next().equals("A"));
/* 153 */     position.setLatitude(parser.nextCoordinate());
/* 154 */     position.setLongitude(parser.nextCoordinate());
/* 155 */     position.setSpeed(parser.nextDouble(0.0D));
/* 156 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 158 */     if (System.currentTimeMillis() - dateBuilder.getDate().getTime() > 2592000000L) {
/* 159 */       parser.skip(3);
/* 160 */       dateBuilder.setCurrentDate();
/*     */     } else {
/* 162 */       dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     } 
/*     */     
/* 165 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 167 */     if (parser.hasNext(5)) {
/* 168 */       position.set("sat", parser.nextInt());
/*     */       
/* 170 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 171 */       if (deviceSession == null) {
/* 172 */         return null;
/*     */       }
/* 174 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 176 */       position.set("ignition", Boolean.valueOf((parser.hasNext() && parser.next().equals("1"))));
/* 177 */       position.set("fuel", Integer.valueOf(parser.nextInt(0)));
/* 178 */       position.set("battery", parser.nextInt());
/*     */     } 
/*     */     
/* 181 */     if (parser.hasNext()) {
/* 182 */       String[] parameters = parser.next().split(",");
/* 183 */       for (int i = 1; i < parameters.length; i++) {
/* 184 */         position.set("io" + i, parameters[i]);
/*     */       }
/*     */     } 
/*     */     
/* 188 */     if (deviceSession != null) {
/* 189 */       return position;
/*     */     }
/* 191 */     this.position = position;
/* 192 */     return null;
/*     */   }
/*     */   private static final Pattern PATTERN_TRCCR = (new PatternBuilder()).text("$TRCCR,").number("(dddd)(dd)(dd)").number("(dd)(dd)(dd).?d*,").expression("([AV]),").number("(-?d+.d+),").number("(-?d+.d+),").number("(d+.d+),").number("(d+.d+),").number("(-?d+.d+),").number("(d+.?d*),").any().compile();
/*     */   private static final Pattern PATTERN_GPIOP = (new PatternBuilder()).text("$GPIOP,").number("[01]{8},").number("[01]{8},").number("d+.d+,").number("d+.d+,").number("d+.d+,").number("d+.d+,").number("(d+.d+),").number("(d+.d+)").any().compile();
/*     */   
/*     */   private Position decodeGpgga(DeviceSession deviceSession, String sentence) {
/* 198 */     Parser parser = new Parser(PATTERN_GPGGA, sentence);
/* 199 */     if (!parser.matches()) {
/* 200 */       return null;
/*     */     }
/*     */     
/* 203 */     Position position = new Position(getProtocolName());
/* 204 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */ 
/*     */     
/* 208 */     DateBuilder dateBuilder = (new DateBuilder()).setCurrentDate().setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 209 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 211 */     position.setValid(true);
/* 212 */     position.setLatitude(parser.nextCoordinate());
/* 213 */     position.setLongitude(parser.nextCoordinate());
/*     */     
/* 215 */     return null;
/*     */   }
/*     */   private static final Pattern PATTERN_QZE = (new PatternBuilder()).text("QZE,").number("(d{15}),").number("(d+),").number("(dd)(dd)(dddd),").number("(dd)(dd)(dd),").number("(-?d+.d+),").number("(-?d+.d+),").number("(d+),").number("(d+),").expression("([AV]),").expression("([01])").compile(); private Position position;
/*     */   
/*     */   private Position decodeGprma(DeviceSession deviceSession, String sentence) {
/* 220 */     Parser parser = new Parser(PATTERN_GPRMA, sentence);
/* 221 */     if (!parser.matches()) {
/* 222 */       return null;
/*     */     }
/*     */     
/* 225 */     Position position = new Position(getProtocolName());
/* 226 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 228 */     position.setTime(new Date());
/* 229 */     position.setValid(parser.next().equals("A"));
/* 230 */     position.setLatitude(parser.nextCoordinate());
/* 231 */     position.setLongitude(parser.nextCoordinate());
/* 232 */     position.setSpeed(parser.nextDouble(0.0D));
/* 233 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 235 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeTrccr(DeviceSession deviceSession, String sentence) {
/* 240 */     Parser parser = new Parser(PATTERN_TRCCR, sentence);
/* 241 */     if (!parser.matches()) {
/* 242 */       return null;
/*     */     }
/*     */     
/* 245 */     Position position = new Position(getProtocolName());
/* 246 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 248 */     position.setTime(parser.nextDateTime());
/*     */     
/* 250 */     position.setValid(parser.next().equals("A"));
/* 251 */     position.setLatitude(parser.nextDouble(0.0D));
/* 252 */     position.setLongitude(parser.nextDouble(0.0D));
/* 253 */     position.setSpeed(parser.nextDouble(0.0D));
/* 254 */     position.setCourse(parser.nextDouble(0.0D));
/* 255 */     position.setAltitude(parser.nextDouble(0.0D));
/*     */     
/* 257 */     position.set("battery", Double.valueOf(parser.nextDouble(0.0D)));
/*     */     
/* 259 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeGpiop(DeviceSession deviceSession, String sentence) {
/* 264 */     Parser parser = new Parser(PATTERN_GPIOP, sentence);
/* 265 */     if (!parser.matches()) {
/* 266 */       return null;
/*     */     }
/*     */     
/* 269 */     Position position = new Position(getProtocolName());
/* 270 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 272 */     getLastLocation(position, null);
/*     */     
/* 274 */     position.set("power", parser.nextDouble());
/* 275 */     position.set("battery", parser.nextDouble());
/*     */     
/* 277 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeQze(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 282 */     Parser parser = new Parser(PATTERN_QZE, sentence);
/* 283 */     if (!parser.matches()) {
/* 284 */       return null;
/*     */     }
/*     */     
/* 287 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 288 */     if (deviceSession == null) {
/* 289 */       return null;
/*     */     }
/*     */     
/* 292 */     Position position = new Position(getProtocolName());
/* 293 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 295 */     position.set("event", parser.nextInt());
/*     */     
/* 297 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/* 298 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 299 */     position.setLongitude(parser.nextDouble().doubleValue());
/* 300 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/* 301 */     position.setCourse(parser.nextInt().intValue());
/* 302 */     position.setValid(parser.next().equals("A"));
/*     */     
/* 304 */     position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/*     */     
/* 306 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 313 */     String sentence = (String)msg;
/*     */ 
/*     */ 
/*     */     
/* 317 */     if (!sentence.startsWith("$") && sentence.contains("$")) {
/* 318 */       int index = sentence.indexOf("$");
/* 319 */       String id = sentence.substring(0, index);
/* 320 */       if (id.endsWith(",")) {
/* 321 */         id = id.substring(0, id.length() - 1);
/* 322 */       } else if (id.endsWith("/")) {
/* 323 */         id = id.substring(id.indexOf('/') + 1, id.length() - 1);
/*     */       } 
/* 325 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 326 */       sentence = sentence.substring(index);
/*     */     } else {
/* 328 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*     */     } 
/*     */     
/* 331 */     if (sentence.startsWith("$PGID"))
/* 332 */     { getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(6, sentence.length() - 3) }); }
/* 333 */     else if (sentence.startsWith("$DEVID"))
/* 334 */     { getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(7, sentence.lastIndexOf('*')) }); }
/* 335 */     else if (sentence.startsWith("$PCPTI"))
/* 336 */     { getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(7, sentence.indexOf(",", 7)) }); }
/* 337 */     else if (sentence.startsWith("IMEI"))
/* 338 */     { getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(5) }); }
/* 339 */     else if (sentence.startsWith("$IMEI"))
/* 340 */     { getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(6) }); }
/* 341 */     else { DeviceSession deviceSession = null; if (sentence.startsWith("$GPFID"))
/* 342 */       { deviceSession = getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(7) });
/* 343 */         if (deviceSession != null && this.position != null) {
/* 344 */           Position position = this.position;
/* 345 */           position.setDeviceId(deviceSession.getDeviceId());
/* 346 */           this.position = null;
/* 347 */           return position;
/*     */         }  }
/* 349 */       else if (sentence.matches("^[0-9A-F]+$"))
/* 350 */       { getDeviceSession(channel, remoteAddress, new String[] { sentence }); }
/* 351 */       else { if (sentence.startsWith("$GPRMC"))
/* 352 */           return decodeGprmc(deviceSession, sentence, remoteAddress, channel); 
/* 353 */         if (sentence.startsWith("$GPGGA") && deviceSession != null)
/* 354 */           return decodeGpgga(deviceSession, sentence); 
/* 355 */         if (sentence.startsWith("$GPRMA") && deviceSession != null)
/* 356 */           return decodeGprma(deviceSession, sentence); 
/* 357 */         if (sentence.startsWith("$TRCCR") && deviceSession != null)
/* 358 */           return decodeTrccr(deviceSession, sentence); 
/* 359 */         if (sentence.startsWith("$GPIOP"))
/* 360 */           return decodeGpiop(deviceSession, sentence); 
/* 361 */         if (sentence.startsWith("QZE"))
/* 362 */           return decodeQze(channel, remoteAddress, sentence);  }
/*     */        }
/*     */     
/* 365 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\T55ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */