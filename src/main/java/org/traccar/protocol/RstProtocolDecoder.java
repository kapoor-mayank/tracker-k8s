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
/*     */ public class RstProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public RstProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*  38 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  39 */     .text("RST;")
/*  40 */     .expression("([AL]);")
/*  41 */     .expression("([^,]+);")
/*  42 */     .expression("(.{5});")
/*  43 */     .number("(d{9});")
/*  44 */     .number("(d+);")
/*  45 */     .number("(d+);")
/*  46 */     .groupBegin()
/*  47 */     .number("(dd)-(dd)-(dddd) ")
/*  48 */     .number("(dd):(dd):(dd);")
/*  49 */     .number("(dd)-(dd)-(dddd) ")
/*  50 */     .number("(dd):(dd):(dd);")
/*  51 */     .number("(-?d+.d+);")
/*  52 */     .number("(-?d+.d+);")
/*  53 */     .number("(d+);")
/*  54 */     .number("(d+);")
/*  55 */     .number("(-?d+);")
/*  56 */     .number("([01]);")
/*  57 */     .number("(d+);")
/*  58 */     .number("(d+);")
/*  59 */     .number("(xx);")
/*  60 */     .number("(xx);")
/*  61 */     .number("(xx);")
/*  62 */     .number("(xx);")
/*  63 */     .number("(xx);")
/*  64 */     .number("(d+.d+);")
/*  65 */     .number("(d+.d+);")
/*  66 */     .number("(d+);")
/*  67 */     .number("(d+);")
/*  68 */     .number("(xx);")
/*  69 */     .number("x{4};")
/*  70 */     .number("(xx);")
/*  71 */     .number("(xx);")
/*  72 */     .groupEnd("?")
/*  73 */     .any()
/*  74 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  80 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  81 */     if (!parser.matches()) {
/*  82 */       return null;
/*     */     }
/*     */     
/*  85 */     parser.next();
/*  86 */     String model = parser.next();
/*  87 */     String firmware = parser.next();
/*  88 */     String serial = parser.next();
/*  89 */     int index = parser.nextInt().intValue();
/*  90 */     int type = parser.nextInt().intValue();
/*     */     
/*  92 */     if (channel != null) {
/*  93 */       String response = "RST;A;" + model + ";" + firmware + ";" + serial + ";" + index + ";6;FIM;";
/*  94 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */     
/*  97 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { serial });
/*  98 */     if (deviceSession == null) {
/*  99 */       return null;
/*     */     }
/*     */     
/* 102 */     if (parser.hasNext()) {
/*     */       
/* 104 */       Position position = new Position(getProtocolName());
/* 105 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 107 */       position.setDeviceTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/* 108 */       position.setFixTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/* 109 */       position.setLatitude(parser.nextDouble().doubleValue());
/* 110 */       position.setLongitude(parser.nextDouble().doubleValue());
/* 111 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/* 112 */       position.setCourse(parser.nextInt().intValue());
/* 113 */       position.setAltitude(parser.nextInt().intValue());
/* 114 */       position.setValid((parser.nextInt().intValue() > 0));
/*     */       
/* 116 */       switch (type) {
/*     */         case 3:
/* 118 */           position.set("alarm", "ignitionOn");
/*     */           break;
/*     */         case 4:
/* 121 */           position.set("alarm", "ignitionOff");
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 127 */       position.set("sat", parser.nextInt());
/* 128 */       position.set("hdop", parser.nextInt());
/* 129 */       position.set("in1", parser.nextHexInt());
/* 130 */       position.set("in2", parser.nextHexInt());
/* 131 */       position.set("in3", parser.nextHexInt());
/* 132 */       position.set("out1", parser.nextHexInt());
/* 133 */       position.set("out2", parser.nextHexInt());
/* 134 */       position.set("power", parser.nextDouble());
/* 135 */       position.set("battery", parser.nextDouble());
/* 136 */       position.set("odometer", parser.nextInt());
/* 137 */       position.set("rssi", parser.nextInt());
/* 138 */       position.set("temp1", Integer.valueOf(parser.nextHexInt().byteValue()));
/*     */       
/* 140 */       int status = (parser.nextHexInt().intValue() << 8) + parser.nextHexInt().intValue();
/* 141 */       position.set("ignition", Boolean.valueOf(BitUtil.check(status, 7)));
/* 142 */       position.set("status", Integer.valueOf(status));
/*     */       
/* 144 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 148 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\RstProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */