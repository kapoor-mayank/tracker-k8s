/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ public class HuaShengProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_POSITION = 43520;
/*     */   public static final int MSG_POSITION_RSP = 65281;
/*     */   public static final int MSG_LOGIN = 43522;
/*     */   public static final int MSG_LOGIN_RSP = 65283;
/*     */   public static final int MSG_UPFAULT = 43538;
/*     */   public static final int MSG_UPFAULT_RSP = 65299;
/*     */   public static final int MSG_HSO_REQ = 2;
/*     */   public static final int MSG_HSO_RSP = 3;
/*     */   
/*     */   public HuaShengProtocolDecoder(Protocol protocol) {
/*  40 */     super(protocol);
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
/*     */   private void sendResponse(Channel channel, int type, int index, ByteBuf content) {
/*  53 */     if (channel != null) {
/*  54 */       ByteBuf response = Unpooled.buffer();
/*  55 */       response.writeByte(192);
/*  56 */       response.writeShort(256);
/*  57 */       response.writeShort(12 + ((content != null) ? content.readableBytes() : 0));
/*  58 */       response.writeShort(type);
/*  59 */       response.writeShort(0);
/*  60 */       response.writeInt(index);
/*  61 */       if (content != null) {
/*  62 */         response.writeBytes(content);
/*  63 */         content.release();
/*     */       } 
/*  65 */       response.writeByte(192);
/*  66 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */   
/*     */   private String decodeAlarm(int event) {
/*  71 */     switch (event) {
/*     */       case 4:
/*  73 */         return "fatigueDriving";
/*     */       case 6:
/*  75 */         return "sos";
/*     */       case 7:
/*  77 */         return "hardBraking";
/*     */       case 8:
/*  79 */         return "hardAcceleration";
/*     */       case 9:
/*  81 */         return "hardCornering";
/*     */       case 10:
/*  83 */         return "accident";
/*     */       case 16:
/*  85 */         return "removing";
/*     */     } 
/*  87 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  95 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  97 */     buf.skipBytes(1);
/*  98 */     buf.readUnsignedByte();
/*  99 */     buf.readUnsignedByte();
/* 100 */     buf.readUnsignedShort();
/*     */     
/* 102 */     int type = buf.readUnsignedShort();
/*     */     
/* 104 */     buf.readUnsignedShort();
/* 105 */     int index = buf.readInt();
/*     */     
/* 107 */     if (type == 43522) {
/*     */       
/* 109 */       while (buf.readableBytes() > 4) {
/* 110 */         int subtype = buf.readUnsignedShort();
/* 111 */         int length = buf.readUnsignedShort() - 4;
/* 112 */         if (subtype == 3) {
/* 113 */           String imei = buf.readCharSequence(length, StandardCharsets.US_ASCII).toString();
/* 114 */           DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 115 */           if (deviceSession != null && channel != null) {
/* 116 */             ByteBuf content = Unpooled.buffer();
/* 117 */             content.writeByte(0);
/* 118 */             sendResponse(channel, 65283, index, content);
/*     */           }  continue;
/*     */         } 
/* 121 */         buf.skipBytes(length);
/*     */       }
/*     */     
/*     */     }
/* 125 */     else if (type == 2) {
/*     */       
/* 127 */       sendResponse(channel, 3, index, (ByteBuf)null);
/*     */     } else {
/* 129 */       if (type == 43538)
/*     */       {
/* 131 */         return decodeFaultCodes(channel, remoteAddress, buf, index);
/*     */       }
/* 133 */       if (type == 43520)
/*     */       {
/* 135 */         return decodePosition(channel, remoteAddress, buf, index);
/*     */       }
/*     */     } 
/*     */     
/* 139 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeFaultCodes(Channel channel, SocketAddress remoteAddress, ByteBuf buf, int index) {
/* 145 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 146 */     if (deviceSession == null) {
/* 147 */       return null;
/*     */     }
/*     */     
/* 150 */     Position position = new Position(getProtocolName());
/* 151 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 153 */     getLastLocation(position, null);
/*     */     
/* 155 */     buf.readUnsignedShort();
/* 156 */     buf.readUnsignedShort();
/*     */     
/* 158 */     StringBuilder codes = new StringBuilder();
/* 159 */     while (buf.readableBytes() > 2) {
/* 160 */       char prefix; String value = ByteBufUtil.hexDump(buf.readSlice(2));
/* 161 */       int digit = Integer.parseInt(value.substring(0, 1), 16);
/*     */       
/* 163 */       switch (digit >> 2) {
/*     */         default:
/* 165 */           prefix = 'P';
/*     */           break;
/*     */         case 1:
/* 168 */           prefix = 'C';
/*     */           break;
/*     */         case 2:
/* 171 */           prefix = 'B';
/*     */           break;
/*     */         case 3:
/* 174 */           prefix = 'U';
/*     */           break;
/*     */       } 
/* 177 */       codes.append(prefix).append(digit % 4).append(value.substring(1));
/* 178 */       if (buf.readableBytes() > 2) {
/* 179 */         codes.append(' ');
/*     */       }
/*     */     } 
/*     */     
/* 183 */     position.set("dtcs", codes.toString());
/*     */     
/* 185 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodePosition(Channel channel, SocketAddress remoteAddress, ByteBuf buf, int index) {
/* 191 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 192 */     if (deviceSession == null) {
/* 193 */       return null;
/*     */     }
/*     */     
/* 196 */     Position position = new Position(getProtocolName());
/* 197 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 199 */     int status = buf.readUnsignedShort();
/*     */     
/* 201 */     position.setValid(BitUtil.check(status, 15));
/*     */     
/* 203 */     position.set("status", Integer.valueOf(status));
/* 204 */     position.set("ignition", Boolean.valueOf(BitUtil.check(status, 14)));
/*     */     
/* 206 */     int event = buf.readUnsignedShort();
/* 207 */     position.set("alarm", decodeAlarm(event));
/* 208 */     position.set("event", Integer.valueOf(event));
/*     */     
/* 210 */     String time = buf.readCharSequence(12, StandardCharsets.US_ASCII).toString();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 218 */     DateBuilder dateBuilder = (new DateBuilder()).setYear(Integer.parseInt(time.substring(0, 2))).setMonth(Integer.parseInt(time.substring(2, 4))).setDay(Integer.parseInt(time.substring(4, 6))).setHour(Integer.parseInt(time.substring(6, 8))).setMinute(Integer.parseInt(time.substring(8, 10))).setSecond(Integer.parseInt(time.substring(10, 12)));
/* 219 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 221 */     position.setLongitude(buf.readInt() * 1.0E-5D);
/* 222 */     position.setLatitude(buf.readInt() * 1.0E-5D);
/*     */     
/* 224 */     position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));
/* 225 */     position.setCourse(buf.readUnsignedShort());
/* 226 */     position.setAltitude(buf.readUnsignedShort());
/*     */     
/* 228 */     position.set("odometer", Integer.valueOf(buf.readUnsignedShort() * 1000));
/*     */     
/* 230 */     Network network = new Network();
/*     */     
/* 232 */     while (buf.readableBytes() > 4) {
/* 233 */       int coolantTemp, rpm; String[] cells, points; int subtype = buf.readUnsignedShort();
/* 234 */       int length = buf.readUnsignedShort() - 4;
/* 235 */       switch (subtype) {
/*     */         case 1:
/* 237 */           coolantTemp = buf.readUnsignedByte() - 40;
/* 238 */           position.set("coolantTemp", 
/* 239 */               Integer.valueOf((position.getSpeed() > 0.0D && coolantTemp <= 215) ? coolantTemp : 0));
/* 240 */           rpm = buf.readUnsignedShort();
/* 241 */           position.set("rpm", 
/* 242 */               Integer.valueOf((position.getSpeed() > 0.0D && rpm <= 65535) ? rpm : 0));
/* 243 */           position.set("averageSpeed", Short.valueOf(buf.readUnsignedByte()));
/* 244 */           buf.readUnsignedShort();
/* 245 */           position.set("fuelConsumption", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/* 246 */           position.set("tripOdometer", Integer.valueOf(buf.readUnsignedShort()));
/* 247 */           position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/* 248 */           position.set("fuel", Double.valueOf(buf.readUnsignedByte() * 0.4D));
/* 249 */           buf.readUnsignedInt();
/*     */           continue;
/*     */         case 5:
/* 252 */           position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/* 253 */           position.set("hdop", Short.valueOf(buf.readUnsignedByte()));
/* 254 */           buf.readUnsignedInt();
/*     */           continue;
/*     */         case 9:
/* 257 */           position.set("vin", buf
/* 258 */               .readCharSequence(length, StandardCharsets.US_ASCII).toString());
/*     */           continue;
/*     */         case 17:
/* 261 */           position.set("hours", Double.valueOf(buf.readUnsignedInt() * 0.05D));
/*     */           continue;
/*     */         case 20:
/* 264 */           position.set("engineLoad", Double.valueOf(buf.readUnsignedByte() / 255.0D));
/* 265 */           position.set("timingAdvance", Double.valueOf(buf.readUnsignedByte() * 0.5D));
/* 266 */           position.set("airTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
/* 267 */           position.set("airFlow", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/* 268 */           position.set("throttle", Double.valueOf(buf.readUnsignedByte() / 255.0D));
/*     */           continue;
/*     */         
/*     */         case 32:
/* 272 */           cells = buf.readCharSequence(length, StandardCharsets.US_ASCII).toString().split("\\+");
/* 273 */           for (String cell : cells) {
/* 274 */             String[] values = cell.split("@");
/* 275 */             network.addCellTower(CellTower.from(
/* 276 */                   Integer.parseInt(values[0]), Integer.parseInt(values[1]), 
/* 277 */                   Integer.parseInt(values[2], 16), Integer.parseInt(values[3], 16)));
/*     */           } 
/*     */           continue;
/*     */         
/*     */         case 33:
/* 282 */           points = buf.readCharSequence(length, StandardCharsets.US_ASCII).toString().split("\\+");
/* 283 */           for (String point : points) {
/* 284 */             String[] values = point.split("@");
/* 285 */             network.addWifiAccessPoint(WifiAccessPoint.from(values[0], Integer.parseInt(values[1])));
/*     */           } 
/*     */           continue;
/*     */       } 
/* 289 */       buf.skipBytes(length);
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 294 */     if (network.getCellTowers() != null || network.getWifiAccessPoints() != null) {
/* 295 */       position.setNetwork(network);
/*     */     }
/*     */     
/* 298 */     sendResponse(channel, 65281, index, (ByteBuf)null);
/*     */     
/* 300 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\HuaShengProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */