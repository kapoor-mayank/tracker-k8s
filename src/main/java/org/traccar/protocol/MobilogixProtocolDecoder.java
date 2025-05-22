/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
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
/*     */ public class MobilogixProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public MobilogixProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*  38 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  39 */     .text("[")
/*  40 */     .number("(dddd)-(dd)-(dd) ")
/*  41 */     .number("(dd):(dd):(dd),")
/*  42 */     .number("Td+,")
/*  43 */     .number("(d),")
/*  44 */     .expression("[^,]+,")
/*  45 */     .expression("([^,]+),")
/*  46 */     .number("(xx),")
/*  47 */     .number("(d+.d+)")
/*  48 */     .groupBegin()
/*  49 */     .text(",")
/*  50 */     .number("(d)")
/*  51 */     .number("(d)")
/*  52 */     .number("(d),")
/*  53 */     .number("(-?d+.d+),")
/*  54 */     .number("(-?d+.d+),")
/*  55 */     .number("(d+.?d*),")
/*  56 */     .number("(d+.?d*)")
/*  57 */     .groupEnd("?")
/*  58 */     .any()
/*  59 */     .compile();
/*     */   
/*     */   private String decodeAlarm(String type) {
/*  62 */     switch (type) {
/*     */       case "T8":
/*  64 */         return "lowBattery";
/*     */       case "T9":
/*  66 */         return "vibration";
/*     */       case "T10":
/*  68 */         return "powerCut";
/*     */       case "T11":
/*  70 */         return "lowPower";
/*     */       case "T12":
/*  72 */         return "geofenceExit";
/*     */       case "T13":
/*  74 */         return "overspeed";
/*     */       case "T15":
/*  76 */         return "tow";
/*     */     } 
/*  78 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  86 */     String sentence = ((String)msg).trim();
/*  87 */     String type = sentence.substring(21, sentence.indexOf(',', 21));
/*     */     
/*  89 */     if (channel != null) {
/*  90 */       String response, time = sentence.substring(1, 20);
/*     */       
/*  92 */       if (type.equals("T1")) {
/*  93 */         response = String.format("[%s,S1,1]", new Object[] { time });
/*     */       } else {
/*  95 */         response = String.format("[%s,S%s]", new Object[] { time, type.substring(1) });
/*     */       } 
/*  97 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */     
/* 100 */     Parser parser = new Parser(PATTERN, sentence);
/* 101 */     if (!parser.matches()) {
/* 102 */       return null;
/*     */     }
/*     */     
/* 105 */     Position position = new Position(getProtocolName());
/*     */     
/* 107 */     position.setDeviceTime(parser.nextDateTime());
/* 108 */     if (parser.nextInt().intValue() == 0) {
/* 109 */       position.set("archive", Boolean.valueOf(true));
/*     */     }
/*     */     
/* 112 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 113 */     if (deviceSession == null) {
/* 114 */       return null;
/*     */     }
/* 116 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 118 */     position.set("type", type);
/* 119 */     position.set("alarm", decodeAlarm(type));
/*     */     
/* 121 */     int status = parser.nextHexInt().intValue();
/* 122 */     position.set("ignition", Boolean.valueOf(BitUtil.check(status, 2)));
/* 123 */     position.set("motion", Boolean.valueOf(BitUtil.check(status, 3)));
/* 124 */     position.set("status", Integer.valueOf(status));
/*     */     
/* 126 */     position.set("battery", parser.nextDouble());
/*     */     
/* 128 */     if (parser.hasNext(7)) {
/*     */       
/* 130 */       position.set("sat", parser.nextInt());
/* 131 */       position.set("rssi", Integer.valueOf(6 * parser.nextInt().intValue() - 111));
/*     */       
/* 133 */       position.setValid((parser.nextInt().intValue() > 0));
/* 134 */       position.setFixTime(position.getDeviceTime());
/*     */       
/* 136 */       position.setLatitude(parser.nextDouble().doubleValue());
/* 137 */       position.setLongitude(parser.nextDouble().doubleValue());
/* 138 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/* 139 */       position.setCourse(parser.nextDouble().doubleValue());
/*     */     }
/*     */     else {
/*     */       
/* 143 */       getLastLocation(position, position.getDeviceTime());
/*     */     } 
/*     */ 
/*     */     
/* 147 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MobilogixProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */