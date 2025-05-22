/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.TimeZone;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
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
/*     */ public class TopinProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_LOGIN = 1;
/*     */   public static final int MSG_GPS = 16;
/*     */   public static final int MSG_GPS_OFFLINE = 17;
/*     */   public static final int MSG_STATUS = 19;
/*     */   public static final int MSG_WIFI_OFFLINE = 23;
/*     */   public static final int MSG_WIFI = 105;
/*     */   
/*     */   public TopinProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, int length, int type, ByteBuf content) {
/*  48 */     if (channel != null) {
/*  49 */       ByteBuf response = Unpooled.buffer();
/*  50 */       response.writeShort(30840);
/*  51 */       response.writeByte(length);
/*  52 */       response.writeByte(type);
/*  53 */       response.writeBytes(content);
/*  54 */       response.writeByte(13);
/*  55 */       response.writeByte(10);
/*  56 */       content.release();
/*  57 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  65 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  67 */     buf.skipBytes(2);
/*  68 */     int length = buf.readUnsignedByte();
/*     */     
/*  70 */     int type = buf.readUnsignedByte();
/*     */ 
/*     */     
/*  73 */     if (type == 1) {
/*  74 */       String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(1);
/*  75 */       DeviceSession deviceSession1 = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  76 */       ByteBuf content = Unpooled.buffer();
/*  77 */       content.writeByte((deviceSession1 != null) ? 1 : 68);
/*  78 */       sendResponse(channel, length, type, content);
/*  79 */       return null;
/*     */     } 
/*  81 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  82 */     if (deviceSession == null) {
/*  83 */       return null;
/*     */     }
/*     */ 
/*     */     
/*  87 */     if (type == 16 || type == 17) {
/*     */       
/*  89 */       Position position = new Position(getProtocolName());
/*  90 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  92 */       ByteBuf time = buf.slice(buf.readerIndex(), 6);
/*     */       
/*  94 */       Gt06ProtocolDecoder.decodeGps(position, buf, false, TimeZone.getTimeZone("UTC"), false);
/*     */       
/*  96 */       ByteBuf content = Unpooled.buffer();
/*  97 */       content.writeBytes(time);
/*  98 */       sendResponse(channel, length, type, content);
/*     */       
/* 100 */       return position;
/*     */     } 
/* 102 */     if (type == 19) {
/*     */       
/* 104 */       Position position = new Position(getProtocolName());
/* 105 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 107 */       getLastLocation(position, null);
/*     */       
/* 109 */       int battery = buf.readUnsignedByte();
/* 110 */       int firmware = buf.readUnsignedByte();
/* 111 */       int timezone = buf.readUnsignedByte();
/* 112 */       int interval = buf.readUnsignedByte();
/* 113 */       int signal = 0;
/* 114 */       if (length >= 7) {
/* 115 */         signal = buf.readUnsignedByte();
/* 116 */         position.set("rssi", Integer.valueOf(signal));
/*     */       } 
/*     */       
/* 119 */       position.set("batteryLevel", Integer.valueOf(battery));
/* 120 */       position.set("versionFw", Integer.valueOf(firmware));
/*     */       
/* 122 */       ByteBuf content = Unpooled.buffer();
/* 123 */       content.writeByte(battery);
/* 124 */       content.writeByte(firmware);
/* 125 */       content.writeByte(timezone);
/* 126 */       content.writeByte(interval);
/* 127 */       if (length >= 7) {
/* 128 */         content.writeByte(signal);
/*     */       }
/* 130 */       sendResponse(channel, length, type, content);
/*     */       
/* 132 */       return position;
/*     */     } 
/* 134 */     if (type == 105 || type == 23) {
/*     */       
/* 136 */       Position position = new Position(getProtocolName());
/* 137 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 139 */       getLastLocation(position, null);
/*     */       
/* 141 */       ByteBuf time = buf.readSlice(6);
/*     */       
/* 143 */       Network network = new Network();
/* 144 */       for (int i = 0; i < length; i++) {
/* 145 */         String mac = String.format("%02x:%02x:%02x:%02x:%02x:%02x", new Object[] {
/* 146 */               Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte()), 
/* 147 */               Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte()) });
/* 148 */         network.addWifiAccessPoint(WifiAccessPoint.from(mac, buf.readUnsignedByte()));
/*     */       } 
/*     */       
/* 151 */       int cellCount = buf.readUnsignedByte();
/* 152 */       int mcc = buf.readUnsignedShort();
/* 153 */       int mnc = buf.readUnsignedByte();
/* 154 */       for (int j = 0; j < cellCount; j++) {
/* 155 */         network.addCellTower(CellTower.from(mcc, mnc, buf
/* 156 */               .readUnsignedShort(), buf.readUnsignedShort(), buf.readUnsignedByte()));
/*     */       }
/*     */       
/* 159 */       position.setNetwork(network);
/*     */       
/* 161 */       ByteBuf content = Unpooled.buffer();
/* 162 */       content.writeBytes(time);
/* 163 */       sendResponse(channel, length, type, content);
/*     */       
/* 165 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 169 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TopinProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */