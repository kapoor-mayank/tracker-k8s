/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitBuffer;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Gl200BinaryProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_RSP_LCB = 3;
/*     */   public static final int MSG_RSP_GEO = 8;
/*     */   public static final int MSG_RSP_COMPRESSED = 100;
/*     */   public static final int MSG_EVT_BPL = 6;
/*     */   public static final int MSG_EVT_VGN = 45;
/*     */   public static final int MSG_EVT_VGF = 46;
/*     */   public static final int MSG_EVT_UPD = 15;
/*     */   public static final int MSG_EVT_IDF = 17;
/*     */   public static final int MSG_EVT_GSS = 21;
/*     */   public static final int MSG_EVT_GES = 26;
/*     */   public static final int MSG_EVT_GPJ = 31;
/*     */   
/*     */   public Gl200BinaryProtocolDecoder(Protocol protocol) {
/*  40 */     super(protocol);
/*     */   }
/*     */   public static final int MSG_EVT_RMD = 35; public static final int MSG_EVT_JDS = 33; public static final int MSG_EVT_CRA = 23; public static final int MSG_EVT_UPC = 34; public static final int MSG_INF_GPS = 2; public static final int MSG_INF_CID = 4; public static final int MSG_INF_CSQ = 5; public static final int MSG_INF_VER = 6; public static final int MSG_INF_BAT = 7; public static final int MSG_INF_TMZ = 9;
/*     */   public static final int MSG_INF_GIR = 10;
/*     */   
/*     */   private Date decodeTime(ByteBuf buf) {
/*  46 */     DateBuilder dateBuilder = (new DateBuilder()).setDate(buf.readUnsignedShort(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*  47 */     return dateBuilder.getDate();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private List<Position> decodeLocation(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/*  56 */     List<Position> positions = new LinkedList<>();
/*     */     
/*  58 */     int type = buf.readUnsignedByte();
/*     */     
/*  60 */     buf.readUnsignedInt();
/*  61 */     buf.readUnsignedShort();
/*  62 */     buf.readUnsignedByte();
/*  63 */     buf.readUnsignedShort();
/*  64 */     buf.readUnsignedShort();
/*     */     
/*  66 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.format("%015d", new Object[] { Long.valueOf(buf.readLong()) }) });
/*  67 */     if (deviceSession == null) {
/*  68 */       return null;
/*     */     }
/*     */     
/*  71 */     int battery = buf.readUnsignedByte();
/*  72 */     int power = buf.readUnsignedShort();
/*     */     
/*  74 */     if (type == 8) {
/*  75 */       buf.readUnsignedByte();
/*  76 */       buf.readUnsignedByte();
/*     */     } 
/*     */     
/*  79 */     buf.readUnsignedByte();
/*  80 */     int satellites = buf.readUnsignedByte();
/*     */     
/*  82 */     if (type != 100) {
/*  83 */       buf.readUnsignedByte();
/*     */     }
/*     */     
/*  86 */     if (type == 3) {
/*  87 */       buf.readUnsignedByte();
/*  88 */       int b = buf.readUnsignedByte();
/*  89 */       while ((b & 0xF) != 15 && (b & 0xF0) != 240) {
/*     */         b = buf.readUnsignedByte();
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/*  95 */     if (type == 100) {
/*     */       
/*  97 */       int count = buf.readUnsignedShort();
/*     */ 
/*     */       
/* 100 */       int speed = 0;
/* 101 */       int heading = 0;
/* 102 */       int latitude = 0;
/* 103 */       int longitude = 0;
/* 104 */       long time = 0L;
/*     */       
/* 106 */       int i = 0; while (true) { if (i < count) {
/*     */           BitBuffer bits;
/* 108 */           if (time > 0L) {
/* 109 */             time++;
/*     */           }
/*     */           
/* 112 */           Position position = new Position(getProtocolName());
/* 113 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */           
/* 115 */           switch (BitUtil.from(buf.getUnsignedByte(buf.readerIndex()), 6)) {
/*     */             case 1:
/* 117 */               bits = new BitBuffer(buf.readSlice(3));
/* 118 */               bits.readUnsigned(2);
/* 119 */               bits.readUnsigned(1);
/* 120 */               speed = bits.readUnsigned(12);
/* 121 */               heading = bits.readUnsigned(9);
/* 122 */               longitude = buf.readInt();
/* 123 */               latitude = buf.readInt();
/* 124 */               if (time == 0L) {
/* 125 */                 time = buf.readUnsignedInt();
/*     */               }
/*     */               break;
/*     */             case 2:
/* 129 */               bits = new BitBuffer(buf.readSlice(5));
/* 130 */               bits.readUnsigned(2);
/* 131 */               bits.readUnsigned(1);
/* 132 */               speed += bits.readSigned(7);
/* 133 */               heading += bits.readSigned(7);
/* 134 */               longitude += bits.readSigned(12);
/* 135 */               latitude += bits.readSigned(11);
/*     */               break;
/*     */             default:
/* 138 */               buf.readUnsignedByte();
/*     */               i++;
/*     */               continue;
/*     */           } 
/* 142 */           position.setValid(true);
/* 143 */           position.setTime(new Date(time * 1000L));
/* 144 */           position.setSpeed(UnitsConverter.knotsFromKph(speed * 0.1D));
/* 145 */           position.setCourse(heading);
/* 146 */           position.setLongitude(longitude * 1.0E-6D);
/* 147 */           position.setLatitude(latitude * 1.0E-6D);
/*     */           
/* 149 */           positions.add(position);
/*     */         } else {
/*     */           break;
/*     */         }  i++; }
/*     */     
/*     */     } else {
/* 155 */       int count = buf.readUnsignedByte();
/*     */       
/* 157 */       for (int i = 0; i < count; i++) {
/*     */         
/* 159 */         Position position = new Position(getProtocolName());
/* 160 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 162 */         position.set("batteryLevel", Integer.valueOf(battery));
/* 163 */         position.set("power", Integer.valueOf(power));
/* 164 */         position.set("sat", Integer.valueOf(satellites));
/*     */         
/* 166 */         int hdop = buf.readUnsignedByte();
/* 167 */         position.setValid((hdop > 0));
/* 168 */         position.set("hdop", Integer.valueOf(hdop));
/*     */         
/* 170 */         position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedMedium() * 0.1D));
/* 171 */         position.setCourse(buf.readUnsignedShort());
/* 172 */         position.setAltitude(buf.readShort());
/* 173 */         position.setLongitude(buf.readInt() * 1.0E-6D);
/* 174 */         position.setLatitude(buf.readInt() * 1.0E-6D);
/*     */         
/* 176 */         position.setTime(decodeTime(buf));
/*     */         
/* 178 */         position.setNetwork(new Network(CellTower.from(buf
/* 179 */                 .readUnsignedShort(), buf.readUnsignedShort(), buf
/* 180 */                 .readUnsignedShort(), buf.readUnsignedShort())));
/*     */         
/* 182 */         buf.readUnsignedByte();
/*     */         
/* 184 */         positions.add(position);
/*     */       } 
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 190 */     return positions;
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
/*     */   private Position decodeEvent(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 208 */     Position position = new Position(getProtocolName());
/*     */     
/* 210 */     int type = buf.readUnsignedByte();
/*     */     
/* 212 */     buf.readUnsignedInt();
/* 213 */     buf.readUnsignedShort();
/* 214 */     buf.readUnsignedByte();
/* 215 */     buf.readUnsignedShort();
/*     */     
/* 217 */     position.set("versionFw", String.valueOf(buf.readUnsignedShort()));
/*     */     
/* 219 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.format("%015d", new Object[] { Long.valueOf(buf.readLong()) }) });
/* 220 */     if (deviceSession == null) {
/* 221 */       return null;
/*     */     }
/* 223 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 225 */     position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte()));
/* 226 */     position.set("power", Integer.valueOf(buf.readUnsignedShort()));
/*     */     
/* 228 */     buf.readUnsignedByte();
/*     */     
/* 230 */     position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */     
/* 232 */     switch (type) {
/*     */       case 6:
/* 234 */         buf.readUnsignedShort();
/*     */         break;
/*     */       case 45:
/*     */       case 46:
/* 238 */         buf.readUnsignedShort();
/* 239 */         buf.readUnsignedByte();
/* 240 */         buf.readUnsignedInt();
/*     */         break;
/*     */       case 15:
/* 243 */         buf.readUnsignedShort();
/* 244 */         buf.readUnsignedByte();
/*     */         break;
/*     */       case 17:
/* 247 */         buf.readUnsignedInt();
/*     */         break;
/*     */       case 21:
/* 250 */         buf.readUnsignedByte();
/* 251 */         buf.readUnsignedInt();
/*     */         break;
/*     */       case 26:
/* 254 */         buf.readUnsignedShort();
/* 255 */         buf.readUnsignedByte();
/* 256 */         buf.readUnsignedByte();
/* 257 */         buf.readUnsignedInt();
/* 258 */         buf.readUnsignedInt();
/*     */         break;
/*     */       case 31:
/* 261 */         buf.readUnsignedByte();
/* 262 */         buf.readUnsignedByte();
/*     */         break;
/*     */       case 35:
/* 265 */         buf.readUnsignedByte();
/*     */         break;
/*     */       case 33:
/* 268 */         buf.readUnsignedByte();
/*     */         break;
/*     */       case 23:
/* 271 */         buf.readUnsignedByte();
/*     */         break;
/*     */       case 34:
/* 274 */         buf.readUnsignedByte();
/* 275 */         buf.readUnsignedShort();
/*     */         break;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 281 */     buf.readUnsignedByte();
/*     */     
/* 283 */     int hdop = buf.readUnsignedByte();
/* 284 */     position.setValid((hdop > 0));
/* 285 */     position.set("hdop", Integer.valueOf(hdop));
/*     */     
/* 287 */     position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedMedium() * 0.1D));
/* 288 */     position.setCourse(buf.readUnsignedShort());
/* 289 */     position.setAltitude(buf.readShort());
/* 290 */     position.setLongitude(buf.readInt() * 1.0E-6D);
/* 291 */     position.setLatitude(buf.readInt() * 1.0E-6D);
/*     */     
/* 293 */     position.setTime(decodeTime(buf));
/*     */     
/* 295 */     position.setNetwork(new Network(CellTower.from(buf
/* 296 */             .readUnsignedShort(), buf.readUnsignedShort(), buf
/* 297 */             .readUnsignedShort(), buf.readUnsignedShort())));
/*     */     
/* 299 */     buf.readUnsignedByte();
/*     */     
/* 301 */     return position;
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
/*     */   private Position decodeInformation(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 314 */     Position position = new Position(getProtocolName());
/*     */     
/* 316 */     int type = buf.readUnsignedByte();
/*     */     
/* 318 */     buf.readUnsignedInt();
/* 319 */     buf.readUnsignedShort();
/*     */     
/* 321 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.format("%015d", new Object[] { Long.valueOf(buf.readLong()) }) });
/* 322 */     if (deviceSession == null) {
/* 323 */       return null;
/*     */     }
/* 325 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 327 */     buf.readUnsignedByte();
/* 328 */     buf.readUnsignedShort();
/*     */     
/* 330 */     position.set("versionFw", String.valueOf(buf.readUnsignedShort()));
/*     */     
/* 332 */     if (type == 6) {
/* 333 */       buf.readUnsignedShort();
/* 334 */       buf.readUnsignedShort();
/* 335 */       buf.readUnsignedShort();
/*     */     } 
/*     */     
/* 338 */     buf.readUnsignedByte();
/* 339 */     buf.readUnsignedByte();
/*     */     
/* 341 */     position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */     
/* 343 */     buf.readUnsignedByte();
/* 344 */     buf.skipBytes(7);
/* 345 */     buf.readUnsignedByte();
/* 346 */     buf.readUnsignedByte();
/* 347 */     buf.readUnsignedShort();
/* 348 */     buf.readUnsignedShort();
/* 349 */     buf.readUnsignedShort();
/* 350 */     buf.readUnsignedInt();
/* 351 */     buf.readUnsignedByte();
/*     */     
/* 353 */     if (type == 7) {
/* 354 */       position.set("charge", Boolean.valueOf((buf.readUnsignedByte() != 0)));
/* 355 */       position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/* 356 */       position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/* 357 */       position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte()));
/*     */     } 
/*     */     
/* 360 */     buf.skipBytes(10);
/*     */     
/* 362 */     if (type == 5) {
/* 363 */       position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/* 364 */       buf.readUnsignedByte();
/*     */     } 
/*     */     
/* 367 */     buf.readUnsignedByte();
/* 368 */     buf.readUnsignedShort();
/*     */     
/* 370 */     if (type == 10) {
/* 371 */       buf.readUnsignedByte();
/* 372 */       buf.readUnsignedByte();
/* 373 */       position.setNetwork(new Network(CellTower.from(buf
/* 374 */               .readUnsignedShort(), buf.readUnsignedShort(), buf
/* 375 */               .readUnsignedShort(), buf.readUnsignedShort())));
/* 376 */       buf.readUnsignedByte();
/* 377 */       buf.readUnsignedByte();
/*     */     } 
/*     */     
/* 380 */     getLastLocation(position, decodeTime(buf));
/*     */     
/* 382 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 389 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 391 */     switch (buf.readSlice(4).toString(StandardCharsets.US_ASCII)) {
/*     */       case "+RSP":
/* 393 */         return decodeLocation(channel, remoteAddress, buf);
/*     */       case "+INF":
/* 395 */         return decodeInformation(channel, remoteAddress, buf);
/*     */       case "+EVT":
/* 397 */         return decodeEvent(channel, remoteAddress, buf);
/*     */     } 
/* 399 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gl200BinaryProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */