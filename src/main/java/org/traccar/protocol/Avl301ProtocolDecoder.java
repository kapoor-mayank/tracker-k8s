/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ public class Avl301ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_LOGIN = 76;
/*     */   public static final int MSG_STATUS = 72;
/*     */   public static final int MSG_GPS_LBS_STATUS = 36;
/*     */   
/*     */   public Avl301ProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */   
/*     */   private String readImei(ByteBuf buf) {
/*  39 */     int b = buf.readUnsignedByte();
/*  40 */     StringBuilder imei = new StringBuilder();
/*  41 */     imei.append(b & 0xF);
/*  42 */     for (int i = 0; i < 7; i++) {
/*  43 */       b = buf.readUnsignedByte();
/*  44 */       imei.append((b & 0xF0) >> 4);
/*  45 */       imei.append(b & 0xF);
/*     */     } 
/*  47 */     return imei.toString();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, int type) {
/*  55 */     if (channel != null) {
/*  56 */       ByteBuf response = Unpooled.buffer(5);
/*  57 */       response.writeByte(36);
/*  58 */       response.writeByte(type);
/*  59 */       response.writeByte(35);
/*  60 */       response.writeByte(13); response.writeByte(10);
/*  61 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  69 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  71 */     buf.skipBytes(1);
/*  72 */     int type = buf.readUnsignedByte();
/*  73 */     buf.readUnsignedByte();
/*     */     
/*  75 */     if (type == 76) {
/*     */       
/*  77 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { readImei(buf) });
/*  78 */       if (deviceSession == null) {
/*  79 */         sendResponse(channel, type);
/*     */       }
/*     */     }
/*  82 */     else if (type == 72) {
/*     */       
/*  84 */       sendResponse(channel, type);
/*     */     }
/*  86 */     else if (type == 36) {
/*     */       
/*  88 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  89 */       if (deviceSession == null) {
/*  90 */         return null;
/*     */       }
/*     */       
/*  93 */       Position position = new Position(getProtocolName());
/*  94 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */ 
/*     */       
/*  98 */       DateBuilder dateBuilder = (new DateBuilder()).setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*  99 */       position.setTime(dateBuilder.getDate());
/*     */       
/* 101 */       int gpsLength = buf.readUnsignedByte();
/* 102 */       position.set("sat", Integer.valueOf(gpsLength & 0xF));
/*     */       
/* 104 */       position.set("satVisible", Short.valueOf(buf.readUnsignedByte()));
/*     */       
/* 106 */       double latitude = buf.readUnsignedInt() / 600000.0D;
/* 107 */       double longitude = buf.readUnsignedInt() / 600000.0D;
/* 108 */       position.setSpeed(buf.readUnsignedByte());
/*     */       
/* 110 */       int union = buf.readUnsignedShort();
/* 111 */       position.setCourse((union & 0x3FF));
/* 112 */       position.setValid(((union & 0x1000) != 0));
/* 113 */       if ((union & 0x400) != 0) {
/* 114 */         latitude = -latitude;
/*     */       }
/* 116 */       if ((union & 0x800) != 0) {
/* 117 */         longitude = -longitude;
/*     */       }
/*     */       
/* 120 */       position.setLatitude(latitude);
/* 121 */       position.setLongitude(longitude);
/*     */       
/* 123 */       if ((union & 0x4000) != 0) {
/* 124 */         position.set("acc", Boolean.valueOf(((union & 0x8000) != 0)));
/*     */       }
/*     */       
/* 127 */       position.setNetwork(new Network(
/* 128 */             CellTower.fromLacCid(buf.readUnsignedShort(), buf.readUnsignedMedium())));
/*     */       
/* 130 */       position.set("alarm", "general");
/* 131 */       int flags = buf.readUnsignedByte();
/* 132 */       position.set("acc", Boolean.valueOf(((flags & 0x2) != 0)));
/*     */ 
/*     */ 
/*     */       
/* 136 */       position.set("power", Short.valueOf(buf.readUnsignedByte()));
/* 137 */       position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */       
/* 139 */       return position;
/*     */     } 
/*     */     
/* 142 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Avl301ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */