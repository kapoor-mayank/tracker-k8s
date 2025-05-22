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
/*     */ public class IntellitracProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public IntellitracProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .expression(".+,").optional()
/*  38 */     .number("(d+),")
/*  39 */     .number("(dddd)(dd)(dd)")
/*  40 */     .number("(dd)(dd)(dd),")
/*  41 */     .number("(-?d+.d+),")
/*  42 */     .number("(-?d+.d+),")
/*  43 */     .number("(d+.?d*),")
/*  44 */     .number("(d+.?d*),")
/*  45 */     .number("(-?d+.?d*),")
/*  46 */     .number("(d+),")
/*  47 */     .number("(d+),")
/*  48 */     .number("(d+),")
/*  49 */     .number("(d+),?")
/*  50 */     .number("(d+.d+)?,?")
/*  51 */     .number("(d+.d+)?,?")
/*  52 */     .groupBegin()
/*  53 */     .number("d{14},d+,")
/*  54 */     .number("(d+),")
/*  55 */     .number("(d+),")
/*  56 */     .number("(-?d+),")
/*  57 */     .number("(d+),")
/*  58 */     .number("(d+),")
/*  59 */     .number("(-?d+),")
/*  60 */     .number("(d+),")
/*  61 */     .number("(d+),")
/*  62 */     .number("(d+),")
/*  63 */     .number("(d+)")
/*  64 */     .groupEnd("?")
/*  65 */     .any()
/*  66 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  72 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  73 */     if (!parser.matches()) {
/*  74 */       return null;
/*     */     }
/*     */     
/*  77 */     Position position = new Position(getProtocolName());
/*     */     
/*  79 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  80 */     if (deviceSession == null) {
/*  81 */       return null;
/*     */     }
/*  83 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  85 */     position.setTime(parser.nextDateTime());
/*     */     
/*  87 */     position.setValid(true);
/*  88 */     position.setLongitude(parser.nextDouble().doubleValue());
/*  89 */     position.setLatitude(parser.nextDouble().doubleValue());
/*  90 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/*  91 */     position.setCourse(parser.nextDouble().doubleValue());
/*  92 */     position.setAltitude(parser.nextDouble().doubleValue());
/*     */     
/*  94 */     position.set("sat", parser.nextInt());
/*  95 */     position.set("index", parser.nextLong());
/*  96 */     position.set("input", parser.nextInt());
/*  97 */     position.set("output", parser.nextInt());
/*     */     
/*  99 */     position.set("adc1", parser.nextDouble());
/* 100 */     position.set("adc2", parser.nextDouble());
/*     */ 
/*     */     
/* 103 */     position.set("obdSpeed", parser.nextInt());
/* 104 */     position.set("rpm", parser.nextInt());
/* 105 */     position.set("coolant", parser.nextInt());
/* 106 */     position.set("fuel", parser.nextInt());
/* 107 */     position.set("fuelConsumption", parser.nextInt());
/* 108 */     position.set("temp1", parser.nextInt());
/* 109 */     position.set("chargerPressure", parser.nextInt());
/* 110 */     position.set("tpl", parser.nextInt());
/* 111 */     position.set("axleWeight", parser.nextInt());
/* 112 */     position.set("obdOdometer", parser.nextInt());
/*     */     
/* 114 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\IntellitracProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */