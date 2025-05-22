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
/*     */ 
/*     */ public class EskyProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public EskyProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */   
/*  39 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  40 */     .expression("..;")
/*  41 */     .number("d+;")
/*  42 */     .number("(d+);")
/*  43 */     .text("R;")
/*  44 */     .number("(d+)[+;]")
/*  45 */     .number("(dd)(dd)(dd)")
/*  46 */     .number("(dd)(dd)(dd)[+;]")
/*  47 */     .number("(-?d+.d+)[+;]")
/*  48 */     .number("(-?d+.d+)[+;]")
/*  49 */     .number("(d+.d+)[+;]")
/*  50 */     .number("(d+)[+;]")
/*  51 */     .groupBegin()
/*  52 */     .text("0x").number("(x+)[+;]")
/*  53 */     .number("(d+)[+;]")
/*  54 */     .number("(d+)[+;]")
/*  55 */     .groupEnd("?")
/*  56 */     .number("(d+)[+;]")
/*  57 */     .number("(d+)")
/*  58 */     .any()
/*  59 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  65 */     String sentence = (String)msg;
/*  66 */     Parser parser = new Parser(PATTERN, sentence);
/*  67 */     if (!parser.matches()) {
/*  68 */       return null;
/*     */     }
/*     */     
/*  71 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  72 */     if (deviceSession == null) {
/*  73 */       return null;
/*     */     }
/*     */     
/*  76 */     Position position = new Position(getProtocolName());
/*  77 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  79 */     position.set("sat", parser.nextInt());
/*     */     
/*  81 */     position.setValid(true);
/*  82 */     position.setTime(parser.nextDateTime());
/*  83 */     position.setLatitude(parser.nextDouble().doubleValue());
/*  84 */     position.setLongitude(parser.nextDouble().doubleValue());
/*  85 */     position.setSpeed(UnitsConverter.knotsFromMps(parser.nextDouble().doubleValue()));
/*  86 */     position.setCourse(parser.nextDouble().doubleValue());
/*     */     
/*  88 */     if (parser.hasNext(3)) {
/*  89 */       int input = parser.nextHexInt().intValue();
/*  90 */       position.set("ignition", Boolean.valueOf(!BitUtil.check(input, 0)));
/*  91 */       position.set("in1", Boolean.valueOf(!BitUtil.check(input, 1)));
/*  92 */       position.set("in2", Boolean.valueOf(!BitUtil.check(input, 2)));
/*  93 */       position.set("event", parser.nextInt());
/*  94 */       position.set("odometer", parser.nextInt());
/*     */     } 
/*     */     
/*  97 */     position.set("adc1", parser.nextInt());
/*  98 */     position.set("battery", Double.valueOf(parser.nextInt().intValue() * 0.01D));
/*     */     
/* 100 */     int index = sentence.lastIndexOf('+');
/* 101 */     if (index > 0 && channel instanceof io.netty.channel.socket.DatagramChannel) {
/* 102 */       channel.writeAndFlush(new NetworkMessage("ACK," + sentence.substring(index + 1) + "#", remoteAddress));
/*     */     }
/*     */     
/* 105 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EskyProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */