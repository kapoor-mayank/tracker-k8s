/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
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
/*     */ public class XexunProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private final boolean full;
/*     */   
/*     */   public XexunProtocolDecoder(Protocol protocol, boolean full) {
/*  35 */     super(protocol);
/*  36 */     this.full = full;
/*     */   }
/*     */   
/*  39 */   private static final Pattern PATTERN_BASIC = (new PatternBuilder())
/*  40 */     .expression("G[PN]RMC,")
/*  41 */     .number("(?:(dd)(dd)(dd))?.?d*,")
/*  42 */     .expression("([AV]),")
/*  43 */     .number("(d*?)(d?d.d+),([NS]),")
/*  44 */     .number("(d*?)(d?d.d+),([EW])?,")
/*  45 */     .number("(d+.?d*),")
/*  46 */     .number("(d+.?d*)?,")
/*  47 */     .number("(?:(dd)(dd)(dd))?,")
/*  48 */     .expression("[^*]*").text("*")
/*  49 */     .number("xx")
/*  50 */     .expression("\\r\\n").optional()
/*  51 */     .expression(",([FL]),")
/*  52 */     .expression("([^,]*),").optional()
/*  53 */     .any()
/*  54 */     .number("imei:(d+),")
/*  55 */     .compile();
/*     */   
/*  57 */   private static final Pattern PATTERN_FULL = (new PatternBuilder())
/*  58 */     .any()
/*  59 */     .number("(d+),")
/*  60 */     .expression("([^,]+)?,")
/*  61 */     .expression(PATTERN_BASIC.pattern())
/*  62 */     .number("(d+),")
/*  63 */     .number("(-?d+.d+)?,")
/*  64 */     .number("[FL]:(d+.d+)V")
/*  65 */     .any()
/*  66 */     .compile();
/*     */   
/*     */   private String decodeStatus(Position position, String value) {
/*  69 */     if (value != null) {
/*  70 */       switch (value.toLowerCase()) {
/*     */         case "acc on":
/*     */         case "accstart":
/*  73 */           position.set("ignition", Boolean.valueOf(true));
/*     */           break;
/*     */         case "acc off":
/*     */         case "accstop":
/*  77 */           position.set("ignition", Boolean.valueOf(false));
/*     */           break;
/*     */         case "help me!":
/*  80 */           position.set("alarm", "sos");
/*     */           break;
/*     */         case "low battery":
/*  83 */           position.set("alarm", "lowBattery");
/*     */           break;
/*     */         case "move!":
/*     */         case "moved!":
/*  87 */           position.set("alarm", "movement");
/*     */           break;
/*     */       } 
/*     */ 
/*     */     
/*     */     }
/*  93 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 100 */     Pattern pattern = PATTERN_BASIC;
/* 101 */     if (this.full) {
/* 102 */       pattern = PATTERN_FULL;
/*     */     }
/*     */     
/* 105 */     Parser parser = new Parser(pattern, (String)msg);
/* 106 */     if (!parser.matches()) {
/* 107 */       return null;
/*     */     }
/*     */     
/* 110 */     Position position = new Position(getProtocolName());
/*     */     
/* 112 */     if (this.full) {
/* 113 */       position.set("serial", parser.next());
/* 114 */       position.set("number", parser.next());
/*     */     } 
/*     */ 
/*     */     
/* 118 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 120 */     position.setValid(parser.next().equals("A"));
/* 121 */     position.setLatitude(parser.nextCoordinate());
/* 122 */     position.setLongitude(parser.nextCoordinate());
/*     */     
/* 124 */     position.setSpeed(convertSpeed(parser.nextDouble(0.0D), "kn"));
/*     */     
/* 126 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 128 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 129 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 131 */     position.set("signal", parser.next());
/*     */     
/* 133 */     decodeStatus(position, parser.next());
/*     */     
/* 135 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 136 */     if (deviceSession == null) {
/* 137 */       return null;
/*     */     }
/* 139 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 141 */     if (this.full) {
/* 142 */       position.set("sat", parser.nextInt());
/*     */       
/* 144 */       position.setAltitude(parser.nextDouble(0.0D));
/*     */       
/* 146 */       position.set("power", Double.valueOf(parser.nextDouble(0.0D)));
/*     */     } 
/*     */     
/* 149 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\XexunProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */