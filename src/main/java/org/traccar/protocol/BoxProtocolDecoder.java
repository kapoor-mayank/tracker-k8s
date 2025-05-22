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
/*     */ public class BoxProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public BoxProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*  38 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  39 */     .text("L,")
/*  40 */     .number("(dd)(dd)(dd)")
/*  41 */     .number("(dd)(dd)(dd),")
/*  42 */     .text("G,")
/*  43 */     .number("(-?d+.d+),")
/*  44 */     .number("(-?d+.d+),")
/*  45 */     .number("(d+.?d*),")
/*  46 */     .number("(d+.?d*),")
/*  47 */     .number("(d+.?d*),")
/*  48 */     .number("(d+),")
/*  49 */     .number("(d+)")
/*  50 */     .groupBegin()
/*  51 */     .text(";")
/*  52 */     .expression("(.+)")
/*  53 */     .groupEnd("?")
/*  54 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  60 */     String sentence = (String)msg;
/*     */     
/*  62 */     if (sentence.startsWith("H,")) {
/*     */       
/*  64 */       int index = sentence.indexOf(',', 2) + 1;
/*  65 */       String id = sentence.substring(index, sentence.indexOf(',', index));
/*  66 */       getDeviceSession(channel, remoteAddress, new String[] { id });
/*     */     }
/*  68 */     else if (sentence.startsWith("E,")) {
/*     */       
/*  70 */       if (channel != null) {
/*  71 */         channel.writeAndFlush(new NetworkMessage("A," + sentence.substring(2) + "\r", remoteAddress));
/*     */       }
/*     */     }
/*  74 */     else if (sentence.startsWith("L,")) {
/*     */       
/*  76 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  77 */       if (deviceSession == null) {
/*  78 */         return null;
/*     */       }
/*     */       
/*  81 */       Parser parser = new Parser(PATTERN, sentence);
/*  82 */       if (!parser.matches()) {
/*  83 */         return null;
/*     */       }
/*     */       
/*  86 */       Position position = new Position(getProtocolName());
/*  87 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  89 */       position.setTime(parser.nextDateTime());
/*     */       
/*  91 */       position.setLatitude(parser.nextDouble().doubleValue());
/*  92 */       position.setLongitude(parser.nextDouble().doubleValue());
/*  93 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/*  94 */       position.setCourse(parser.nextDouble().doubleValue());
/*     */       
/*  96 */       position.set("tripOdometer", Double.valueOf(parser.nextDouble().doubleValue() * 1000.0D));
/*  97 */       position.set("event", parser.next());
/*     */       
/*  99 */       int status = parser.nextInt().intValue();
/* 100 */       position.set("ignition", Boolean.valueOf(BitUtil.check(status, 0)));
/* 101 */       position.set("motion", Boolean.valueOf(BitUtil.check(status, 1)));
/* 102 */       position.setValid(true);
/* 103 */       position.set("status", Integer.valueOf(status));
/*     */       
/* 105 */       if (parser.hasNext()) {
/* 106 */         String[] data = parser.next().split(";");
/* 107 */         for (String item : data) {
/* 108 */           int valueIndex = item.indexOf(',');
/* 109 */           position.set(item.substring(0, valueIndex).toLowerCase(), item.substring(valueIndex + 1));
/*     */         } 
/*     */       } 
/*     */       
/* 113 */       return position;
/*     */     } 
/*     */     
/* 116 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\BoxProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */