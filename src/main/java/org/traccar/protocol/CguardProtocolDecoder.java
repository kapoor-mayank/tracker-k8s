/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
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
/*     */ public class CguardProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public CguardProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN_NV = (new PatternBuilder())
/*  37 */     .text("NV:")
/*  38 */     .number("(dd)(dd)(dd) ")
/*  39 */     .number("(dd)(dd)(dd)")
/*  40 */     .number(":(-?d+.d+)")
/*  41 */     .number(":(-?d+.d+)")
/*  42 */     .number(":(d+.?d*)")
/*  43 */     .number(":(?:NAN|(d+.?d*))")
/*  44 */     .number(":(?:NAN|(d+.?d*))")
/*  45 */     .number(":(?:NAN|(d+.?d*))").optional()
/*  46 */     .compile();
/*     */   
/*  48 */   private static final Pattern PATTERN_BC = (new PatternBuilder())
/*  49 */     .text("BC:")
/*  50 */     .number("(dd)(dd)(dd) ")
/*  51 */     .number("(dd)(dd)(dd):")
/*  52 */     .expression("(.+)")
/*  53 */     .compile();
/*     */ 
/*     */   
/*     */   private Position decodePosition(DeviceSession deviceSession, String sentence) {
/*  57 */     Parser parser = new Parser(PATTERN_NV, sentence);
/*  58 */     if (!parser.matches()) {
/*  59 */       return null;
/*     */     }
/*     */     
/*  62 */     Position position = new Position(getProtocolName());
/*  63 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  65 */     position.setTime(parser.nextDateTime());
/*     */     
/*  67 */     position.setValid(true);
/*  68 */     position.setLatitude(parser.nextDouble(0.0D));
/*  69 */     position.setLongitude(parser.nextDouble(0.0D));
/*  70 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/*     */     
/*  72 */     position.setAccuracy(parser.nextDouble(0.0D));
/*     */     
/*  74 */     position.setCourse(parser.nextDouble(0.0D));
/*  75 */     position.setAltitude(parser.nextDouble(0.0D));
/*     */     
/*  77 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeStatus(DeviceSession deviceSession, String sentence) {
/*  82 */     Parser parser = new Parser(PATTERN_BC, sentence);
/*  83 */     if (!parser.matches()) {
/*  84 */       return null;
/*     */     }
/*     */     
/*  87 */     Position position = new Position(getProtocolName());
/*  88 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  90 */     getLastLocation(position, parser.nextDateTime());
/*     */     
/*  92 */     String[] data = parser.next().split(":");
/*  93 */     for (int i = 0; i < data.length / 2; i++) {
/*  94 */       String key = data[i * 2];
/*  95 */       String value = data[i * 2 + 1];
/*  96 */       switch (key) {
/*     */         case "CSQ1":
/*  98 */           position.set("rssi", Integer.valueOf(Integer.parseInt(value)));
/*     */           break;
/*     */         case "NSQ1":
/* 101 */           position.set("sat", Integer.valueOf(Integer.parseInt(value)));
/*     */           break;
/*     */         case "BAT1":
/* 104 */           if (value.contains(".")) {
/* 105 */             position.set("battery", Double.valueOf(Double.parseDouble(value))); break;
/*     */           } 
/* 107 */           position.set("batteryLevel", Integer.valueOf(Integer.parseInt(value)));
/*     */           break;
/*     */         
/*     */         case "PWR1":
/* 111 */           position.set("power", Double.valueOf(Double.parseDouble(value)));
/*     */           break;
/*     */         default:
/* 114 */           position.set(key.toLowerCase(), value);
/*     */           break;
/*     */       } 
/*     */     
/*     */     } 
/* 119 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 126 */     String sentence = (String)msg;
/*     */     
/* 128 */     if (sentence.startsWith("ID:") || sentence.startsWith("IDRO:")) {
/* 129 */       getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(sentence.indexOf(':') + 1) });
/* 130 */       return null;
/*     */     } 
/*     */     
/* 133 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 134 */     if (deviceSession == null) {
/* 135 */       return null;
/*     */     }
/*     */     
/* 138 */     if (sentence.startsWith("NV:"))
/* 139 */       return decodePosition(deviceSession, sentence); 
/* 140 */     if (sentence.startsWith("BC:")) {
/* 141 */       return decodeStatus(deviceSession, sentence);
/*     */     }
/*     */     
/* 144 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CguardProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */