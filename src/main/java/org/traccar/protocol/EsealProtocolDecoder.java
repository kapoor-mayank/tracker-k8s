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
/*     */ public class EsealProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private String config;
/*     */   
/*     */   public EsealProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*  38 */     this.config = Context.getConfig().getString(getProtocolName() + ".config");
/*     */   }
/*     */   
/*  41 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  42 */     .text("##S,")
/*  43 */     .expression("[^,]+,")
/*  44 */     .number("(d+),")
/*  45 */     .number("d+,")
/*  46 */     .expression("[^,]+,")
/*  47 */     .expression("([^,]+),")
/*  48 */     .number("(d+),")
/*  49 */     .number("(dddd)-(dd)-(dd),")
/*  50 */     .number("(dd):(dd):(dd),")
/*  51 */     .number("d+,")
/*  52 */     .expression("([AV]),")
/*  53 */     .number("(d+.d+)([NS]) ")
/*  54 */     .number("(d+.d+)([EW]),")
/*  55 */     .number("(d+),")
/*  56 */     .number("(d+),")
/*  57 */     .expression("([^,]+),")
/*  58 */     .number("(d+.d+),")
/*  59 */     .expression("([^,]+),")
/*  60 */     .number("(d+.d+),")
/*  61 */     .number("(-?d+),")
/*  62 */     .text("E##")
/*  63 */     .compile();
/*     */   
/*     */   private void sendResponse(Channel channel, String prefix, String type, String payload) {
/*  66 */     if (channel != null) {
/*  67 */       channel.writeAndFlush(new NetworkMessage(prefix + type + "," + payload + ",E##\r\n", channel
/*  68 */             .remoteAddress()));
/*     */     }
/*     */   }
/*     */   
/*     */   private String decodeAlarm(String type) {
/*  73 */     switch (type) {
/*     */       case "Event-Door":
/*  75 */         return "door";
/*     */       case "Event-Shock":
/*  77 */         return "shock";
/*     */       case "Event-Drop":
/*  79 */         return "fallDown";
/*     */       case "Event-Lock":
/*  81 */         return "lock";
/*     */       case "Event-RC-Unlock":
/*  83 */         return "unlock";
/*     */     } 
/*  85 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  93 */     String sentence = (String)msg;
/*  94 */     Parser parser = new Parser(PATTERN, sentence);
/*  95 */     if (!parser.matches()) {
/*  96 */       return null;
/*     */     }
/*     */     
/*  99 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 100 */     if (deviceSession == null) {
/* 101 */       return null;
/*     */     }
/*     */     
/* 104 */     Position position = new Position(getProtocolName());
/* 105 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 107 */     String type = parser.next();
/* 108 */     String prefix = sentence.substring(0, sentence.indexOf(type));
/* 109 */     int index = parser.nextInt().intValue();
/*     */     
/* 111 */     position.set("index", Integer.valueOf(index));
/* 112 */     position.set("alarm", decodeAlarm(type));
/*     */     
/* 114 */     switch (type) {
/*     */       case "Startup":
/* 116 */         sendResponse(channel, prefix, type + " ACK", index + "," + this.config);
/*     */         break;
/*     */       case "Normal":
/*     */       case "Button-Normal":
/*     */       case "Termination":
/*     */       case "Event-Door":
/*     */       case "Event-Shock":
/*     */       case "Event-Drop":
/*     */       case "Event-Lock":
/*     */       case "Event-RC-Unlock":
/* 126 */         sendResponse(channel, prefix, type + " ACK", String.valueOf(index));
/*     */         break;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 132 */     position.setTime(parser.nextDateTime());
/* 133 */     position.setValid(parser.next().equals("A"));
/* 134 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 135 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 136 */     position.setCourse(parser.nextInt().intValue());
/* 137 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/*     */     
/* 139 */     switch (parser.next()) {
/*     */       case "Open":
/* 141 */         position.set("door", Boolean.valueOf(true));
/*     */         break;
/*     */       case "Close":
/* 144 */         position.set("door", Boolean.valueOf(false));
/*     */         break;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 150 */     position.set("acceleration", parser.nextDouble());
/* 151 */     position.set("nfc", parser.next());
/* 152 */     position.set("battery", parser.nextDouble());
/* 153 */     position.set("rssi", parser.nextInt());
/*     */     
/* 155 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EsealProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */