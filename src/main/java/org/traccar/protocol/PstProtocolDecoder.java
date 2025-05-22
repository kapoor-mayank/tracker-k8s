/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Date;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ public class PstProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_ACK = 0;
/*     */   public static final int MSG_STATUS = 5;
/*     */   public static final int MSG_COMMAND = 6;
/*     */   
/*     */   public PstProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Date readDate(ByteBuf buf) {
/*  44 */     long value = buf.readUnsignedInt();
/*  45 */     return (new DateBuilder())
/*  46 */       .setYear((int)BitUtil.between(value, 26, 32))
/*  47 */       .setMonth((int)BitUtil.between(value, 22, 26))
/*  48 */       .setDay((int)BitUtil.between(value, 17, 22))
/*  49 */       .setHour((int)BitUtil.between(value, 12, 17))
/*  50 */       .setMinute((int)BitUtil.between(value, 6, 12))
/*  51 */       .setSecond((int)BitUtil.between(value, 0, 6)).getDate();
/*     */   }
/*     */   
/*     */   private double readCoordinate(ByteBuf buf) {
/*  55 */     long value = buf.readUnsignedInt();
/*  56 */     int sign = BitUtil.check(value, 31) ? -1 : 1;
/*  57 */     value = BitUtil.to(value, 31);
/*  58 */     return sign * (BitUtil.from(value, 16) + BitUtil.to(value, 16) / 10000.0D) / 60.0D;
/*     */   }
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, long id, int version, long index, int type) {
/*  63 */     if (channel != null) {
/*     */       
/*  65 */       ByteBuf response = Unpooled.buffer();
/*  66 */       response.writeInt((int)id);
/*  67 */       response.writeByte(version);
/*  68 */       response.writeInt((int)index);
/*  69 */       response.writeByte(0);
/*  70 */       response.writeByte(type);
/*  71 */       response.writeShort(Checksum.crc16(Checksum.CRC16_XMODEM, response.nioBuffer()));
/*     */       
/*  73 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  82 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  84 */     long id = buf.readUnsignedInt();
/*  85 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(id) });
/*  86 */     if (deviceSession == null) {
/*  87 */       return null;
/*     */     }
/*     */     
/*  90 */     int version = buf.readUnsignedByte();
/*  91 */     long index = buf.readUnsignedInt();
/*     */     
/*  93 */     int type = buf.readUnsignedByte();
/*     */     
/*  95 */     sendResponse(channel, remoteAddress, id, version, index, type);
/*     */     
/*  97 */     if (type == 5) {
/*     */       
/*  99 */       Position position = new Position(getProtocolName());
/* 100 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 102 */       position.setDeviceTime(readDate(buf));
/*     */       
/* 104 */       int status = buf.readUnsignedByte();
/* 105 */       position.set("blocked", Boolean.valueOf(BitUtil.check(status, 4)));
/* 106 */       position.set("ignition", Boolean.valueOf(BitUtil.check(status, 7)));
/* 107 */       position.set("status", Integer.valueOf(status));
/*     */       
/* 109 */       int count = buf.readUnsignedByte();
/* 110 */       for (int i = 0; i < count; i++) {
/*     */         
/* 112 */         int battery, tag = buf.readUnsignedByte();
/* 113 */         int length = buf.readUnsignedByte();
/*     */         
/* 115 */         switch (tag) {
/*     */           case 9:
/* 117 */             buf.readUnsignedByte();
/* 118 */             buf.readUnsignedByte();
/* 119 */             buf.readUnsignedByte();
/*     */             break;
/*     */           case 13:
/* 122 */             battery = buf.readUnsignedByte();
/* 123 */             if (battery <= 20) {
/* 124 */               position.set("batteryLevel", Integer.valueOf(battery * 5));
/*     */             }
/*     */             break;
/*     */           case 16:
/* 128 */             position.setValid(true);
/* 129 */             position.setFixTime(readDate(buf));
/* 130 */             position.setLatitude(readCoordinate(buf));
/* 131 */             position.setLongitude(readCoordinate(buf));
/* 132 */             position.setSpeed(buf.readUnsignedByte());
/* 133 */             position.setCourse((buf.readUnsignedByte() * 2));
/* 134 */             position.setAltitude(buf.readShort());
/* 135 */             buf.readUnsignedInt();
/*     */             break;
/*     */           default:
/* 138 */             buf.skipBytes(length);
/*     */             break;
/*     */         } 
/*     */       
/*     */       } 
/* 143 */       return (position.getFixTime() != null) ? position : null;
/*     */     } 
/*     */     
/* 146 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PstProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */