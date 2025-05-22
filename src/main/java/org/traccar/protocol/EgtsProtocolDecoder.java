/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ public class EgtsProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int PT_RESPONSE = 0;
/*     */   public static final int PT_APPDATA = 1;
/*     */   public static final int PT_SIGNED_APPDATA = 2;
/*     */   public static final int SERVICE_AUTH = 1;
/*     */   public static final int SERVICE_TELEDATA = 2;
/*     */   public static final int SERVICE_COMMANDS = 4;
/*     */   public static final int SERVICE_FIRMWARE = 9;
/*     */   public static final int SERVICE_ECALL = 10;
/*     */   public static final int MSG_RECORD_RESPONSE = 0;
/*     */   public static final int MSG_TERM_IDENTITY = 1;
/*     */   public static final int MSG_MODULE_DATA = 2;
/*     */   public static final int MSG_VEHICLE_DATA = 3;
/*     */   public static final int MSG_AUTH_PARAMS = 4;
/*     */   public static final int MSG_AUTH_INFO = 5;
/*     */   
/*     */   public EgtsProtocolDecoder(Protocol protocol) {
/*  39 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static final int MSG_SERVICE_INFO = 6;
/*     */ 
/*     */   
/*     */   public static final int MSG_RESULT_CODE = 7;
/*     */   
/*     */   public static final int MSG_POS_DATA = 16;
/*     */   
/*     */   public static final int MSG_EXT_POS_DATA = 17;
/*     */   
/*     */   public static final int MSG_AD_SENSORS_DATA = 18;
/*     */   
/*     */   public static final int MSG_COUNTERS_DATA = 19;
/*     */   
/*     */   public static final int MSG_STATE_DATA = 20;
/*     */   
/*     */   public static final int MSG_LOOPIN_DATA = 22;
/*     */   
/*     */   public static final int MSG_ABS_DIG_SENS_DATA = 23;
/*     */   
/*     */   public static final int MSG_ABS_AN_SENS_DATA = 24;
/*     */   
/*     */   public static final int MSG_ABS_CNTR_DATA = 25;
/*     */   
/*     */   public static final int MSG_ABS_LOOPIN_DATA = 26;
/*     */   
/*     */   public static final int MSG_LIQUID_LEVEL_SENSOR = 27;
/*     */   
/*     */   public static final int MSG_PASSENGERS_COUNTERS = 28;
/*     */   
/*     */   private int packetId;
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, int packetType, int index, int serviceType, int type, ByteBuf content) {
/*  77 */     if (channel != null) {
/*     */       
/*  79 */       ByteBuf data = Unpooled.buffer();
/*  80 */       data.writeByte(type);
/*  81 */       data.writeShortLE(content.readableBytes());
/*  82 */       data.writeBytes(content);
/*  83 */       content.release();
/*     */       
/*  85 */       ByteBuf record = Unpooled.buffer();
/*  86 */       if (packetType == 0) {
/*  87 */         record.writeShortLE(index);
/*  88 */         record.writeByte(0);
/*     */       } 
/*  90 */       record.writeShortLE(data.readableBytes());
/*  91 */       record.writeShortLE(0);
/*  92 */       record.writeByte(0);
/*  93 */       record.writeByte(serviceType);
/*  94 */       record.writeByte(serviceType);
/*  95 */       record.writeBytes(data);
/*  96 */       data.release();
/*  97 */       int recordChecksum = Checksum.crc16(Checksum.CRC16_CCITT_FALSE, record.nioBuffer());
/*     */       
/*  99 */       ByteBuf response = Unpooled.buffer();
/* 100 */       response.writeByte(1);
/* 101 */       response.writeByte(0);
/* 102 */       response.writeByte(0);
/* 103 */       response.writeByte(11);
/* 104 */       response.writeByte(0);
/* 105 */       response.writeShortLE(record.readableBytes());
/* 106 */       response.writeShortLE(this.packetId++);
/* 107 */       response.writeByte(packetType);
/* 108 */       response.writeByte(Checksum.crc8(Checksum.CRC8_EGTS, response.nioBuffer()));
/* 109 */       response.writeBytes(record);
/* 110 */       record.release();
/* 111 */       response.writeShortLE(recordChecksum);
/*     */       
/* 113 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 122 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 124 */     int index = buf.getUnsignedShort(buf.readerIndex() + 5 + 2);
/* 125 */     buf.skipBytes(buf.getUnsignedByte(buf.readerIndex() + 3));
/*     */     
/* 127 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 129 */     while (buf.readableBytes() > 2) {
/*     */       
/* 131 */       int length = buf.readUnsignedShortLE();
/* 132 */       int recordIndex = buf.readUnsignedShortLE();
/* 133 */       int recordFlags = buf.readUnsignedByte();
/*     */       
/* 135 */       if (BitUtil.check(recordFlags, 0)) {
/* 136 */         buf.readUnsignedIntLE();
/*     */       }
/*     */       
/* 139 */       if (BitUtil.check(recordFlags, 1)) {
/* 140 */         buf.readUnsignedIntLE();
/*     */       }
/* 142 */       if (BitUtil.check(recordFlags, 2)) {
/* 143 */         buf.readUnsignedIntLE();
/*     */       }
/*     */       
/* 146 */       int serviceType = buf.readUnsignedByte();
/* 147 */       buf.readUnsignedByte();
/*     */       
/* 149 */       int recordEnd = buf.readerIndex() + length;
/*     */       
/* 151 */       Position position = new Position(getProtocolName());
/* 152 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 153 */       if (deviceSession != null) {
/* 154 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */       }
/*     */       
/* 157 */       ByteBuf response = Unpooled.buffer();
/* 158 */       response.writeShortLE(recordIndex);
/* 159 */       response.writeByte(0);
/* 160 */       sendResponse(channel, 0, index, serviceType, 0, response);
/*     */       
/* 162 */       while (buf.readerIndex() < recordEnd) {
/* 163 */         int type = buf.readUnsignedByte();
/* 164 */         int end = buf.readUnsignedShortLE() + buf.readerIndex();
/*     */         
/* 166 */         if (type == 1) {
/*     */           
/* 168 */           buf.readUnsignedIntLE();
/* 169 */           int flags = buf.readUnsignedByte();
/*     */           
/* 171 */           if (BitUtil.check(flags, 0)) {
/* 172 */             buf.readUnsignedShortLE();
/*     */           }
/* 174 */           if (BitUtil.check(flags, 1)) {
/* 175 */             getDeviceSession(channel, remoteAddress, new String[] { buf
/* 176 */                   .readSlice(15).toString(StandardCharsets.US_ASCII).trim() });
/*     */           }
/* 178 */           if (BitUtil.check(flags, 2)) {
/* 179 */             getDeviceSession(channel, remoteAddress, new String[] { buf
/* 180 */                   .readSlice(16).toString(StandardCharsets.US_ASCII).trim() });
/*     */           }
/* 182 */           if (BitUtil.check(flags, 3)) {
/* 183 */             buf.skipBytes(3);
/*     */           }
/* 185 */           if (BitUtil.check(flags, 5)) {
/* 186 */             buf.skipBytes(3);
/*     */           }
/* 188 */           if (BitUtil.check(flags, 6)) {
/* 189 */             buf.readUnsignedShortLE();
/*     */           }
/* 191 */           if (BitUtil.check(flags, 7)) {
/* 192 */             getDeviceSession(channel, remoteAddress, new String[] { buf
/* 193 */                   .readSlice(15).toString(StandardCharsets.US_ASCII).trim() });
/*     */           }
/*     */           
/* 196 */           response = Unpooled.buffer();
/* 197 */           response.writeByte(0);
/* 198 */           sendResponse(channel, 1, 0, serviceType, 7, response);
/*     */         }
/* 200 */         else if (type == 16) {
/*     */           
/* 202 */           position.setTime(new Date((buf.readUnsignedIntLE() + 1262304000L) * 1000L));
/* 203 */           position.setLatitude(buf.readUnsignedIntLE() * 90.0D / 4.294967295E9D);
/* 204 */           position.setLongitude(buf.readUnsignedIntLE() * 180.0D / 4.294967295E9D);
/*     */           
/* 206 */           int flags = buf.readUnsignedByte();
/* 207 */           position.setValid(BitUtil.check(flags, 0));
/* 208 */           if (BitUtil.check(flags, 5)) {
/* 209 */             position.setLatitude(-position.getLatitude());
/*     */           }
/* 211 */           if (BitUtil.check(flags, 6)) {
/* 212 */             position.setLongitude(-position.getLongitude());
/*     */           }
/*     */           
/* 215 */           int speed = buf.readUnsignedShortLE();
/* 216 */           position.setSpeed(UnitsConverter.knotsFromKph(BitUtil.to(speed, 14) * 0.1D));
/* 217 */           position.setCourse((buf.readUnsignedByte() + (BitUtil.check(speed, 15) ? 256 : 0)));
/*     */           
/* 219 */           position.set("odometer", Integer.valueOf(buf.readUnsignedMediumLE() * 100));
/* 220 */           position.set("input", Short.valueOf(buf.readUnsignedByte()));
/* 221 */           position.set("event", Short.valueOf(buf.readUnsignedByte()));
/*     */           
/* 223 */           if (BitUtil.check(flags, 7)) {
/* 224 */             position.setAltitude(buf.readMediumLE());
/*     */           }
/*     */         }
/* 227 */         else if (type == 17) {
/*     */           
/* 229 */           int flags = buf.readUnsignedByte();
/*     */           
/* 231 */           if (BitUtil.check(flags, 0)) {
/* 232 */             position.set("vdop", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */           }
/* 234 */           if (BitUtil.check(flags, 1)) {
/* 235 */             position.set("hdop", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */           }
/* 237 */           if (BitUtil.check(flags, 2)) {
/* 238 */             position.set("pdop", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */           }
/* 240 */           if (BitUtil.check(flags, 3)) {
/* 241 */             position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */           }
/*     */         }
/* 244 */         else if (type == 18) {
/*     */           
/* 246 */           buf.readUnsignedByte();
/*     */           
/* 248 */           position.set("output", Short.valueOf(buf.readUnsignedByte()));
/*     */           
/* 250 */           buf.readUnsignedByte();
/*     */         } 
/*     */ 
/*     */         
/* 254 */         buf.readerIndex(end);
/*     */       } 
/*     */       
/* 257 */       if (serviceType == 2 && deviceSession != null) {
/* 258 */         positions.add(position);
/*     */       }
/*     */     } 
/*     */     
/* 262 */     return positions.isEmpty() ? null : positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EgtsProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */