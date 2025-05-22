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
/*     */ public class ItsProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public ItsProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */   
/*  39 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  40 */     .expression("[^$]*")
/*  41 */     .text("$")
/*  42 */     .expression(",?[^,]+,")
/*  43 */     .groupBegin()
/*  44 */     .expression("[^,]*,")
/*  45 */     .expression("[^,]+,")
/*  46 */     .expression("(..),")
/*  47 */     .number("(d+),").optional()
/*  48 */     .expression("([LH]),")
/*  49 */     .or()
/*  50 */     .expression("([^,]+),")
/*  51 */     .groupEnd()
/*  52 */     .number("(d{15}),")
/*  53 */     .groupBegin()
/*  54 */     .expression("(..),")
/*  55 */     .or()
/*  56 */     .expression("[^,]*,")
/*  57 */     .number("([01]),").optional()
/*  58 */     .groupEnd()
/*  59 */     .number("(dd),?(dd),?(d{2,4}),")
/*  60 */     .number("(dd),?(dd),?(dd),")
/*  61 */     .expression("([01AV]),").optional()
/*  62 */     .number("(d+.d+),([NS]),")
/*  63 */     .number("(d+.d+),([EW]),")
/*  64 */     .groupBegin()
/*  65 */     .number("(d+.?d*),")
/*  66 */     .number("(d+.?d*),")
/*  67 */     .number("(d+),")
/*  68 */     .groupBegin()
/*  69 */     .number("(d+.?d*),")
/*  70 */     .number("d+.?d*,")
/*  71 */     .number("d+.?d*,")
/*  72 */     .expression("[^,]*,")
/*  73 */     .number("([01]),")
/*  74 */     .number("([01]),")
/*  75 */     .number("(d+.?d*),")
/*  76 */     .number("(d+.?d*),")
/*  77 */     .number("([01]),")
/*  78 */     .expression("[CO]?,")
/*  79 */     .expression("(.*),")
/*  80 */     .number("([012]{4}),")
/*  81 */     .number("([01]{2}),")
/*  82 */     .groupBegin()
/*  83 */     .number("d+,")
/*  84 */     .number("(d+.d+),")
/*  85 */     .number("(d+.d+),")
/*  86 */     .groupEnd("?")
/*  87 */     .groupEnd("?")
/*  88 */     .or()
/*  89 */     .number("(-?d+.d+),")
/*  90 */     .number("(d+.d+),")
/*  91 */     .groupEnd()
/*  92 */     .any()
/*  93 */     .compile();
/*     */   
/*     */   private String decodeAlarm(String status) {
/*  96 */     switch (status) {
/*     */       case "WD":
/*     */       case "EA":
/*  99 */         return "sos";
/*     */       case "BL":
/* 101 */         return "lowBattery";
/*     */       case "HB":
/* 103 */         return "hardBraking";
/*     */       case "HA":
/* 105 */         return "hardAcceleration";
/*     */       case "RT":
/* 107 */         return "hardCornering";
/*     */       case "OS":
/* 109 */         return "overspeed";
/*     */       case "TA":
/* 111 */         return "tampering";
/*     */       case "BD":
/* 113 */         return "powerCut";
/*     */       case "BR":
/* 115 */         return "powerRestored";
/*     */     } 
/* 117 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 125 */     String sentence = (String)msg;
/*     */     
/* 127 */     if (channel != null && sentence.startsWith("$,01,")) {
/* 128 */       channel.writeAndFlush(new NetworkMessage("$,1,*", remoteAddress));
/*     */     }
/*     */     
/* 131 */     Parser parser = new Parser(PATTERN, sentence);
/* 132 */     if (!parser.matches()) {
/* 133 */       return null;
/*     */     }
/*     */     
/* 136 */     String status = parser.next();
/* 137 */     Integer event = parser.nextInt();
/* 138 */     boolean history = "H".equals(parser.next());
/* 139 */     String type = parser.next();
/*     */     
/* 141 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 142 */     if (deviceSession == null) {
/* 143 */       return null;
/*     */     }
/*     */     
/* 146 */     Position position = new Position(getProtocolName());
/* 147 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 149 */     if (type != null && type.equals("EMR")) {
/* 150 */       position.set("alarm", "sos");
/*     */     }
/*     */     
/* 153 */     if (event != null) {
/* 154 */       position.set("event", event);
/*     */     }
/* 156 */     if (history) {
/* 157 */       position.set("archive", Boolean.valueOf(true));
/*     */     }
/*     */     
/* 160 */     if (parser.hasNext()) {
/* 161 */       status = parser.next();
/*     */     }
/* 163 */     if (status != null) {
/* 164 */       if (status.equals("IN")) {
/* 165 */         position.set("ignition", Boolean.valueOf(true));
/* 166 */       } else if (status.equals("IF")) {
/* 167 */         position.set("ignition", Boolean.valueOf(false));
/*     */       } else {
/* 169 */         position.set("alarm", decodeAlarm(status));
/*     */       } 
/*     */     }
/*     */     
/* 173 */     if (parser.hasNext()) {
/* 174 */       position.setValid((parser.nextInt().intValue() == 1));
/*     */     }
/* 176 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/* 177 */     if (parser.hasNext()) {
/* 178 */       position.setValid(parser.next().matches("[1A]"));
/*     */     }
/* 180 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 181 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/*     */     
/* 183 */     if (parser.hasNext(3)) {
/* 184 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/* 185 */       position.setCourse(parser.nextDouble().doubleValue());
/* 186 */       position.set("sat", parser.nextInt());
/*     */     } 
/*     */     
/* 189 */     if (parser.hasNext(8)) {
/* 190 */       position.setAltitude(parser.nextDouble().doubleValue());
/* 191 */       position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/* 192 */       position.set("charge", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/* 193 */       position.set("power", parser.nextDouble());
/* 194 */       position.set("battery", parser.nextDouble());
/*     */       
/* 196 */       position.set("emergency", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/*     */       
/* 198 */       String[] cells = parser.next().split(",");
/* 199 */       int mcc = Integer.parseInt(cells[1]);
/* 200 */       int mnc = Integer.parseInt(cells[2]);
/* 201 */       int lac = Integer.parseInt(cells[3], 16);
/* 202 */       int cid = Integer.parseInt(cells[4], 16);
/* 203 */       Network network = new Network(CellTower.from(mcc, mnc, lac, cid, Integer.parseInt(cells[0])));
/* 204 */       if (!cells[5].startsWith("(")) {
/* 205 */         for (int i = 0; i < 4; i++) {
/* 206 */           lac = Integer.parseInt(cells[5 + 3 * i + 1], 16);
/* 207 */           cid = Integer.parseInt(cells[5 + 3 * i + 2], 16);
/* 208 */           if (lac > 0 && cid > 0) {
/* 209 */             network.addCellTower(CellTower.from(mcc, mnc, lac, cid));
/*     */           }
/*     */         } 
/*     */       }
/* 213 */       position.setNetwork(network);
/*     */       
/* 215 */       String input = parser.next();
/* 216 */       if (input.charAt(input.length() - 1) == '2') {
/* 217 */         input = input.substring(0, input.length() - 1) + '0';
/*     */       }
/* 219 */       position.set("input", Integer.valueOf(Integer.parseInt(input, 2)));
/* 220 */       position.set("output", parser.nextBinInt());
/*     */     } 
/*     */     
/* 223 */     if (parser.hasNext(2)) {
/* 224 */       position.set("adc1", parser.nextDouble());
/* 225 */       position.set("adc2", parser.nextDouble());
/*     */     } 
/*     */     
/* 228 */     if (parser.hasNext(2)) {
/* 229 */       position.setAltitude(parser.nextDouble().doubleValue());
/* 230 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/*     */     } 
/*     */     
/* 233 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ItsProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */