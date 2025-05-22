/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.HashSet;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ public class BlackKiteProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private static final int TAG_IMEI = 3;
/*     */   private static final int TAG_DATE = 32;
/*     */   private static final int TAG_COORDINATES = 48;
/*     */   private static final int TAG_SPEED_COURSE = 51;
/*     */   private static final int TAG_ALTITUDE = 52;
/*     */   private static final int TAG_STATUS = 64;
/*     */   private static final int TAG_DIGITAL_OUTPUTS = 69;
/*     */   private static final int TAG_DIGITAL_INPUTS = 70;
/*     */   private static final int TAG_INPUT_VOLTAGE1 = 80;
/*     */   private static final int TAG_INPUT_VOLTAGE2 = 81;
/*     */   private static final int TAG_INPUT_VOLTAGE3 = 82;
/*     */   private static final int TAG_INPUT_VOLTAGE4 = 83;
/*     */   private static final int TAG_XT1 = 96;
/*     */   private static final int TAG_XT2 = 97;
/*     */   private static final int TAG_XT3 = 98;
/*     */   
/*     */   public BlackKiteProtocolDecoder(Protocol protocol) {
/*  40 */     super(protocol);
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
/*     */   private void sendReply(Channel channel, int checksum) {
/*  60 */     if (channel != null) {
/*  61 */       ByteBuf reply = Unpooled.buffer(3);
/*  62 */       reply.writeByte(2);
/*  63 */       reply.writeShortLE((short)checksum);
/*  64 */       channel.writeAndFlush(new NetworkMessage(reply, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  72 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  74 */     buf.readUnsignedByte();
/*  75 */     int length = (buf.readUnsignedShortLE() & 0x7FFF) + 3;
/*     */     
/*  77 */     List<Position> positions = new LinkedList<>();
/*  78 */     Set<Integer> tags = new HashSet<>();
/*  79 */     boolean hasLocation = false;
/*  80 */     Position position = new Position(getProtocolName());
/*     */     
/*  82 */     while (buf.readerIndex() < length) {
/*     */ 
/*     */       
/*  85 */       int status, input, i, output, j, tag = buf.readUnsignedByte();
/*  86 */       if (tags.contains(Integer.valueOf(tag))) {
/*  87 */         if (hasLocation && position.getFixTime() != null) {
/*  88 */           positions.add(position);
/*     */         }
/*  90 */         tags.clear();
/*  91 */         hasLocation = false;
/*  92 */         position = new Position(getProtocolName());
/*     */       } 
/*  94 */       tags.add(Integer.valueOf(tag));
/*     */       
/*  96 */       switch (tag) {
/*     */         
/*     */         case 3:
/*  99 */           getDeviceSession(channel, remoteAddress, new String[] { buf.readSlice(15).toString(StandardCharsets.US_ASCII) });
/*     */ 
/*     */         
/*     */         case 32:
/* 103 */           position.setTime(new Date(buf.readUnsignedIntLE() * 1000L));
/*     */ 
/*     */         
/*     */         case 48:
/* 107 */           hasLocation = true;
/* 108 */           position.setValid(((buf.readUnsignedByte() & 0xF0) == 0));
/* 109 */           position.setLatitude(buf.readIntLE() / 1000000.0D);
/* 110 */           position.setLongitude(buf.readIntLE() / 1000000.0D);
/*     */ 
/*     */         
/*     */         case 51:
/* 114 */           position.setSpeed(buf.readUnsignedShortLE() * 0.0539957D);
/* 115 */           position.setCourse(buf.readUnsignedShortLE() * 0.1D);
/*     */ 
/*     */         
/*     */         case 52:
/* 119 */           position.setAltitude(buf.readShortLE());
/*     */ 
/*     */         
/*     */         case 64:
/* 123 */           status = buf.readUnsignedShortLE();
/* 124 */           position.set("ignition", Boolean.valueOf(BitUtil.check(status, 9)));
/* 125 */           if (BitUtil.check(status, 15)) {
/* 126 */             position.set("alarm", "general");
/*     */           }
/* 128 */           position.set("charge", Boolean.valueOf(BitUtil.check(status, 2)));
/*     */ 
/*     */         
/*     */         case 70:
/* 132 */           input = buf.readUnsignedShortLE();
/* 133 */           for (i = 0; i < 16; i++) {
/* 134 */             position.set("io" + (i + 1), Boolean.valueOf(BitUtil.check(input, i)));
/*     */           }
/*     */ 
/*     */         
/*     */         case 69:
/* 139 */           output = buf.readUnsignedShortLE();
/* 140 */           for (j = 0; j < 16; j++) {
/* 141 */             position.set("io" + (j + 17), Boolean.valueOf(BitUtil.check(output, j)));
/*     */           }
/*     */ 
/*     */         
/*     */         case 80:
/* 146 */           position.set("adc1", Double.valueOf(buf.readUnsignedShortLE() / 1000.0D));
/*     */ 
/*     */         
/*     */         case 81:
/* 150 */           position.set("adc2", Double.valueOf(buf.readUnsignedShortLE() / 1000.0D));
/*     */ 
/*     */         
/*     */         case 82:
/* 154 */           position.set("adc3", Double.valueOf(buf.readUnsignedShortLE() / 1000.0D));
/*     */ 
/*     */         
/*     */         case 83:
/* 158 */           position.set("adc4", Double.valueOf(buf.readUnsignedShortLE() / 1000.0D));
/*     */ 
/*     */         
/*     */         case 96:
/*     */         case 97:
/*     */         case 98:
/* 164 */           buf.skipBytes(16);
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     } 
/* 173 */     if (hasLocation && position.getFixTime() != null) {
/* 174 */       positions.add(position);
/*     */     }
/*     */     
/* 177 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 178 */     if (deviceSession == null) {
/* 179 */       return null;
/*     */     }
/*     */     
/* 182 */     sendReply(channel, buf.readUnsignedShortLE());
/*     */     
/* 184 */     for (Position p : positions) {
/* 185 */       p.setDeviceId(deviceSession.getDeviceId());
/*     */     }
/*     */     
/* 188 */     if (positions.isEmpty()) {
/* 189 */       return null;
/*     */     }
/*     */     
/* 192 */     return positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\BlackKiteProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */