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
/*     */ public class DwayProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public DwayProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  38 */     .text("AA55,")
/*  39 */     .number("d+,")
/*  40 */     .number("(d+),")
/*  41 */     .number("d+,")
/*  42 */     .number("(dd)(dd)(dd),")
/*  43 */     .number("(dd)(dd)(dd),")
/*  44 */     .number("(-?d+.d+),")
/*  45 */     .number("(-?d+.d+),")
/*  46 */     .number("(-?d+),")
/*  47 */     .number(" ?(d+.d+),")
/*  48 */     .number("(d+),")
/*  49 */     .number("([01]{4}),")
/*  50 */     .number("([01]{4}),")
/*  51 */     .number("([01]+),")
/*  52 */     .number("(d+),")
/*  53 */     .number("(d+),")
/*  54 */     .number("(d+),")
/*  55 */     .number("(d+)")
/*  56 */     .any()
/*  57 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  63 */     String sentence = (String)msg;
/*  64 */     if (sentence.equals("AA55,HB")) {
/*  65 */       if (channel != null) {
/*  66 */         channel.writeAndFlush(new NetworkMessage("55AA,HB,OK\r\n", remoteAddress));
/*     */       }
/*  68 */       return null;
/*     */     } 
/*     */     
/*  71 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  72 */     if (!parser.matches()) {
/*  73 */       return null;
/*     */     }
/*     */     
/*  76 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  77 */     if (deviceSession == null) {
/*  78 */       return null;
/*     */     }
/*     */     
/*  81 */     Position position = new Position(getProtocolName());
/*  82 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  84 */     position.setValid(true);
/*  85 */     position.setTime(parser.nextDateTime());
/*  86 */     position.setLatitude(parser.nextDouble().doubleValue());
/*  87 */     position.setLongitude(parser.nextDouble().doubleValue());
/*  88 */     position.setAltitude(parser.nextDouble(0.0D));
/*  89 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/*  90 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/*  92 */     position.set("input", parser.nextBinInt());
/*  93 */     position.set("output", parser.nextBinInt());
/*     */     
/*  95 */     position.set("battery", Double.valueOf(parser.nextInt().intValue() * 0.001D));
/*  96 */     position.set("adc1", Double.valueOf(parser.nextInt().intValue() * 0.001D));
/*  97 */     position.set("adc2", Double.valueOf(parser.nextInt().intValue() * 0.001D));
/*  98 */     position.set("driverUniqueId", parser.next());
/*     */     
/* 100 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\DwayProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */