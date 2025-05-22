/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.ObdDecoder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ public class UlbotechProtocolDecoder
/*     */   extends BaseProtocolDecoder {
/*     */   private static final short DATA_GPS = 1;
/*     */   private static final short DATA_LBS = 2;
/*     */   private static final short DATA_STATUS = 3;
/*     */   private static final short DATA_ODOMETER = 4;
/*     */   private static final short DATA_ADC = 5;
/*     */   private static final short DATA_GEOFENCE = 6;
/*     */   private static final short DATA_OBD2 = 7;
/*     */   private static final short DATA_FUEL = 8;
/*     */   private static final short DATA_OBD2_ALARM = 9;
/*     */   private static final short DATA_HARSH_DRIVER = 10;
/*     */   private static final short DATA_CANBUS = 11;
/*     */   private static final short DATA_J1708 = 12;
/*     */   private static final short DATA_VIN = 13;
/*     */   private static final short DATA_RFID = 14;
/*     */   private static final short DATA_EVENT = 16;
/*     */   
/*     */   public UlbotechProtocolDecoder(Protocol protocol) {
/*  45 */     super(protocol);
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
/*     */   private void decodeObd(Position position, ByteBuf buf, int length) {
/*  66 */     int end = buf.readerIndex() + length;
/*     */     
/*  68 */     while (buf.readerIndex() < end) {
/*  69 */       int parameterLength = buf.getUnsignedByte(buf.readerIndex()) >> 4;
/*  70 */       int mode = buf.readUnsignedByte() & 0xF;
/*  71 */       position.add(ObdDecoder.decode(mode, ByteBufUtil.hexDump(buf.readSlice(parameterLength - 1))));
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeJ1708(Position position, ByteBuf buf, int length) {
/*  77 */     int end = buf.readerIndex() + length;
/*     */     
/*  79 */     while (buf.readerIndex() < end) {
/*  80 */       int mark = buf.readUnsignedByte();
/*  81 */       int len = BitUtil.between(mark, 0, 6);
/*  82 */       int type = BitUtil.between(mark, 6, 8);
/*  83 */       int id = buf.readUnsignedByte();
/*  84 */       if (type == 3) {
/*  85 */         id += 256;
/*     */       }
/*  87 */       String value = ByteBufUtil.hexDump(buf.readSlice(len - 1));
/*  88 */       if (type == 2 || type == 3) {
/*  89 */         position.set("pid" + id, value);
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeDriverBehavior(Position position, ByteBuf buf) {
/*  96 */     int value = buf.readUnsignedByte();
/*     */     
/*  98 */     if (BitUtil.check(value, 0)) {
/*  99 */       position.set("rapidAcceleration", Boolean.valueOf(true));
/*     */     }
/* 101 */     if (BitUtil.check(value, 1)) {
/* 102 */       position.set("roughBraking", Boolean.valueOf(true));
/*     */     }
/* 104 */     if (BitUtil.check(value, 2)) {
/* 105 */       position.set("harshCourse", Boolean.valueOf(true));
/*     */     }
/* 107 */     if (BitUtil.check(value, 3)) {
/* 108 */       position.set("noWarmUp", Boolean.valueOf(true));
/*     */     }
/* 110 */     if (BitUtil.check(value, 4)) {
/* 111 */       position.set("longIdle", Boolean.valueOf(true));
/*     */     }
/* 113 */     if (BitUtil.check(value, 5)) {
/* 114 */       position.set("fatigueDriving", Boolean.valueOf(true));
/*     */     }
/* 116 */     if (BitUtil.check(value, 6)) {
/* 117 */       position.set("roughTerrain", Boolean.valueOf(true));
/*     */     }
/* 119 */     if (BitUtil.check(value, 7)) {
/* 120 */       position.set("highRpm", Boolean.valueOf(true));
/*     */     }
/*     */   }
/*     */   
/*     */   private String decodeAlarm(int alarm) {
/* 125 */     if (BitUtil.check(alarm, 0)) {
/* 126 */       return "powerOff";
/*     */     }
/* 128 */     if (BitUtil.check(alarm, 1)) {
/* 129 */       return "movement";
/*     */     }
/* 131 */     if (BitUtil.check(alarm, 2)) {
/* 132 */       return "overspeed";
/*     */     }
/* 134 */     if (BitUtil.check(alarm, 4)) {
/* 135 */       return "geofence";
/*     */     }
/* 137 */     if (BitUtil.check(alarm, 10)) {
/* 138 */       return "sos";
/*     */     }
/* 140 */     return null;
/*     */   }
/*     */   
/*     */   private void decodeAdc(Position position, ByteBuf buf, int length) {
/* 144 */     for (int i = 0; i < length / 2; i++) {
/* 145 */       int value = buf.readUnsignedShort();
/* 146 */       int id = BitUtil.from(value, 12);
/* 147 */       value = BitUtil.to(value, 12);
/* 148 */       switch (id) {
/*     */         case 0:
/* 150 */           position.set("power", Double.valueOf((value * 110) / 4096.0D - 10.0D));
/*     */           break;
/*     */         case 1:
/* 153 */           position.set("temp1", Double.valueOf((value * 180) / 4096.0D - 55.0D));
/*     */           break;
/*     */         case 2:
/* 156 */           position.set("battery", Double.valueOf((value * 110) / 4096.0D - 10.0D));
/*     */           break;
/*     */         case 3:
/* 159 */           position.set("adc1", Double.valueOf((value * 110) / 4096.0D - 10.0D));
/*     */           break;
/*     */         default:
/* 162 */           position.set("io" + id, Integer.valueOf(value));
/*     */           break;
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/* 168 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 169 */     .text("*TS")
/* 170 */     .number("dd,")
/* 171 */     .number("(d{15}),")
/* 172 */     .number("(dd)(dd)(dd)")
/* 173 */     .number("(dd)(dd)(dd),")
/* 174 */     .expression("([^#]+)")
/* 175 */     .text("#")
/* 176 */     .compile();
/*     */ 
/*     */   
/*     */   private Object decodeText(Channel channel, SocketAddress remoteAddress, String sentence) {
/* 180 */     Parser parser = new Parser(PATTERN, sentence);
/* 181 */     if (!parser.matches()) {
/* 182 */       return null;
/*     */     }
/*     */     
/* 185 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 186 */     if (deviceSession == null) {
/* 187 */       return null;
/*     */     }
/*     */     
/* 190 */     Position position = new Position(getProtocolName());
/* 191 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */ 
/*     */     
/* 195 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0)).setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */     
/* 197 */     getLastLocation(position, dateBuilder.getDate());
/*     */     
/* 199 */     position.set("result", parser.next());
/*     */     
/* 201 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Object decodeBinary(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/* 206 */     buf.readUnsignedByte();
/* 207 */     buf.readUnsignedByte();
/* 208 */     buf.readUnsignedByte();
/*     */     
/* 210 */     String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(1);
/*     */     
/* 212 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 213 */     if (deviceSession == null) {
/* 214 */       return null;
/*     */     }
/*     */     
/* 217 */     if (deviceSession.getTimeZone() == null) {
/* 218 */       deviceSession.setTimeZone(getTimeZone(deviceSession.getDeviceId()));
/*     */     }
/*     */     
/* 221 */     Position position = new Position(getProtocolName());
/* 222 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 224 */     long seconds = buf.readUnsignedInt() & 0x7FFFFFFFL;
/* 225 */     seconds += 946684800L;
/* 226 */     seconds -= (deviceSession.getTimeZone().getRawOffset() / 1000);
/* 227 */     Date time = new Date(seconds * 1000L);
/*     */     
/* 229 */     boolean hasLocation = false;
/*     */     
/* 231 */     while (buf.readableBytes() > 3) {
/*     */       
/* 233 */       int status, type = buf.readUnsignedByte();
/* 234 */       int length = (type == 11) ? buf.readUnsignedShort() : buf.readUnsignedByte();
/*     */       
/* 236 */       switch (type) {
/*     */         
/*     */         case 1:
/* 239 */           hasLocation = true;
/* 240 */           position.setValid(true);
/* 241 */           position.setLatitude(buf.readInt() / 1000000.0D);
/* 242 */           position.setLongitude(buf.readInt() / 1000000.0D);
/* 243 */           position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));
/* 244 */           position.setCourse(buf.readUnsignedShort());
/* 245 */           position.set("hdop", Integer.valueOf(buf.readUnsignedShort()));
/*     */           continue;
/*     */         
/*     */         case 2:
/* 249 */           if (length == 11) {
/* 250 */             position.setNetwork(new Network(CellTower.from(buf
/* 251 */                     .readUnsignedShort(), buf.readUnsignedShort(), buf
/* 252 */                     .readUnsignedShort(), buf.readUnsignedInt(), -buf.readUnsignedByte())));
/*     */           } else {
/* 254 */             position.setNetwork(new Network(CellTower.from(buf
/* 255 */                     .readUnsignedShort(), buf.readUnsignedShort(), buf
/* 256 */                     .readUnsignedShort(), buf.readUnsignedShort(), -buf.readUnsignedByte())));
/*     */           } 
/* 258 */           if (length > 9 && length != 11) {
/* 259 */             buf.skipBytes(length - 9);
/*     */           }
/*     */           continue;
/*     */         
/*     */         case 3:
/* 264 */           status = buf.readUnsignedShort();
/* 265 */           position.set("ignition", Boolean.valueOf(BitUtil.check(status, 9)));
/* 266 */           position.set("status", Integer.valueOf(status));
/* 267 */           position.set("alarm", decodeAlarm(buf.readUnsignedShort()));
/*     */           continue;
/*     */         
/*     */         case 4:
/* 271 */           position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*     */           continue;
/*     */         
/*     */         case 5:
/* 275 */           decodeAdc(position, buf, length);
/*     */           continue;
/*     */         
/*     */         case 6:
/* 279 */           position.set("geofenceIn", Long.valueOf(buf.readUnsignedInt()));
/* 280 */           position.set("geofenceAlarm", Long.valueOf(buf.readUnsignedInt()));
/*     */           continue;
/*     */         
/*     */         case 7:
/* 284 */           decodeObd(position, buf, length);
/*     */           continue;
/*     */         
/*     */         case 8:
/* 288 */           position.set("fuelConsumption", Double.valueOf(buf.readUnsignedInt() / 10000.0D));
/*     */           continue;
/*     */         
/*     */         case 9:
/* 292 */           decodeObd(position, buf, length);
/*     */           continue;
/*     */         
/*     */         case 10:
/* 296 */           decodeDriverBehavior(position, buf);
/*     */           continue;
/*     */         
/*     */         case 11:
/* 300 */           position.set("can", ByteBufUtil.hexDump(buf.readSlice(length)));
/*     */           continue;
/*     */         
/*     */         case 12:
/* 304 */           decodeJ1708(position, buf, length);
/*     */           continue;
/*     */         
/*     */         case 13:
/* 308 */           position.set("vin", buf.readSlice(length).toString(StandardCharsets.US_ASCII));
/*     */           continue;
/*     */         
/*     */         case 14:
/* 312 */           position.set("driverUniqueId", buf
/* 313 */               .readSlice(length - 1).toString(StandardCharsets.US_ASCII));
/* 314 */           position.set("authorized", Boolean.valueOf((buf.readUnsignedByte() != 0)));
/*     */           continue;
/*     */         
/*     */         case 16:
/* 318 */           position.set("event", Short.valueOf(buf.readUnsignedByte()));
/* 319 */           if (length > 1) {
/* 320 */             position.set("eventMask", Long.valueOf(buf.readUnsignedInt()));
/*     */           }
/*     */           continue;
/*     */       } 
/*     */       
/* 325 */       buf.skipBytes(length);
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 330 */     if (!hasLocation) {
/* 331 */       getLastLocation(position, time);
/*     */     } else {
/* 333 */       position.setTime(time);
/*     */     } 
/*     */     
/* 336 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 343 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 345 */     if (buf.getUnsignedByte(buf.readerIndex()) == 248) {
/*     */       
/* 347 */       if (channel != null) {
/* 348 */         ByteBuf response = Unpooled.buffer();
/* 349 */         response.writeByte(248);
/* 350 */         response.writeByte(1);
/* 351 */         response.writeByte(254);
/* 352 */         response.writeShort(buf.getShort(response.writerIndex() - 1 - 2));
/* 353 */         response.writeShort(Checksum.crc16(Checksum.CRC16_XMODEM, response.nioBuffer(1, 4)));
/* 354 */         response.writeByte(248);
/* 355 */         channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */       } 
/*     */       
/* 358 */       return decodeBinary(channel, remoteAddress, buf);
/*     */     } 
/*     */     
/* 361 */     if (channel != null) {
/* 362 */       channel.writeAndFlush(new NetworkMessage(Unpooled.copiedBuffer(String.format("*TS01,ACK:%04X#", new Object[] {
/* 363 */                   Integer.valueOf(Checksum.crc16(Checksum.CRC16_XMODEM, buf.nioBuffer(1, buf.writerIndex() - 2)))
/*     */                 }), StandardCharsets.US_ASCII), remoteAddress));
/*     */     }
/*     */     
/* 367 */     return decodeText(channel, remoteAddress, buf.toString(StandardCharsets.US_ASCII));
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\UlbotechProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */