/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
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
/*     */ public class FlextrackProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public FlextrackProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */   
/*  39 */   private static final Pattern PATTERN_LOGON = (new PatternBuilder())
/*  40 */     .number("(-?d+),")
/*  41 */     .text("LOGON,")
/*  42 */     .number("(d+),")
/*  43 */     .number("(d+)")
/*  44 */     .compile();
/*     */   
/*  46 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  47 */     .number("(-?d+),")
/*  48 */     .text("UNITSTAT,")
/*  49 */     .number("(dddd)(dd)(dd),")
/*  50 */     .number("(dd)(dd)(dd),")
/*  51 */     .number("d+,")
/*  52 */     .number("([NS])(d+).(d+.d+),")
/*  53 */     .number("([EW])(d+).(d+.d+),")
/*  54 */     .number("(d+),")
/*  55 */     .number("(d+),")
/*  56 */     .number("(d+),")
/*  57 */     .number("(d+),")
/*  58 */     .number("(-?d+),")
/*  59 */     .number("(x+),")
/*  60 */     .number("(ddd)")
/*  61 */     .number("(dd),")
/*  62 */     .number("(-?d+),")
/*  63 */     .number("(d+),")
/*  64 */     .number("(x+),")
/*  65 */     .number("d+,")
/*  66 */     .number("(x+),")
/*  67 */     .number("(d+)")
/*  68 */     .compile();
/*     */   
/*     */   private void sendAcknowledgement(Channel channel, SocketAddress remoteAddress, String index) {
/*  71 */     if (channel != null) {
/*  72 */       channel.writeAndFlush(new NetworkMessage(index + ",ACK\r", remoteAddress));
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  80 */     String sentence = (String)msg;
/*     */     
/*  82 */     if (sentence.contains("LOGON")) {
/*     */       
/*  84 */       Parser parser = new Parser(PATTERN_LOGON, sentence);
/*  85 */       if (!parser.matches()) {
/*  86 */         return null;
/*     */       }
/*     */       
/*  89 */       sendAcknowledgement(channel, remoteAddress, parser.next());
/*     */       
/*  91 */       String id = parser.next();
/*  92 */       String iccid = parser.next();
/*     */       
/*  94 */       getDeviceSession(channel, remoteAddress, new String[] { iccid, id });
/*     */     }
/*  96 */     else if (sentence.contains("UNITSTAT")) {
/*     */       
/*  98 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  99 */       if (deviceSession == null) {
/* 100 */         return null;
/*     */       }
/*     */       
/* 103 */       Parser parser = new Parser(PATTERN, sentence);
/* 104 */       if (!parser.matches()) {
/* 105 */         return null;
/*     */       }
/*     */       
/* 108 */       Position position = new Position(getProtocolName());
/* 109 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 111 */       sendAcknowledgement(channel, remoteAddress, parser.next());
/*     */       
/* 113 */       position.setTime(parser.nextDateTime());
/*     */       
/* 115 */       position.setValid(true);
/* 116 */       position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 117 */       position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 118 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt(0)));
/* 119 */       position.setCourse(parser.nextInt(0));
/*     */       
/* 121 */       position.set("sat", Integer.valueOf(parser.nextInt(0)));
/* 122 */       position.set("battery", Integer.valueOf(parser.nextInt(0)));
/* 123 */       int rssi = parser.nextInt(0);
/* 124 */       position.set("status", Integer.valueOf(parser.nextHexInt(0)));
/*     */       
/* 126 */       int mcc = parser.nextInt(0);
/* 127 */       int mnc = parser.nextInt(0);
/*     */       
/* 129 */       position.setAltitude(parser.nextInt(0));
/*     */       
/* 131 */       position.set("hdop", Double.valueOf(parser.nextInt(0) * 0.1D));
/*     */       
/* 133 */       position.setNetwork(new Network(CellTower.from(mcc, mnc, parser
/* 134 */               .nextHexInt(0), parser.nextHexInt(0), rssi)));
/*     */       
/* 136 */       position.set("odometer", Integer.valueOf(parser.nextInt(0)));
/*     */       
/* 138 */       return position;
/*     */     } 
/*     */     
/* 141 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FlextrackProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */