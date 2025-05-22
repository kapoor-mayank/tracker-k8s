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
/*     */ public class Stl060ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public Stl060ProtocolDecoder(Protocol protocol) {
/*  32 */     super(protocol);
/*     */   }
/*     */   
/*  35 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  36 */     .any()
/*  37 */     .text("$1,")
/*  38 */     .number("(d+),")
/*  39 */     .text("D001,")
/*  40 */     .expression("[^,]*,")
/*  41 */     .number("(dd)/(dd)/(dd),")
/*  42 */     .number("(dd):(dd):(dd),")
/*  43 */     .number("(dd)(dd).?(d+)([NS]),")
/*  44 */     .number("(ddd)(dd).?(d+)([EW]),")
/*  45 */     .number("(d+.?d*),")
/*  46 */     .number("(d+.?d*),")
/*  47 */     .groupBegin()
/*  48 */     .number("(d+),")
/*  49 */     .number("(d+),")
/*  50 */     .number("(d+),")
/*  51 */     .number("(d+),")
/*  52 */     .number("(d+),")
/*  53 */     .or()
/*  54 */     .expression("([01]),")
/*  55 */     .expression("([01]),")
/*  56 */     .expression("0,0,")
/*  57 */     .number("(d+),")
/*  58 */     .expression("([^,]+),")
/*  59 */     .number("(d+),")
/*  60 */     .number("(d+),")
/*  61 */     .number("(d+),")
/*  62 */     .expression("([01]),")
/*  63 */     .expression("([01]),")
/*  64 */     .expression("([01]),")
/*  65 */     .groupEnd()
/*  66 */     .expression("([AV])")
/*  67 */     .any()
/*  68 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  74 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  75 */     if (!parser.matches()) {
/*  76 */       return null;
/*     */     }
/*     */     
/*  79 */     Position position = new Position(getProtocolName());
/*     */     
/*  81 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  82 */     if (deviceSession == null) {
/*  83 */       return null;
/*     */     }
/*  85 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  87 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/*  89 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN_HEM));
/*  90 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN_HEM));
/*  91 */     position.setSpeed(parser.nextDouble(0.0D));
/*  92 */     position.setCourse(parser.nextDouble(0.0D));
/*     */ 
/*     */     
/*  95 */     if (parser.hasNext(5)) {
/*  96 */       position.set("odometer", Integer.valueOf(parser.nextInt(0)));
/*  97 */       position.set("ignition", Boolean.valueOf((parser.nextInt(0) == 1)));
/*  98 */       position.set("input", Integer.valueOf(parser.nextInt(0) + parser.nextInt(0) << 1));
/*  99 */       position.set("fuel", Integer.valueOf(parser.nextInt(0)));
/*     */     } 
/*     */ 
/*     */     
/* 103 */     if (parser.hasNext(10)) {
/* 104 */       position.set("charge", Boolean.valueOf((parser.nextInt(0) == 1)));
/* 105 */       position.set("ignition", Boolean.valueOf((parser.nextInt(0) == 1)));
/* 106 */       position.set("input", Integer.valueOf(parser.nextInt(0)));
/* 107 */       position.set("driverUniqueId", parser.next());
/* 108 */       position.set("odometer", Integer.valueOf(parser.nextInt(0)));
/* 109 */       position.set("temp1", Integer.valueOf(parser.nextInt(0)));
/* 110 */       position.set("fuel", Integer.valueOf(parser.nextInt(0)));
/* 111 */       position.set("acceleration", Boolean.valueOf((parser.nextInt(0) == 1)));
/* 112 */       position.set("output", Integer.valueOf(parser.nextInt(0) + parser.nextInt(0) << 1));
/*     */     } 
/*     */     
/* 115 */     position.setValid(parser.next().equals("A"));
/*     */     
/* 117 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Stl060ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */