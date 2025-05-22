/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
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
/*     */ 
/*     */ public class GranitProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private static final int HEADER_LENGTH = 6;
/*     */   private double adc1Ratio;
/*     */   private double adc2Ratio;
/*     */   private double adc3Ratio;
/*     */   private double adc4Ratio;
/*     */   
/*     */   public GranitProtocolDecoder(Protocol protocol) {
/*  46 */     super(protocol);
/*  47 */     this.adc1Ratio = Context.getConfig().getDouble("granit.adc1Ratio", 1.0D);
/*  48 */     this.adc2Ratio = Context.getConfig().getDouble("granit.adc2Ratio", 1.0D);
/*  49 */     this.adc3Ratio = Context.getConfig().getDouble("granit.adc3Ratio", 1.0D);
/*  50 */     this.adc4Ratio = Context.getConfig().getDouble("granit.adc4Ratio", 1.0D);
/*     */   }
/*     */   
/*     */   public static void appendChecksum(ByteBuf buffer, int length) {
/*  54 */     buffer.writeByte(42);
/*  55 */     int checksum = Checksum.xor(buffer.nioBuffer(0, length)) & 0xFF;
/*  56 */     String checksumString = String.format("%02X", new Object[] { Integer.valueOf(checksum) });
/*  57 */     buffer.writeBytes(checksumString.getBytes(StandardCharsets.US_ASCII));
/*  58 */     buffer.writeByte(13); buffer.writeByte(10);
/*     */   }
/*     */   
/*     */   private static void sendResponseCurrent(Channel channel, int deviceId, long time) {
/*  62 */     ByteBuf response = Unpooled.buffer();
/*  63 */     response.writeBytes("BB+UGRC~".getBytes(StandardCharsets.US_ASCII));
/*  64 */     response.writeShortLE(6);
/*  65 */     response.writeInt((int)time);
/*  66 */     response.writeShortLE(deviceId);
/*  67 */     appendChecksum(response, 16);
/*  68 */     channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */   }
/*     */   
/*     */   private static void sendResponseArchive(Channel channel, int deviceId, int packNum) {
/*  72 */     ByteBuf response = Unpooled.buffer();
/*  73 */     response.writeBytes("BB+ARCF~".getBytes(StandardCharsets.US_ASCII));
/*  74 */     response.writeShortLE(4);
/*  75 */     response.writeShortLE(packNum);
/*  76 */     response.writeShortLE(deviceId);
/*  77 */     appendChecksum(response, 14);
/*  78 */     channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */   }
/*     */   
/*     */   private void decodeStructure(ByteBuf buf, Position position) {
/*  82 */     short flags = buf.readUnsignedByte();
/*  83 */     position.setValid(BitUtil.check(flags, 7));
/*  84 */     if (BitUtil.check(flags, 1)) {
/*  85 */       position.set("alarm", "general");
/*     */     }
/*     */     
/*  88 */     short satDel = buf.readUnsignedByte();
/*  89 */     position.set("sat", Integer.valueOf(BitUtil.from(satDel, 4)));
/*     */     
/*  91 */     int pdop = BitUtil.to(satDel, 4);
/*  92 */     position.set("pdop", Integer.valueOf(pdop));
/*     */     
/*  94 */     int lonDegrees = buf.readUnsignedByte();
/*  95 */     int latDegrees = buf.readUnsignedByte();
/*  96 */     int lonMinutes = buf.readUnsignedShortLE();
/*  97 */     int latMinutes = buf.readUnsignedShortLE();
/*     */     
/*  99 */     double latitude = latDegrees + latMinutes / 60000.0D;
/* 100 */     double longitude = lonDegrees + lonMinutes / 60000.0D;
/*     */     
/* 102 */     if (position.getValid()) {
/* 103 */       if (!BitUtil.check(flags, 4)) {
/* 104 */         latitude = -latitude;
/*     */       }
/* 106 */       if (!BitUtil.check(flags, 5)) {
/* 107 */         longitude = -longitude;
/*     */       }
/*     */     } 
/*     */     
/* 111 */     position.setLongitude(longitude);
/* 112 */     position.setLatitude(latitude);
/*     */     
/* 114 */     position.setSpeed(buf.readUnsignedByte());
/*     */     
/* 116 */     int course = buf.readUnsignedByte();
/* 117 */     if (BitUtil.check(flags, 6)) {
/* 118 */       course |= 0x100;
/*     */     }
/* 120 */     position.setCourse(course);
/*     */     
/* 122 */     position.set("distance", Short.valueOf(buf.readShortLE()));
/*     */     
/* 124 */     int analogIn1 = buf.readUnsignedByte();
/* 125 */     int analogIn2 = buf.readUnsignedByte();
/* 126 */     int analogIn3 = buf.readUnsignedByte();
/* 127 */     int analogIn4 = buf.readUnsignedByte();
/*     */     
/* 129 */     int analogInHi = buf.readUnsignedByte();
/*     */     
/* 131 */     analogIn1 = analogInHi << 8 & 0x300 | analogIn1;
/* 132 */     analogIn2 = analogInHi << 6 & 0x300 | analogIn2;
/* 133 */     analogIn3 = analogInHi << 4 & 0x300 | analogIn3;
/* 134 */     analogIn4 = analogInHi << 2 & 0x300 | analogIn4;
/*     */     
/* 136 */     position.set("adc1", Double.valueOf(analogIn1 * this.adc1Ratio));
/* 137 */     position.set("adc2", Double.valueOf(analogIn2 * this.adc2Ratio));
/* 138 */     position.set("adc3", Double.valueOf(analogIn3 * this.adc3Ratio));
/* 139 */     position.set("adc4", Double.valueOf(analogIn4 * this.adc4Ratio));
/*     */     
/* 141 */     position.setAltitude((buf.readUnsignedByte() * 10));
/*     */     
/* 143 */     int output = buf.readUnsignedByte();
/* 144 */     for (int i = 0; i < 8; i++) {
/* 145 */       position.set("io" + (i + 1), Boolean.valueOf(BitUtil.check(output, i)));
/*     */     }
/* 147 */     buf.readUnsignedByte();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 153 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 155 */     int indexTilde = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)126);
/*     */     
/* 157 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*     */     
/* 159 */     if (deviceSession != null && indexTilde == -1) {
/* 160 */       String bufString = buf.toString(StandardCharsets.US_ASCII);
/* 161 */       Position position = new Position(getProtocolName());
/* 162 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 164 */       position.setTime(new Date());
/* 165 */       getLastLocation(position, new Date());
/* 166 */       position.setValid(false);
/* 167 */       position.set("result", bufString);
/* 168 */       return position;
/*     */     } 
/*     */     
/* 171 */     if (buf.readableBytes() < 6) {
/* 172 */       return null;
/*     */     }
/* 174 */     String header = buf.readSlice(6).toString(StandardCharsets.US_ASCII);
/*     */     
/* 176 */     if (header.equals("+RRCB~")) {
/*     */       
/* 178 */       buf.skipBytes(2);
/* 179 */       int deviceId = buf.readUnsignedShortLE();
/* 180 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(deviceId) });
/* 181 */       if (deviceSession == null) {
/* 182 */         return null;
/*     */       }
/* 184 */       long unixTime = buf.readUnsignedIntLE();
/* 185 */       if (channel != null) {
/* 186 */         sendResponseCurrent(channel, deviceId, unixTime);
/*     */       }
/* 188 */       Position position = new Position(getProtocolName());
/* 189 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 191 */       position.setTime(new Date(unixTime * 1000L));
/*     */       
/* 193 */       decodeStructure(buf, position);
/* 194 */       return position;
/*     */     } 
/* 196 */     if (header.equals("+DDAT~")) {
/*     */       
/* 198 */       buf.skipBytes(2);
/* 199 */       int deviceId = buf.readUnsignedShortLE();
/* 200 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(deviceId) });
/* 201 */       if (deviceSession == null) {
/* 202 */         return null;
/*     */       }
/* 204 */       byte format = buf.readByte();
/* 205 */       if (format != 4) {
/* 206 */         return null;
/*     */       }
/* 208 */       byte nblocks = buf.readByte();
/* 209 */       int packNum = buf.readUnsignedShortLE();
/* 210 */       if (channel != null) {
/* 211 */         sendResponseArchive(channel, deviceId, packNum);
/*     */       }
/* 213 */       List<Position> positions = new ArrayList<>();
/* 214 */       while (nblocks > 0) {
/* 215 */         nblocks = (byte)(nblocks - 1);
/* 216 */         long unixTime = buf.readUnsignedIntLE();
/* 217 */         int timeIncrement = buf.getUnsignedShortLE(buf.readerIndex() + 120);
/* 218 */         for (int i = 0; i < 6; i++) {
/* 219 */           if (buf.getUnsignedByte(buf.readerIndex()) != 254) {
/* 220 */             Position position = new Position(getProtocolName());
/* 221 */             position.setDeviceId(deviceSession.getDeviceId());
/* 222 */             position.setTime(new Date((unixTime + (i * timeIncrement)) * 1000L));
/* 223 */             decodeStructure(buf, position);
/* 224 */             position.set("archive", Boolean.valueOf(true));
/* 225 */             positions.add(position);
/*     */           } else {
/* 227 */             buf.skipBytes(20);
/*     */           } 
/*     */         } 
/* 230 */         buf.skipBytes(2);
/*     */       } 
/* 232 */       return positions;
/*     */     } 
/*     */ 
/*     */     
/* 236 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GranitProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */