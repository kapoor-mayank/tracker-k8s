/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
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
/*     */ public class WondexProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public WondexProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */   
/*  40 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  41 */     .number("[^d]*")
/*  42 */     .number("(d+),")
/*  43 */     .number("(dddd)(dd)(dd)")
/*  44 */     .number("(dd)(dd)(dd),")
/*  45 */     .number("(-?d+.d+),")
/*  46 */     .number("(-?d+.d+),")
/*  47 */     .number("(d+),")
/*  48 */     .number("(d+),")
/*  49 */     .number("(-?d+.?d*),")
/*  50 */     .number("(d+),")
/*  51 */     .number("(d+),?")
/*  52 */     .number("(d+.d+)V,").optional()
/*  53 */     .number("(d+.d+)?,?")
/*  54 */     .number("(d+)?,?")
/*  55 */     .number("(d+.d+)?,?")
/*  56 */     .number("(d+.d+)?,?")
/*  57 */     .number("(d+)?")
/*  58 */     .any()
/*  59 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  65 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  67 */     if (buf.getUnsignedByte(0) == 208) {
/*     */       
/*  69 */       long deviceId = Long.reverseBytes(buf.getLong(0)) >> 32L & 0xFFFFFFFFL;
/*  70 */       getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(deviceId) });
/*     */       
/*  72 */       return null;
/*  73 */     }  if (buf.toString(StandardCharsets.US_ASCII).startsWith("$OK:") || buf
/*  74 */       .toString(StandardCharsets.US_ASCII).startsWith("$ERR:") || buf
/*  75 */       .toString(StandardCharsets.US_ASCII).startsWith("$MSG:")) {
/*     */       
/*  77 */       DeviceSession deviceSession1 = getDeviceSession(channel, remoteAddress, new String[0]);
/*     */       
/*  79 */       Position position1 = new Position(getProtocolName());
/*  80 */       position1.setDeviceId(deviceSession1.getDeviceId());
/*  81 */       getLastLocation(position1, new Date());
/*  82 */       position1.set("result", buf.toString(StandardCharsets.US_ASCII));
/*     */       
/*  84 */       return position1;
/*     */     } 
/*     */ 
/*     */     
/*  88 */     Parser parser = new Parser(PATTERN, buf.toString(StandardCharsets.US_ASCII));
/*  89 */     if (!parser.matches()) {
/*  90 */       return null;
/*     */     }
/*     */     
/*  93 */     Position position = new Position(getProtocolName());
/*     */     
/*  95 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  96 */     if (deviceSession == null) {
/*  97 */       return null;
/*     */     }
/*  99 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 101 */     position.setTime(parser.nextDateTime());
/*     */     
/* 103 */     position.setLongitude(parser.nextDouble(0.0D));
/* 104 */     position.setLatitude(parser.nextDouble(0.0D));
/* 105 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/* 106 */     position.setCourse(parser.nextDouble(0.0D));
/* 107 */     position.setAltitude(parser.nextDouble(0.0D));
/*     */     
/* 109 */     int satellites = parser.nextInt(0);
/* 110 */     position.setValid((satellites != 0));
/* 111 */     position.set("sat", Integer.valueOf(satellites));
/*     */     
/* 113 */     position.set("event", parser.next());
/* 114 */     position.set("battery", parser.nextDouble());
/* 115 */     if (parser.hasNext()) {
/* 116 */       position.set("odometer", Double.valueOf(parser.nextDouble(0.0D) * 1000.0D));
/*     */     }
/* 118 */     if (parser.hasNext()) {
/* 119 */       int input = parser.nextInt().intValue();
/* 120 */       for (int i = 0; i < 4; i++) {
/* 121 */         position.set("in" + (i + 1), Boolean.valueOf(BitUtil.check(input, i)));
/*     */       }
/*     */     } 
/* 124 */     position.set("adc1", parser.next());
/* 125 */     position.set("adc2", parser.next());
/* 126 */     if (parser.hasNext()) {
/* 127 */       int output = parser.nextInt().intValue();
/* 128 */       for (int i = 0; i < 4; i++) {
/* 129 */         position.set("output" + (i + 1), Boolean.valueOf(BitUtil.check(output, i)));
/*     */       }
/*     */     } 
/*     */     
/* 133 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WondexProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */