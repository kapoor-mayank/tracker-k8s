/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Calendar;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.TimeZone;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitBuffer;
/*     */ import org.traccar.helper.UnitsConverter;
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
/*     */ public class GalileoProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private ByteBuf photo;
/*     */   
/*     */   public GalileoProtocolDecoder(Protocol protocol) {
/*  46 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*  51 */   private static final Map<Integer, Integer> TAG_LENGTH_MAP = new HashMap<>();
/*     */   
/*     */   static {
/*  54 */     int[] l1 = { 1, 2, 53, 67, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 213, 136, 138, 139, 140, 160, 175, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174 };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  61 */     int[] l2 = { 4, 16, 52, 64, 65, 66, 69, 70, 84, 85, 86, 87, 88, 89, 96, 97, 98, 112, 113, 114, 115, 116, 117, 118, 119, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 214, 215, 216, 217, 218 };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  68 */     int[] l3 = { 99, 100, 111, 93, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110 };
/*     */ 
/*     */ 
/*     */     
/*  72 */     int[] l4 = { 32, 51, 68, 144, 192, 194, 195, 211, 212, 219, 220, 221, 222, 223, 240, 249, 90, 71, 241, 242, 243, 244, 245, 246, 247, 248, 226, 233 };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  78 */     for (int i : l1) {
/*  79 */       TAG_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(1));
/*     */     }
/*  81 */     for (int i : l2) {
/*  82 */       TAG_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(2));
/*     */     }
/*  84 */     for (int i : l3) {
/*  85 */       TAG_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(3));
/*     */     }
/*  87 */     for (int i : l4) {
/*  88 */       TAG_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(4));
/*     */     }
/*  90 */     TAG_LENGTH_MAP.put(Integer.valueOf(91), Integer.valueOf(7));
/*  91 */     TAG_LENGTH_MAP.put(Integer.valueOf(92), Integer.valueOf(68));
/*     */   }
/*     */   
/*     */   private static int getTagLength(int tag) {
/*  95 */     Integer length = TAG_LENGTH_MAP.get(Integer.valueOf(tag));
/*  96 */     if (length == null) {
/*  97 */       throw new IllegalArgumentException("Unknown tag: " + tag);
/*     */     }
/*  99 */     return length.intValue();
/*     */   }
/*     */   
/*     */   private void sendReply(Channel channel, int header, int checksum) {
/* 103 */     if (channel != null) {
/* 104 */       ByteBuf reply = Unpooled.buffer(3);
/* 105 */       reply.writeByte(header);
/* 106 */       reply.writeShortLE((short)checksum);
/* 107 */       channel.writeAndFlush(new NetworkMessage(reply, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */   
/*     */   private void decodeTag(Position position, ByteBuf buf, int tag) {
/* 112 */     if (tag >= 80 && tag <= 87) {
/* 113 */       position.set("adc" + (tag - 80), Integer.valueOf(buf.readUnsignedShortLE()));
/* 114 */     } else if (tag >= 96 && tag <= 98) {
/* 115 */       position.set("fuel" + (tag - 96), Integer.valueOf(buf.readUnsignedShortLE()));
/* 116 */     } else if (tag >= 160 && tag <= 175) {
/* 117 */       position.set("can8BitR" + (tag - 160 + 15), Short.valueOf(buf.readUnsignedByte()));
/* 118 */     } else if (tag >= 176 && tag <= 185) {
/* 119 */       position.set("can16BitR" + (tag - 176 + 5), Integer.valueOf(buf.readUnsignedShortLE()));
/* 120 */     } else if (tag >= 196 && tag <= 210) {
/* 121 */       position.set("can8BitR" + (tag - 196), Short.valueOf(buf.readUnsignedByte()));
/* 122 */     } else if (tag >= 214 && tag <= 218) {
/* 123 */       position.set("can16BitR" + (tag - 214), Integer.valueOf(buf.readUnsignedShortLE()));
/* 124 */     } else if (tag >= 219 && tag <= 223) {
/* 125 */       position.set("can32BitR" + (tag - 219), Long.valueOf(buf.readUnsignedIntLE()));
/* 126 */     } else if (tag >= 226 && tag <= 233) {
/* 127 */       position.set("userData" + (tag - 226), Long.valueOf(buf.readUnsignedIntLE()));
/* 128 */     } else if (tag >= 240 && tag <= 249) {
/* 129 */       position.set("can32BitR" + (tag - 240 + 5), Long.valueOf(buf.readUnsignedIntLE()));
/*     */     } else {
/* 131 */       decodeTagOther(position, buf, tag);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void decodeTagOther(Position position, ByteBuf buf, int tag) {
/* 136 */     switch (tag) {
/*     */       case 1:
/* 138 */         position.set("versionHw", Short.valueOf(buf.readUnsignedByte()));
/*     */         return;
/*     */       case 2:
/* 141 */         position.set("versionFw", Short.valueOf(buf.readUnsignedByte()));
/*     */         return;
/*     */       case 4:
/* 144 */         position.set("deviceId", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */         return;
/*     */       case 16:
/* 147 */         position.set("index", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */         return;
/*     */       case 32:
/* 150 */         position.setTime(new Date(buf.readUnsignedIntLE() * 1000L));
/*     */         return;
/*     */       case 51:
/* 153 */         position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShortLE() * 0.1D));
/* 154 */         position.setCourse(buf.readUnsignedShortLE() * 0.1D);
/*     */         return;
/*     */       case 52:
/* 157 */         position.setAltitude(buf.readShortLE());
/*     */         return;
/*     */       case 53:
/* 160 */         position.set("hdop", Double.valueOf(buf.readUnsignedByte() * 0.1D));
/*     */         return;
/*     */       case 64:
/* 163 */         position.set("status", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */         return;
/*     */       case 65:
/* 166 */         position.set("power", Double.valueOf(buf.readUnsignedShortLE() / 1000.0D));
/*     */         return;
/*     */       case 66:
/* 169 */         position.set("battery", Double.valueOf(buf.readUnsignedShortLE() / 1000.0D));
/*     */         return;
/*     */       case 67:
/* 172 */         position.set("deviceTemp", Byte.valueOf(buf.readByte()));
/*     */         return;
/*     */       case 68:
/* 175 */         position.set("acceleration", Long.valueOf(buf.readUnsignedIntLE()));
/*     */         return;
/*     */       case 69:
/* 178 */         position.set("output", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */         return;
/*     */       case 70:
/* 181 */         position.set("input", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */         return;
/*     */       case 72:
/* 184 */         position.set("statusExtended", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */         return;
/*     */       case 88:
/* 187 */         position.set("rs2320", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */         return;
/*     */       case 89:
/* 190 */         position.set("rs2321", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */         return;
/*     */       case 144:
/* 193 */         position.set("driverUniqueId", String.valueOf(buf.readUnsignedIntLE()));
/*     */         return;
/*     */       case 192:
/* 196 */         position.set("fuelTotal", Double.valueOf(buf.readUnsignedIntLE() * 0.5D));
/*     */         return;
/*     */       case 193:
/* 199 */         position.set("fuel", Double.valueOf(buf.readUnsignedByte() * 0.4D));
/* 200 */         position.set("temp1", Integer.valueOf(buf.readUnsignedByte() - 40));
/* 201 */         position.set("rpm", Double.valueOf(buf.readUnsignedShortLE() * 0.125D));
/*     */         return;
/*     */       case 194:
/* 204 */         position.set("canB0", Long.valueOf(buf.readUnsignedIntLE()));
/*     */         return;
/*     */       case 195:
/* 207 */         position.set("canB1", Long.valueOf(buf.readUnsignedIntLE()));
/*     */         return;
/*     */       case 212:
/* 210 */         position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*     */         return;
/*     */       case 224:
/* 213 */         position.set("index", Long.valueOf(buf.readUnsignedIntLE()));
/*     */         return;
/*     */       case 225:
/* 216 */         position.set("result", buf
/* 217 */             .readSlice(buf.readUnsignedByte()).toString(StandardCharsets.US_ASCII));
/*     */         return;
/*     */       case 234:
/* 220 */         position.set("userDataArray", ByteBufUtil.hexDump(buf.readSlice(buf.readUnsignedByte())));
/*     */         return;
/*     */     } 
/* 223 */     buf.skipBytes(getTagLength(tag));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 232 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 234 */     int header = buf.readUnsignedByte();
/* 235 */     if (header == 1) {
/* 236 */       if (buf.getUnsignedMedium(buf.readerIndex() + 2) == 65564) {
/* 237 */         return decodeIridiumPosition(channel, remoteAddress, buf);
/*     */       }
/* 239 */       return decodePositions(channel, remoteAddress, buf);
/*     */     } 
/* 241 */     if (header == 7) {
/* 242 */       return decodePhoto(channel, remoteAddress, buf);
/*     */     }
/*     */     
/* 245 */     return null;
/*     */   }
/*     */   
/*     */   private void decodeMinimalDataSet(Position position, ByteBuf buf) {
/* 249 */     BitBuffer bits = new BitBuffer(buf.readSlice(10));
/* 250 */     bits.readUnsigned(1);
/*     */     
/* 252 */     Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
/* 253 */     calendar.set(6, 1);
/* 254 */     calendar.set(11, calendar.getActualMinimum(11));
/* 255 */     calendar.set(12, calendar.getActualMinimum(12));
/* 256 */     calendar.set(13, calendar.getActualMinimum(13));
/* 257 */     calendar.set(14, calendar.getActualMinimum(14));
/* 258 */     calendar.add(13, bits.readUnsigned(25));
/* 259 */     position.setTime(calendar.getTime());
/*     */     
/* 261 */     position.setValid((bits.readUnsigned(1) == 0));
/* 262 */     position.setLongitude((360 * bits.readUnsigned(22)) / 4194304.0D - 180.0D);
/* 263 */     position.setLatitude((360 * bits.readUnsigned(21)) / 2097152.0D - 90.0D);
/* 264 */     if (bits.readUnsigned(1) > 0) {
/* 265 */       position.set("alarm", "general");
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeIridiumPosition(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 271 */     buf.readUnsignedShortLE();
/*     */     
/* 273 */     buf.skipBytes(3);
/* 274 */     buf.readUnsignedIntLE();
/*     */     
/* 276 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { buf
/* 277 */           .readSlice(15).toString(StandardCharsets.US_ASCII) });
/* 278 */     if (deviceSession == null) {
/* 279 */       return null;
/*     */     }
/*     */     
/* 282 */     Position position = new Position(getProtocolName());
/* 283 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 285 */     buf.readUnsignedByte();
/* 286 */     buf.skipBytes(4);
/* 287 */     buf.readUnsignedIntLE();
/*     */     
/* 289 */     buf.skipBytes(23);
/*     */     
/* 291 */     buf.skipBytes(3);
/* 292 */     decodeMinimalDataSet(position, buf);
/*     */     
/* 294 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private List<Position> decodePositions(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 299 */     int length = (buf.readUnsignedShortLE() & 0x7FFF) + 3;
/*     */     
/* 301 */     List<Position> positions = new LinkedList<>();
/* 302 */     Set<Integer> tags = new HashSet<>();
/* 303 */     boolean hasLocation = false;
/*     */     
/* 305 */     DeviceSession deviceSession = null;
/* 306 */     Position position = new Position(getProtocolName());
/*     */     
/* 308 */     while (buf.readerIndex() < length) {
/*     */       
/* 310 */       int tag = buf.readUnsignedByte();
/* 311 */       if (tags.contains(Integer.valueOf(tag))) {
/* 312 */         if (hasLocation && position.getFixTime() != null) {
/* 313 */           positions.add(position);
/*     */         }
/* 315 */         tags.clear();
/* 316 */         hasLocation = false;
/* 317 */         position = new Position(getProtocolName());
/*     */       } 
/* 319 */       tags.add(Integer.valueOf(tag));
/*     */       
/* 321 */       if (tag == 3) {
/* 322 */         deviceSession = getDeviceSession(channel, remoteAddress, new String[] { buf
/* 323 */               .readSlice(15).toString(StandardCharsets.US_ASCII) }); continue;
/* 324 */       }  if (tag == 48) {
/* 325 */         hasLocation = true;
/* 326 */         position.setValid(((buf.readUnsignedByte() & 0xF0) == 0));
/* 327 */         position.setLatitude(buf.readIntLE() / 1000000.0D);
/* 328 */         position.setLongitude(buf.readIntLE() / 1000000.0D); continue;
/*     */       } 
/* 330 */       decodeTag(position, buf, tag);
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 335 */     if (deviceSession == null) {
/* 336 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 337 */       if (deviceSession == null) {
/* 338 */         return null;
/*     */       }
/*     */     } 
/*     */     
/* 342 */     if (hasLocation && position.getFixTime() != null) {
/* 343 */       positions.add(position);
/* 344 */     } else if (position.getAttributes().containsKey("result")) {
/* 345 */       position.setDeviceId(deviceSession.getDeviceId());
/* 346 */       getLastLocation(position, null);
/* 347 */       positions.add(position);
/*     */     } 
/*     */     
/* 350 */     sendReply(channel, 2, buf.readUnsignedShortLE());
/*     */     
/* 352 */     for (Position p : positions) {
/* 353 */       p.setDeviceId(deviceSession.getDeviceId());
/*     */     }
/*     */     
/* 356 */     return positions.isEmpty() ? null : positions;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodePhoto(Channel channel, SocketAddress remoteAddress, ByteBuf buf) throws Exception {
/* 362 */     int length = buf.readUnsignedShortLE();
/*     */     
/* 364 */     Position position = null;
/*     */     
/* 366 */     if (this.photo == null) {
/* 367 */       this.photo = Unpooled.buffer();
/*     */     }
/*     */     
/* 370 */     buf.readUnsignedByte();
/*     */     
/* 372 */     if (length > 1) {
/*     */       
/* 374 */       this.photo.writeBytes(buf, length - 1);
/*     */     }
/*     */     else {
/*     */       
/* 378 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 379 */       String uniqueId = Context.getIdentityManager().getById(deviceSession.getDeviceId()).getUniqueId();
/*     */       
/* 381 */       position = new Position(getProtocolName());
/* 382 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 384 */       getLastLocation(position, null);
/*     */       
/* 386 */       position.set("image", Context.getMediaManager().writeFile(uniqueId, this.photo, "jpg"));
/* 387 */       this.photo.release();
/* 388 */       this.photo = null;
/*     */     } 
/*     */ 
/*     */     
/* 392 */     sendReply(channel, 7, buf.readUnsignedShortLE());
/*     */     
/* 394 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GalileoProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */