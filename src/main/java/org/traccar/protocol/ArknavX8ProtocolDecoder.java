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
/*     */ public class ArknavX8ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public ArknavX8ProtocolDecoder(Protocol protocol) {
/*  32 */     super(protocol);
/*     */   }
/*     */   
/*  35 */   private static final Pattern PATTERN_1G = (new PatternBuilder())
/*  36 */     .expression("(..),")
/*  37 */     .number("(dd)(dd)(dd)")
/*  38 */     .number("(dd)(dd)(dd),")
/*  39 */     .expression("([AV]),")
/*  40 */     .number("(d+)(dd.d+)([NS]),")
/*  41 */     .number("(d+)(dd.d+)([EW]),")
/*  42 */     .number("(d+.d+),")
/*  43 */     .number("(d+),")
/*  44 */     .number("(d+.d+),")
/*  45 */     .number("(d+)")
/*  46 */     .compile();
/*     */   
/*  48 */   private static final Pattern PATTERN_2G = (new PatternBuilder())
/*  49 */     .expression("..,")
/*  50 */     .number("(dd)(dd)(dd)")
/*  51 */     .number("(dd)(dd)(dd),")
/*  52 */     .number("(d+),")
/*  53 */     .number("(d+.d+),")
/*  54 */     .number("(d+.d+),")
/*  55 */     .number("(d+.d+),")
/*  56 */     .number("(d+.d+)")
/*  57 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  63 */     String sentence = (String)msg;
/*     */     
/*  65 */     if (sentence.charAt(2) != ',') {
/*  66 */       getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(0, 15) });
/*  67 */       return null;
/*     */     } 
/*     */     
/*  70 */     switch (sentence.substring(0, 2)) {
/*     */       case "1G":
/*     */       case "1R":
/*     */       case "1M":
/*  74 */         return decode1G(channel, remoteAddress, sentence);
/*     */       case "2G":
/*  76 */         return decode2G(channel, remoteAddress, sentence);
/*     */     } 
/*  78 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decode1G(Channel channel, SocketAddress remoteAddress, String sentence) {
/*  84 */     Parser parser = new Parser(PATTERN_1G, sentence);
/*  85 */     if (!parser.matches()) {
/*  86 */       return null;
/*     */     }
/*     */     
/*  89 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  90 */     if (deviceSession == null) {
/*  91 */       return null;
/*     */     }
/*     */     
/*  94 */     Position position = new Position(getProtocolName());
/*  95 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  97 */     position.set("type", parser.next());
/*     */     
/*  99 */     position.setTime(parser.nextDateTime());
/*     */     
/* 101 */     position.setValid(parser.next().equals("A"));
/* 102 */     position.setLatitude(parser.nextCoordinate());
/* 103 */     position.setLongitude(parser.nextCoordinate());
/* 104 */     position.setSpeed(parser.nextDouble(0.0D));
/* 105 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 107 */     position.set("hdop", Double.valueOf(parser.nextDouble(0.0D)));
/* 108 */     position.set("status", parser.next());
/*     */     
/* 110 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decode2G(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 115 */     Parser parser = new Parser(PATTERN_2G, sentence);
/* 116 */     if (!parser.matches()) {
/* 117 */       return null;
/*     */     }
/*     */     
/* 120 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 121 */     if (deviceSession == null) {
/* 122 */       return null;
/*     */     }
/*     */     
/* 125 */     Position position = new Position(getProtocolName());
/* 126 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 128 */     getLastLocation(position, parser.nextDateTime());
/*     */     
/* 130 */     position.set("sat", parser.nextInt());
/* 131 */     position.setAltitude(parser.nextDouble().doubleValue());
/* 132 */     position.set("power", parser.nextDouble());
/* 133 */     position.set("battery", parser.nextDouble());
/* 134 */     position.set("odometer", Double.valueOf(parser.nextDouble().doubleValue() * 1852.0D / 3600.0D));
/*     */     
/* 136 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ArknavX8ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */