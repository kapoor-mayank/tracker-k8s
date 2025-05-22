/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
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
/*     */ public class RoboTrackProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_ID = 0;
/*     */   public static final int MSG_ACK = 128;
/*     */   public static final int MSG_GPS = 3;
/*     */   public static final int MSG_GSM = 4;
/*     */   public static final int MSG_IMAGE_START = 6;
/*     */   public static final int MSG_IMAGE_DATA = 7;
/*     */   public static final int MSG_IMAGE_END = 8;
/*     */   
/*     */   public RoboTrackProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
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
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  53 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  55 */     int type = buf.readUnsignedByte();
/*     */     
/*  57 */     if (type == 0) {
/*     */       
/*  59 */       buf.skipBytes(16);
/*     */       
/*  61 */       String imei = buf.readSlice(15).toString(StandardCharsets.US_ASCII);
/*     */       
/*  63 */       if (getDeviceSession(channel, remoteAddress, new String[] { imei }) != null && channel != null) {
/*  64 */         ByteBuf response = Unpooled.buffer();
/*  65 */         response.writeByte(128);
/*  66 */         response.writeByte(1);
/*  67 */         response.writeByte(102);
/*  68 */         channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */       }
/*     */     
/*  71 */     } else if (type == 3 || type == 4) {
/*     */       
/*  73 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  74 */       if (deviceSession == null) {
/*  75 */         return null;
/*     */       }
/*     */       
/*  78 */       Position position = new Position(getProtocolName());
/*  79 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  81 */       position.setDeviceTime(new Date(buf.readUnsignedIntLE() * 1000L));
/*     */       
/*  83 */       if (type == 3) {
/*     */         
/*  85 */         position.setValid(true);
/*  86 */         position.setFixTime(position.getDeviceTime());
/*  87 */         position.setLatitude(buf.readIntLE() * 1.0E-6D);
/*  88 */         position.setLongitude(buf.readIntLE() * 1.0E-6D);
/*  89 */         position.setSpeed(UnitsConverter.knotsFromKph(buf.readByte()));
/*     */       }
/*     */       else {
/*     */         
/*  93 */         getLastLocation(position, position.getDeviceTime());
/*     */         
/*  95 */         position.setNetwork(new Network(CellTower.from(buf
/*  96 */                 .readUnsignedShortLE(), buf.readUnsignedShortLE(), buf
/*  97 */                 .readUnsignedShortLE(), buf.readUnsignedShortLE())));
/*     */         
/*  99 */         buf.readUnsignedByte();
/*     */       } 
/*     */ 
/*     */       
/* 103 */       int value = buf.readUnsignedByte();
/*     */       
/* 105 */       position.set("sat", Integer.valueOf(BitUtil.to(value, 4)));
/* 106 */       position.set("rssi", Integer.valueOf(BitUtil.between(value, 4, 7)));
/* 107 */       position.set("motion", Boolean.valueOf(BitUtil.check(value, 7)));
/*     */       
/* 109 */       value = buf.readUnsignedByte();
/*     */       
/* 111 */       position.set("charge", Boolean.valueOf(BitUtil.check(value, 0)));
/*     */       int i;
/* 113 */       for (i = 1; i <= 4; i++) {
/* 114 */         position.set("in" + i, Boolean.valueOf(BitUtil.check(value, i)));
/*     */       }
/*     */       
/* 117 */       position.set("batteryLevel", Integer.valueOf(BitUtil.from(value, 5) * 100 / 7));
/* 118 */       position.set("deviceTemp", Byte.valueOf(buf.readByte()));
/*     */       
/* 120 */       for (i = 1; i <= 3; i++) {
/* 121 */         position.set("adc" + i, Integer.valueOf(buf.readUnsignedShortLE()));
/*     */       }
/*     */       
/* 124 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 128 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\RoboTrackProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */