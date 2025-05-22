/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
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
/*     */ public class M2cProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public M2cProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*  38 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  39 */     .text("#M2C,")
/*  40 */     .expression("[^,]+,")
/*  41 */     .expression("[^,]+,")
/*  42 */     .number("d+,")
/*  43 */     .number("(d+),")
/*  44 */     .number("(d+),")
/*  45 */     .expression("([LH]),")
/*  46 */     .number("d+,")
/*  47 */     .number("(d+),")
/*  48 */     .number("(dd)(dd)(dd),")
/*  49 */     .number("(dd)(dd)(dd),")
/*  50 */     .number("(-?d+.d+),")
/*  51 */     .number("(-?d+.d+),")
/*  52 */     .number("(-?d+),")
/*  53 */     .number("(d+),")
/*  54 */     .number("(d+.d+),")
/*  55 */     .number("(d+),")
/*  56 */     .number("(d+),")
/*  57 */     .number("(d+),")
/*  58 */     .number("(d+),")
/*  59 */     .number("(d+),")
/*  60 */     .number("(d+),")
/*  61 */     .number("(d+),")
/*  62 */     .number("(d+),")
/*  63 */     .number("(d+.?d*),")
/*  64 */     .any()
/*  65 */     .compile();
/*     */ 
/*     */   
/*     */   private Position decodePosition(Channel channel, SocketAddress remoteAddress, String line) {
/*  69 */     Parser parser = new Parser(PATTERN, line);
/*  70 */     if (!parser.matches()) {
/*  71 */       return null;
/*     */     }
/*     */     
/*  74 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  75 */     if (deviceSession == null) {
/*  76 */       return null;
/*     */     }
/*     */     
/*  79 */     Position position = new Position(getProtocolName());
/*  80 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  82 */     position.set("index", parser.nextInt());
/*     */     
/*  84 */     if (parser.next().equals("H")) {
/*  85 */       position.set("archive", Boolean.valueOf(true));
/*     */     }
/*     */     
/*  88 */     position.set("event", parser.nextInt());
/*     */     
/*  90 */     position.setValid(true);
/*  91 */     position.setTime(parser.nextDateTime());
/*  92 */     position.setLatitude(parser.nextDouble().doubleValue());
/*  93 */     position.setLongitude(parser.nextDouble().doubleValue());
/*  94 */     position.setAltitude(parser.nextInt().intValue());
/*  95 */     position.setCourse(parser.nextInt().intValue());
/*  96 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/*     */     
/*  98 */     position.set("sat", parser.nextInt());
/*  99 */     position.set("odometer", parser.nextLong());
/* 100 */     position.set("input", parser.nextInt());
/* 101 */     position.set("output", parser.nextInt());
/* 102 */     position.set("power", Double.valueOf(parser.nextInt().intValue() * 0.001D));
/* 103 */     position.set("battery", Double.valueOf(parser.nextInt().intValue() * 0.001D));
/* 104 */     position.set("adc1", parser.nextInt());
/* 105 */     position.set("adc2", parser.nextInt());
/* 106 */     position.set("temp1", parser.nextDouble());
/*     */     
/* 108 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 115 */     String sentence = (String)msg;
/* 116 */     sentence = sentence.substring(1);
/*     */     
/* 118 */     List<Position> positions = new LinkedList<>();
/* 119 */     for (String line : sentence.split("\r\n")) {
/* 120 */       if (!line.isEmpty()) {
/* 121 */         Position position = decodePosition(channel, remoteAddress, line);
/* 122 */         if (position != null) {
/* 123 */           positions.add(position);
/*     */         }
/*     */       } 
/*     */     } 
/*     */     
/* 128 */     return positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\M2cProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */