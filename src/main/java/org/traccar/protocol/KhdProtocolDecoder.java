/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BcdUtil;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class KhdProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_LOGIN = 177;
/*     */   public static final int MSG_CONFIRMATION = 33;
/*     */   public static final int MSG_ON_DEMAND = 129;
/*     */   public static final int MSG_POSITION_UPLOAD = 128;
/*     */   public static final int MSG_POSITION_REUPLOAD = 142;
/*     */   public static final int MSG_ALARM = 130;
/*     */   public static final int MSG_ADMIN_NUMBER = 131;
/*     */   public static final int MSG_SEND_TEXT = 132;
/*     */   public static final int MSG_REPLY = 133;
/*     */   public static final int MSG_SMS_ALARM_SWITCH = 134;
/*     */   public static final int MSG_PERIPHERAL = 163;
/*     */   
/*     */   public KhdProtocolDecoder(Protocol protocol) {
/*  41 */     super(protocol);
/*     */   }
/*     */   
/*     */   private String[] readIdentifiers(ByteBuf buf) {
/*  45 */     String[] identifiers = new String[2];
/*     */     
/*  47 */     String uniqueId1 = ByteBufUtil.hexDump(buf, buf.readerIndex(), 4);
/*     */     
/*  49 */     int b1 = buf.readUnsignedByte();
/*  50 */     int b2 = buf.readUnsignedByte() - 128;
/*  51 */     int b3 = buf.readUnsignedByte() - 128;
/*  52 */     int b4 = buf.readUnsignedByte();
/*  53 */     String uniqueId2 = String.format("%02d%02d%02d%02d", new Object[] { Integer.valueOf(b1), Integer.valueOf(b2), Integer.valueOf(b3), Integer.valueOf(b4) });
/*     */     
/*  55 */     if (Context.getConfig().getBoolean(getProtocolName() + ".preferDecimalId")) {
/*  56 */       identifiers[0] = uniqueId2;
/*  57 */       identifiers[1] = uniqueId1;
/*     */     } else {
/*  59 */       identifiers[0] = uniqueId1;
/*  60 */       identifiers[1] = uniqueId2;
/*     */     } 
/*     */     
/*  63 */     return identifiers;
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
/*     */   private void decodeAlarmStatus(Position position, byte[] status) {
/*  79 */     if (BitUtil.check(status[0], 0)) {
/*  80 */       position.set("alarm", "ignitionTrigger");
/*  81 */     } else if (BitUtil.check(status[0], 4)) {
/*  82 */       position.set("alarm", "lowPower");
/*  83 */     } else if (BitUtil.check(status[0], 6)) {
/*  84 */       position.set("alarm", "geofenceExit");
/*  85 */     } else if (BitUtil.check(status[0], 7)) {
/*  86 */       position.set("alarm", "geofenceEnter");
/*  87 */     } else if (BitUtil.check(status[1], 0)) {
/*  88 */       position.set("alarm", "sos");
/*  89 */     } else if (BitUtil.check(status[1], 1)) {
/*  90 */       position.set("alarm", "overspeed");
/*  91 */     } else if (BitUtil.check(status[1], 2)) {
/*  92 */       position.set("armed", Boolean.valueOf(false));
/*  93 */       position.set("alarmMode", Boolean.valueOf(false));
/*  94 */     } else if (BitUtil.check(status[1], 3)) {
/*  95 */       position.set("alarm", "powerCut");
/*  96 */     } else if (BitUtil.check(status[1], 4)) {
/*  97 */       position.set("armed", Boolean.valueOf(true));
/*  98 */       position.set("alarmMode", Boolean.valueOf(true));
/*  99 */     } else if (BitUtil.check(status[1], 5)) {
/* 100 */       position.set("alarm", "sensorHeavyTrigger");
/* 101 */     } else if (BitUtil.check(status[1], 6)) {
/* 102 */       position.set("alarm", "tow");
/* 103 */     } else if (BitUtil.check(status[1], 7)) {
/* 104 */       position.set("alarm", "door");
/* 105 */     } else if (BitUtil.check(status[2], 2)) {
/* 106 */       position.set("alarm", "temperature");
/* 107 */     } else if (BitUtil.check(status[2], 4)) {
/* 108 */       position.set("alarm", "tampering");
/* 109 */     } else if (BitUtil.check(status[2], 6)) {
/* 110 */       position.set("alarm", "fatigueDriving");
/* 111 */     } else if (BitUtil.check(status[2], 7)) {
/* 112 */       position.set("alarm", "idle");
/* 113 */     } else if (BitUtil.check(status[4], 0)) {
/* 114 */       if (BitUtil.check(status[7], 7)) {
/* 115 */         position.set("alarm", "accOff");
/*     */       } else {
/* 117 */         position.set("alarm", "accOn");
/*     */       } 
/* 119 */     } else if (BitUtil.check(status[4], 1)) {
/* 120 */       position.set("alarm", "valetMode");
/* 121 */     } else if (BitUtil.check(status[4], 2)) {
/* 122 */       if (BitUtil.check(status[7], 7)) {
/* 123 */         position.set("alarm", "ack");
/*     */       } else {
/* 125 */         position.set("alarm", "carAlarmDisconnect");
/*     */       } 
/* 127 */     } else if (BitUtil.check(status[4], 3)) {
/* 128 */       position.set("alarm", "armModeAndDoorTrigger");
/* 129 */     } else if (BitUtil.check(status[6], 3)) {
/* 130 */       position.set("alarm", "vibration");
/* 131 */     } else if (BitUtil.check(status[6], 4)) {
/* 132 */       position.set("alarm", "hardBraking");
/* 133 */     } else if (BitUtil.check(status[6], 5)) {
/* 134 */       position.set("alarm", "hardAcceleration");
/* 135 */     } else if (BitUtil.check(status[6], 6)) {
/* 136 */       position.set("alarm", "hardCornering");
/* 137 */     } else if (BitUtil.check(status[6], 7)) {
/* 138 */       position.set("alarm", "accident");
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 146 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 148 */     buf.skipBytes(2);
/* 149 */     int type = buf.readUnsignedByte();
/* 150 */     buf.readUnsignedShort();
/*     */     
/* 152 */     if (type == 177 || type == 131 || type == 132 || type == 134 || type == 142) {
/*     */ 
/*     */       
/* 155 */       ByteBuf response = Unpooled.buffer();
/* 156 */       response.writeByte(41);
/* 157 */       response.writeByte(41);
/* 158 */       response.writeByte(33);
/* 159 */       response.writeShort(5);
/* 160 */       response.writeByte(buf.getByte(buf.writerIndex() - 2));
/* 161 */       response.writeByte(type);
/* 162 */       response.writeByte((buf.writerIndex() > 9) ? buf.getByte(9) : 0);
/* 163 */       response.writeByte(Checksum.xor(response.nioBuffer()));
/* 164 */       response.writeByte(13);
/*     */       
/* 166 */       if (channel != null) {
/* 167 */         channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/* 172 */     if (type == 129 || type == 128 || type == 142 || type == 130 || type == 133 || type == 163) {
/*     */ 
/*     */       
/* 175 */       Position position = new Position(getProtocolName());
/*     */       
/* 177 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, readIdentifiers(buf));
/* 178 */       if (deviceSession == null) {
/* 179 */         return null;
/*     */       }
/* 181 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 189 */       DateBuilder dateBuilder = (new DateBuilder()).setYear(BcdUtil.readInteger(buf, 2)).setMonth(BcdUtil.readInteger(buf, 2)).setDay(BcdUtil.readInteger(buf, 2)).setHour(BcdUtil.readInteger(buf, 2)).setMinute(BcdUtil.readInteger(buf, 2)).setSecond(BcdUtil.readInteger(buf, 2));
/* 190 */       position.setTime(dateBuilder.getDate());
/*     */       
/* 192 */       position.setLatitude(BcdUtil.readCoordinate(buf));
/* 193 */       position.setLongitude(BcdUtil.readCoordinate(buf));
/* 194 */       position.setSpeed(UnitsConverter.knotsFromKph(BcdUtil.readInteger(buf, 4)));
/* 195 */       position.setCourse(BcdUtil.readInteger(buf, 4));
/* 196 */       position.setValid(((buf.readUnsignedByte() & 0x80) != 0));
/*     */       
/* 198 */       if (type != 130) {
/*     */         
/* 200 */         int odometer = buf.readUnsignedMedium();
/* 201 */         if (BitUtil.to(odometer, 16) > 0) {
/* 202 */           position.set("odometer", Integer.valueOf(odometer));
/* 203 */         } else if (odometer > 0) {
/* 204 */           position.set("fuel", Integer.valueOf(BitUtil.from(odometer, 16)));
/*     */         } 
/*     */         
/* 207 */         long status = buf.readUnsignedInt();
/* 208 */         String binaryStatus = String.format("%32s", new Object[] { Long.toBinaryString(status) }).replace(" ", "0");
/* 209 */         position.set("status", binaryStatus);
/* 210 */         position.set("blocked", Boolean.valueOf(!BitUtil.check(status, 26)));
/* 211 */         position.set("lock", Boolean.valueOf(!BitUtil.check(status, 2)));
/*     */         
/* 213 */         buf.readUnsignedShort();
/* 214 */         buf.readUnsignedByte();
/* 215 */         buf.readUnsignedByte();
/* 216 */         buf.readUnsignedByte();
/* 217 */         buf.readUnsignedByte();
/* 218 */         buf.readUnsignedByte();
/*     */         
/* 220 */         position.set("result", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/* 222 */         if (type == 163) {
/*     */           int i; Network network; int count, j;
/* 224 */           buf.readUnsignedShort();
/*     */           
/* 226 */           int dataType = buf.readUnsignedByte();
/*     */           
/* 228 */           buf.readUnsignedByte();
/*     */           
/* 230 */           switch (dataType) {
/*     */             case 1:
/* 232 */               position.set("fuel", 
/* 233 */                   Integer.valueOf(buf.readUnsignedByte() * 100 + buf.readUnsignedByte()));
/*     */               break;
/*     */             case 2:
/* 236 */               position.set("temp1", 
/* 237 */                   Integer.valueOf(buf.readUnsignedByte() * 100 + buf.readUnsignedByte()));
/*     */               break;
/*     */             case 24:
/* 240 */               for (i = 1; i <= 4; i++) {
/* 241 */                 double value = buf.readUnsignedShort();
/* 242 */                 if (value > 0.0D && value < 65535.0D) {
/* 243 */                   position.set("fuel" + i, Double.valueOf(value / 65534.0D));
/*     */                 }
/*     */               } 
/*     */               break;
/*     */             case 32:
/* 248 */               position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte()));
/*     */               break;
/*     */             case 35:
/* 251 */               network = new Network();
/* 252 */               count = buf.readUnsignedByte();
/* 253 */               for (j = 0; j < count; j++) {
/* 254 */                 network.addCellTower(CellTower.from(buf
/* 255 */                       .readUnsignedShort(), buf.readUnsignedByte(), buf
/* 256 */                       .readUnsignedShort(), buf.readUnsignedShort(), buf.readUnsignedByte()));
/*     */               }
/* 258 */               if (count > 0) {
/* 259 */                 position.setNetwork(network);
/*     */               }
/*     */               break;
/*     */           } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*     */         } 
/*     */       } else {
/* 270 */         buf.readUnsignedByte();
/* 271 */         buf.readUnsignedByte();
/*     */         
/* 273 */         byte[] alarmStatus = new byte[8];
/* 274 */         buf.readBytes(alarmStatus);
/*     */         
/* 276 */         decodeAlarmStatus(position, alarmStatus);
/*     */       } 
/*     */ 
/*     */       
/* 280 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 284 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\KhdProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */