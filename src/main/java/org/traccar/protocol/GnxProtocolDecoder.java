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
/*     */ public class GnxProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public GnxProtocolDecoder(Protocol protocol) {
/*  32 */     super(protocol);
/*     */   }
/*     */   
/*  35 */   private static final Pattern PATTERN_LOCATION = (new PatternBuilder())
/*  36 */     .number("(d+),")
/*  37 */     .number("d+,")
/*  38 */     .expression("([01]),")
/*  39 */     .number("(dd)(dd)(dd),")
/*  40 */     .number("(dd)(dd)(dd),")
/*  41 */     .number("(dd)(dd)(dd),")
/*  42 */     .number("(dd)(dd)(dd),")
/*  43 */     .number("(d),")
/*  44 */     .number("(dd.d+),")
/*  45 */     .expression("([NS]),")
/*  46 */     .number("(ddd.d+),")
/*  47 */     .expression("([EW]),")
/*  48 */     .compile();
/*     */   
/*  50 */   private static final Pattern PATTERN_MIF = (new PatternBuilder())
/*  51 */     .text("$GNX_MIF,")
/*  52 */     .expression(PATTERN_LOCATION.pattern())
/*  53 */     .expression("[01],")
/*  54 */     .expression("([^,]+),")
/*  55 */     .any()
/*  56 */     .compile();
/*     */   
/*  58 */   private static final Pattern PATTERN_OTHER = (new PatternBuilder())
/*  59 */     .text("$GNX_")
/*  60 */     .expression("...,")
/*  61 */     .expression(PATTERN_LOCATION.pattern())
/*  62 */     .any()
/*  63 */     .compile();
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*     */     Pattern pattern;
/*  69 */     String sentence = (String)msg;
/*  70 */     String type = sentence.substring(5, 8);
/*     */ 
/*     */     
/*  73 */     if (type.equals("MIF")) {
/*  74 */       pattern = PATTERN_MIF;
/*     */     } else {
/*  76 */       pattern = PATTERN_OTHER;
/*     */     } 
/*     */     
/*  79 */     Parser parser = new Parser(pattern, sentence);
/*  80 */     if (!parser.matches()) {
/*  81 */       return null;
/*     */     }
/*     */     
/*  84 */     Position position = new Position(getProtocolName());
/*     */     
/*  86 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  87 */     if (deviceSession == null) {
/*  88 */       return null;
/*     */     }
/*  90 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  92 */     if (parser.nextInt(0) == 1) {
/*  93 */       position.set("archive", Boolean.valueOf(true));
/*     */     }
/*     */     
/*  96 */     position.setDeviceTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY, "GMT+5:30"));
/*  97 */     position.setFixTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY, "GMT+5:30"));
/*     */     
/*  99 */     position.setValid((parser.nextInt(0) != 0));
/*     */     
/* 101 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 102 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/*     */     
/* 104 */     if (type.equals("MIF")) {
/* 105 */       position.set("driverUniqueId", parser.next());
/*     */     }
/*     */     
/* 108 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GnxProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */