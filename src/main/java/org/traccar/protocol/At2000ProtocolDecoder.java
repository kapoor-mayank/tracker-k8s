/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import javax.crypto.Cipher;
/*     */ import javax.crypto.spec.IvParameterSpec;
/*     */ import javax.crypto.spec.SecretKeySpec;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DataConverter;
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
/*     */ public class At2000ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private static final int BLOCK_LENGTH = 16;
/*     */   public static final int MSG_ACKNOWLEDGEMENT = 0;
/*     */   public static final int MSG_DEVICE_ID = 1;
/*     */   public static final int MSG_TRACK_REQUEST = 136;
/*     */   public static final int MSG_TRACK_RESPONSE = 137;
/*     */   public static final int MSG_SESSION_END = 12;
/*     */   private Cipher cipher;
/*     */   
/*     */   public At2000ProtocolDecoder(Protocol protocol) {
/*  43 */     super(protocol);
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
/*     */   private static void sendRequest(Channel channel) {
/*  55 */     if (channel != null) {
/*  56 */       ByteBuf response = Unpooled.buffer(16);
/*  57 */       response.writeByte(136);
/*  58 */       response.writeMedium(0);
/*  59 */       response.writerIndex(16);
/*  60 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  68 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  70 */     if (buf.getUnsignedByte(buf.readerIndex()) == 1) {
/*  71 */       buf.readUnsignedByte();
/*     */     }
/*     */     
/*  74 */     int type = buf.readUnsignedByte();
/*  75 */     buf.readUnsignedMediumLE();
/*  76 */     buf.skipBytes(12);
/*     */     
/*  78 */     if (type == 1) {
/*     */       
/*  80 */       String imei = buf.readSlice(15).toString(StandardCharsets.US_ASCII);
/*  81 */       if (getDeviceSession(channel, remoteAddress, new String[] { imei }) != null)
/*     */       {
/*  83 */         byte[] iv = new byte[16];
/*  84 */         buf.readBytes(iv);
/*  85 */         IvParameterSpec ivSpec = new IvParameterSpec(iv);
/*     */ 
/*     */         
/*  88 */         SecretKeySpec keySpec = new SecretKeySpec(DataConverter.parseHex("000102030405060708090a0b0c0d0e0f"), "AES");
/*     */         
/*  90 */         this.cipher = Cipher.getInstance("AES/CBC/NoPadding");
/*  91 */         this.cipher.init(2, keySpec, ivSpec);
/*     */         
/*  93 */         byte[] data = new byte[16];
/*  94 */         buf.readBytes(data);
/*  95 */         this.cipher.update(data);
/*     */       }
/*     */     
/*     */     }
/*  99 */     else if (type == 137) {
/*     */       
/* 101 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 102 */       if (deviceSession == null) {
/* 103 */         return null;
/*     */       }
/*     */       
/* 106 */       if (buf.capacity() <= 16) {
/* 107 */         return null;
/*     */       }
/*     */       
/* 110 */       List<Position> positions = new LinkedList<>();
/*     */       
/* 112 */       byte[] data = new byte[buf.capacity() - 16];
/* 113 */       buf.readBytes(data);
/* 114 */       buf = Unpooled.wrappedBuffer(this.cipher.update(data));
/*     */       try {
/* 116 */         while (buf.readableBytes() >= 63) {
/*     */           
/* 118 */           Position position = new Position(getProtocolName());
/* 119 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */           
/* 121 */           buf.readUnsignedShortLE();
/* 122 */           buf.readUnsignedShortLE();
/*     */           
/* 124 */           position.setValid(true);
/*     */           
/* 126 */           position.setTime(new Date(buf.readLongLE() * 1000L));
/*     */           
/* 128 */           position.setLatitude(buf.readFloatLE());
/* 129 */           position.setLongitude(buf.readFloatLE());
/* 130 */           position.setAltitude(buf.readFloatLE());
/* 131 */           position.setSpeed(UnitsConverter.knotsFromKph(buf.readFloatLE()));
/* 132 */           position.setCourse(buf.readFloatLE());
/*     */           
/* 134 */           buf.readUnsignedIntLE();
/* 135 */           buf.readUnsignedIntLE();
/* 136 */           buf.readUnsignedIntLE();
/* 137 */           buf.readUnsignedIntLE();
/* 138 */           buf.readUnsignedShortLE();
/*     */           
/* 140 */           position.set("adc1", Integer.valueOf(buf.readUnsignedShortLE()));
/* 141 */           position.set("adc1", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */           
/* 143 */           position.set("power", Double.valueOf(buf.readUnsignedShortLE() * 0.001D));
/*     */           
/* 145 */           buf.readUnsignedShortLE();
/* 146 */           position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/* 147 */           buf.readUnsignedByte();
/*     */           
/* 149 */           position.set("battery", Short.valueOf(buf.readUnsignedByte()));
/* 150 */           position.set("temp1", Short.valueOf(buf.readUnsignedByte()));
/* 151 */           position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */           
/* 153 */           positions.add(position);
/*     */         } 
/*     */       } finally {
/*     */         
/* 157 */         buf.release();
/*     */       } 
/*     */       
/* 160 */       return positions;
/*     */     } 
/*     */ 
/*     */     
/* 164 */     if (type == 1) {
/* 165 */       sendRequest(channel);
/*     */     }
/*     */     
/* 168 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\At2000ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */