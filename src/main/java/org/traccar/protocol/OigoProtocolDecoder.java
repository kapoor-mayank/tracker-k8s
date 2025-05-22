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
/*     */ public class OigoProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_AR_LOCATION = 0;
/*     */   public static final int MSG_AR_REMOTE_START = 16;
/*     */   public static final int MSG_ACKNOWLEDGEMENT = 224;
/*     */   
/*     */   public OigoProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodeArMessage(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/*     */     DeviceSession deviceSession;
/*     */     String imei, meid;
/*  47 */     buf.skipBytes(1);
/*  48 */     buf.readUnsignedShort();
/*     */     
/*  50 */     int type = buf.readUnsignedByte();
/*     */     
/*  52 */     int tag = buf.readUnsignedByte();
/*     */ 
/*     */     
/*  55 */     switch (BitUtil.to(tag, 3)) {
/*     */       case 0:
/*  57 */         imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(1);
/*  58 */         deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*     */         break;
/*     */       case 1:
/*  61 */         buf.skipBytes(1);
/*  62 */         meid = buf.readSlice(14).toString(StandardCharsets.US_ASCII);
/*  63 */         deviceSession = getDeviceSession(channel, remoteAddress, new String[] { meid });
/*     */         break;
/*     */       default:
/*  66 */         deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*     */         break;
/*     */     } 
/*     */     
/*  70 */     if (deviceSession == null || type != 0) {
/*  71 */       return null;
/*     */     }
/*     */     
/*  74 */     Position position = new Position(getProtocolName());
/*  75 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  77 */     position.set("event", Short.valueOf(buf.readUnsignedByte()));
/*     */     
/*  79 */     int mask = buf.readInt();
/*     */     
/*  81 */     if (BitUtil.check(mask, 0)) {
/*  82 */       position.set("index", Integer.valueOf(buf.readUnsignedShort()));
/*     */     }
/*     */     
/*  85 */     if (BitUtil.check(mask, 1)) {
/*  86 */       int date = buf.readUnsignedByte();
/*     */ 
/*     */       
/*  89 */       DateBuilder dateBuilder = (new DateBuilder()).setDate(BitUtil.between(date, 4, 8) + 2010, BitUtil.to(date, 4), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*  90 */       position.setTime(dateBuilder.getDate());
/*     */     } 
/*     */     
/*  93 */     if (BitUtil.check(mask, 2)) {
/*  94 */       buf.skipBytes(5);
/*     */     }
/*     */     
/*  97 */     if (BitUtil.check(mask, 3)) {
/*  98 */       position.setLatitude(buf.readUnsignedInt() * 1.0E-6D - 90.0D);
/*  99 */       position.setLongitude(buf.readUnsignedInt() * 1.0E-6D - 180.0D);
/*     */     } 
/*     */     
/* 102 */     if (BitUtil.check(mask, 4)) {
/* 103 */       int status = buf.readUnsignedByte();
/* 104 */       position.setValid((BitUtil.between(status, 4, 8) != 0));
/* 105 */       position.set("sat", Integer.valueOf(BitUtil.to(status, 4)));
/* 106 */       position.set("hdop", Double.valueOf(buf.readUnsignedByte() * 0.1D));
/*     */     } 
/*     */     
/* 109 */     if (BitUtil.check(mask, 5)) {
/* 110 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 113 */     if (BitUtil.check(mask, 6)) {
/* 114 */       position.setCourse(buf.readUnsignedShort());
/*     */     }
/*     */     
/* 117 */     if (BitUtil.check(mask, 7)) {
/* 118 */       position.setAltitude(buf.readShort());
/*     */     }
/*     */     
/* 121 */     if (BitUtil.check(mask, 8)) {
/* 122 */       position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */     }
/*     */     
/* 125 */     if (BitUtil.check(mask, 9)) {
/* 126 */       position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*     */     }
/*     */     
/* 129 */     if (BitUtil.check(mask, 10)) {
/* 130 */       position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*     */     }
/*     */     
/* 133 */     if (BitUtil.check(mask, 11)) {
/* 134 */       buf.skipBytes(2);
/*     */     }
/*     */     
/* 137 */     if (BitUtil.check(mask, 12)) {
/* 138 */       position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 1000L));
/*     */     }
/*     */     
/* 141 */     if (BitUtil.check(mask, 13)) {
/* 142 */       buf.skipBytes(6);
/*     */     }
/*     */     
/* 145 */     if (BitUtil.check(mask, 14)) {
/* 146 */       buf.skipBytes(5);
/*     */     }
/*     */     
/* 149 */     if (BitUtil.check(mask, 15)) {
/* 150 */       buf.readUnsignedShort();
/*     */     }
/*     */     
/* 153 */     return position;
/*     */   }
/*     */   
/*     */   private double convertCoordinate(long value) {
/* 157 */     boolean negative = (value < 0L);
/* 158 */     value = Math.abs(value);
/* 159 */     double minutes = (value % 100000L) * 0.001D;
/* 160 */     value /= 100000L;
/* 161 */     double degrees = value + minutes / 60.0D;
/* 162 */     return negative ? -degrees : degrees;
/*     */   }
/*     */   
/*     */   private Position decodeMgMessage(Channel channel, SocketAddress remoteAddress, ByteBuf buf) {
/*     */     DeviceSession deviceSession;
/* 167 */     buf.readUnsignedByte();
/* 168 */     int flags = buf.getUnsignedByte(buf.readerIndex());
/*     */ 
/*     */     
/* 171 */     if (BitUtil.check(flags, 6)) {
/* 172 */       buf.readUnsignedByte();
/* 173 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*     */     } else {
/* 175 */       String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(1);
/* 176 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*     */     } 
/*     */     
/* 179 */     if (deviceSession == null) {
/* 180 */       return null;
/*     */     }
/*     */     
/* 183 */     Position position = new Position(getProtocolName());
/* 184 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 186 */     buf.skipBytes(8);
/*     */     
/* 188 */     int date = buf.readUnsignedShort();
/*     */ 
/*     */ 
/*     */     
/* 192 */     DateBuilder dateBuilder = (new DateBuilder()).setDate(2010 + BitUtil.from(date, 12), BitUtil.between(date, 8, 12), BitUtil.to(date, 8)).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), 0);
/*     */     
/* 194 */     position.setValid(true);
/* 195 */     position.setLatitude(convertCoordinate(buf.readInt()));
/* 196 */     position.setLongitude(convertCoordinate(buf.readInt()));
/*     */     
/* 198 */     position.setAltitude(UnitsConverter.metersFromFeet(buf.readShort()));
/* 199 */     position.setCourse(buf.readUnsignedShort());
/* 200 */     position.setSpeed(UnitsConverter.knotsFromMph(buf.readUnsignedByte()));
/*     */     
/* 202 */     position.set("power", Double.valueOf(buf.readUnsignedByte() * 0.1D));
/* 203 */     position.set("io1", Short.valueOf(buf.readUnsignedByte()));
/*     */     
/* 205 */     dateBuilder.setSecond(buf.readUnsignedByte());
/* 206 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 208 */     position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */     
/* 210 */     int index = buf.readUnsignedByte();
/*     */     
/* 212 */     position.set("versionFw", Short.valueOf(buf.readUnsignedByte()));
/* 213 */     position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/* 214 */     position.set("odometer", Long.valueOf((long)(buf.readUnsignedInt() * 1609.34D)));
/*     */     
/* 216 */     if (channel != null && BitUtil.check(flags, 7)) {
/* 217 */       ByteBuf response = Unpooled.buffer();
/* 218 */       response.writeByte(224);
/* 219 */       response.writeByte(index);
/* 220 */       response.writeByte(0);
/* 221 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */     
/* 224 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 231 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 233 */     if (buf.getUnsignedByte(buf.readerIndex()) == 126) {
/* 234 */       return decodeArMessage(channel, remoteAddress, buf);
/*     */     }
/* 236 */     return decodeMgMessage(channel, remoteAddress, buf);
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OigoProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */