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
/*     */ public class Gt02ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_DATA = 16;
/*     */   public static final int MSG_HEARTBEAT = 26;
/*     */   public static final int MSG_RESPONSE = 28;
/*     */   
/*     */   public Gt02ProtocolDecoder(Protocol protocol) {
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
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  48 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  50 */     buf.skipBytes(2);
/*  51 */     buf.readByte();
/*     */     
/*  53 */     Position position = new Position(getProtocolName());
/*     */ 
/*     */     
/*  56 */     int power = buf.readUnsignedByte();
/*  57 */     int gsm = buf.readUnsignedByte();
/*     */     
/*  59 */     String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(1);
/*  60 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  61 */     if (deviceSession == null) {
/*  62 */       return null;
/*     */     }
/*  64 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  66 */     position.set("index", Integer.valueOf(buf.readUnsignedShort()));
/*     */     
/*  68 */     int type = buf.readUnsignedByte();
/*     */     
/*  70 */     if (type == 26) {
/*     */       
/*  72 */       getLastLocation(position, null);
/*     */       
/*  74 */       position.set("power", Integer.valueOf(power));
/*  75 */       position.set("rssi", Integer.valueOf(gsm));
/*     */       
/*  77 */       if (channel != null) {
/*  78 */         byte[] response = { 84, 104, 26, 13, 10 };
/*  79 */         channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(response), remoteAddress));
/*     */       }
/*     */     
/*  82 */     } else if (type == 16) {
/*     */ 
/*     */ 
/*     */       
/*  86 */       DateBuilder dateBuilder = (new DateBuilder()).setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*  87 */       position.setTime(dateBuilder.getDate());
/*     */       
/*  89 */       double latitude = buf.readUnsignedInt() / 1800000.0D;
/*  90 */       double longitude = buf.readUnsignedInt() / 1800000.0D;
/*     */       
/*  92 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*  93 */       position.setCourse(buf.readUnsignedShort());
/*     */       
/*  95 */       buf.skipBytes(3);
/*     */       
/*  97 */       long flags = buf.readUnsignedInt();
/*  98 */       position.setValid(BitUtil.check(flags, 0));
/*  99 */       if (!BitUtil.check(flags, 1)) {
/* 100 */         latitude = -latitude;
/*     */       }
/* 102 */       if (!BitUtil.check(flags, 2)) {
/* 103 */         longitude = -longitude;
/*     */       }
/*     */       
/* 106 */       position.setLatitude(latitude);
/* 107 */       position.setLongitude(longitude);
/*     */     }
/* 109 */     else if (type == 28) {
/*     */       
/* 111 */       getLastLocation(position, null);
/*     */       
/* 113 */       position.set("result", buf
/* 114 */           .readSlice(buf.readUnsignedByte()).toString(StandardCharsets.US_ASCII));
/*     */     }
/*     */     else {
/*     */       
/* 118 */       return null;
/*     */     } 
/*     */ 
/*     */     
/* 122 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gt02ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */