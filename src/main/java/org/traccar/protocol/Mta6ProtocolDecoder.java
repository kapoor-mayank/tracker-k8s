/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import io.netty.handler.codec.http.DefaultFullHttpResponse;
/*     */ import io.netty.handler.codec.http.FullHttpRequest;
/*     */ import io.netty.handler.codec.http.HttpResponseStatus;
/*     */ import io.netty.handler.codec.http.HttpVersion;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.slf4j.Logger;
/*     */ import org.slf4j.LoggerFactory;
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
/*     */ 
/*     */ public class Mta6ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*  45 */   private static final Logger LOGGER = LoggerFactory.getLogger(Mta6ProtocolDecoder.class);
/*     */   
/*     */   private final boolean simple;
/*     */   
/*     */   public Mta6ProtocolDecoder(Protocol protocol, boolean simple) {
/*  50 */     super(protocol);
/*  51 */     this.simple = simple;
/*     */   }
/*     */   
/*     */   private void sendContinue(Channel channel) {
/*  55 */     DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
/*     */     
/*  57 */     channel.writeAndFlush(new NetworkMessage(defaultFullHttpResponse, channel.remoteAddress()));
/*     */   }
/*     */   
/*     */   private void sendResponse(Channel channel, short packetId, short packetCount) {
/*  61 */     ByteBuf begin = Unpooled.copiedBuffer("#ACK#", StandardCharsets.US_ASCII);
/*  62 */     ByteBuf end = Unpooled.buffer(3);
/*  63 */     end.writeByte(packetId);
/*  64 */     end.writeByte(packetCount);
/*  65 */     end.writeByte(0);
/*     */ 
/*     */     
/*  68 */     DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(new ByteBuf[] { begin, end }));
/*  69 */     channel.writeAndFlush(new NetworkMessage(defaultFullHttpResponse, channel.remoteAddress()));
/*     */   }
/*     */   
/*     */   private static class FloatReader { private int previousFloat;
/*     */     
/*     */     private FloatReader() {}
/*     */     
/*     */     public float readFloat(ByteBuf buf) {
/*  77 */       switch (buf.getUnsignedByte(buf.readerIndex()) >> 6)
/*     */       { case 0:
/*  79 */           this.previousFloat = buf.readInt() << 2;
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
/*  94 */           return Float.intBitsToFloat(this.previousFloat);case 1: this.previousFloat = (this.previousFloat & 0xFFFFFF00) + ((buf.readUnsignedByte() & 0x3F) << 2); return Float.intBitsToFloat(this.previousFloat);case 2: this.previousFloat = (this.previousFloat & 0xFFFF0000) + ((buf.readUnsignedShort() & 0x3FFF) << 2); return Float.intBitsToFloat(this.previousFloat);case 3: this.previousFloat = (this.previousFloat & 0xFF000000) + ((buf.readUnsignedMedium() & 0x3FFFFF) << 2); return Float.intBitsToFloat(this.previousFloat); }  Mta6ProtocolDecoder.LOGGER.warn("MTA6 float decoding error", new IllegalArgumentException()); return Float.intBitsToFloat(this.previousFloat);
/*     */     } }
/*     */ 
/*     */   
/*     */   private static class TimeReader extends FloatReader {
/*     */     private long weekNumber;
/*     */     
/*     */     private TimeReader() {}
/*     */     
/*     */     public Date readTime(ByteBuf buf) {
/* 104 */       long weekTime = (long)(readFloat(buf) * 1000.0F);
/* 105 */       if (this.weekNumber == 0L) {
/* 106 */         this.weekNumber = buf.readUnsignedShort();
/*     */       }
/*     */       
/* 109 */       DateBuilder dateBuilder = (new DateBuilder()).setDate(1980, 1, 6);
/* 110 */       dateBuilder.addMillis(this.weekNumber * 7L * 24L * 60L * 60L * 1000L + weekTime);
/*     */       
/* 112 */       return dateBuilder.getDate();
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   private List<Position> parseFormatA(DeviceSession deviceSession, ByteBuf buf) {
/* 118 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 120 */     FloatReader latitudeReader = new FloatReader();
/* 121 */     FloatReader longitudeReader = new FloatReader();
/* 122 */     TimeReader timeReader = new TimeReader();
/*     */     
/*     */     try {
/* 125 */       while (buf.isReadable()) {
/* 126 */         Position position = new Position(getProtocolName());
/* 127 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 129 */         short flags = buf.readUnsignedByte();
/*     */         
/* 131 */         short event = buf.readUnsignedByte();
/* 132 */         if (BitUtil.check(event, 7)) {
/* 133 */           if (BitUtil.check(event, 6)) {
/* 134 */             buf.skipBytes(8);
/*     */           } else {
/* 136 */             while (BitUtil.check(event, 7)) {
/* 137 */               event = buf.readUnsignedByte();
/*     */             }
/*     */           } 
/*     */         }
/*     */         
/* 142 */         position.setLatitude(latitudeReader.readFloat(buf) / Math.PI * 180.0D);
/* 143 */         position.setLongitude(longitudeReader.readFloat(buf) / Math.PI * 180.0D);
/* 144 */         position.setTime(timeReader.readTime(buf));
/*     */         
/* 146 */         if (BitUtil.check(flags, 0)) {
/* 147 */           buf.readUnsignedByte();
/*     */         }
/*     */         
/* 150 */         if (BitUtil.check(flags, 1)) {
/* 151 */           position.setAltitude(buf.readUnsignedShort());
/*     */         }
/*     */         
/* 154 */         if (BitUtil.check(flags, 2)) {
/* 155 */           position.setSpeed((buf.readUnsignedShort() & 0x3FF));
/* 156 */           position.setCourse(buf.readUnsignedByte());
/*     */         } 
/*     */         
/* 159 */         if (BitUtil.check(flags, 3)) {
/* 160 */           position.set("odometer", Integer.valueOf(buf.readUnsignedShort() * 1000));
/*     */         }
/*     */         
/* 163 */         if (BitUtil.check(flags, 4)) {
/* 164 */           position.set("fuelConsumptionAccumulator1", Long.valueOf(buf.readUnsignedInt()));
/* 165 */           position.set("fuelConsumptionAccumulator2", Long.valueOf(buf.readUnsignedInt()));
/* 166 */           position.set("hours1", Integer.valueOf(buf.readUnsignedShort()));
/* 167 */           position.set("hours2", Integer.valueOf(buf.readUnsignedShort()));
/*     */         } 
/*     */         
/* 170 */         if (BitUtil.check(flags, 5)) {
/* 171 */           position.set("adc1", Integer.valueOf(buf.readUnsignedShort() & 0x3FF));
/* 172 */           position.set("adc2", Integer.valueOf(buf.readUnsignedShort() & 0x3FF));
/* 173 */           position.set("adc3", Integer.valueOf(buf.readUnsignedShort() & 0x3FF));
/* 174 */           position.set("adc4", Integer.valueOf(buf.readUnsignedShort() & 0x3FF));
/*     */         } 
/*     */         
/* 177 */         if (BitUtil.check(flags, 6)) {
/* 178 */           position.set("temp1", Byte.valueOf(buf.readByte()));
/* 179 */           buf.getUnsignedByte(buf.readerIndex());
/* 180 */           position.set("input", Integer.valueOf(buf.readUnsignedShort() & 0xFFF));
/* 181 */           buf.readUnsignedShort();
/*     */         } 
/*     */         
/* 184 */         if (BitUtil.check(flags, 7)) {
/* 185 */           position.set("battery", Integer.valueOf(buf.getUnsignedByte(buf.readerIndex()) >> 2));
/* 186 */           position.set("power", Integer.valueOf(buf.readUnsignedShort() & 0x3FF));
/* 187 */           position.set("deviceTemp", Byte.valueOf(buf.readByte()));
/*     */           
/* 189 */           position.set("rssi", Integer.valueOf(buf.getUnsignedByte(buf.readerIndex()) >> 4 & 0x7));
/*     */           
/* 191 */           int satellites = buf.readUnsignedByte() & 0xF;
/* 192 */           position.setValid((satellites >= 3));
/* 193 */           position.set("sat", Integer.valueOf(satellites));
/*     */         } 
/* 195 */         positions.add(position);
/*     */       } 
/* 197 */     } catch (IndexOutOfBoundsException error) {
/* 198 */       LOGGER.warn("MTA6 parsing error", error);
/*     */     } 
/*     */     
/* 201 */     return positions;
/*     */   }
/*     */   
/*     */   private Position parseFormatA1(DeviceSession deviceSession, ByteBuf buf) {
/* 205 */     Position position = new Position(getProtocolName());
/* 206 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 208 */     short flags = buf.readUnsignedByte();
/*     */ 
/*     */     
/* 211 */     short event = buf.readUnsignedByte();
/* 212 */     if (BitUtil.check(event, 7)) {
/* 213 */       if (BitUtil.check(event, 6)) {
/* 214 */         buf.skipBytes(8);
/*     */       } else {
/* 216 */         while (BitUtil.check(event, 7)) {
/* 217 */           event = buf.readUnsignedByte();
/*     */         }
/*     */       } 
/*     */     }
/*     */     
/* 222 */     position.setLatitude((new FloatReader()).readFloat(buf) / Math.PI * 180.0D);
/* 223 */     position.setLongitude((new FloatReader()).readFloat(buf) / Math.PI * 180.0D);
/* 224 */     position.setTime((new TimeReader()).readTime(buf));
/*     */     
/* 226 */     position.set("status", Short.valueOf(buf.readUnsignedByte()));
/*     */     
/* 228 */     if (BitUtil.check(flags, 0)) {
/* 229 */       position.setAltitude(buf.readUnsignedShort());
/* 230 */       position.setSpeed(buf.readUnsignedByte());
/* 231 */       position.setCourse(buf.readByte());
/* 232 */       position.set("odometer", Float.valueOf((new FloatReader()).readFloat(buf)));
/*     */     } 
/*     */     
/* 235 */     if (BitUtil.check(flags, 1)) {
/* 236 */       position.set("fuelConsumption", Float.valueOf((new FloatReader()).readFloat(buf)));
/* 237 */       position.set("hours", Long.valueOf(UnitsConverter.msFromHours((new FloatReader()).readFloat(buf))));
/* 238 */       position.set("tank", Double.valueOf(buf.readUnsignedByte() * 0.4D));
/*     */     } 
/*     */     
/* 241 */     if (BitUtil.check(flags, 2)) {
/* 242 */       position.set("engine", Double.valueOf(buf.readUnsignedShort() * 0.125D));
/* 243 */       position.set("pedals", Short.valueOf(buf.readUnsignedByte()));
/* 244 */       position.set("temp1", Integer.valueOf(buf.readUnsignedByte() - 40));
/* 245 */       position.set("serviceOdometer", Integer.valueOf(buf.readUnsignedShort()));
/*     */     } 
/*     */     
/* 248 */     if (BitUtil.check(flags, 3)) {
/* 249 */       position.set("fuel", Integer.valueOf(buf.readUnsignedShort()));
/* 250 */       position.set("adc2", Integer.valueOf(buf.readUnsignedShort()));
/* 251 */       position.set("adc3", Integer.valueOf(buf.readUnsignedShort()));
/* 252 */       position.set("adc4", Integer.valueOf(buf.readUnsignedShort()));
/*     */     } 
/*     */     
/* 255 */     if (BitUtil.check(flags, 4)) {
/* 256 */       position.set("temp1", Byte.valueOf(buf.readByte()));
/* 257 */       buf.getUnsignedByte(buf.readerIndex());
/* 258 */       position.set("input", Integer.valueOf(buf.readUnsignedShort() & 0xFFF));
/* 259 */       buf.readUnsignedShort();
/*     */     } 
/*     */     
/* 262 */     if (BitUtil.check(flags, 5)) {
/* 263 */       position.set("battery", Integer.valueOf(buf.getUnsignedByte(buf.readerIndex()) >> 2));
/* 264 */       position.set("power", Integer.valueOf(buf.readUnsignedShort() & 0x3FF));
/* 265 */       position.set("deviceTemp", Byte.valueOf(buf.readByte()));
/*     */       
/* 267 */       position.set("rssi", Integer.valueOf(buf.getUnsignedByte(buf.readerIndex()) >> 5));
/*     */       
/* 269 */       int satellites = buf.readUnsignedByte() & 0x1F;
/* 270 */       position.setValid((satellites >= 3));
/* 271 */       position.set("sat", Integer.valueOf(satellites));
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 276 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 283 */     FullHttpRequest request = (FullHttpRequest)msg;
/* 284 */     ByteBuf buf = request.content();
/*     */     
/* 286 */     buf.skipBytes("id=".length());
/* 287 */     int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)38);
/* 288 */     String uniqueId = buf.toString(buf.readerIndex(), index - buf.readerIndex(), StandardCharsets.US_ASCII);
/* 289 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { uniqueId });
/* 290 */     if (deviceSession == null) {
/* 291 */       return null;
/*     */     }
/* 293 */     buf.skipBytes(uniqueId.length());
/* 294 */     buf.skipBytes("&bin=".length());
/*     */     
/* 296 */     short packetId = buf.readUnsignedByte();
/* 297 */     short offset = buf.readUnsignedByte();
/* 298 */     short packetCount = buf.readUnsignedByte();
/* 299 */     buf.readUnsignedByte();
/* 300 */     buf.readUnsignedByte();
/* 301 */     buf.skipBytes(offset - 5);
/*     */     
/* 303 */     if (channel != null) {
/* 304 */       sendContinue(channel);
/* 305 */       sendResponse(channel, packetId, packetCount);
/*     */     } 
/*     */     
/* 308 */     if (packetId == 49 || packetId == 50 || packetId == 54) {
/* 309 */       if (this.simple) {
/* 310 */         return parseFormatA1(deviceSession, buf);
/*     */       }
/* 312 */       return parseFormatA(deviceSession, buf);
/*     */     } 
/*     */ 
/*     */     
/* 316 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Mta6ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */