///*     */ package org.traccar.protocol;
///*     */
///*     */ import io.netty.channel.Channel;
///*     */ import java.net.SocketAddress;
///*     */ import java.text.DateFormat;
///*     */ import java.text.SimpleDateFormat;
///*     */ import java.util.TimeZone;
///*     */ import java.util.regex.Pattern;
///*     */ import org.traccar.BaseProtocolDecoder;
///*     */ import org.traccar.Context;
///*     */ import org.traccar.DeviceSession;
///*     */ import org.traccar.NetworkMessage;
///*     */ import org.traccar.Protocol;
///*     */ import org.traccar.helper.Checksum;
///*     */ import org.traccar.helper.DataConverter;
///*     */ import org.traccar.helper.Parser;
///*     */ import org.traccar.helper.PatternBuilder;
///*     */ import org.traccar.helper.UnitsConverter;
///*     */ import org.traccar.model.CellTower;
///*     */ import org.traccar.model.Network;
///*     */ import org.traccar.model.Position;
///*     */ import org.traccar.protobuf.starlink.StarLinkMessage;
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */ public class StarLinkProtocolDecoder
///*     */   extends BaseProtocolDecoder
///*     */ {
///*     */   public static final int MSG_REQUEST_PARAMETER = 3;
///*     */   public static final int MSG_EVENT_REPORT = 6;
///*  45 */   private static final Pattern PATTERN = (new PatternBuilder())
///*  46 */     .expression(".")
///*  47 */     .text("SLU")
///*  48 */     .number("(x{6}|d{15}),")
///*  49 */     .number("(d+),")
///*  50 */     .number("(d+),")
///*  51 */     .expression("(.+)")
///*  52 */     .text("*")
///*  53 */     .number("xx")
///*  54 */     .compile();
///*     */
///*     */   private String format;
///*     */   private String dateFormat;
///*     */
///*     */   public StarLinkProtocolDecoder(Protocol protocol) {
///*  60 */     super(protocol);
///*  61 */     setDateFormat(Context.getConfig().getString(getProtocolName() + ".dateFormat", "yyMMddHHmmss"));
///*     */   }
///*     */
///*     */   public void setFormat(String format) {
///*  65 */     this.format = format.trim();
///*     */   }
///*     */
///*     */   public DateFormat getDateFormat(long deviceId) {
///*  69 */     DateFormat dateFormat = new SimpleDateFormat(Context.getIdentityManager().lookupAttributeString(deviceId,
///*  70 */           getProtocolName() + ".dateFormat", this.dateFormat, false));
///*  71 */     dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
///*  72 */     return dateFormat;
///*     */   }
///*     */
///*     */   public void setDateFormat(String dateFormat) {
///*  76 */     this.dateFormat = dateFormat;
///*     */   }
///*     */
///*     */   private double parseCoordinate(String value) {
///*  80 */     int minutesIndex = value.indexOf('.') - 2;
///*  81 */     double result = Double.parseDouble(value.substring(1, minutesIndex));
///*  82 */     result += Double.parseDouble(value.substring(minutesIndex)) / 60.0D;
///*  83 */     return (value.charAt(0) == '+') ? result : -result;
///*     */   }
///*     */
///*     */   private String decodeAlarm(int event) {
///*  87 */     switch (event) {
///*     */       case 6:
///*  89 */         return "overspeed";
///*     */       case 7:
///*  91 */         return "geofenceEnter";
///*     */       case 8:
///*  93 */         return "geofenceExit";
///*     */       case 9:
///*  95 */         return "powerCut";
///*     */       case 11:
///*  97 */         return "lowBattery";
///*     */       case 26:
///*  99 */         return "tow";
///*     */       case 36:
///* 101 */         return "sos";
///*     */       case 42:
///* 103 */         return "jamming";
///*     */     }
///* 105 */     return null;
///*     */   }
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
///* 113 */     Parser parser = new Parser(PATTERN, (String)msg);
///* 114 */     if (!parser.matches()) {
///* 115 */       return null;
///*     */     }
///*     */
///* 118 */     String uniqueId = parser.next();
///* 119 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { uniqueId });
///* 120 */     if (deviceSession == null) {
///* 121 */       return null;
///*     */     }
///*     */
///*     */
///* 125 */     int type = parser.nextInt(0);
///* 126 */     int index = parser.nextInt(0);
///* 127 */     if (type == 3) {
///* 128 */       String str = parser.next();
///* 129 */       this.format = str.substring(str.indexOf(',') + 1);
///* 130 */       return null;
///* 131 */     }  if (type != 6) {
///* 132 */       return null;
///*     */     }
///*     */
///* 135 */     Position position = new Position(getProtocolName());
///* 136 */     position.setDeviceId(deviceSession.getDeviceId());
///* 137 */     position.setValid(true);
///*     */
///* 139 */     position.set("index", Integer.valueOf(index));
///*     */
///* 141 */     String[] data = parser.next().split(",");
///* 142 */     Integer lac = null, cid = null;
///* 143 */     int event = 0;
///*     */
///* 145 */     if (this.format == null) {
///* 146 */       String response = "$SRV" + uniqueId + ",03,01,177";
///* 147 */       response = response + "*" + Checksum.sum(response.substring(1)) + "\r\n";
///* 148 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
///* 149 */       return null;
///*     */     }
///*     */
///* 152 */     String[] dataTags = this.format.split(",");
///* 153 */     DateFormat dateFormat = getDateFormat(deviceSession.getDeviceId());
///*     */
///* 155 */     for (int i = 0; i < Math.min(data.length, dataTags.length); i++) {
///* 156 */       if (!data[i].isEmpty()) {
///*     */         StarLinkMessage.mEventReport_TDx message;
///*     */
///* 159 */         switch (dataTags[i]) {
///*     */           case "#ALT#":
///*     */           case "#ALTD#":
///* 162 */             position.setAltitude(Double.parseDouble(data[i]));
///*     */             break;
///*     */           case "#DAL#":
///*     */           case "#DID#":
///* 166 */             position.set("driverUniqueId", data[i]);
///*     */             break;
///*     */           case "#EDT#":
///* 169 */             position.setDeviceTime(dateFormat.parse(data[i]));
///*     */             break;
///*     */           case "#EDV1#":
///*     */           case "#EDV2#":
///* 173 */             position.set("external" + dataTags[i].charAt(4), data[i]);
///*     */             break;
///*     */           case "#EID#":
///* 176 */             event = Integer.parseInt(data[i]);
///* 177 */             position.set("alarm", decodeAlarm(event));
///* 178 */             position.set("event", Integer.valueOf(event));
///* 179 */             if (event == 24 || event == 4) {
///* 180 */               position.set("ignition", Boolean.valueOf(true)); break;
///* 181 */             }  if (event == 25 || event == 5) {
///* 182 */               position.set("ignition", Boolean.valueOf(false));
///*     */             }
///*     */             break;
///*     */           case "#EDSC#":
///* 186 */             position.set("reason", data[i]);
///*     */             break;
///*     */           case "#IARM#":
///* 189 */             position.set("armed", Boolean.valueOf((Integer.parseInt(data[i]) > 0)));
///*     */             break;
///*     */           case "#PDT#":
///* 192 */             position.setFixTime(dateFormat.parse(data[i]));
///*     */             break;
///*     */           case "#LAT#":
///* 195 */             position.setLatitude(parseCoordinate(data[i]));
///*     */             break;
///*     */           case "#LONG#":
///* 198 */             position.setLongitude(parseCoordinate(data[i]));
///*     */             break;
///*     */           case "#SPD#":
///* 201 */             position.setSpeed(Double.parseDouble(data[i]));
///*     */             break;
///*     */           case "#SPDK#":
///* 204 */             position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(data[i])));
///*     */             break;
///*     */           case "#HEAD#":
///* 207 */             position.setCourse(Integer.parseInt(data[i]));
///*     */             break;
///*     */           case "#ODO#":
///*     */           case "#ODOD#":
///* 211 */             position.set("odometer", Long.valueOf((long)(Double.parseDouble(data[i]) * 1000.0D)));
///*     */             break;
///*     */           case "#BATC#":
///* 214 */             position.set("batteryLevel", Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */           case "#BATH#":
///* 217 */             position.set("batteryHealth", Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */           case "#TVI#":
///* 220 */             position.set("deviceTemp", Double.valueOf(Double.parseDouble(data[i])));
///*     */             break;
///*     */           case "#CFL#":
///* 223 */             position.set("fuel", Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */           case "#CFL2#":
///* 226 */             position.set("fuel2", Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */           case "#IN1#":
///*     */           case "#IN2#":
///*     */           case "#IN3#":
///*     */           case "#IN4#":
///* 232 */             position.set("in" + dataTags[i].charAt(3), Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */           case "#OUT1#":
///*     */           case "#OUT2#":
///*     */           case "#OUT3#":
///*     */           case "#OUT4#":
///* 238 */             position.set("out" + dataTags[i].charAt(4), Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */           case "#OUTA#":
///*     */           case "#OUTB#":
///*     */           case "#OUTC#":
///*     */           case "#OUTD#":
///* 244 */             position.set("out" + (dataTags[i].charAt(4) - 65 + 1), Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */           case "#PDOP#":
///* 247 */             position.set("pdop", Double.valueOf(Double.parseDouble(data[i])));
///*     */             break;
///*     */           case "#LAC#":
///* 250 */             if (!data[i].isEmpty()) {
///* 251 */               lac = Integer.valueOf(Integer.parseInt(data[i]));
///*     */             }
///*     */             break;
///*     */           case "#CID#":
///* 255 */             if (!data[i].isEmpty()) {
///* 256 */               cid = Integer.valueOf(Integer.parseInt(data[i]));
///*     */             }
///*     */             break;
///*     */           case "#CSS#":
///* 260 */             position.set("rssi", Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */           case "#VIN#":
///* 263 */             position.set("power", Double.valueOf(Double.parseDouble(data[i])));
///*     */             break;
///*     */           case "#VBAT#":
///* 266 */             position.set("battery", Double.valueOf(Double.parseDouble(data[i])));
///*     */             break;
///*     */           case "#DEST#":
///* 269 */             position.set("destination", data[i]);
///*     */             break;
///*     */           case "#IGN#":
///*     */           case "#IGNL#":
///*     */           case "#ENG#":
///* 274 */             position.set("ignition", Boolean.valueOf((Integer.parseInt(data[i]) > 0)));
///*     */             break;
///*     */           case "#DUR#":
///*     */           case "#TDUR#":
///* 278 */             position.set("hours", Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */           case "#SAT#":
///*     */           case "#SATN#":
///* 282 */             position.set("satVisible", Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */           case "#SATU#":
///* 285 */             position.set("sat", Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */           case "#STRT#":
///* 288 */             position.set("starter", Double.valueOf(Double.parseDouble(data[i])));
///*     */             break;
///*     */           case "#TS1#":
///* 291 */             position.set("sensor1State", Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */           case "#TS2#":
///* 294 */             position.set("sensor2State", Integer.valueOf(Integer.parseInt(data[i])));
///*     */             break;
///*     */
///*     */           case "#TD1#":
///*     */           case "#TD2#":
///* 299 */             message = StarLinkMessage.mEventReport_TDx.parseFrom(DataConverter.parseBase64(data[i]));
///* 300 */             position.set("sensor" + message
///* 301 */                 .getSensorNumber() + "Id", message
///* 302 */                 .getSensorID());
///* 303 */             position.set("sensor" + message
///* 304 */                 .getSensorNumber() + "Temp",
///* 305 */                 Double.valueOf(message.getTemperature() * 0.1D));
///* 306 */             position.set("sensor" + message
///* 307 */                 .getSensorNumber() + "Humidity",
///* 308 */                 Double.valueOf(message.getTemperature() * 0.1D));
///* 309 */             position.set("sensor" + message
///* 310 */                 .getSensorNumber() + "Voltage",
///* 311 */                 Double.valueOf(message.getVoltage() * 0.001D));
///*     */             break;
///*     */         }
///*     */
///*     */
///*     */       }
///*     */     }
///* 318 */     if (position.getFixTime() == null) {
///* 319 */       getLastLocation(position, null);
///*     */     }
///*     */
///* 322 */     if (lac != null && cid != null) {
///* 323 */       position.setNetwork(new Network(CellTower.fromLacCid(lac.intValue(), cid.intValue())));
///*     */     }
///*     */
///* 326 */     if (event == 20) {
///* 327 */       String rfid = data[data.length - 1];
///* 328 */       if (rfid.matches("0+")) {
///* 329 */         rfid = data[data.length - 2];
///*     */       }
///* 331 */       position.set("driverUniqueId", rfid);
///*     */     }
///*     */
///* 334 */     return position;
///*     */   }
///*     */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\StarLinkProtocolDecoder.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */