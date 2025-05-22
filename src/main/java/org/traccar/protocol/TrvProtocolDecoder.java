/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
/*     */ import org.traccar.model.Position;
/*     */ import org.traccar.model.WifiAccessPoint;
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
/*     */ public class TrvProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TrvProtocolDecoder(Protocol protocol) {
/*  40 */     super(protocol);
/*     */   }
/*     */   
/*  43 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  44 */     .expression("[A-Z]{2,3}")
/*  45 */     .expression("[A-Z]P")
/*  46 */     .number("dd")
/*  47 */     .number("(dd)(dd)(dd)")
/*  48 */     .expression("([AV])")
/*  49 */     .number("(dd)(dd.d+)")
/*  50 */     .expression("([NS])")
/*  51 */     .number("(ddd)(dd.d+)")
/*  52 */     .expression("([EW])")
/*  53 */     .number("(ddd.d)")
/*  54 */     .number("(dd)(dd)(dd)")
/*  55 */     .number("([d.]{6})")
/*  56 */     .number("(ddd)")
/*  57 */     .number("(ddd)")
/*  58 */     .number("(ddd)")
/*  59 */     .number("(d)")
/*  60 */     .number("(dd)")
/*  61 */     .number("(dd)")
/*  62 */     .number("(?:d{3,5})?,")
/*  63 */     .number("(d+),")
/*  64 */     .number("(d+),")
/*  65 */     .number("(d+),")
/*  66 */     .number("(d+)")
/*  67 */     .groupBegin()
/*  68 */     .text(",")
/*  69 */     .expression("(")
/*  70 */     .groupBegin()
/*  71 */     .expression("[^\\|]+")
/*  72 */     .number("|xx-xx-xx-xx-xx-xx")
/*  73 */     .number("|d+&?")
/*  74 */     .groupEnd("+")
/*  75 */     .expression(")")
/*  76 */     .groupEnd("?")
/*  77 */     .any()
/*  78 */     .compile();
/*     */   
/*  80 */   private static final Pattern PATTERN_HEARTBEAT = (new PatternBuilder())
/*  81 */     .expression("[A-Z]{2,3}")
/*  82 */     .text("CP01,")
/*  83 */     .number("(ddd)")
/*  84 */     .number("(ddd)")
/*  85 */     .number("(ddd)")
/*  86 */     .number("(d)")
/*  87 */     .number("(dd)")
/*  88 */     .number("(dd)")
/*  89 */     .groupBegin()
/*  90 */     .number("(ddd)")
/*  91 */     .number("d")
/*  92 */     .number("ddd")
/*  93 */     .number("d")
/*  94 */     .number("dddd")
/*  95 */     .number("(d)")
/*  96 */     .number("(d)")
/*  97 */     .number("(d)")
/*  98 */     .groupEnd("?")
/*  99 */     .any()
/* 100 */     .compile();
/*     */   
/* 102 */   private static final Pattern PATTERN_LBS = (new PatternBuilder())
/* 103 */     .expression("[A-Z]{2,3}")
/* 104 */     .text("AP02,")
/* 105 */     .expression("[^,]+,")
/* 106 */     .number("[01],")
/* 107 */     .number("d+,")
/* 108 */     .number("(d+),")
/* 109 */     .number("(d+),")
/* 110 */     .expression("(")
/* 111 */     .groupBegin()
/* 112 */     .number("d+|")
/* 113 */     .number("d+|")
/* 114 */     .number("d+,")
/* 115 */     .groupEnd("+")
/* 116 */     .expression(")")
/* 117 */     .number("d+,")
/* 118 */     .expression("(.*)")
/* 119 */     .compile();
/*     */   
/*     */   private Boolean decodeOptionalValue(Parser parser, int activeValue) {
/* 122 */     int value = parser.nextInt().intValue();
/* 123 */     if (value != 0) {
/* 124 */       return Boolean.valueOf((value == activeValue));
/*     */     }
/* 126 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeCommon(Position position, Parser parser) {
/* 131 */     position.set("rssi", parser.nextInt());
/* 132 */     position.set("sat", parser.nextInt());
/* 133 */     position.set("battery", parser.nextInt());
/* 134 */     position.set("ignition", decodeOptionalValue(parser, 1));
/* 135 */     position.set("armed", decodeOptionalValue(parser, 1));
/*     */     
/* 137 */     int mode = parser.nextInt().intValue();
/* 138 */     if (mode != 0) {
/* 139 */       position.set("mode", Integer.valueOf(mode));
/*     */     }
/*     */   }
/*     */   
/*     */   private void decodeWifi(Network network, String data) {
/* 144 */     for (String wifi : data.split("&")) {
/* 145 */       if (!wifi.isEmpty()) {
/* 146 */         String[] values = wifi.split("\\|");
/* 147 */         network.addWifiAccessPoint(WifiAccessPoint.from(values[1]
/* 148 */               .replace('-', ':'), Integer.parseInt(values[2])));
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 157 */     String sentence = (String)msg;
/*     */     
/* 159 */     String id = sentence.startsWith("TRV") ? sentence.substring(0, 3) : sentence.substring(0, 2);
/* 160 */     String type = sentence.substring(id.length(), id.length() + 4);
/*     */     
/* 162 */     if (channel != null) {
/* 163 */       String responseHeader = id + (char)(type.charAt(0) + 1) + type.substring(1);
/* 164 */       if (type.equals("AP00") && id.equals("IW")) {
/* 165 */         String time = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
/* 166 */         channel.writeAndFlush(new NetworkMessage(responseHeader + "," + time + ",0#", remoteAddress));
/* 167 */       } else if (type.equals("AP14")) {
/* 168 */         channel.writeAndFlush(new NetworkMessage(responseHeader + ",0.000,0.000#", remoteAddress));
/*     */       } else {
/* 170 */         channel.writeAndFlush(new NetworkMessage(responseHeader + "#", remoteAddress));
/*     */       } 
/*     */     } 
/*     */     
/* 174 */     if (type.equals("AP00")) {
/* 175 */       getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(id.length() + type.length()) });
/* 176 */       return null;
/*     */     } 
/*     */     
/* 179 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 180 */     if (deviceSession == null) {
/* 181 */       return null;
/*     */     }
/*     */     
/* 184 */     if (type.equals("CP01")) {
/*     */       
/* 186 */       Parser parser = new Parser(PATTERN_HEARTBEAT, sentence);
/* 187 */       if (!parser.matches()) {
/* 188 */         return null;
/*     */       }
/*     */       
/* 191 */       Position position = new Position(getProtocolName());
/* 192 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 194 */       getLastLocation(position, null);
/*     */       
/* 196 */       decodeCommon(position, parser);
/*     */       
/* 198 */       if (parser.hasNext(3)) {
/* 199 */         position.set("blocked", decodeOptionalValue(parser, 2));
/* 200 */         position.set("charge", decodeOptionalValue(parser, 1));
/* 201 */         position.set("motion", decodeOptionalValue(parser, 1));
/*     */       } 
/*     */       
/* 204 */       return position;
/*     */     } 
/* 206 */     if (type.equals("AP01") || type.equals("AP10") || type.equals("YP03") || type.equals("YP14")) {
/*     */       
/* 208 */       Parser parser = new Parser(PATTERN, sentence);
/* 209 */       if (!parser.matches()) {
/* 210 */         return null;
/*     */       }
/*     */       
/* 213 */       Position position = new Position(getProtocolName());
/* 214 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */       
/* 217 */       DateBuilder dateBuilder = (new DateBuilder()).setDate(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*     */       
/* 219 */       position.setValid(parser.next().equals("A"));
/* 220 */       position.setLatitude(parser.nextCoordinate());
/* 221 */       position.setLongitude(parser.nextCoordinate());
/* 222 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/*     */       
/* 224 */       dateBuilder.setTime(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/* 225 */       position.setTime(dateBuilder.getDate());
/*     */       
/* 227 */       position.setCourse(parser.nextDouble().doubleValue());
/*     */       
/* 229 */       decodeCommon(position, parser);
/*     */       
/* 231 */       Network network = new Network();
/*     */       
/* 233 */       network.addCellTower(CellTower.from(parser
/* 234 */             .nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue()));
/*     */       
/* 236 */       if (parser.hasNext()) {
/* 237 */         decodeWifi(network, parser.next());
/*     */       }
/*     */       
/* 240 */       position.setNetwork(network);
/*     */       
/* 242 */       return position;
/*     */     } 
/* 244 */     if (type.equals("AP02")) {
/*     */       
/* 246 */       Parser parser = new Parser(PATTERN_LBS, sentence);
/* 247 */       if (!parser.matches()) {
/* 248 */         return null;
/*     */       }
/*     */       
/* 251 */       Position position = new Position(getProtocolName());
/* 252 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 254 */       getLastLocation(position, null);
/*     */       
/* 256 */       int mcc = parser.nextInt().intValue();
/* 257 */       int mnc = parser.nextInt().intValue();
/*     */       
/* 259 */       Network network = new Network();
/*     */       
/* 261 */       for (String cell : parser.next().split(",")) {
/* 262 */         if (!cell.isEmpty()) {
/* 263 */           String[] values = cell.split("\\|");
/* 264 */           network.addCellTower(CellTower.from(mcc, mnc, 
/*     */                 
/* 266 */                 Integer.parseInt(values[0]), 
/* 267 */                 Integer.parseInt(values[1]), 
/* 268 */                 Integer.parseInt(values[2])));
/*     */         } 
/*     */       } 
/*     */       
/* 272 */       decodeWifi(network, parser.next());
/*     */       
/* 274 */       position.setNetwork(network);
/*     */       
/* 276 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 280 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TrvProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */