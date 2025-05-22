/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ public class GoSafeProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public GoSafeProtocolDecoder(Protocol protocol) {
/*  42 */     super(protocol);
/*     */   }
/*     */   
/*  45 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  46 */     .text("*GS")
/*  47 */     .number("d+,")
/*  48 */     .number("(d+),")
/*  49 */     .expression("([^#]*)#?")
/*  50 */     .compile();
/*     */   
/*  52 */   private static final Pattern PATTERN_OLD = (new PatternBuilder())
/*  53 */     .text("*GS")
/*  54 */     .number("d+,")
/*  55 */     .number("(d+),")
/*  56 */     .text("GPS:")
/*  57 */     .number("(dd)(dd)(dd);")
/*  58 */     .number("d;").optional()
/*  59 */     .expression("([AV]);")
/*  60 */     .number("([NS])(d+.d+);")
/*  61 */     .number("([EW])(d+.d+);")
/*  62 */     .number("(d+)?;")
/*  63 */     .number("(d+);")
/*  64 */     .number("(d+.?d*)").optional()
/*  65 */     .number("(dd)(dd)(dd)")
/*  66 */     .any()
/*  67 */     .compile();
/*     */   
/*     */   private void decodeFragment(Position position, String fragment) {
/*     */     String[] values;
/*  71 */     int dataIndex = fragment.indexOf(':');
/*  72 */     int index = 0;
/*     */     
/*  74 */     if (fragment.length() == dataIndex + 1) {
/*  75 */       values = new String[0];
/*     */     } else {
/*  77 */       values = fragment.substring(dataIndex + 1).split(";");
/*     */     } 
/*     */     
/*  80 */     switch (fragment.substring(0, dataIndex)) {
/*     */       case "GPS":
/*  82 */         position.setValid(values[index++].equals("A"));
/*  83 */         position.set("sat", Integer.valueOf(Integer.parseInt(values[index++])));
/*  84 */         position.setLatitude(Double.parseDouble(values[index].substring(1)));
/*  85 */         if (values[index++].charAt(0) == 'S') {
/*  86 */           position.setLatitude(-position.getLatitude());
/*     */         }
/*  88 */         position.setLongitude(Double.parseDouble(values[index].substring(1)));
/*  89 */         if (values[index++].charAt(0) == 'W') {
/*  90 */           position.setLongitude(-position.getLongitude());
/*     */         }
/*  92 */         if (!values[index++].isEmpty()) {
/*  93 */           position.setSpeed(UnitsConverter.knotsFromKph(Integer.parseInt(values[index - 1])));
/*     */         }
/*  95 */         position.setCourse(Integer.parseInt(values[index++]));
/*  96 */         if (index < values.length) {
/*  97 */           position.setAltitude(Integer.parseInt(values[index++]));
/*     */         }
/*  99 */         if (index < values.length) {
/* 100 */           position.set("hdop", Double.valueOf(Double.parseDouble(values[index++])));
/*     */         }
/* 102 */         if (index < values.length) {
/* 103 */           position.set("vdop", Double.valueOf(Double.parseDouble(values[index++])));
/*     */         }
/*     */         break;
/*     */       case "GSM":
/* 107 */         index++;
/* 108 */         index++;
/* 109 */         position.setNetwork(new Network(CellTower.from(
/* 110 */                 Integer.parseInt(values[index++]), Integer.parseInt(values[index++]), 
/* 111 */                 Integer.parseInt(values[index++], 16), Integer.parseInt(values[index++], 16), 
/* 112 */                 Integer.parseInt(values[index++]))));
/*     */         break;
/*     */       case "COT":
/* 115 */         if (index < values.length) {
/* 116 */           position.set("odometer", Long.valueOf(Long.parseLong(values[index++])));
/*     */         }
/* 118 */         if (index < values.length) {
/* 119 */           String[] hours = values[index].split("-");
/* 120 */           position.set("hours", Integer.valueOf((Integer.parseInt(hours[0]) * 3600 + ((hours.length > 1) ? (
/* 121 */                 Integer.parseInt(hours[1]) * 60) : 0) + ((hours.length > 2) ? 
/* 122 */                 Integer.parseInt(hours[2]) : 0)) * 1000));
/*     */         } 
/*     */         break;
/*     */       case "ADC":
/* 126 */         position.set("power", Double.valueOf(Double.parseDouble(values[index++])));
/* 127 */         if (index < values.length) {
/* 128 */           position.set("battery", Double.valueOf(Double.parseDouble(values[index++])));
/*     */         }
/* 130 */         if (index < values.length) {
/* 131 */           position.set("adc1", Double.valueOf(Double.parseDouble(values[index++])));
/*     */         }
/* 133 */         if (index < values.length) {
/* 134 */           position.set("adc2", Double.valueOf(Double.parseDouble(values[index++])));
/*     */         }
/*     */         break;
/*     */       case "DTT":
/* 138 */         position.set("status", Integer.valueOf(Integer.parseInt(values[index++], 16)));
/* 139 */         if (!values[index++].isEmpty()) {
/* 140 */           int io = Integer.parseInt(values[index - 1], 16);
/* 141 */           position.set("ignition", Boolean.valueOf(BitUtil.check(io, 0)));
/* 142 */           position.set("in1", Boolean.valueOf(BitUtil.check(io, 1)));
/* 143 */           position.set("in2", Boolean.valueOf(BitUtil.check(io, 2)));
/* 144 */           position.set("in3", Boolean.valueOf(BitUtil.check(io, 3)));
/* 145 */           position.set("in4", Boolean.valueOf(BitUtil.check(io, 4)));
/* 146 */           position.set("out1", Boolean.valueOf(BitUtil.check(io, 5)));
/* 147 */           position.set("out2", Boolean.valueOf(BitUtil.check(io, 6)));
/* 148 */           position.set("out3", Boolean.valueOf(BitUtil.check(io, 7)));
/*     */         } 
/* 150 */         position.set("geofence", values[index++] + values[index++]);
/* 151 */         position.set("eventStatus", values[index++]);
/* 152 */         if (index < values.length) {
/* 153 */           position.set("packetType", values[index++]);
/*     */         }
/*     */         break;
/*     */       case "ETD":
/* 157 */         position.set("eventData", values[index++]);
/*     */         break;
/*     */       case "OBD":
/* 160 */         position.set("obd", values[index++]);
/*     */         break;
/*     */       case "TAG":
/* 163 */         position.set("tagData", values[index++]);
/*     */         break;
/*     */       case "IWD":
/* 166 */         while (index < values.length) {
/* 167 */           int sensorIndex = Integer.parseInt(values[index++]);
/* 168 */           int dataType = Integer.parseInt(values[index++]);
/* 169 */           if (dataType == 0) {
/* 170 */             position.set("driverUniqueId", values[index++]); continue;
/* 171 */           }  if (dataType == 1) {
/* 172 */             index++;
/* 173 */             position.set("temp" + sensorIndex, Double.valueOf(Double.parseDouble(values[index++])));
/*     */           } 
/*     */         } 
/*     */         break;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodePosition(DeviceSession deviceSession, String sentence) throws ParseException {
/* 184 */     Position position = new Position(getProtocolName());
/* 185 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 187 */     int index = 0;
/* 188 */     String[] fragments = sentence.split(",");
/*     */     
/* 190 */     position.setTime((new SimpleDateFormat("HHmmssddMMyy")).parse(fragments[index++]));
/*     */     
/* 192 */     for (; index < fragments.length; index++) {
/* 193 */       if (!fragments[index].isEmpty()) {
/* 194 */         if (fragments[index].matches("\\p{XDigit}+")) {
/* 195 */           position.set("event", Integer.valueOf(Integer.parseInt(fragments[index], 16)));
/*     */         } else {
/* 197 */           decodeFragment(position, fragments[index]);
/*     */         } 
/*     */       }
/*     */     } 
/*     */     
/* 202 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 209 */     if (channel != null) {
/* 210 */       channel.writeAndFlush(new NetworkMessage("1234", remoteAddress));
/*     */     }
/*     */     
/* 213 */     String sentence = (String)msg;
/* 214 */     Pattern pattern = PATTERN;
/* 215 */     if (sentence.startsWith("*GS02")) {
/* 216 */       pattern = PATTERN_OLD;
/*     */     }
/*     */     
/* 219 */     Parser parser = new Parser(pattern, (String)msg);
/* 220 */     if (!parser.matches()) {
/* 221 */       return null;
/*     */     }
/*     */     
/* 224 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 225 */     if (deviceSession == null) {
/* 226 */       return null;
/*     */     }
/*     */     
/* 229 */     if (pattern == PATTERN_OLD) {
/*     */       
/* 231 */       Position position = new Position(getProtocolName());
/* 232 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */       
/* 235 */       DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */       
/* 237 */       position.setValid(parser.next().equals("A"));
/* 238 */       position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
/* 239 */       position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
/* 240 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/* 241 */       position.setCourse(parser.nextDouble(0.0D));
/*     */       
/* 243 */       position.set("hdop", parser.next());
/*     */       
/* 245 */       dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 246 */       position.setTime(dateBuilder.getDate());
/*     */       
/* 248 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 252 */     List<Position> positions = new LinkedList<>();
/* 253 */     for (String item : parser.next().split("\\$")) {
/* 254 */       positions.add(decodePosition(deviceSession, item));
/*     */     }
/* 256 */     return positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GoSafeProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */