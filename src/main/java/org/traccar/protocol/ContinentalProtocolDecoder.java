/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Date;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
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
/*     */ public class ContinentalProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_KEEPALIVE = 0;
/*     */   public static final int MSG_STATUS = 2;
/*     */   public static final int MSG_ACK = 6;
/*     */   public static final int MSG_NACK = 21;
/*     */   
/*     */   public ContinentalProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private double readCoordinate(ByteBuf buf, boolean extended) {
/*  42 */     long value = buf.readUnsignedInt();
/*  43 */     if (extended ? ((value & 0x8000000L) != 0L) : ((value & 0x800000L) != 0L)) {
/*  44 */       value |= extended ? -268435456L : -16777216L;
/*     */     }
/*  46 */     return (int)value / (extended ? 360000.0D : 3600.0D);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  53 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  55 */     buf.skipBytes(2);
/*  56 */     buf.readUnsignedShort();
/*  57 */     buf.readUnsignedByte();
/*     */     
/*  59 */     long serialNumber = buf.readUnsignedInt();
/*  60 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(serialNumber) });
/*  61 */     if (deviceSession == null) {
/*  62 */       return null;
/*     */     }
/*     */     
/*  65 */     buf.readUnsignedByte();
/*     */     
/*  67 */     int type = buf.readUnsignedByte();
/*     */     
/*  69 */     if (type == 2) {
/*     */       
/*  71 */       Position position = new Position(getProtocolName());
/*  72 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  74 */       position.setFixTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */       
/*  76 */       boolean extended = (buf.getUnsignedByte(buf.readerIndex()) != 0);
/*  77 */       position.setLatitude(readCoordinate(buf, extended));
/*  78 */       position.setLongitude(readCoordinate(buf, extended));
/*     */       
/*  80 */       position.setCourse(buf.readUnsignedShort());
/*  81 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort()));
/*     */       
/*  83 */       position.setValid((buf.readUnsignedByte() > 0));
/*     */       
/*  85 */       position.setDeviceTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */       
/*  87 */       position.set("event", Integer.valueOf(buf.readUnsignedShort()));
/*     */       
/*  89 */       int input = buf.readUnsignedShort();
/*  90 */       position.set("ignition", Boolean.valueOf(BitUtil.check(input, 0)));
/*  91 */       position.set("input", Integer.valueOf(input));
/*     */       
/*  93 */       position.set("output", Integer.valueOf(buf.readUnsignedShort()));
/*  94 */       position.set("battery", Short.valueOf(buf.readUnsignedByte()));
/*  95 */       position.set("deviceTemp", Byte.valueOf(buf.readByte()));
/*     */       
/*  97 */       buf.readUnsignedShort();
/*     */       
/*  99 */       if (buf.readableBytes() > 4) {
/* 100 */         position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*     */       }
/*     */       
/* 103 */       if (buf.readableBytes() > 4) {
/* 104 */         position.set("hours", Long.valueOf(UnitsConverter.msFromHours(buf.readUnsignedInt())));
/*     */       }
/*     */       
/* 107 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 111 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ContinentalProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */