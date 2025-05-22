/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class AnytrekProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public AnytrekProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, int type) {
/*  40 */     if (channel != null) {
/*  41 */       ByteBuf response = Unpooled.buffer();
/*  42 */       response.writeShort(30840);
/*  43 */       response.writeShortLE(7);
/*  44 */       response.writeByte(type);
/*  45 */       response.writeByte(0);
/*  46 */       response.writeShortLE(0);
/*  47 */       response.writeByte(0);
/*  48 */       response.writeShortLE(0);
/*  49 */       response.writeByte(13);
/*  50 */       response.writeByte(10);
/*  51 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  59 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  61 */     buf.skipBytes(2);
/*  62 */     buf.readUnsignedShortLE();
/*  63 */     int type = buf.readUnsignedByte();
/*     */     
/*  65 */     String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(2);
/*  66 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  67 */     if (deviceSession == null) {
/*  68 */       return null;
/*     */     }
/*     */     
/*  71 */     Position position = new Position(getProtocolName());
/*  72 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  74 */     position.set("versionFw", Integer.valueOf(buf.readUnsignedShortLE()));
/*  75 */     position.set("battery", Double.valueOf(buf.readUnsignedShortLE() * 0.01D));
/*  76 */     position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */ 
/*     */ 
/*     */     
/*  80 */     DateBuilder dateBuilder = (new DateBuilder()).setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*  81 */     position.setTime(dateBuilder.getDate());
/*     */     
/*  83 */     position.set("sat", Integer.valueOf(BitUtil.to(buf.readUnsignedByte(), 4)));
/*     */     
/*  85 */     double latitude = buf.readUnsignedIntLE() / 1800000.0D;
/*  86 */     double longitude = buf.readUnsignedIntLE() / 1800000.0D;
/*  87 */     position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*     */     
/*  89 */     int flags = buf.readUnsignedShortLE();
/*  90 */     position.setCourse(BitUtil.to(flags, 10));
/*  91 */     position.setValid(BitUtil.check(flags, 12));
/*     */     
/*  93 */     if (!BitUtil.check(flags, 10)) {
/*  94 */       latitude = -latitude;
/*     */     }
/*  96 */     if (BitUtil.check(flags, 11)) {
/*  97 */       longitude = -longitude;
/*     */     }
/*     */     
/* 100 */     position.setLatitude(latitude);
/* 101 */     position.setLongitude(longitude);
/*     */     
/* 103 */     buf.readUnsignedIntLE();
/* 104 */     buf.readUnsignedIntLE();
/*     */     
/* 106 */     flags = buf.readUnsignedByte();
/* 107 */     position.set("charge", Boolean.valueOf(BitUtil.check(flags, 0)));
/* 108 */     position.set("ignition", Boolean.valueOf(BitUtil.check(flags, 1)));
/* 109 */     position.set("alarm", BitUtil.check(flags, 4) ? "general" : null);
/*     */     
/* 111 */     buf.readUnsignedShortLE();
/*     */     
/* 113 */     position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*     */     
/* 115 */     sendResponse(channel, remoteAddress, type);
/*     */     
/* 117 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AnytrekProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */