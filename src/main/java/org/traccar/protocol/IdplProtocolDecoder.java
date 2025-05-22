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
/*     */ public class IdplProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public IdplProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .text("*ID")
/*  38 */     .number("(d+),")
/*  39 */     .number("(d+),")
/*  40 */     .number("(dd)(dd)(dd),")
/*  41 */     .number("(dd)(dd)(dd),")
/*  42 */     .expression("([A|V]),")
/*  43 */     .number("(dd)(dd).?(d+),([NS]),")
/*  44 */     .number("(ddd)(dd).?(d+),([EW]),")
/*  45 */     .number("(d{1,3}.dd),")
/*  46 */     .number("(d{1,3}.dd),")
/*  47 */     .number("(d{1,2}),")
/*  48 */     .number("(d{1,3}),")
/*  49 */     .expression("([A|N|S]),")
/*  50 */     .expression("([0|1]),")
/*  51 */     .number("(d.dd),")
/*  52 */     .expression("([0|1]),")
/*  53 */     .expression("([0|1]),")
/*  54 */     .expression("([0|1])([0|1]),")
/*  55 */     .expression("([0|1|2]),")
/*  56 */     .number("(d{1,3}),")
/*  57 */     .number("(d{1,3}),")
/*  58 */     .expression("([0-9A-Z]{3}),")
/*  59 */     .expression("([L|R]),")
/*  60 */     .number("(x{4})#")
/*  61 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  67 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  68 */     if (!parser.matches()) {
/*  69 */       return null;
/*     */     }
/*     */     
/*  72 */     Position position = new Position(getProtocolName());
/*     */     
/*  74 */     position.set("type", Integer.valueOf(parser.nextInt(0)));
/*     */     
/*  76 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  77 */     if (deviceSession == null) {
/*  78 */       return null;
/*     */     }
/*  80 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  82 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/*  84 */     position.setValid(parser.next().equals("A"));
/*  85 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN_HEM));
/*  86 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN_HEM));
/*  87 */     position.setSpeed(parser.nextDouble(0.0D));
/*  88 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/*  90 */     position.set("sat", Integer.valueOf(parser.nextInt(0)));
/*  91 */     position.set("rssi", Integer.valueOf(parser.nextInt(0)));
/*  92 */     position.set("vehicleStatus", parser.next());
/*  93 */     position.set("power", Integer.valueOf(parser.nextInt(0)));
/*  94 */     position.set("battery", Double.valueOf(parser.nextDouble(0.0D)));
/*  95 */     if (parser.nextInt(0) == 1) {
/*  96 */       position.set("alarm", "sos");
/*     */     }
/*  98 */     parser.nextInt(0);
/*  99 */     position.set("acStatus", Integer.valueOf(parser.nextInt(0)));
/* 100 */     position.set("ignition", Boolean.valueOf((parser.nextInt(0) == 1)));
/* 101 */     position.set("output", Integer.valueOf(parser.nextInt(0)));
/* 102 */     position.set("adc1", Integer.valueOf(parser.nextInt(0)));
/* 103 */     position.set("adc2", Integer.valueOf(parser.nextInt(0)));
/* 104 */     position.set("versionFw", parser.next());
/* 105 */     position.set("archive", Boolean.valueOf(parser.next().equals("R")));
/*     */     
/* 107 */     parser.next();
/*     */     
/* 109 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\IdplProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */