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
/*     */ public class C2stekProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public C2stekProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  38 */     .text("PA$")
/*  39 */     .number("(d+)")
/*  40 */     .text("$")
/*  41 */     .expression(".#")
/*  42 */     .number("(dd)(dd)(dd)#")
/*  43 */     .number("(dd)(dd)(dd)#")
/*  44 */     .number("([01])#")
/*  45 */     .number("([+-]?d+.d+)#")
/*  46 */     .number("([+-]?d+.d+)#")
/*  47 */     .number("(d+.d+)#")
/*  48 */     .number("(d+.d+)#")
/*  49 */     .number("(-?d+.d+)#")
/*  50 */     .number("(d+)#")
/*  51 */     .number("d+#")
/*  52 */     .number("(x+)#")
/*  53 */     .number("([01])")
/*  54 */     .number("([01])")
/*  55 */     .number("([01])#")
/*  56 */     .any()
/*  57 */     .text("$AP")
/*  58 */     .compile();
/*     */   
/*     */   private String decodeAlarm(int alarm) {
/*  61 */     switch (alarm) {
/*     */       case 2:
/*  63 */         return "shock";
/*     */       case 3:
/*  65 */         return "powerCut";
/*     */       case 4:
/*  67 */         return "overspeed";
/*     */       case 5:
/*  69 */         return "sos";
/*     */       case 6:
/*  71 */         return "door";
/*     */       case 10:
/*  73 */         return "lowBattery";
/*     */       case 11:
/*  75 */         return "fault";
/*     */     } 
/*  77 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  85 */     String sentence = (String)msg;
/*  86 */     if (sentence.contains("$20$") && channel != null) {
/*  87 */       channel.writeAndFlush(new NetworkMessage(sentence, remoteAddress));
/*     */     }
/*     */     
/*  90 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  91 */     if (!parser.matches()) {
/*  92 */       return null;
/*     */     }
/*     */     
/*  95 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  96 */     if (deviceSession == null) {
/*  97 */       return null;
/*     */     }
/*     */     
/* 100 */     Position position = new Position(getProtocolName());
/* 101 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 103 */     position.setTime(parser.nextDateTime());
/* 104 */     position.setValid((parser.nextInt().intValue() > 0));
/* 105 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 106 */     position.setLongitude(parser.nextDouble().doubleValue());
/* 107 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/* 108 */     position.setCourse(parser.nextDouble().doubleValue());
/* 109 */     position.setAltitude(parser.nextDouble().doubleValue());
/*     */     
/* 111 */     position.set("battery", Double.valueOf(parser.nextInt().intValue() * 0.001D));
/* 112 */     position.set("alarm", decodeAlarm(parser.nextHexInt().intValue()));
/*     */     
/* 114 */     position.set("armed", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/* 115 */     position.set("door", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/* 116 */     position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/*     */     
/* 118 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\C2stekProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */