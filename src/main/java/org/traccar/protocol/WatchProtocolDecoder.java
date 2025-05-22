/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.BufferUtil;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.helper.StringUtil;
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
/*     */ public class WatchProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private ByteBuf audio;
/*     */   
/*     */   public WatchProtocolDecoder(Protocol protocol) {
/*  47 */     super(protocol);
/*     */   }
/*     */   
/*  50 */   private static final Pattern PATTERN_POSITION = (new PatternBuilder())
/*  51 */     .number("(dd)(dd)(dd),")
/*  52 */     .number("(dd)(dd)(dd),")
/*  53 */     .expression("([AV]),")
/*  54 */     .number(" *(-?d+.d+),")
/*  55 */     .expression("([NS])?,")
/*  56 */     .number(" *(-?d+.d+),")
/*  57 */     .expression("([EW])?,")
/*  58 */     .number("(d+.?d*),")
/*  59 */     .number("(d+.?d*),")
/*  60 */     .number("(-?d+.?d*),")
/*  61 */     .number("(d+),")
/*  62 */     .number("(d+),")
/*  63 */     .number("(d+),")
/*  64 */     .number("(d+),")
/*  65 */     .number("d+,")
/*  66 */     .number("(x+),")
/*  67 */     .expression("(.*)")
/*  68 */     .compile(); private boolean hasIndex;
/*     */   
/*     */   private void sendResponse(Channel channel, String id, String index, String content) {
/*  71 */     if (channel != null) {
/*     */       String response;
/*  73 */       if (index != null) {
/*  74 */         response = String.format("[%s*%s*%s*%04x*%s]", new Object[] { this.manufacturer, id, index, 
/*  75 */               Integer.valueOf(content.length()), content });
/*     */       } else {
/*  77 */         response = String.format("[%s*%s*%04x*%s]", new Object[] { this.manufacturer, id, 
/*  78 */               Integer.valueOf(content.length()), content });
/*     */       } 
/*  80 */       ByteBuf buf = Unpooled.copiedBuffer(response, StandardCharsets.US_ASCII);
/*  81 */       channel.writeAndFlush(new NetworkMessage(buf, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */   private String manufacturer;
/*     */   private String decodeAlarm(int status) {
/*  86 */     if (BitUtil.check(status, 0))
/*  87 */       return "lowBattery"; 
/*  88 */     if (BitUtil.check(status, 1))
/*  89 */       return "geofenceExit"; 
/*  90 */     if (BitUtil.check(status, 2))
/*  91 */       return "geofenceEnter"; 
/*  92 */     if (BitUtil.check(status, 3))
/*  93 */       return "watchState"; 
/*  94 */     if (BitUtil.check(status, 14))
/*  95 */       return "powerCut"; 
/*  96 */     if (BitUtil.check(status, 16))
/*  97 */       return "sos"; 
/*  98 */     if (BitUtil.check(status, 17))
/*  99 */       return "lowBattery"; 
/* 100 */     if (BitUtil.check(status, 18))
/* 101 */       return "geofenceExit"; 
/* 102 */     if (BitUtil.check(status, 19))
/* 103 */       return "geofenceEnter"; 
/* 104 */     if (BitUtil.check(status, 20))
/* 105 */       return "removing"; 
/* 106 */     if (BitUtil.check(status, 21) || BitUtil.check(status, 22)) {
/* 107 */       return "fallDown";
/*     */     }
/* 109 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodePosition(DeviceSession deviceSession, String data) {
/* 115 */     Parser parser = new Parser(PATTERN_POSITION, data);
/* 116 */     if (!parser.matches()) {
/* 117 */       return null;
/*     */     }
/*     */     
/* 120 */     Position position = new Position(getProtocolName());
/* 121 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 123 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 125 */     position.setValid(parser.next().equals("A"));
/* 126 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 127 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 128 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/* 129 */     position.setCourse(parser.nextDouble(0.0D));
/* 130 */     position.setAltitude(parser.nextDouble(0.0D));
/*     */     
/* 132 */     position.set("sat", Integer.valueOf(parser.nextInt(0)));
/* 133 */     position.set("rssi", Integer.valueOf(parser.nextInt(0)));
/* 134 */     position.set("batteryLevel", Integer.valueOf(parser.nextInt(0)));
/*     */     
/* 136 */     position.set("steps", Integer.valueOf(parser.nextInt(0)));
/*     */     
/* 138 */     int status = parser.nextHexInt(0);
/* 139 */     position.set("alarm", decodeAlarm(status));
/* 140 */     if (BitUtil.check(status, 4)) {
/* 141 */       position.set("motion", Boolean.valueOf(true));
/*     */     }
/*     */     
/* 144 */     String[] values = parser.next().split(",");
/* 145 */     int index = 0;
/*     */     
/* 147 */     if (values.length < 4 || !StringUtil.containsHex(values[index + 3])) {
/*     */       
/* 149 */       Network network = new Network();
/*     */       
/* 151 */       int cellCount = Integer.parseInt(values[index++]);
/* 152 */       if (cellCount > 0) {
/* 153 */         index++;
/* 154 */         int mcc = !values[index].isEmpty() ? Integer.parseInt(values[index++]) : 0;
/* 155 */         int mnc = !values[index].isEmpty() ? Integer.parseInt(values[index++]) : 0;
/*     */         
/* 157 */         for (int i = 0; i < cellCount; i++) {
/* 158 */           int lac = Integer.parseInt(values[index], StringUtil.containsHex(values[index++]) ? 16 : 10);
/* 159 */           int cid = Integer.parseInt(values[index], StringUtil.containsHex(values[index++]) ? 16 : 10);
/* 160 */           String rssi = values[index++];
/* 161 */           if (!rssi.isEmpty()) {
/* 162 */             network.addCellTower(CellTower.from(mcc, mnc, lac, cid, Integer.parseInt(rssi)));
/*     */           } else {
/* 164 */             network.addCellTower(CellTower.from(mcc, mnc, lac, cid));
/*     */           } 
/*     */         } 
/*     */       } 
/*     */       
/* 169 */       if (index < values.length && !values[index].isEmpty()) {
/* 170 */         int wifiCount = Integer.parseInt(values[index++]);
/*     */         
/* 172 */         for (int i = 0; i < wifiCount; i++) {
/* 173 */           index++;
/* 174 */           String macAddress = values[index++];
/* 175 */           String rssi = values[index++];
/* 176 */           if (!macAddress.isEmpty() && !macAddress.equals("0") && !rssi.isEmpty()) {
/* 177 */             network.addWifiAccessPoint(WifiAccessPoint.from(macAddress, Integer.parseInt(rssi)));
/*     */           }
/*     */         } 
/*     */       } 
/*     */       
/* 182 */       if (network.getCellTowers() != null || network.getWifiAccessPoints() != null) {
/* 183 */         position.setNetwork(network);
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/* 188 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean getHasIndex() {
/* 195 */     return this.hasIndex;
/*     */   }
/*     */   
/*     */   public String getManufacturer() {
/* 199 */     return this.manufacturer;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 206 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 208 */     buf.skipBytes(1);
/* 209 */     this.manufacturer = buf.readSlice(2).toString(StandardCharsets.US_ASCII);
/* 210 */     buf.skipBytes(1);
/*     */     
/* 212 */     int idIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)42);
/* 213 */     String id = buf.readSlice(idIndex - buf.readerIndex()).toString(StandardCharsets.US_ASCII);
/* 214 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 215 */     if (deviceSession == null) {
/* 216 */       return null;
/*     */     }
/*     */     
/* 219 */     buf.skipBytes(1);
/*     */     
/* 221 */     String index = null;
/* 222 */     int contentIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)42);
/* 223 */     if (contentIndex + 5 < buf.writerIndex() && buf.getByte(contentIndex + 5) == 42 && buf
/* 224 */       .toString(contentIndex + 1, 4, StandardCharsets.US_ASCII).matches("\\p{XDigit}+")) {
/* 225 */       int indexLength = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)42) - buf.readerIndex();
/* 226 */       this.hasIndex = true;
/* 227 */       index = buf.readSlice(indexLength).toString(StandardCharsets.US_ASCII);
/* 228 */       buf.skipBytes(1);
/*     */     } 
/*     */     
/* 231 */     buf.skipBytes(4);
/* 232 */     buf.skipBytes(1);
/*     */     
/* 234 */     buf.writerIndex(buf.writerIndex() - 1);
/*     */     
/* 236 */     contentIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)44);
/* 237 */     if (contentIndex < 0) {
/* 238 */       contentIndex = buf.writerIndex();
/*     */     }
/*     */     
/* 241 */     String type = buf.readSlice(contentIndex - buf.readerIndex()).toString(StandardCharsets.US_ASCII);
/*     */     
/* 243 */     if (contentIndex < buf.writerIndex()) {
/* 244 */       buf.readerIndex(contentIndex + 1);
/*     */     }
/*     */     
/* 247 */     if (type.equals("INIT")) {
/*     */       
/* 249 */       sendResponse(channel, id, index, "INIT,1");
/*     */     }
/* 251 */     else if (type.equals("LK")) {
/*     */       
/* 253 */       if (!Context.getConfig().getBoolean(getProtocolName() + ".noHeartbeatAck")) {
/* 254 */         sendResponse(channel, id, index, "LK");
/*     */       }
/*     */       
/* 257 */       if (buf.isReadable()) {
/* 258 */         String[] values = buf.toString(StandardCharsets.US_ASCII).split(",");
/* 259 */         if (values.length >= 3) {
/* 260 */           Position position = new Position(getProtocolName());
/* 261 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */           
/* 263 */           getLastLocation(position, null);
/*     */           
/* 265 */           position.set("batteryLevel", Integer.valueOf(Integer.parseInt(values[2])));
/* 266 */           position.set("steps", Integer.valueOf(Integer.parseInt(values[0])));
/*     */           
/* 268 */           return position;
/*     */         } 
/*     */       } 
/*     */     } else {
/* 272 */       if (type.startsWith("UD") || type.startsWith("AL") || type.startsWith("WT")) {
/*     */         
/* 274 */         Position position = decodePosition(deviceSession, buf.toString(StandardCharsets.US_ASCII));
/*     */         
/* 276 */         if (type.startsWith("AL")) {
/* 277 */           if (position != null && !position.getAttributes().containsKey("alarm")) {
/* 278 */             position.set("alarm", "sos");
/*     */           }
/* 280 */           sendResponse(channel, id, index, "AL");
/*     */         } 
/*     */         
/* 283 */         return position;
/*     */       } 
/* 285 */       if (type.equals("TKQ") || type.equals("TKQ2")) {
/*     */         
/* 287 */         sendResponse(channel, id, index, type);
/*     */       }
/* 289 */       else if (type.equalsIgnoreCase("PULSE") || type
/* 290 */         .equalsIgnoreCase("HEART") || type
/* 291 */         .equalsIgnoreCase("BLOOD") || type
/* 292 */         .equalsIgnoreCase("BPHRT") || type
/* 293 */         .equalsIgnoreCase("TEMP") || type
/* 294 */         .equalsIgnoreCase("btemp2") || type
/* 295 */         .equalsIgnoreCase("oxygen")) {
/*     */         
/* 297 */         if (buf.isReadable()) {
/*     */           
/* 299 */           Position position = new Position(getProtocolName());
/* 300 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */           
/* 302 */           getLastLocation(position, new Date());
/*     */           
/* 304 */           String[] values = buf.toString(StandardCharsets.US_ASCII).split(",");
/* 305 */           int valueIndex = 0;
/*     */           
/* 307 */           if (type.equalsIgnoreCase("TEMP")) {
/* 308 */             position.set("temp1", Double.valueOf(Double.parseDouble(values[valueIndex])));
/* 309 */           } else if (type.equalsIgnoreCase("btemp2")) {
/* 310 */             if (Integer.parseInt(values[valueIndex++]) > 0) {
/* 311 */               position.set("temp1", Double.valueOf(Double.parseDouble(values[valueIndex])));
/*     */             }
/* 313 */           } else if (type.equalsIgnoreCase("oxygen")) {
/* 314 */             position.set("bloodOxygen", Integer.valueOf(Integer.parseInt(values[++valueIndex])));
/*     */           } else {
/* 316 */             if (type.equalsIgnoreCase("BPHRT") || type.equalsIgnoreCase("BLOOD")) {
/* 317 */               position.set("pressureHigh", values[valueIndex++]);
/* 318 */               position.set("pressureLow", values[valueIndex++]);
/*     */             } 
/* 320 */             if (valueIndex <= values.length - 1) {
/* 321 */               position.set("heartRate", Integer.valueOf(Integer.parseInt(values[valueIndex])));
/*     */             }
/*     */           } 
/*     */           
/* 325 */           return position;
/*     */         } 
/*     */       } else {
/*     */         
/* 329 */         if (type.equals("img")) {
/*     */           
/* 331 */           Position position = new Position(getProtocolName());
/* 332 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */           
/* 334 */           getLastLocation(position, null);
/*     */           
/* 336 */           int timeIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)44);
/* 337 */           buf.readerIndex(timeIndex + 12 + 2);
/* 338 */           position.set("image", Context.getMediaManager().writeFile(id, buf, "jpg"));
/*     */           
/* 340 */           return position;
/*     */         } 
/* 342 */         if (type.equals("JXTK")) {
/*     */           
/* 344 */           int dataIndex = BufferUtil.indexOf(buf, buf.readerIndex(), buf.writerIndex(), (byte)44, 4) + 1;
/*     */           
/* 346 */           String[] values = buf.readCharSequence(dataIndex - buf.readerIndex(), StandardCharsets.US_ASCII).toString().split(",");
/*     */           
/* 348 */           int current = Integer.parseInt(values[2]);
/* 349 */           int total = Integer.parseInt(values[3]);
/*     */           
/* 351 */           if (this.audio == null) {
/* 352 */             this.audio = Unpooled.buffer();
/*     */           }
/* 354 */           this.audio.writeBytes(buf);
/*     */           
/* 356 */           sendResponse(channel, id, index, "JXTKR,1");
/*     */           
/* 358 */           if (current < total) {
/* 359 */             return null;
/*     */           }
/* 361 */           Position position = new Position(getProtocolName());
/* 362 */           position.setDeviceId(deviceSession.getDeviceId());
/* 363 */           getLastLocation(position, null);
/* 364 */           position.set("audio", Context.getMediaManager().writeFile(id, this.audio, "amr"));
/* 365 */           this.audio.release();
/* 366 */           this.audio = null;
/* 367 */           return position;
/*     */         } 
/*     */         
/* 370 */         if (type.equals("TK")) {
/*     */           
/* 372 */           if (buf.readableBytes() == 1) {
/* 373 */             return null;
/*     */           }
/*     */           
/* 376 */           Position position = new Position(getProtocolName());
/* 377 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */           
/* 379 */           getLastLocation(position, null);
/*     */           
/* 381 */           position.set("audio", Context.getMediaManager().writeFile(id, buf, "amr"));
/*     */           
/* 383 */           return position;
/*     */         } 
/*     */       } 
/*     */     } 
/* 387 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WatchProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */