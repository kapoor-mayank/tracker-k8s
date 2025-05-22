/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
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
/*     */ public class AlematicsProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public AlematicsProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  38 */     .text("$T,")
/*  39 */     .number("(d+),")
/*  40 */     .number("(d+),")
/*  41 */     .number("(d+),")
/*  42 */     .number("(dddd)(dd)(dd)")
/*  43 */     .number("(dd)(dd)(dd),")
/*  44 */     .number("(dddd)(dd)(dd)")
/*  45 */     .number("(dd)(dd)(dd),")
/*  46 */     .number("(-?d+.d+),")
/*  47 */     .number("(-?d+.d+),")
/*  48 */     .number("(d+),")
/*  49 */     .number("(d+),")
/*  50 */     .number("(-?d+),")
/*  51 */     .number("(d+.d),")
/*  52 */     .number("(d+),")
/*  53 */     .number("(d+),")
/*  54 */     .number("(d+),")
/*  55 */     .number("(d+.d+),")
/*  56 */     .number("(d+.d+),")
/*  57 */     .number("(d+),")
/*  58 */     .groupBegin()
/*  59 */     .text("0,$S,")
/*  60 */     .expression("(.*)")
/*  61 */     .or()
/*  62 */     .number("(d+),")
/*  63 */     .expression("(.*)")
/*  64 */     .or()
/*  65 */     .any()
/*  66 */     .groupEnd()
/*  67 */     .compile();
/*     */ 
/*     */   
/*     */   private void decodeExtras(Position position, Parser parser) {
/*  71 */     int mask = parser.nextInt().intValue();
/*  72 */     String[] data = parser.next().split(",");
/*     */     
/*  74 */     int index = 0;
/*     */     
/*  76 */     if (BitUtil.check(mask, 0)) {
/*  77 */       index++;
/*     */     }
/*     */     
/*  80 */     if (BitUtil.check(mask, 1)) {
/*  81 */       position.set("power", Integer.valueOf(Integer.parseInt(data[index++])));
/*     */     }
/*     */     
/*  84 */     if (BitUtil.check(mask, 2)) {
/*  85 */       position.set("battery", Integer.valueOf(Integer.parseInt(data[index++])));
/*     */     }
/*     */     
/*  88 */     if (BitUtil.check(mask, 3)) {
/*  89 */       position.set("obdSpeed", Integer.valueOf(Integer.parseInt(data[index++])));
/*     */     }
/*     */     
/*  92 */     if (BitUtil.check(mask, 4)) {
/*  93 */       position.set("rpm", Integer.valueOf(Integer.parseInt(data[index++])));
/*     */     }
/*     */     
/*  96 */     if (BitUtil.check(mask, 5)) {
/*  97 */       position.set("rssi", Integer.valueOf(Integer.parseInt(data[index++])));
/*     */     }
/*     */     
/* 100 */     if (BitUtil.check(mask, 6)) {
/* 101 */       index++;
/*     */     }
/*     */     
/* 104 */     if (BitUtil.check(mask, 7)) {
/* 105 */       index++;
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 114 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 115 */     if (!parser.matches()) {
/* 116 */       return null;
/*     */     }
/*     */     
/* 119 */     Position position = new Position(getProtocolName());
/*     */     
/* 121 */     position.set("type", parser.nextInt());
/* 122 */     position.set("index", parser.nextInt());
/*     */     
/* 124 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 125 */     if (deviceSession == null) {
/* 126 */       return null;
/*     */     }
/* 128 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 130 */     position.setFixTime(parser.nextDateTime());
/* 131 */     position.setDeviceTime(parser.nextDateTime());
/*     */     
/* 133 */     position.setValid(true);
/* 134 */     position.setLatitude(parser.nextDouble(0.0D));
/* 135 */     position.setLongitude(parser.nextDouble(0.0D));
/* 136 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt(0)));
/* 137 */     position.setCourse(parser.nextInt(0));
/* 138 */     position.setAltitude(parser.nextInt(0));
/*     */     
/* 140 */     position.set("hdop", parser.nextDouble());
/* 141 */     position.set("sat", parser.nextInt());
/* 142 */     position.set("input", parser.nextInt());
/* 143 */     position.set("output", parser.nextInt());
/* 144 */     position.set("adc1", parser.nextDouble());
/* 145 */     position.set("power", parser.nextDouble());
/* 146 */     position.set("odometer", parser.nextInt());
/*     */     
/* 148 */     if (parser.hasNext()) {
/* 149 */       position.set("text", parser.next());
/* 150 */     } else if (parser.hasNext()) {
/* 151 */       decodeExtras(position, parser);
/*     */     } 
/*     */     
/* 154 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AlematicsProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */