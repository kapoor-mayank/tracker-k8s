/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
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
/*     */ public class ArnaviTextProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public ArnaviTextProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*  38 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  39 */     .text("$AV,")
/*  40 */     .number("Vd,")
/*  41 */     .number("(d+),")
/*  42 */     .number("(d+),")
/*  43 */     .number("(d+),")
/*  44 */     .number("(d+),")
/*  45 */     .number("-?d+,")
/*  46 */     .expression("[01],")
/*  47 */     .expression("([01]),")
/*  48 */     .number("(d+),")
/*  49 */     .number("d+,d+,")
/*  50 */     .number("d+,d+,").optional()
/*  51 */     .expression("[01],")
/*  52 */     .number("(d+),")
/*  53 */     .groupBegin()
/*  54 */     .number("(d+.d+)?,")
/*  55 */     .number("(?:d+.d+)?,")
/*  56 */     .groupEnd("?")
/*  57 */     .number("(dd)(dd)(dd),")
/*  58 */     .number("(dd)(dd.d+)([NS]),")
/*  59 */     .number("(ddd)(dd.d+)([EW]),")
/*  60 */     .number("(d+.d+),")
/*  61 */     .number("(d+.d+),")
/*  62 */     .number("(dd)(dd)(dd)")
/*  63 */     .any()
/*  64 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  70 */     ByteBuf buf = (ByteBuf)msg;
/*  71 */     Parser parser = new Parser(PATTERN, buf.toString(StandardCharsets.US_ASCII));
/*  72 */     if (!parser.matches()) {
/*  73 */       return null;
/*     */     }
/*     */     
/*  76 */     Position position = new Position(getProtocolName());
/*     */     
/*  78 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  79 */     if (deviceSession == null) {
/*  80 */       return null;
/*     */     }
/*  82 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  84 */     position.set("index", parser.nextInt());
/*  85 */     position.set("power", Double.valueOf(parser.nextInt().intValue() * 0.01D));
/*  86 */     position.set("battery", Double.valueOf(parser.nextInt().intValue() * 0.01D));
/*  87 */     position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/*  88 */     position.set("input", parser.nextInt());
/*  89 */     position.set("sat", parser.nextInt());
/*     */     
/*  91 */     position.setAltitude(parser.nextDouble(0.0D));
/*     */ 
/*     */     
/*  94 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*     */     
/*  96 */     position.setValid(true);
/*  97 */     position.setLatitude(parser.nextCoordinate());
/*  98 */     position.setLongitude(parser.nextCoordinate());
/*  99 */     position.setSpeed(parser.nextDouble().doubleValue());
/* 100 */     position.setCourse(parser.nextDouble().doubleValue());
/*     */     
/* 102 */     dateBuilder.setDateReverse(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/* 103 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 105 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ArnaviTextProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */