/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.HashMap;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.ObdDecoder;
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
/*     */ public class CastelProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*  44 */   private static final Map<Integer, Integer> PID_LENGTH_MAP = new HashMap<>(); public static final short MSG_SC_LOGIN = 4097; public static final short MSG_SC_LOGIN_RESPONSE = -28671; public static final short MSG_SC_LOGOUT = 4098; public static final short MSG_SC_HEARTBEAT = 4099; public static final short MSG_SC_HEARTBEAT_RESPONSE = -28669; public static final short MSG_SC_GPS = 16385; public static final short MSG_SC_PID_DATA = 16386; public static final short MSG_SC_SUPPORTED_PID = 16388; public static final short MSG_SC_OBD_DATA = 16389; public static final short MSG_SC_DTCS_PASSENGER = 16390; public static final short MSG_SC_DTCS_COMMERCIAL = 16395;
/*     */   
/*     */   static {
/*  47 */     int[] l1 = { 4, 5, 6, 7, 8, 9, 11, 13, 14, 15, 17, 18, 19, 28, 29, 30, 44, 45, 46, 47, 48, 51, 67, 69, 70, 71, 72, 73, 74, 75, 76, 81, 82, 90 };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  54 */     int[] l2 = { 2, 3, 10, 12, 16, 20, 21, 22, 23, 24, 25, 26, 27, 31, 33, 34, 35, 49, 50, 60, 61, 62, 63, 66, 68, 77, 78, 80, 83, 84, 85, 86, 87, 88, 89 };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  61 */     int[] l4 = { 0, 1, 32, 36, 37, 38, 39, 40, 41, 42, 43, 52, 53, 54, 55, 56, 57, 58, 59, 64, 65, 79 };
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  66 */     for (int i : l1) {
/*  67 */       PID_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(1));
/*     */     }
/*  69 */     for (int i : l2) {
/*  70 */       PID_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(2));
/*     */     }
/*  72 */     for (int i : l4)
/*  73 */       PID_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(4)); 
/*     */   }
/*     */   public static final short MSG_SC_ALARM = 16391; public static final short MSG_SC_CELL = 16392; public static final short MSG_SC_GPS_SLEEP = 16393; public static final short MSG_SC_FUEL = 16398; public static final short MSG_SC_AGPS_REQUEST = 20737; public static final short MSG_SC_QUERY_RESPONSE = -24574; public static final short MSG_SC_CURRENT_LOCATION = -20479; public static final short MSG_CC_LOGIN = 16385; public static final short MSG_CC_LOGIN_RESPONSE = -32767; public static final short MSG_CC_HEARTBEAT = 16902; public static final short MSG_CC_PETROL_CONTROL = 17795; public static final short MSG_CC_HEARTBEAT_RESPONSE = -32250;
/*     */   
/*     */   public CastelProtocolDecoder(Protocol protocol) {
/*  78 */     super(protocol);
/*     */   }
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
/*     */   private Position readPosition(DeviceSession deviceSession, ByteBuf buf) {
/* 108 */     Position position = new Position(getProtocolName());
/* 109 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */ 
/*     */     
/* 113 */     DateBuilder dateBuilder = (new DateBuilder()).setDateReverse(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/* 114 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 116 */     double lat = buf.readUnsignedIntLE() / 3600000.0D;
/* 117 */     double lon = buf.readUnsignedIntLE() / 3600000.0D;
/* 118 */     position.setSpeed(UnitsConverter.knotsFromCps(buf.readUnsignedShortLE()));
/* 119 */     position.setCourse(buf.readUnsignedShortLE() * 0.1D);
/*     */     
/* 121 */     int flags = buf.readUnsignedByte();
/* 122 */     if ((flags & 0x2) == 0) {
/* 123 */       lat = -lat;
/*     */     }
/* 125 */     if ((flags & 0x1) == 0) {
/* 126 */       lon = -lon;
/*     */     }
/* 128 */     position.setLatitude(lat);
/* 129 */     position.setLongitude(lon);
/* 130 */     position.setValid(((flags & 0xC) > 0));
/* 131 */     position.set("sat", Integer.valueOf(flags >> 4));
/*     */     
/* 133 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position createPosition(DeviceSession deviceSession) {
/* 138 */     Position position = new Position(getProtocolName());
/* 139 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 141 */     getLastLocation(position, null);
/*     */     
/* 143 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeObd(Position position, ByteBuf buf, boolean groups) {
/* 148 */     int count = buf.readUnsignedByte();
/*     */     
/* 150 */     int[] pids = new int[count]; int i;
/* 151 */     for (i = 0; i < count; i++) {
/* 152 */       pids[i] = buf.readUnsignedShortLE() & 0xFF;
/*     */     }
/*     */     
/* 155 */     if (groups) {
/* 156 */       buf.readUnsignedByte();
/* 157 */       buf.readUnsignedByte();
/*     */     } 
/*     */     
/* 160 */     for (i = 0; i < count; i++) {
/*     */       int value;
/* 162 */       switch (((Integer)PID_LENGTH_MAP.get(Integer.valueOf(pids[i]))).intValue()) {
/*     */         case 1:
/* 164 */           value = buf.readUnsignedByte();
/*     */           break;
/*     */         case 2:
/* 167 */           value = buf.readUnsignedShortLE();
/*     */           break;
/*     */         case 4:
/* 170 */           value = buf.readIntLE();
/*     */           break;
/*     */         default:
/* 173 */           value = 0;
/*     */           break;
/*     */       } 
/* 176 */       position.add(ObdDecoder.decodeData(pids[i], value, false));
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeStat(Position position, ByteBuf buf) {
/* 182 */     buf.readUnsignedIntLE();
/* 183 */     buf.readUnsignedIntLE();
/* 184 */     position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/* 185 */     position.set("tripOdometer", Long.valueOf(buf.readUnsignedIntLE()));
/* 186 */     position.set("fuelConsumption", Long.valueOf(buf.readUnsignedIntLE()));
/* 187 */     buf.readUnsignedShortLE();
/*     */     
/* 189 */     long state = buf.readUnsignedIntLE();
/* 190 */     position.set("alarm", BitUtil.check(state, 4) ? "hardAcceleration" : null);
/* 191 */     position.set("alarm", BitUtil.check(state, 5) ? "hardBraking" : null);
/* 192 */     position.set("alarm", BitUtil.check(state, 6) ? "idle" : null);
/* 193 */     position.set("ignition", Boolean.valueOf(BitUtil.check(state, 18)));
/* 194 */     position.set("status", Long.valueOf(state));
/*     */     
/* 196 */     buf.skipBytes(8);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, int version, ByteBuf id, short type, ByteBuf content) {
/* 203 */     if (channel != null) {
/* 204 */       int length = 5 + id.readableBytes() + 2 + 2 + 2;
/* 205 */       if (content != null) {
/* 206 */         length += content.readableBytes();
/*     */       }
/*     */       
/* 209 */       ByteBuf response = Unpooled.buffer(length);
/* 210 */       response.writeByte(64); response.writeByte(64);
/* 211 */       response.writeShortLE(length);
/* 212 */       response.writeByte(version);
/* 213 */       response.writeBytes(id);
/* 214 */       response.writeShort(type);
/* 215 */       if (content != null) {
/* 216 */         response.writeBytes(content);
/* 217 */         content.release();
/*     */       } 
/* 219 */       response.writeShortLE(
/* 220 */           Checksum.crc16(Checksum.CRC16_X25, response.nioBuffer(0, response.writerIndex())));
/* 221 */       response.writeByte(13); response.writeByte(10);
/* 222 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, ByteBuf id, short type) {
/* 229 */     if (channel != null) {
/* 230 */       int length = 4 + id.readableBytes() + 2 + 4 + 8 + 2 + 2;
/*     */       
/* 232 */       ByteBuf response = Unpooled.buffer(length);
/* 233 */       response.writeByte(64); response.writeByte(64);
/* 234 */       response.writeShortLE(length);
/* 235 */       response.writeBytes(id);
/* 236 */       response.writeShort(type);
/* 237 */       response.writeIntLE(0);
/* 238 */       for (int i = 0; i < 8; i++) {
/* 239 */         response.writeByte(255);
/*     */       }
/* 241 */       response.writeShortLE(
/* 242 */           Checksum.crc16(Checksum.CRC16_X25, response.nioBuffer(0, response.writerIndex())));
/* 243 */       response.writeByte(13); response.writeByte(10);
/* 244 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */   
/*     */   private void decodeAlarm(Position position, int alarm) {
/* 249 */     switch (alarm) {
/*     */       case 1:
/* 251 */         position.set("alarm", "overspeed");
/*     */         break;
/*     */       case 2:
/* 254 */         position.set("alarm", "lowPower");
/*     */         break;
/*     */       case 3:
/* 257 */         position.set("alarm", "temperature");
/*     */         break;
/*     */       case 4:
/* 260 */         position.set("alarm", "hardAcceleration");
/*     */         break;
/*     */       case 5:
/* 263 */         position.set("alarm", "hardBraking");
/*     */         break;
/*     */       case 6:
/* 266 */         position.set("alarm", "idleEngine");
/*     */         break;
/*     */       case 8:
/* 269 */         position.set("alarm", "highRpm");
/*     */         break;
/*     */       case 9:
/* 272 */         position.set("alarm", "powerOn");
/*     */         break;
/*     */       case 11:
/* 275 */         position.set("alarm", "quickLaneChange");
/*     */         break;
/*     */       case 12:
/* 278 */         position.set("alarm", "hardCornering");
/*     */         break;
/*     */       case 14:
/* 281 */         position.set("alarm", "powerOff");
/*     */         break;
/*     */       case 20:
/* 284 */         position.set("alarm", "illegalIgnition");
/*     */         break;
/*     */       case 22:
/* 287 */         position.set("ignition", Boolean.valueOf(true));
/*     */         break;
/*     */       case 23:
/* 290 */         position.set("ignition", Boolean.valueOf(false));
/*     */         break;
/*     */       case 27:
/* 293 */         position.set("alarm", "dangerousDriving");
/*     */         break;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeSc(Channel channel, SocketAddress remoteAddress, ByteBuf buf, int version, ByteBuf id, short type, DeviceSession deviceSession) {
/* 304 */     if (type == 4099) {
/*     */       
/* 306 */       sendResponse(channel, remoteAddress, version, id, (short)-28669, (ByteBuf)null);
/*     */     }
/* 308 */     else if (type == 4097 || type == 4098 || type == 16385 || type == 16391 || type == -20479 || type == 16398) {
/*     */ 
/*     */       
/* 311 */       if (type == 4097) {
/* 312 */         ByteBuf response = Unpooled.buffer(10);
/* 313 */         response.writeIntLE(-1);
/* 314 */         response.writeShortLE(0);
/* 315 */         response.writeIntLE((int)(System.currentTimeMillis() / 1000L));
/* 316 */         sendResponse(channel, remoteAddress, version, id, (short)-28671, response);
/*     */       } 
/*     */       
/* 319 */       if (type == 16385) {
/* 320 */         buf.readUnsignedByte();
/* 321 */       } else if (type == 16391) {
/* 322 */         buf.readUnsignedIntLE();
/* 323 */       } else if (type == -20479) {
/* 324 */         buf.readUnsignedShortLE();
/*     */       } 
/*     */       
/* 327 */       buf.readUnsignedIntLE();
/* 328 */       buf.readUnsignedIntLE();
/* 329 */       long odometer = buf.readUnsignedIntLE();
/* 330 */       long tripOdometer = buf.readUnsignedIntLE();
/* 331 */       long fuelConsumption = buf.readUnsignedIntLE();
/* 332 */       buf.readUnsignedShortLE();
/* 333 */       long status = buf.readUnsignedIntLE();
/* 334 */       buf.skipBytes(8);
/*     */       
/* 336 */       int count = buf.readUnsignedByte();
/*     */       
/* 338 */       List<Position> positions = new LinkedList<>();
/*     */       
/* 340 */       for (int i = 0; i < count; i++) {
/* 341 */         Position position = readPosition(deviceSession, buf);
/* 342 */         position.set("odometer", Long.valueOf(odometer));
/* 343 */         position.set("tripOdometer", Long.valueOf(tripOdometer));
/* 344 */         position.set("fuelConsumption", Long.valueOf(fuelConsumption));
/* 345 */         position.set("status", Long.valueOf(status));
/* 346 */         positions.add(position);
/*     */       } 
/*     */       
/* 349 */       if (type == 16391) {
/* 350 */         int alarmCount = buf.readUnsignedByte();
/* 351 */         for (int j = 0; j < alarmCount; j++) {
/* 352 */           if (buf.readUnsignedByte() != 0) {
/* 353 */             int alarm = buf.readUnsignedByte();
/* 354 */             for (Position position : positions) {
/* 355 */               decodeAlarm(position, alarm);
/*     */             }
/* 357 */             buf.readUnsignedShortLE();
/* 358 */             buf.readUnsignedShortLE();
/*     */           } 
/*     */         } 
/* 361 */       } else if (type == 16398) {
/* 362 */         for (Position position : positions) {
/* 363 */           position.set("adc1", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */         }
/*     */       } 
/*     */       
/* 367 */       if (!positions.isEmpty()) {
/* 368 */         return positions;
/*     */       }
/*     */     } else {
/* 371 */       if (type == 16393) {
/*     */         
/* 373 */         buf.readUnsignedIntLE();
/*     */         
/* 375 */         return readPosition(deviceSession, buf);
/*     */       } 
/* 377 */       if (type == 20737)
/*     */       {
/* 379 */         return readPosition(deviceSession, buf);
/*     */       }
/* 381 */       if (type == 16386) {
/*     */         
/* 383 */         Position position = createPosition(deviceSession);
/*     */         
/* 385 */         decodeStat(position, buf);
/*     */         
/* 387 */         buf.readUnsignedShortLE();
/* 388 */         decodeObd(position, buf, true);
/*     */         
/* 390 */         return position;
/*     */       } 
/* 392 */       if (type == 16390) {
/*     */         
/* 394 */         Position position = createPosition(deviceSession);
/*     */         
/* 396 */         decodeStat(position, buf);
/*     */         
/* 398 */         buf.readUnsignedByte();
/* 399 */         position.add(ObdDecoder.decodeCodes(ByteBufUtil.hexDump(buf.readSlice(buf.readUnsignedByte()))));
/*     */         
/* 401 */         return position;
/*     */       } 
/* 403 */       if (type == 16389) {
/*     */         
/* 405 */         Position position = createPosition(deviceSession);
/*     */         
/* 407 */         decodeStat(position, buf);
/*     */         
/* 409 */         buf.readUnsignedByte();
/* 410 */         decodeObd(position, buf, false);
/*     */         
/* 412 */         return position;
/*     */       } 
/* 414 */       if (type == 16392) {
/*     */         
/* 416 */         Position position = createPosition(deviceSession);
/*     */         
/* 418 */         decodeStat(position, buf);
/*     */         
/* 420 */         position.setNetwork(new Network(
/* 421 */               CellTower.fromLacCid(buf.readUnsignedShortLE(), buf.readUnsignedShortLE())));
/*     */         
/* 423 */         return position;
/*     */       } 
/* 425 */       if (type == -24574) {
/*     */         
/* 427 */         Position position = createPosition(deviceSession);
/*     */         
/* 429 */         buf.readUnsignedShortLE();
/* 430 */         buf.readUnsignedByte();
/* 431 */         buf.readUnsignedByte();
/*     */         
/* 433 */         int failureCount = buf.readUnsignedByte();
/* 434 */         for (int i = 0; i < failureCount; i++) {
/* 435 */           buf.readUnsignedShortLE();
/*     */         }
/*     */         
/* 438 */         int successCount = buf.readUnsignedByte();
/* 439 */         for (int j = 0; j < successCount; j++) {
/* 440 */           buf.readUnsignedShortLE();
/* 441 */           position.set("result", buf
/* 442 */               .readSlice(buf.readUnsignedShortLE()).toString(StandardCharsets.US_ASCII));
/*     */         } 
/*     */         
/* 445 */         return position;
/*     */       } 
/*     */     } 
/*     */     
/* 449 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeCc(Channel channel, SocketAddress remoteAddress, ByteBuf buf, int version, ByteBuf id, short type, DeviceSession deviceSession) {
/* 456 */     if (type == 16902) {
/*     */       
/* 458 */       sendResponse(channel, remoteAddress, version, id, (short)-32250, (ByteBuf)null);
/*     */       
/* 460 */       buf.readUnsignedByte();
/* 461 */       int count = buf.readUnsignedByte();
/*     */       
/* 463 */       List<Position> positions = new LinkedList<>();
/*     */       
/* 465 */       for (int i = 0; i < count; i++) {
/* 466 */         Position position = readPosition(deviceSession, buf);
/*     */         
/* 468 */         position.set("status", Long.valueOf(buf.readUnsignedIntLE()));
/* 469 */         position.set("battery", Short.valueOf(buf.readUnsignedByte()));
/* 470 */         position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*     */         
/* 472 */         buf.readUnsignedByte();
/* 473 */         buf.readUnsignedByte();
/* 474 */         buf.readUnsignedByte();
/*     */         
/* 476 */         position.setNetwork(new Network(
/* 477 */               CellTower.fromLacCid(buf.readUnsignedShortLE(), buf.readUnsignedShortLE())));
/*     */         
/* 479 */         positions.add(position);
/*     */       } 
/*     */       
/* 482 */       return positions;
/*     */     } 
/* 484 */     if (type == 16385) {
/*     */       
/* 486 */       sendResponse(channel, remoteAddress, version, id, (short)-32767, (ByteBuf)null);
/*     */       
/* 488 */       Position position = readPosition(deviceSession, buf);
/*     */       
/* 490 */       position.set("status", Long.valueOf(buf.readUnsignedIntLE()));
/* 491 */       position.set("battery", Short.valueOf(buf.readUnsignedByte()));
/* 492 */       position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*     */       
/* 494 */       buf.readUnsignedByte();
/* 495 */       buf.readUnsignedByte();
/* 496 */       buf.readUnsignedByte();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 502 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 506 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeMpip(Channel channel, SocketAddress remoteAddress, ByteBuf buf, int version, ByteBuf id, short type, DeviceSession deviceSession) {
/* 513 */     if (type == 16385) {
/*     */       
/* 515 */       sendResponse(channel, remoteAddress, version, id, type, (ByteBuf)null);
/*     */       
/* 517 */       return readPosition(deviceSession, buf);
/*     */     } 
/* 519 */     if (type == 8193) {
/*     */       
/* 521 */       sendResponse(channel, remoteAddress, id, (short)4097);
/*     */       
/* 523 */       buf.readUnsignedIntLE();
/* 524 */       buf.readUnsignedIntLE();
/* 525 */       buf.readUnsignedByte();
/*     */       
/* 527 */       return readPosition(deviceSession, buf);
/*     */     } 
/* 529 */     if (type == 16897 || type == 16898 || type == 16902)
/*     */     {
/* 531 */       return readPosition(deviceSession, buf);
/*     */     }
/* 533 */     if (type == 16900) {
/*     */       
/* 535 */       List<Position> positions = new LinkedList<>();
/*     */       
/* 537 */       for (int i = 0; i < 8; i++) {
/* 538 */         Position position = readPosition(deviceSession, buf);
/* 539 */         buf.skipBytes(31);
/* 540 */         positions.add(position);
/*     */       } 
/*     */       
/* 543 */       return positions;
/*     */     } 
/*     */ 
/*     */     
/* 547 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 554 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 556 */     int header = buf.readUnsignedShortLE();
/* 557 */     buf.readUnsignedShortLE();
/*     */     
/* 559 */     int version = -1;
/* 560 */     if (header == 16448) {
/* 561 */       version = buf.readUnsignedByte();
/*     */     }
/*     */     
/* 564 */     ByteBuf id = buf.readSlice(20);
/* 565 */     short type = buf.readShort();
/*     */     
/* 567 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id
/* 568 */           .toString(StandardCharsets.US_ASCII).trim() });
/* 569 */     if (deviceSession == null) {
/* 570 */       return null;
/*     */     }
/*     */     
/* 573 */     switch (version) {
/*     */       case -1:
/* 575 */         return decodeMpip(channel, remoteAddress, buf, version, id, type, deviceSession);
/*     */       case 3:
/*     */       case 4:
/* 578 */         return decodeSc(channel, remoteAddress, buf, version, id, type, deviceSession);
/*     */     } 
/* 580 */     return decodeCc(channel, remoteAddress, buf, version, id, type, deviceSession);
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CastelProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */