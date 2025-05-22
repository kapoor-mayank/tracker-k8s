/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
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
/*     */ public class UproProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public UproProtocolDecoder(Protocol protocol) {
/*  40 */     super(protocol);
/*     */   }
/*     */   
/*  43 */   private static final Pattern PATTERN_HEADER = (new PatternBuilder())
/*  44 */     .text("*")
/*  45 */     .expression("(..20)")
/*  46 */     .expression("([01])")
/*  47 */     .number("(d+),")
/*  48 */     .expression("(.)")
/*  49 */     .expression("(.)")
/*  50 */     .any()
/*  51 */     .compile();
/*     */   
/*  53 */   private static final Pattern PATTERN_LOCATION = (new PatternBuilder())
/*  54 */     .number("(dd)(dd)(dd)")
/*  55 */     .number("(dd)(dd)(dddd)")
/*  56 */     .number("(ddd)(dd)(dddd)")
/*  57 */     .number("(d)")
/*  58 */     .number("(dd)")
/*  59 */     .number("(dd)")
/*  60 */     .number("(dd)(dd)(dd)")
/*  61 */     .compile();
/*     */   
/*     */   private void decodeLocation(Position position, String data) {
/*  64 */     Parser parser = new Parser(PATTERN_LOCATION, data);
/*  65 */     if (parser.matches()) {
/*     */ 
/*     */       
/*  68 */       DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */       
/*  70 */       position.setValid(true);
/*  71 */       position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN));
/*  72 */       position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN));
/*     */       
/*  74 */       int flags = parser.nextInt(0);
/*  75 */       position.setValid(BitUtil.check(flags, 0));
/*  76 */       if (!BitUtil.check(flags, 1)) {
/*  77 */         position.setLatitude(-position.getLatitude());
/*     */       }
/*  79 */       if (!BitUtil.check(flags, 2)) {
/*  80 */         position.setLongitude(-position.getLongitude());
/*     */       }
/*     */       
/*  83 */       position.setSpeed((parser.nextInt(0) * 2));
/*  84 */       position.setCourse((parser.nextInt(0) * 10));
/*     */       
/*  86 */       dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*  87 */       position.setTime(dateBuilder.getDate());
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private String decodeAlarm(int alarm) {
/*  93 */     if (BitUtil.check(alarm, 2)) {
/*  94 */       return "tampering";
/*     */     }
/*  96 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 103 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 105 */     if (buf.getByte(buf.readerIndex()) != 42) {
/* 106 */       return null;
/*     */     }
/*     */     
/* 109 */     int headerIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)38);
/* 110 */     if (headerIndex < 0) {
/* 111 */       headerIndex = buf.writerIndex();
/*     */     }
/* 113 */     String header = buf.readSlice(headerIndex - buf.readerIndex()).toString(StandardCharsets.US_ASCII);
/*     */     
/* 115 */     Parser parser = new Parser(PATTERN_HEADER, header);
/* 116 */     if (!parser.matches()) {
/* 117 */       return null;
/*     */     }
/*     */     
/* 120 */     String head = parser.next();
/* 121 */     boolean reply = parser.next().equals("1");
/*     */     
/* 123 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 124 */     if (deviceSession == null) {
/* 125 */       return null;
/*     */     }
/*     */     
/* 128 */     Position position = new Position(getProtocolName());
/* 129 */     position.setDeviceId(deviceSession.getDeviceId());
/* 130 */     Network network = new Network();
/*     */     
/* 132 */     String type = parser.next();
/* 133 */     String subtype = parser.next();
/*     */     
/* 135 */     if (reply && channel != null) {
/* 136 */       channel.writeAndFlush(new NetworkMessage("*" + head + "Y" + type + subtype + "#", remoteAddress));
/*     */     }
/*     */     
/* 139 */     while (buf.readableBytes() > 1) {
/*     */       int count; String stringValue; long odometer; String[] cells;
/* 141 */       buf.readByte();
/*     */       
/* 143 */       byte dataType = buf.readByte();
/*     */       
/* 145 */       int delimiterIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)38);
/* 146 */       if (delimiterIndex < 0) {
/* 147 */         delimiterIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)35);
/* 148 */         if (delimiterIndex < 0) {
/* 149 */           delimiterIndex = buf.writerIndex();
/*     */         }
/*     */       } 
/*     */       
/* 153 */       ByteBuf data = buf.readSlice(delimiterIndex - buf.readerIndex());
/* 154 */       int mcc = 0, mnc = 0;
/*     */ 
/*     */       
/* 157 */       switch (dataType) {
/*     */         case 65:
/* 159 */           decodeLocation(position, data.toString(StandardCharsets.US_ASCII));
/*     */         
/*     */         case 66:
/* 162 */           position.set("status", data.toString(StandardCharsets.US_ASCII));
/*     */         
/*     */         case 67:
/* 165 */           odometer = 0L;
/* 166 */           while (data.isReadable()) {
/* 167 */             odometer <<= 4L;
/* 168 */             odometer += (data.readByte() - 48);
/*     */           } 
/* 170 */           position.set("odometer", Long.valueOf(odometer * 2L * 1852L / 3600L));
/*     */         
/*     */         case 70:
/* 173 */           position.setSpeed(
/* 174 */               Integer.parseInt(data.readSlice(4).toString(StandardCharsets.US_ASCII)) * 0.1D);
/*     */         
/*     */         case 71:
/* 177 */           position.setAltitude(
/* 178 */               Integer.parseInt(data.readSlice(6).toString(StandardCharsets.US_ASCII)) * 0.1D);
/*     */         
/*     */         case 73:
/* 181 */           stringValue = data.toString(StandardCharsets.US_ASCII);
/* 182 */           count = Integer.parseInt(stringValue.substring(0, 1));
/* 183 */           if (stringValue.length() == 6 + count * 10) {
/* 184 */             mcc = Integer.parseInt(stringValue.substring(1, 4));
/* 185 */             mnc = Integer.parseInt(stringValue.substring(4, 6));
/* 186 */             for (int i = 0; i < count; i++) {
/* 187 */               int offset = 6 + i * 10;
/* 188 */               network.addCellTower(CellTower.from(mcc, mnc, 
/*     */                     
/* 190 */                     Integer.parseInt(stringValue.substring(offset, offset + 4), 16), 
/* 191 */                     Integer.parseInt(stringValue.substring(offset + 4, offset + 8), 16), 
/* 192 */                     Integer.parseInt(stringValue.substring(offset + 8, offset + 10))));
/*     */             } 
/*     */           } 
/*     */         
/*     */         case 74:
/* 197 */           if (data.readableBytes() == 6) {
/* 198 */             char index = (char)data.readUnsignedByte();
/* 199 */             int status = data.readUnsignedByte();
/* 200 */             double value = Integer.parseInt(data.readSlice(4).toString(StandardCharsets.US_ASCII)) * 0.1D;
/* 201 */             if (BitUtil.check(status, 0)) {
/* 202 */               value = -value;
/*     */             }
/* 204 */             position.set("temp" + index, Double.valueOf(value));
/*     */           } 
/*     */         
/*     */         case 75:
/* 208 */           position.set("statusExtended", data.toString(StandardCharsets.US_ASCII));
/*     */         
/*     */         case 77:
/* 211 */           if (data.readableBytes() == 3) {
/* 212 */             position.set("batteryLevel", 
/* 213 */                 Double.valueOf(Integer.parseInt(data.readSlice(3).toString(StandardCharsets.US_ASCII)) * 0.1D)); continue;
/* 214 */           }  if (data.readableBytes() == 4) {
/* 215 */             char index = (char)data.readUnsignedByte();
/* 216 */             data.readUnsignedByte();
/* 217 */             position.set("humidity" + index, 
/*     */                 
/* 219 */                 Integer.valueOf(Integer.parseInt(data.readSlice(2).toString(StandardCharsets.US_ASCII))));
/*     */           } 
/*     */         
/*     */         case 78:
/* 223 */           position.set("rssi", 
/* 224 */               Integer.valueOf(Integer.parseInt(data.readSlice(2).toString(StandardCharsets.US_ASCII))));
/*     */         
/*     */         case 79:
/* 227 */           position.set("sat", 
/* 228 */               Integer.valueOf(Integer.parseInt(data.readSlice(2).toString(StandardCharsets.US_ASCII))));
/*     */         
/*     */         case 80:
/* 231 */           if (data.readableBytes() >= 16) {
/* 232 */             position.setNetwork(new Network(CellTower.from(
/* 233 */                     Integer.parseInt(data.readSlice(4).toString(StandardCharsets.US_ASCII)), 
/* 234 */                     Integer.parseInt(data.readSlice(4).toString(StandardCharsets.US_ASCII)), 
/* 235 */                     Integer.parseInt(data.readSlice(4).toString(StandardCharsets.US_ASCII), 16), 
/* 236 */                     Integer.parseInt(data.readSlice(4).toString(StandardCharsets.US_ASCII), 16))));
/*     */           }
/*     */         
/*     */         case 81:
/* 240 */           if (!head.startsWith("HQ")) {
/* 241 */             position.set("obdPid", ByteBufUtil.hexDump(data));
/*     */           }
/*     */         
/*     */         case 82:
/* 245 */           if (head.startsWith("HQ")) {
/* 246 */             position.set("rssi", 
/* 247 */                 Integer.valueOf(Integer.parseInt(data.readSlice(2).toString(StandardCharsets.US_ASCII))));
/* 248 */             position.set("sat", 
/* 249 */                 Integer.valueOf(Integer.parseInt(data.readSlice(2).toString(StandardCharsets.US_ASCII)))); continue;
/*     */           } 
/* 251 */           position.set("odbTravel", ByteBufUtil.hexDump(data));
/*     */ 
/*     */         
/*     */         case 83:
/* 255 */           position.set("obdTraffic", ByteBufUtil.hexDump(data));
/*     */         
/*     */         case 84:
/* 258 */           if (data.readableBytes() == 2) {
/* 259 */             position.set("batteryLevel", 
/* 260 */                 Integer.valueOf(Integer.parseInt(data.toString(StandardCharsets.US_ASCII))));
/*     */           }
/*     */         
/*     */         case 86:
/* 264 */           position.set("power", 
/* 265 */               Double.valueOf(Integer.parseInt(data.readSlice(4).toString(StandardCharsets.US_ASCII)) * 0.1D));
/*     */         
/*     */         case 87:
/* 268 */           position.set("alarm", 
/* 269 */               decodeAlarm(Integer.parseInt(data.readSlice(2).toString(StandardCharsets.US_ASCII))));
/*     */         
/*     */         case 88:
/* 272 */           cells = data.toString(StandardCharsets.US_ASCII).split(";");
/* 273 */           if (!cells[0].startsWith("(")) {
/* 274 */             for (int i = 0; i < cells.length; i++) {
/* 275 */               String[] values = cells[i].split(",");
/* 276 */               int index = 0;
/* 277 */               if (i == 0) {
/* 278 */                 mcc = Integer.parseInt(values[index++]);
/* 279 */                 mnc = Integer.parseInt(values[index++]);
/*     */               } 
/* 281 */               network.addCellTower(CellTower.from(mcc, mnc, 
/*     */                     
/* 283 */                     Integer.parseInt(values[index++]), 
/* 284 */                     Integer.parseInt(values[index++]), 
/* 285 */                     Integer.parseInt(values[index])));
/*     */             } 
/* 287 */             position.setNetwork(network);
/*     */           } 
/*     */         
/*     */         case 89:
/* 291 */           stringValue = data.toString(StandardCharsets.US_ASCII);
/* 292 */           count = Integer.parseInt(stringValue.substring(0, 1));
/* 293 */           if (stringValue.length() == 6 + count * 14) {
/* 294 */             mcc = Integer.parseInt(stringValue.substring(1, 4));
/* 295 */             mnc = Integer.parseInt(stringValue.substring(4, 6));
/* 296 */             for (int i = 0; i < count; i++) {
/* 297 */               int offset = 6 + i * 14;
/* 298 */               network.addCellTower(CellTower.from(mcc, mnc, 
/*     */                     
/* 300 */                     Integer.parseInt(stringValue.substring(offset, offset + 4), 16), 
/* 301 */                     Long.parseLong(stringValue.substring(offset + 4, offset + 12), 16), 
/* 302 */                     Integer.parseInt(stringValue.substring(offset + 12, offset + 14))));
/*     */             }  continue;
/*     */           } 
/* 305 */           position.set("power", 
/* 306 */               Double.valueOf(Integer.parseInt(data.readSlice(5).toString(StandardCharsets.US_ASCII)) * 0.001D));
/*     */ 
/*     */         
/*     */         case 98:
/* 310 */           if (data.readableBytes() > 3) {
/* 311 */             position.set("serial", data.toString(StandardCharsets.US_ASCII).substring(3));
/*     */           }
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     } 
/* 320 */     if (network.getCellTowers() != null || network.getWifiAccessPoints() != null) {
/* 321 */       position.setNetwork(network);
/*     */     }
/*     */     
/* 324 */     if (position.getLatitude() == 0.0D || position.getLongitude() == 0.0D) {
/* 325 */       if (position.getAttributes().isEmpty()) {
/* 326 */         return null;
/*     */       }
/* 328 */       getLastLocation(position, position.getDeviceTime());
/*     */     } 
/*     */     
/* 331 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\UproProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */