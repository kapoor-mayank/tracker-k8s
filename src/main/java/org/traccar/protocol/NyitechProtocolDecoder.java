/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
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
/*     */ public class NyitechProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final short MSG_LOGIN = 4097;
/*     */   public static final short MSG_COMPREHENSIVE_LIVE = 8193;
/*     */   public static final short MSG_COMPREHENSIVE_HISTORY = 8194;
/*     */   public static final short MSG_ALARM = 8195;
/*     */   public static final short MSG_FIXED = 8196;
/*     */   
/*     */   public NyitechProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
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
/*     */   private void decodeLocation(Position position, ByteBuf buf) {
/*  47 */     DateBuilder dateBuilder = (new DateBuilder()).setDateReverse(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*  48 */     position.setTime(dateBuilder.getDate());
/*     */     
/*  50 */     int flags = buf.readUnsignedByte();
/*  51 */     position.setValid((BitUtil.to(flags, 2) > 0));
/*     */     
/*  53 */     double lat = buf.readUnsignedIntLE() / 3600000.0D;
/*  54 */     double lon = buf.readUnsignedIntLE() / 3600000.0D;
/*     */     
/*  56 */     position.setLatitude(BitUtil.check(flags, 2) ? lat : -lat);
/*  57 */     position.setLongitude(BitUtil.check(flags, 3) ? lon : -lon);
/*     */     
/*  59 */     position.setSpeed(UnitsConverter.knotsFromCps(buf.readUnsignedShortLE()));
/*  60 */     position.setCourse(buf.readUnsignedShortLE() * 0.1D);
/*  61 */     position.setAltitude(buf.readShortLE() * 0.1D);
/*     */   }
/*     */   
/*     */   private String decodeAlarm(int type) {
/*  65 */     switch (type) {
/*     */       case 9:
/*  67 */         return "hardAcceleration";
/*     */       case 10:
/*  69 */         return "hardBraking";
/*     */       case 11:
/*  71 */         return "hardCornering";
/*     */       case 14:
/*  73 */         return "sos";
/*     */     } 
/*  75 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  83 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  85 */     buf.skipBytes(2);
/*  86 */     buf.readUnsignedShortLE();
/*     */     
/*  88 */     String id = buf.readCharSequence(12, StandardCharsets.US_ASCII).toString();
/*  89 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/*  90 */     if (deviceSession == null) {
/*  91 */       return null;
/*     */     }
/*     */     
/*  94 */     int type = buf.readUnsignedShortLE();
/*     */     
/*  96 */     if (type != 4097 && type != 8193 && type != 8194 && type != 8195 && type != 8196)
/*     */     {
/*  98 */       return null;
/*     */     }
/*     */     
/* 101 */     Position position = new Position(getProtocolName());
/* 102 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 104 */     if (type == 8193 || type == 8194) {
/* 105 */       buf.skipBytes(6);
/* 106 */       buf.skipBytes(3);
/* 107 */     } else if (type == 8195) {
/* 108 */       buf.readUnsignedShortLE();
/* 109 */       buf.readUnsignedByte();
/* 110 */       position.set("alarm", decodeAlarm(buf.readUnsignedByte()));
/* 111 */       buf.readUnsignedShortLE();
/* 112 */       buf.readUnsignedShortLE();
/* 113 */       buf.skipBytes(6);
/* 114 */     } else if (type == 8196) {
/* 115 */       buf.skipBytes(6);
/*     */     } 
/*     */     
/* 118 */     decodeLocation(position, buf);
/*     */     
/* 120 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NyitechProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */