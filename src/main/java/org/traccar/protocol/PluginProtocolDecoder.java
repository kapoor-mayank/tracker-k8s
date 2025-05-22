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
/*     */ public class PluginProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public PluginProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*  38 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  39 */     .expression("[^0-9,]*,?")
/*  40 */     .number("([^,]+),")
/*  41 */     .number("(dddd)(dd)(dd)")
/*  42 */     .number("(dd)(dd)(dd),")
/*  43 */     .number("(-?d+.d+),")
/*  44 */     .number("(-?d+.d+),")
/*  45 */     .number("(d+.?d*),")
/*  46 */     .number("(d+),")
/*  47 */     .number("(-?d+),")
/*  48 */     .number("(-?d+),")
/*  49 */     .number("d+,")
/*  50 */     .number("(d+.?d*),")
/*  51 */     .number("(d+),")
/*  52 */     .number("(d+.?d*),")
/*  53 */     .expression("[^,]*,")
/*  54 */     .text("0")
/*  55 */     .groupBegin()
/*  56 */     .number(",(-?d+.?d*)")
/*  57 */     .number(",(-?d+.?d*)")
/*  58 */     .number(",d+")
/*  59 */     .number(",(d+)")
/*  60 */     .number(",(d+)")
/*  61 */     .number(",d+")
/*  62 */     .number(",d+")
/*  63 */     .number(",d+")
/*  64 */     .number(",d+")
/*  65 */     .number(",(d+)")
/*  66 */     .number(",(d+)")
/*  67 */     .groupEnd("?")
/*  68 */     .groupBegin()
/*  69 */     .text(",+,")
/*  70 */     .number("(d+),")
/*  71 */     .groupEnd("?")
/*  72 */     .any()
/*  73 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  79 */     if (channel != null) {
/*  80 */       channel.writeAndFlush(new NetworkMessage("$$hb,1#", remoteAddress));
/*     */     }
/*     */     
/*  83 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  84 */     if (!parser.matches()) {
/*  85 */       return null;
/*     */     }
/*     */     
/*  88 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  89 */     if (deviceSession == null) {
/*  90 */       return null;
/*     */     }
/*     */     
/*  93 */     Position position = new Position(getProtocolName());
/*  94 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  96 */     position.setTime(parser.nextDateTime());
/*  97 */     position.setLongitude(parser.nextDouble().doubleValue());
/*  98 */     position.setLatitude(parser.nextDouble().doubleValue());
/*  99 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/* 100 */     position.setCourse(parser.nextInt().intValue());
/* 101 */     position.setAltitude(parser.nextInt().intValue());
/*     */     
/* 103 */     position.set("sat", parser.nextInt());
/* 104 */     position.set("odometer", Long.valueOf((long)(parser.nextDouble().doubleValue() * 1000.0D)));
/*     */     
/* 106 */     long status = parser.nextLong().longValue();
/* 107 */     position.setValid(BitUtil.check(status, 0));
/* 108 */     position.set("ignition", Boolean.valueOf(BitUtil.check(status, 1)));
/* 109 */     for (int i = 0; i < 4; i++) {
/* 110 */       position.set("in" + (i + 1), Boolean.valueOf(BitUtil.check(status, 20 + i)));
/*     */     }
/* 112 */     position.set("status", Long.valueOf(status));
/*     */     
/* 114 */     position.set("fuel", parser.nextDouble());
/*     */     
/* 116 */     if (parser.hasNext(6)) {
/* 117 */       position.set("temp1", parser.nextDouble());
/* 118 */       position.set("temp2", parser.nextDouble());
/* 119 */       position.set("rpm", parser.nextInt());
/* 120 */       position.set("obdSpeed", parser.nextInt());
/* 121 */       position.set("throttle", Double.valueOf(parser.nextInt().intValue() * 0.1D));
/* 122 */       position.set("power", Double.valueOf(parser.nextInt().intValue() * 0.1D));
/*     */     } 
/*     */     
/* 125 */     if (parser.hasNext()) {
/* 126 */       int event = parser.nextInt().intValue();
/* 127 */       switch (event) {
/*     */         case 11317:
/* 129 */           position.set("alarm", "hardAcceleration");
/*     */           break;
/*     */         case 11319:
/* 132 */           position.set("alarm", "hardBraking");
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 138 */       position.set("event", Integer.valueOf(event));
/*     */     } 
/*     */     
/* 141 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PluginProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */