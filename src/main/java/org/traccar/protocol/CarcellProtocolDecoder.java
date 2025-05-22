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
/*     */ 
/*     */ public class CarcellProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public CarcellProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  38 */     .expression("([$%])")
/*  39 */     .number("(d+),")
/*  40 */     .groupBegin()
/*  41 */     .number("([NS])(dd)(dd).(dddd),")
/*  42 */     .number("([EW])(ddd)(dd).(dddd),")
/*  43 */     .or()
/*  44 */     .text("CEL,")
/*  45 */     .number("([NS])(d+.d+),")
/*  46 */     .number("([EW])(d+.d+),")
/*  47 */     .groupEnd()
/*  48 */     .number("(d+),")
/*  49 */     .number("(d+),")
/*  50 */     .groupBegin()
/*  51 */     .number("([-+]ddd)([-+]ddd)([-+]ddd),")
/*  52 */     .or()
/*  53 */     .number("(d+),")
/*  54 */     .groupEnd()
/*  55 */     .number("(d+),")
/*  56 */     .number("(d+),")
/*  57 */     .number("(d),")
/*  58 */     .number("(d+),")
/*  59 */     .expression("([CG]),?")
/*  60 */     .number("(dd)(dd)(dd),")
/*  61 */     .number("(dd)(dd)(dd),")
/*  62 */     .number("(d),")
/*  63 */     .number("(d),")
/*  64 */     .groupBegin()
/*  65 */     .number("(d),")
/*  66 */     .expression("([AF])")
/*  67 */     .number("(d),")
/*  68 */     .number("(d+),")
/*  69 */     .or()
/*  70 */     .number("(dd),")
/*  71 */     .expression("([AF])")
/*  72 */     .number("(d),")
/*  73 */     .number("(d{2,4}),")
/*  74 */     .number("(d{20}),")
/*  75 */     .groupEnd()
/*  76 */     .number("(xx)")
/*  77 */     .any()
/*  78 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  84 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  85 */     if (!parser.matches()) {
/*  86 */       return null;
/*     */     }
/*     */     
/*  89 */     Position position = new Position(getProtocolName());
/*  90 */     position.set("archive", Boolean.valueOf(parser.next().equals("%")));
/*  91 */     position.setValid(true);
/*     */     
/*  93 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  94 */     if (deviceSession == null) {
/*  95 */       return null;
/*     */     }
/*  97 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  99 */     if (parser.hasNext(8)) {
/* 100 */       position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_MIN));
/* 101 */       position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_MIN));
/*     */     } 
/*     */     
/* 104 */     if (parser.hasNext(4)) {
/* 105 */       position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
/* 106 */       position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
/*     */     } 
/*     */     
/* 109 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt(0)));
/* 110 */     position.setCourse(parser.nextInt(0));
/*     */     
/* 112 */     if (parser.hasNext(3)) {
/* 113 */       position.set("x", Integer.valueOf(parser.nextInt(0)));
/* 114 */       position.set("y", Integer.valueOf(parser.nextInt(0)));
/* 115 */       position.set("z", Integer.valueOf(parser.nextInt(0)));
/*     */     } 
/*     */     
/* 118 */     if (parser.hasNext(1)) {
/* 119 */       position.set("acceleration", Integer.valueOf(parser.nextInt(0)));
/*     */     }
/*     */     
/* 122 */     Double internalBattery = Double.valueOf((parser.nextDouble(0.0D) + 100.0D) * 0.0294D);
/* 123 */     position.set("battery", internalBattery);
/* 124 */     position.set("rssi", Integer.valueOf(parser.nextInt(0)));
/* 125 */     position.set("jamming", Boolean.valueOf(parser.next().equals("1")));
/* 126 */     position.set("gps", Integer.valueOf(parser.nextInt(0)));
/*     */     
/* 128 */     position.set("clockType", parser.next());
/*     */     
/* 130 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 132 */     position.set("blocked", Boolean.valueOf(parser.next().equals("1")));
/* 133 */     position.set("ignition", Boolean.valueOf(parser.next().equals("1")));
/*     */     
/* 135 */     if (parser.hasNext(4)) {
/* 136 */       position.set("cloned", Boolean.valueOf(parser.next().equals("1")));
/*     */       
/* 138 */       parser.next();
/*     */       
/* 140 */       String painelStatus = parser.next();
/* 141 */       if (painelStatus.equals("1")) {
/* 142 */         position.set("alarm", "general");
/*     */       }
/* 144 */       position.set("painel", Boolean.valueOf(painelStatus.equals("2")));
/*     */       
/* 146 */       Double mainVoltage = Double.valueOf(parser.nextDouble(0.0D) / 100.0D);
/* 147 */       position.set("power", mainVoltage);
/*     */     } 
/*     */     
/* 150 */     if (parser.hasNext(5)) {
/* 151 */       position.set("timeUntilDelivery", Integer.valueOf(parser.nextInt(0)));
/* 152 */       parser.next();
/* 153 */       position.set("input", parser.next());
/*     */       
/* 155 */       Double mainVoltage = Double.valueOf(parser.nextDouble(0.0D) / 100.0D);
/* 156 */       position.set("power", mainVoltage);
/*     */       
/* 158 */       position.set("iccid", parser.next());
/*     */     } 
/*     */     
/* 161 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CarcellProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */