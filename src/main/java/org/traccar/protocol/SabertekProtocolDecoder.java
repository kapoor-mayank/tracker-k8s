///*     */ package org.traccar.protocol;
///*     */
///*     */ import io.netty.buffer.Unpooled;
///*     */ import io.netty.channel.Channel;
///*     */ import java.net.SocketAddress;
///*     */ import java.util.Date;
///*     */ import java.util.regex.Pattern;
///*     */ import org.traccar.BaseProtocolDecoder;
///*     */ import org.traccar.DeviceSession;
///*     */ import org.traccar.NetworkMessage;
///*     */ import org.traccar.Protocol;
///*     */ import org.traccar.helper.BitUtil;
///*     */ import org.traccar.helper.Parser;
///*     */ import org.traccar.helper.PatternBuilder;
///*     */ import org.traccar.helper.UnitsConverter;
///*     */ import org.traccar.model.Position;
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */ public class SabertekProtocolDecoder
///*     */   extends BaseProtocolDecoder
///*     */ {
///*     */   public SabertekProtocolDecoder(Protocol protocol) {
///*  37 */     super(protocol);
///*     */   }
///*     */
///*  40 */   private static final Pattern PATTERN = (new PatternBuilder())
///*  41 */     .text(",")
///*  42 */     .number("(d+),")
///*  43 */     .number("d,")
///*  44 */     .groupBegin()
///*  45 */     .number("d+,")
///*  46 */     .number("d+,")
///*  47 */     .expression("[^,]*,")
///*  48 */     .number("(dddd)(dd)(dd)")
///*  49 */     .number("(dd)(dd)(dd),")
///*  50 */     .groupEnd("?")
///*  51 */     .number("(d+),")
///*  52 */     .number("(d+),")
///*  53 */     .number("(d+),")
///*  54 */     .number("(d+),")
///*  55 */     .number("(d),")
///*  56 */     .number("(-?d+.d+),")
///*  57 */     .number("(-?d+.d+),")
///*  58 */     .number("(d+),")
///*  59 */     .number("(d+),")
///*  60 */     .number("(d+),")
///*  61 */     .number("(d+),")
///*  62 */     .number("(d+),")
///*  63 */     .compile();
///*     */
///*     */
///*     */
///*     */
///*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
///*  69 */     Parser parser = new Parser(PATTERN, (String)msg);
///*  70 */     if (!parser.matches()) {
///*  71 */       return null;
///*     */     }
///*     */
///*  74 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
///*  75 */     if (channel != null) {
///*  76 */       channel.writeAndFlush(new NetworkMessage(
///*  77 */             Unpooled.wrappedBuffer(new byte[] { (byte)((deviceSession != null) ? 6 : 21) }, ), remoteAddress));
///*     */     }
///*  79 */     if (deviceSession == null) {
///*  80 */       return null;
///*     */     }
///*     */
///*  83 */     Position position = new Position(getProtocolName());
///*  84 */     position.setDeviceId(deviceSession.getDeviceId());
///*     */
///*  86 */     if (parser.hasNext(6)) {
///*  87 */       position.setTime(parser.nextDateTime());
///*     */     } else {
///*  89 */       position.setTime(new Date());
///*     */     }
///*     */
///*  92 */     position.set("batteryLevel", parser.nextInt());
///*  93 */     position.set("rssi", parser.nextInt());
///*     */
///*  95 */     int state = parser.nextInt().intValue();
///*     */
///*  97 */     position.set("ignition", Boolean.valueOf(BitUtil.check(state, 0)));
///*  98 */     position.set("charge", Boolean.valueOf(BitUtil.check(state, 1)));
///*     */
///* 100 */     if (BitUtil.check(state, 2)) {
///* 101 */       position.set("alarm", "jamming");
///*     */     }
///* 103 */     if (BitUtil.check(state, 3)) {
///* 104 */       position.set("alarm", "tampering");
///*     */     }
///*     */
///* 107 */     int events = parser.nextInt().intValue();
///*     */
///* 109 */     if (BitUtil.check(events, 0)) {
///* 110 */       position.set("alarm", "hardBraking");
///*     */     }
///* 112 */     if (BitUtil.check(events, 1)) {
///* 113 */       position.set("alarm", "overspeed");
///*     */     }
///* 115 */     if (BitUtil.check(events, 2)) {
///* 116 */       position.set("alarm", "accident");
///*     */     }
///* 118 */     if (BitUtil.check(events, 3)) {
///* 119 */       position.set("alarm", "hardCornering");
///*     */     }
///*     */
///* 122 */     position.setValid((parser.nextInt().intValue() == 1));
///* 123 */     position.setLatitude(parser.nextDouble().doubleValue());
///* 124 */     position.setLongitude(parser.nextDouble().doubleValue());
///* 125 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
///* 126 */     position.setCourse(parser.nextInt().intValue());
///* 127 */     position.setAltitude(parser.nextInt().intValue());
///*     */
///* 129 */     position.set("sat", parser.nextInt());
///* 130 */     position.set("odometer", Long.valueOf(parser.nextInt().intValue() * 1000L));
///*     */
///* 132 */     return position;
///*     */   }
///*     */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SabertekProtocolDecoder.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */