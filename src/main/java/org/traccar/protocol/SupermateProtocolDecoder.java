/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Calendar;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
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
/*     */ public class SupermateProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public SupermateProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */   
/*  40 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  41 */     .number("d+:")
/*  42 */     .number("(d+):")
/*  43 */     .number("d+:").text("*,")
/*  44 */     .number("(d+),")
/*  45 */     .expression("([^,]{2}),")
/*  46 */     .expression("([AV]),")
/*  47 */     .number("(xx)(xx)(xx),")
/*  48 */     .number("(xx)(xx)(xx),")
/*  49 */     .number("(x)(x{7}),")
/*  50 */     .number("(x)(x{7}),")
/*  51 */     .number("(x{4}),")
/*  52 */     .number("(x{4}),")
/*  53 */     .number("(x{12}),")
/*  54 */     .number("(x+),")
/*  55 */     .number("(d+),")
/*  56 */     .number("(x{4}),")
/*  57 */     .number("(x+)?")
/*  58 */     .any()
/*  59 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  65 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  66 */     if (!parser.matches()) {
/*  67 */       return null;
/*     */     }
/*     */     
/*  70 */     Position position = new Position(getProtocolName());
/*     */     
/*  72 */     String imei = parser.next();
/*  73 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  74 */     if (deviceSession == null) {
/*  75 */       return null;
/*     */     }
/*  77 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  79 */     position.set("commandId", parser.next());
/*  80 */     position.set("command", parser.next());
/*     */     
/*  82 */     position.setValid(parser.next().equals("A"));
/*     */ 
/*     */ 
/*     */     
/*  86 */     DateBuilder dateBuilder = (new DateBuilder()).setDate(parser.nextHexInt(0), parser.nextHexInt(0), parser.nextHexInt(0)).setTime(parser.nextHexInt(0), parser.nextHexInt(0), parser.nextHexInt(0));
/*  87 */     position.setTime(dateBuilder.getDate());
/*     */     
/*  89 */     if (parser.nextHexInt(0) == 8) {
/*  90 */       position.setLatitude(-parser.nextHexInt(0) / 600000.0D);
/*     */     } else {
/*  92 */       position.setLatitude(parser.nextHexInt(0) / 600000.0D);
/*     */     } 
/*     */     
/*  95 */     if (parser.nextHexInt(0) == 8) {
/*  96 */       position.setLongitude(-parser.nextHexInt(0) / 600000.0D);
/*     */     } else {
/*  98 */       position.setLongitude(parser.nextHexInt(0) / 600000.0D);
/*     */     } 
/*     */     
/* 101 */     position.setSpeed(parser.nextHexInt(0) / 100.0D);
/* 102 */     position.setCourse(parser.nextHexInt(0) / 100.0D);
/*     */     
/* 104 */     position.set("status", parser.next());
/* 105 */     position.set("signal", parser.next());
/* 106 */     position.set("power", Double.valueOf(parser.nextDouble(0.0D)));
/* 107 */     position.set("oil", Integer.valueOf(parser.nextHexInt(0)));
/* 108 */     position.set("odometer", Integer.valueOf(parser.nextHexInt(0)));
/*     */     
/* 110 */     if (channel != null) {
/* 111 */       Calendar calendar = Calendar.getInstance();
/* 112 */       String content = String.format("#1:%s:1:*,00000000,UP,%02x%02x%02x,%02x%02x%02x#", new Object[] { imei, 
/* 113 */             Integer.valueOf(calendar.get(1)), Integer.valueOf(calendar.get(2) + 1), Integer.valueOf(calendar.get(5)), 
/* 114 */             Integer.valueOf(calendar.get(11)), Integer.valueOf(calendar.get(12)), Integer.valueOf(calendar.get(13)) });
/* 115 */       channel.writeAndFlush(new NetworkMessage(
/* 116 */             Unpooled.copiedBuffer(content, StandardCharsets.US_ASCII), remoteAddress));
/*     */     } 
/*     */     
/* 119 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SupermateProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */