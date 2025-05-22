/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
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
/*     */ public class NoranProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_UPLOAD_POSITION = 8;
/*     */   public static final int MSG_UPLOAD_POSITION_NEW = 50;
/*     */   public static final int MSG_CONTROL = 2;
/*     */   public static final int MSG_CONTROL_RESPONSE = 32777;
/*     */   public static final int MSG_ALARM = 3;
/*     */   public static final int MSG_SHAKE_HAND = 0;
/*     */   public static final int MSG_SHAKE_HAND_RESPONSE = 32768;
/*     */   public static final int MSG_IMAGE_SIZE = 512;
/*     */   public static final int MSG_IMAGE_PACKET = 513;
/*     */   
/*     */   public NoranProtocolDecoder(Protocol protocol) {
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
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  55 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  57 */     buf.readUnsignedShortLE();
/*  58 */     int type = buf.readUnsignedShortLE();
/*     */     
/*  60 */     if (type == 0 && channel != null) {
/*     */       
/*  62 */       ByteBuf response = Unpooled.buffer(13);
/*  63 */       response.writeCharSequence("\r\n*KW", StandardCharsets.US_ASCII);
/*  64 */       response.writeByte(0);
/*  65 */       response.writeShortLE(response.capacity());
/*  66 */       response.writeShortLE(32768);
/*  67 */       response.writeByte(1);
/*  68 */       response.writeCharSequence("\r\n", StandardCharsets.US_ASCII);
/*     */       
/*  70 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     }
/*  72 */     else if (type == 8 || type == 50 || type == 32777 || type == 3) {
/*     */       ByteBuf rawId;
/*     */       
/*  75 */       boolean newFormat = false;
/*  76 */       if ((type == 8 && buf.readableBytes() == 48) || (type == 3 && buf
/*  77 */         .readableBytes() == 48) || (type == 32777 && buf
/*  78 */         .readableBytes() == 57)) {
/*  79 */         newFormat = true;
/*     */       }
/*     */       
/*  82 */       Position position = new Position(getProtocolName());
/*     */       
/*  84 */       if (type == 32777) {
/*  85 */         buf.readUnsignedIntLE();
/*  86 */         buf.readUnsignedIntLE();
/*     */       } 
/*     */       
/*  89 */       position.setValid(BitUtil.check(buf.readUnsignedByte(), 0));
/*     */       
/*  91 */       short alarm = buf.readUnsignedByte();
/*  92 */       switch (alarm) {
/*     */         case 1:
/*  94 */           position.set("alarm", "sos");
/*     */           break;
/*     */         case 2:
/*  97 */           position.set("alarm", "overspeed");
/*     */           break;
/*     */         case 3:
/* 100 */           position.set("alarm", "geofenceExit");
/*     */           break;
/*     */         case 9:
/* 103 */           position.set("alarm", "powerOff");
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 109 */       if (newFormat) {
/* 110 */         position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedIntLE()));
/* 111 */         position.setCourse(buf.readFloatLE());
/*     */       } else {
/* 113 */         position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/* 114 */         position.setCourse(buf.readUnsignedShortLE());
/*     */       } 
/* 116 */       position.setLongitude(buf.readFloatLE());
/* 117 */       position.setLatitude(buf.readFloatLE());
/*     */       
/* 119 */       if (!newFormat) {
/* 120 */         long timeValue = buf.readUnsignedIntLE();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/* 127 */         DateBuilder dateBuilder = (new DateBuilder()).setYear((int)BitUtil.from(timeValue, 26)).setMonth((int)BitUtil.between(timeValue, 22, 26)).setDay((int)BitUtil.between(timeValue, 17, 22)).setHour((int)BitUtil.between(timeValue, 12, 17)).setMinute((int)BitUtil.between(timeValue, 6, 12)).setSecond((int)BitUtil.to(timeValue, 6));
/* 128 */         position.setTime(dateBuilder.getDate());
/*     */       } 
/*     */ 
/*     */       
/* 132 */       if (newFormat) {
/* 133 */         rawId = buf.readSlice(12);
/*     */       } else {
/* 135 */         rawId = buf.readSlice(11);
/*     */       } 
/* 137 */       String id = rawId.toString(StandardCharsets.US_ASCII).replaceAll("[^\\p{Print}]", "");
/* 138 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 139 */       if (deviceSession == null) {
/* 140 */         return null;
/*     */       }
/* 142 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 144 */       if (newFormat) {
/* 145 */         DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
/* 146 */         position.setTime(dateFormat.parse(buf.readSlice(17).toString(StandardCharsets.US_ASCII)));
/* 147 */         buf.readByte();
/*     */       } 
/*     */       
/* 150 */       if (!newFormat) {
/* 151 */         position.set("io1", Short.valueOf(buf.readUnsignedByte()));
/* 152 */         position.set("fuel", Short.valueOf(buf.readUnsignedByte()));
/* 153 */       } else if (type == 50) {
/* 154 */         position.set("temp1", Short.valueOf(buf.readShortLE()));
/* 155 */         position.set("odometer", Float.valueOf(buf.readFloatLE()));
/*     */       } 
/*     */       
/* 158 */       return position;
/*     */     } 
/*     */     
/* 161 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NoranProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */