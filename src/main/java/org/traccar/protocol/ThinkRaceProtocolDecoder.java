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
/*     */ 
/*     */ public class ThinkRaceProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_LOGIN = 128;
/*     */   public static final int MSG_GPS = 144;
/*     */   
/*     */   public ThinkRaceProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static double convertCoordinate(long raw, boolean negative) {
/*  44 */     long degrees = raw / 1000000L;
/*  45 */     double minutes = (raw % 1000000L) * 1.0E-4D;
/*  46 */     double result = degrees + minutes / 60.0D;
/*  47 */     if (negative) {
/*  48 */       result = -result;
/*     */     }
/*  50 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  57 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  59 */     buf.skipBytes(2);
/*  60 */     ByteBuf id = buf.readSlice(12);
/*  61 */     buf.readUnsignedByte();
/*  62 */     int type = buf.readUnsignedByte();
/*  63 */     buf.readUnsignedShort();
/*     */     
/*  65 */     if (type == 128) {
/*     */       
/*  67 */       int command = buf.readUnsignedByte();
/*     */       
/*  69 */       if (command == 1) {
/*  70 */         String imei = buf.toString(buf.readerIndex(), 15, StandardCharsets.US_ASCII);
/*  71 */         DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  72 */         if (deviceSession != null && channel != null) {
/*  73 */           ByteBuf response = Unpooled.buffer();
/*  74 */           response.writeByte(72); response.writeByte(82);
/*  75 */           response.writeBytes(id);
/*  76 */           response.writeByte(44);
/*  77 */           response.writeByte(type);
/*  78 */           response.writeShort(2);
/*  79 */           response.writeShort(32768);
/*  80 */           response.writeShort(0);
/*  81 */           channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */         }
/*     */       
/*     */       } 
/*  85 */     } else if (type == 144) {
/*     */       
/*  87 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  88 */       if (deviceSession == null) {
/*  89 */         return null;
/*     */       }
/*     */       
/*  92 */       Position position = new Position(getProtocolName());
/*  93 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  95 */       position.setTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */       
/*  97 */       int flags = buf.readUnsignedByte();
/*     */       
/*  99 */       position.setValid(true);
/* 100 */       position.setLatitude(convertCoordinate(buf.readUnsignedInt(), !BitUtil.check(flags, 0)));
/* 101 */       position.setLongitude(convertCoordinate(buf.readUnsignedInt(), !BitUtil.check(flags, 1)));
/*     */       
/* 103 */       position.setSpeed(buf.readUnsignedByte());
/* 104 */       position.setCourse(buf.readUnsignedByte());
/*     */       
/* 106 */       position.setNetwork(new Network(
/* 107 */             CellTower.fromLacCid(buf.readUnsignedShort(), buf.readUnsignedShort())));
/*     */       
/* 109 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 113 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ThinkRaceProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */