/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Date;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ public class CalAmpProtocolDecoder
/*     */   extends BaseProtocolDecoder {
/*     */   public static final int MSG_NULL = 0;
/*     */   public static final int MSG_ACK = 1;
/*     */   public static final int MSG_EVENT_REPORT = 2;
/*     */   public static final int MSG_ID_REPORT = 3;
/*     */   public static final int MSG_USER_DATA = 4;
/*     */   public static final int MSG_APP_DATA = 5;
/*     */   public static final int MSG_CONFIG = 6;
/*     */   public static final int MSG_UNIT_REQUEST = 7;
/*     */   public static final int MSG_LOCATE_REPORT = 8;
/*     */   public static final int MSG_USER_DATA_ACC = 9;
/*     */   public static final int MSG_MINI_EVENT_REPORT = 10;
/*     */   public static final int MSG_MINI_USER_DATA = 11;
/*     */   public static final int SERVICE_UNACKNOWLEDGED = 0;
/*     */   public static final int SERVICE_ACKNOWLEDGED = 1;
/*     */   public static final int SERVICE_RESPONSE = 2;
/*     */   
/*     */   public CalAmpProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
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
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, int type, int index, int result) {
/*  57 */     if (channel != null) {
/*  58 */       ByteBuf response = Unpooled.buffer(10);
/*  59 */       response.writeByte(2);
/*  60 */       response.writeByte(1);
/*  61 */       response.writeShort(index);
/*  62 */       response.writeByte(type);
/*  63 */       response.writeByte(result);
/*  64 */       response.writeByte(0);
/*  65 */       response.writeMedium(0);
/*  66 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodePosition(DeviceSession deviceSession, int type, ByteBuf buf) {
/*  72 */     Position position = new Position(getProtocolName());
/*  73 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  75 */     position.setTime(new Date(buf.readUnsignedInt() * 1000L));
/*  76 */     if (type != 10) {
/*  77 */       buf.readUnsignedInt();
/*     */     }
/*  79 */     position.setLatitude(buf.readInt() * 1.0E-7D);
/*  80 */     position.setLongitude(buf.readInt() * 1.0E-7D);
/*  81 */     if (type != 10) {
/*  82 */       position.setAltitude(buf.readInt() * 0.01D);
/*  83 */       position.setSpeed(UnitsConverter.knotsFromCps(buf.readUnsignedInt()));
/*     */     } 
/*  85 */     position.setCourse(buf.readShort());
/*  86 */     if (type == 10) {
/*  87 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*     */     }
/*     */     
/*  90 */     if (type == 10) {
/*  91 */       position.set("sat", Integer.valueOf(buf.getUnsignedByte(buf.readerIndex()) & 0xF));
/*  92 */       position.setValid(((buf.readUnsignedByte() & 0x20) == 0));
/*     */     } else {
/*  94 */       position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*  95 */       position.setValid(((buf.readUnsignedByte() & 0x8) == 0));
/*     */     } 
/*     */     
/*  98 */     if (type != 10) {
/*  99 */       position.set("carrier", Integer.valueOf(buf.readUnsignedShort()));
/* 100 */       position.set("rssi", Short.valueOf(buf.readShort()));
/*     */     } 
/*     */     
/* 103 */     position.set("modem", Short.valueOf(buf.readUnsignedByte()));
/*     */     
/* 105 */     if (type != 10) {
/* 106 */       position.set("hdop", Short.valueOf(buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 109 */     int input = buf.readUnsignedByte();
/* 110 */     position.set("input", Integer.valueOf(input));
/* 111 */     position.set("ignition", Boolean.valueOf(BitUtil.check(input, 0)));
/*     */     
/* 113 */     if (type != 10) {
/* 114 */       position.set("status", Short.valueOf(buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 117 */     if (type == 2 || type == 10) {
/* 118 */       if (type != 10) {
/* 119 */         buf.readUnsignedByte();
/*     */       }
/* 121 */       position.set("event", Short.valueOf(buf.readUnsignedByte()));
/*     */     } 
/*     */     
/* 124 */     int accType = BitUtil.from(buf.getUnsignedByte(buf.readerIndex()), 6);
/* 125 */     int accCount = BitUtil.to(buf.readUnsignedByte(), 6);
/*     */     
/* 127 */     if (type != 10) {
/* 128 */       position.set("append", Short.valueOf(buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 131 */     if (accType == 1) {
/* 132 */       buf.readUnsignedInt();
/* 133 */       buf.readUnsignedInt();
/*     */     } 
/*     */     
/* 136 */     for (int i = 0; i < accCount; i++) {
/* 137 */       if (buf.readableBytes() >= 4) {
/* 138 */         if (i == 7) {
/* 139 */           position.set("driverUniqueId", String.valueOf(buf.readUnsignedInt()));
/*     */         } else {
/* 141 */           position.set("acc" + i, Long.valueOf(buf.readUnsignedInt()));
/*     */         } 
/*     */       }
/*     */     } 
/*     */     
/* 146 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 153 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 155 */     if (BitUtil.check(buf.getByte(buf.readerIndex()), 7)) {
/*     */       
/* 157 */       int content = buf.readUnsignedByte();
/*     */       
/* 159 */       if (BitUtil.check(content, 0)) {
/* 160 */         String id = ByteBufUtil.hexDump(buf.readSlice(buf.readUnsignedByte())).replace("f", "");
/* 161 */         getDeviceSession(channel, remoteAddress, new String[] { id });
/*     */       } 
/*     */       
/* 164 */       if (BitUtil.check(content, 1)) {
/* 165 */         buf.skipBytes(buf.readUnsignedByte());
/*     */       }
/*     */       
/* 168 */       if (BitUtil.check(content, 2)) {
/* 169 */         buf.skipBytes(buf.readUnsignedByte());
/*     */       }
/*     */       
/* 172 */       if (BitUtil.check(content, 3)) {
/* 173 */         buf.skipBytes(buf.readUnsignedByte());
/*     */       }
/*     */       
/* 176 */       if (BitUtil.check(content, 4)) {
/* 177 */         buf.skipBytes(buf.readUnsignedByte());
/*     */       }
/*     */       
/* 180 */       if (BitUtil.check(content, 5)) {
/* 181 */         buf.skipBytes(buf.readUnsignedByte());
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/* 186 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 187 */     if (deviceSession == null) {
/* 188 */       return null;
/*     */     }
/*     */     
/* 191 */     int service = buf.readUnsignedByte();
/* 192 */     int type = buf.readUnsignedByte();
/* 193 */     int index = buf.readUnsignedShort();
/*     */     
/* 195 */     if (service == 1) {
/* 196 */       sendResponse(channel, remoteAddress, type, index, 0);
/*     */     }
/*     */     
/* 199 */     if (type == 2 || type == 8 || type == 10) {
/* 200 */       return decodePosition(deviceSession, type, buf);
/*     */     }
/*     */     
/* 203 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CalAmpProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */