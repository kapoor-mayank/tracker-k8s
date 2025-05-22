/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import org.slf4j.Logger;
/*     */ import org.slf4j.LoggerFactory;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class SkypatrolProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*  35 */   private static final Logger LOGGER = LoggerFactory.getLogger(SkypatrolProtocolDecoder.class);
/*     */   
/*     */   private final long defaultMask;
/*     */   
/*     */   public SkypatrolProtocolDecoder(Protocol protocol) {
/*  40 */     super(protocol);
/*  41 */     this.defaultMask = Context.getConfig().getInteger(getProtocolName() + ".mask");
/*     */   }
/*     */   
/*     */   private static double convertCoordinate(long coordinate) {
/*  45 */     int sign = 1;
/*  46 */     if (coordinate > 2147483647L) {
/*  47 */       sign = -1;
/*  48 */       coordinate = 4294967295L - coordinate;
/*     */     } 
/*     */     
/*  51 */     long degrees = coordinate / 1000000L;
/*  52 */     double minutes = (coordinate % 1000000L) / 10000.0D;
/*     */     
/*  54 */     return sign * (degrees + minutes / 60.0D);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  61 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  63 */     int apiNumber = buf.readUnsignedShort();
/*  64 */     int commandType = buf.readUnsignedByte();
/*  65 */     int messageType = BitUtil.from(buf.readUnsignedByte(), 4);
/*  66 */     long mask = this.defaultMask;
/*  67 */     if (buf.readUnsignedByte() == 4) {
/*  68 */       mask = buf.readUnsignedInt();
/*     */     }
/*     */ 
/*     */     
/*  72 */     if (apiNumber == 5 && commandType == 2 && messageType == 1 && BitUtil.check(mask, 0)) {
/*     */       String id;
/*  74 */       Position position = new Position(getProtocolName());
/*     */       
/*  76 */       if (BitUtil.check(mask, 1)) {
/*  77 */         position.set("status", Long.valueOf(buf.readUnsignedInt()));
/*     */       }
/*     */ 
/*     */       
/*  81 */       if (BitUtil.check(mask, 23)) {
/*  82 */         id = buf.toString(buf.readerIndex(), 8, StandardCharsets.US_ASCII).trim();
/*  83 */         buf.skipBytes(8);
/*  84 */       } else if (BitUtil.check(mask, 2)) {
/*  85 */         id = buf.toString(buf.readerIndex(), 22, StandardCharsets.US_ASCII).trim();
/*  86 */         buf.skipBytes(22);
/*     */       } else {
/*  88 */         LOGGER.warn("No device id field");
/*  89 */         return null;
/*     */       } 
/*  91 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/*  92 */       if (deviceSession == null) {
/*  93 */         return null;
/*     */       }
/*  95 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  97 */       if (BitUtil.check(mask, 3)) {
/*  98 */         position.set("io1", Integer.valueOf(buf.readUnsignedShort()));
/*     */       }
/*     */       
/* 101 */       if (BitUtil.check(mask, 4)) {
/* 102 */         position.set("adc1", Integer.valueOf(buf.readUnsignedShort()));
/*     */       }
/*     */       
/* 105 */       if (BitUtil.check(mask, 5)) {
/* 106 */         position.set("adc2", Integer.valueOf(buf.readUnsignedShort()));
/*     */       }
/*     */       
/* 109 */       if (BitUtil.check(mask, 7)) {
/* 110 */         buf.readUnsignedByte();
/*     */       }
/*     */       
/* 113 */       DateBuilder dateBuilder = new DateBuilder();
/*     */       
/* 115 */       if (BitUtil.check(mask, 8)) {
/* 116 */         dateBuilder.setDateReverse(buf
/* 117 */             .readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*     */       }
/*     */       
/* 120 */       if (BitUtil.check(mask, 9)) {
/* 121 */         position.setValid((buf.readUnsignedByte() == 1));
/*     */       }
/*     */       
/* 124 */       if (BitUtil.check(mask, 10)) {
/* 125 */         position.setLatitude(convertCoordinate(buf.readUnsignedInt()));
/*     */       }
/*     */       
/* 128 */       if (BitUtil.check(mask, 11)) {
/* 129 */         position.setLongitude(convertCoordinate(buf.readUnsignedInt()));
/*     */       }
/*     */       
/* 132 */       if (BitUtil.check(mask, 12)) {
/* 133 */         position.setSpeed(buf.readUnsignedShort() / 10.0D);
/*     */       }
/*     */       
/* 136 */       if (BitUtil.check(mask, 13)) {
/* 137 */         position.setCourse(buf.readUnsignedShort() / 10.0D);
/*     */       }
/*     */       
/* 140 */       if (BitUtil.check(mask, 14)) {
/* 141 */         dateBuilder.setTime(buf
/* 142 */             .readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*     */       }
/*     */       
/* 145 */       position.setTime(dateBuilder.getDate());
/*     */       
/* 147 */       if (BitUtil.check(mask, 15)) {
/* 148 */         position.setAltitude(buf.readMedium());
/*     */       }
/*     */       
/* 151 */       if (BitUtil.check(mask, 16)) {
/* 152 */         position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */       }
/*     */       
/* 155 */       if (BitUtil.check(mask, 17)) {
/* 156 */         position.set("battery", Integer.valueOf(buf.readUnsignedShort()));
/*     */       }
/*     */       
/* 159 */       if (BitUtil.check(mask, 20)) {
/* 160 */         position.set("tripOdometer", Long.valueOf(buf.readUnsignedInt()));
/*     */       }
/*     */       
/* 163 */       if (BitUtil.check(mask, 21)) {
/* 164 */         position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*     */       }
/*     */       
/* 167 */       if (BitUtil.check(mask, 22)) {
/* 168 */         buf.skipBytes(6);
/*     */       }
/*     */       
/* 171 */       if (BitUtil.check(mask, 24)) {
/* 172 */         position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*     */       }
/*     */       
/* 175 */       if (BitUtil.check(mask, 25)) {
/* 176 */         buf.skipBytes(18);
/*     */       }
/*     */       
/* 179 */       if (BitUtil.check(mask, 26)) {
/* 180 */         buf.skipBytes(54);
/*     */       }
/*     */       
/* 183 */       if (BitUtil.check(mask, 28)) {
/* 184 */         position.set("index", Integer.valueOf(buf.readUnsignedShort()));
/*     */       }
/*     */       
/* 187 */       return position;
/*     */     } 
/*     */     
/* 190 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SkypatrolProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */