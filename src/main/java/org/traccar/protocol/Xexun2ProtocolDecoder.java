/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
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
/*     */ public class Xexun2ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_POSITION = 20;
/*     */   
/*     */   public Xexun2ProtocolDecoder(Protocol protocol) {
/*  41 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, int type, int index, ByteBuf imei) {
/*  47 */     if (channel != null) {
/*  48 */       ByteBuf response = Unpooled.buffer();
/*  49 */       response.writeByte(250);
/*  50 */       response.writeByte(175);
/*     */       
/*  52 */       response.writeShort(type);
/*  53 */       response.writeShort(index);
/*  54 */       response.writeBytes(imei);
/*  55 */       response.writeShort(1);
/*  56 */       response.writeShort(65534);
/*  57 */       response.writeByte(1);
/*     */       
/*  59 */       response.writeByte(250);
/*  60 */       response.writeByte(175);
/*     */       
/*  62 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */   
/*     */   private String decodeAlarm(long value) {
/*  67 */     if (BitUtil.check(value, 0)) {
/*  68 */       return "sos";
/*     */     }
/*  70 */     if (BitUtil.check(value, 1)) {
/*  71 */       return "removing";
/*     */     }
/*  73 */     if (BitUtil.check(value, 15)) {
/*  74 */       return "fallDown";
/*     */     }
/*  76 */     return null;
/*     */   }
/*     */   
/*     */   private double convertCoordinate(double value) {
/*  80 */     double degrees = Math.floor(value / 100.0D);
/*  81 */     double minutes = value - degrees * 100.0D;
/*  82 */     return degrees + minutes / 60.0D;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  89 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  91 */     buf.skipBytes(2);
/*  92 */     int type = buf.readUnsignedShort();
/*  93 */     int index = buf.readUnsignedShort();
/*     */     
/*  95 */     ByteBuf imei = buf.readSlice(8);
/*  96 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] {
/*  97 */           ByteBufUtil.hexDump(imei).substring(0, 15) });
/*  98 */     if (deviceSession == null) {
/*  99 */       return null;
/*     */     }
/*     */     
/* 102 */     sendResponse(channel, type, index, imei);
/*     */     
/* 104 */     buf.readUnsignedShort();
/* 105 */     buf.readUnsignedShort();
/*     */     
/* 107 */     if (type == 20) {
/* 108 */       List<Integer> lengths = new ArrayList<>();
/* 109 */       List<Position> positions = new ArrayList<>();
/*     */       
/* 111 */       int count = buf.readUnsignedByte(); int i;
/* 112 */       for (i = 0; i < count; i++) {
/* 113 */         lengths.add(Integer.valueOf(buf.readUnsignedShort()));
/*     */       }
/*     */       
/* 116 */       for (i = 0; i < count; i++) {
/* 117 */         int endIndex = buf.readerIndex() + ((Integer)lengths.get(i)).intValue();
/*     */         
/* 119 */         Position position = new Position(getProtocolName());
/* 120 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 122 */         position.set("index", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/* 124 */         position.setDeviceTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */         
/* 126 */         position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/* 128 */         int battery = buf.readUnsignedShort();
/* 129 */         position.set("charge", Boolean.valueOf(BitUtil.check(battery, 15)));
/* 130 */         position.set("batteryLevel", Integer.valueOf(BitUtil.to(battery, 15)));
/*     */         
/* 132 */         int mask = buf.readUnsignedByte();
/*     */         
/* 134 */         if (BitUtil.check(mask, 0)) {
/* 135 */           position.set("alarm", decodeAlarm(buf.readUnsignedInt()));
/*     */         }
/* 137 */         if (BitUtil.check(mask, 1)) {
/* 138 */           int positionMask = buf.readUnsignedByte();
/* 139 */           if (BitUtil.check(positionMask, 0)) {
/* 140 */             position.setValid(true);
/* 141 */             position.setFixTime(position.getDeviceTime());
/* 142 */             position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/* 143 */             position.setLongitude(convertCoordinate(buf.readFloat()));
/* 144 */             position.setLatitude(convertCoordinate(buf.readFloat()));
/*     */           } 
/* 146 */           Network network = new Network();
/* 147 */           if (BitUtil.check(positionMask, 1)) {
/* 148 */             int wifiCount = buf.readUnsignedByte();
/* 149 */             for (int j = 0; j < wifiCount; j++) {
/* 150 */               String mac = ByteBufUtil.hexDump(buf.readSlice(6)).replaceAll("(..)", "$1:");
/* 151 */               network.addWifiAccessPoint(WifiAccessPoint.from(mac
/* 152 */                     .substring(0, mac.length() - 1), buf.readUnsignedByte()));
/*     */             } 
/*     */           } 
/* 155 */           if (BitUtil.check(positionMask, 2)) {
/* 156 */             int cellCount = buf.readUnsignedByte();
/* 157 */             for (int j = 0; j < cellCount; j++) {
/* 158 */               network.addCellTower(CellTower.from(buf
/* 159 */                     .readUnsignedShort(), buf.readUnsignedShort(), buf
/* 160 */                     .readInt(), buf.readUnsignedInt(), buf.readUnsignedByte()));
/*     */             }
/*     */           } 
/* 163 */           if (network.getWifiAccessPoints() != null || network.getCellTowers() != null) {
/* 164 */             position.setNetwork(network);
/*     */           }
/* 166 */           if (BitUtil.check(positionMask, 3)) {
/* 167 */             buf.skipBytes(12 * buf.readUnsignedByte());
/*     */           }
/* 169 */           if (BitUtil.check(positionMask, 5)) {
/* 170 */             position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 0.1D));
/* 171 */             position.setCourse(buf.readUnsignedShort() * 0.1D);
/*     */           } 
/* 173 */           if (BitUtil.check(positionMask, 6)) {
/* 174 */             position.setValid(true);
/* 175 */             position.setFixTime(position.getDeviceTime());
/* 176 */             position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/* 177 */             position.setLongitude(convertCoordinate(buf.readDouble()));
/* 178 */             position.setLatitude(convertCoordinate(buf.readDouble()));
/*     */           } 
/*     */         } 
/*     */         
/* 182 */         if (BitUtil.check(mask, 3)) {
/* 183 */           buf.readUnsignedInt();
/*     */         }
/* 185 */         if (BitUtil.check(mask, 4)) {
/* 186 */           buf.skipBytes(20);
/* 187 */           buf.skipBytes(8);
/* 188 */           buf.skipBytes(10);
/*     */         } 
/* 190 */         if (BitUtil.check(mask, 5)) {
/* 191 */           buf.skipBytes(12);
/*     */         }
/*     */         
/* 194 */         if (!position.getValid()) {
/* 195 */           getLastLocation(position, position.getDeviceTime());
/*     */         }
/* 197 */         positions.add(position);
/*     */         
/* 199 */         buf.readerIndex(endIndex);
/*     */       } 
/*     */       
/* 202 */       return positions;
/*     */     } 
/*     */     
/* 205 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Xexun2ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */