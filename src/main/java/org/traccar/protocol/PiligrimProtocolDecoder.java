/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import io.netty.handler.codec.http.DefaultFullHttpResponse;
/*     */ import io.netty.handler.codec.http.FullHttpRequest;
/*     */ import io.netty.handler.codec.http.HttpResponseStatus;
/*     */ import io.netty.handler.codec.http.HttpVersion;
/*     */ import io.netty.handler.codec.http.QueryStringDecoder;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
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
/*     */ public class PiligrimProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_GPS = 241;
/*     */   public static final int MSG_GPS_SENSORS = 242;
/*     */   public static final int MSG_EVENTS = 243;
/*     */   
/*     */   public PiligrimProtocolDecoder(Protocol protocol) {
/*  43 */     super(protocol);
/*     */   }
/*     */   
/*     */   private void sendResponse(Channel channel, String message) {
/*  47 */     if (channel != null) {
/*     */ 
/*     */       
/*  50 */       DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(message, StandardCharsets.US_ASCII));
/*  51 */       channel.writeAndFlush(new NetworkMessage(defaultFullHttpResponse, channel.remoteAddress()));
/*     */     } 
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
/*  63 */     FullHttpRequest request = (FullHttpRequest)msg;
/*  64 */     String uri = request.uri();
/*     */     
/*  66 */     if (uri.startsWith("/config")) {
/*     */       
/*  68 */       sendResponse(channel, "CONFIG: OK");
/*     */     }
/*  70 */     else if (uri.startsWith("/addlog")) {
/*     */       
/*  72 */       sendResponse(channel, "ADDLOG: OK");
/*     */     }
/*  74 */     else if (uri.startsWith("/inform")) {
/*     */       
/*  76 */       sendResponse(channel, "INFORM: OK");
/*     */     }
/*  78 */     else if (uri.startsWith("/bingps")) {
/*     */       
/*  80 */       sendResponse(channel, "BINGPS: OK");
/*     */       
/*  82 */       QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
/*  83 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { ((List<String>)decoder
/*  84 */             .parameters().get("imei")).get(0) });
/*  85 */       if (deviceSession == null) {
/*  86 */         return null;
/*     */       }
/*     */       
/*  89 */       List<Position> positions = new LinkedList<>();
/*  90 */       ByteBuf buf = request.content();
/*     */       
/*  92 */       while (buf.readableBytes() > 2) {
/*     */         
/*  94 */         buf.readUnsignedByte();
/*  95 */         int type = buf.readUnsignedByte();
/*  96 */         buf.readUnsignedByte();
/*     */         
/*  98 */         if (type == 241 || type == 242) {
/*     */           
/* 100 */           Position position = new Position(getProtocolName());
/* 101 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */           
/* 107 */           DateBuilder dateBuilder = (new DateBuilder()).setDay(buf.readUnsignedByte()).setMonth(buf.getByte(buf.readerIndex()) & 0xF).setYear(2010 + (buf.readUnsignedByte() >> 4)).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/* 108 */           position.setTime(dateBuilder.getDate());
/*     */           
/* 110 */           double latitude = buf.readUnsignedByte();
/* 111 */           latitude += buf.readUnsignedByte() / 60.0D;
/* 112 */           latitude += buf.readUnsignedByte() / 6000.0D;
/* 113 */           latitude += buf.readUnsignedByte() / 600000.0D;
/*     */           
/* 115 */           double longitude = buf.readUnsignedByte();
/* 116 */           longitude += buf.readUnsignedByte() / 60.0D;
/* 117 */           longitude += buf.readUnsignedByte() / 6000.0D;
/* 118 */           longitude += buf.readUnsignedByte() / 600000.0D;
/*     */           
/* 120 */           int flags = buf.readUnsignedByte();
/* 121 */           if (BitUtil.check(flags, 0)) {
/* 122 */             latitude = -latitude;
/*     */           }
/* 124 */           if (BitUtil.check(flags, 1)) {
/* 125 */             longitude = -longitude;
/*     */           }
/* 127 */           position.setLatitude(latitude);
/* 128 */           position.setLongitude(longitude);
/*     */           
/* 130 */           int satellites = buf.readUnsignedByte();
/* 131 */           position.set("sat", Integer.valueOf(satellites));
/* 132 */           position.setValid((satellites >= 3));
/*     */           
/* 134 */           position.setSpeed(buf.readUnsignedByte());
/*     */           
/* 136 */           double course = (buf.readUnsignedByte() << 1);
/* 137 */           course += (flags >> 2 & 0x1);
/* 138 */           course += buf.readUnsignedByte() / 100.0D;
/* 139 */           position.setCourse(course);
/*     */           
/* 141 */           if (type == 242) {
/* 142 */             double power = buf.readUnsignedByte();
/* 143 */             power += (buf.readUnsignedByte() << 8);
/* 144 */             position.set("power", Double.valueOf(power * 0.01D));
/*     */             
/* 146 */             double battery = buf.readUnsignedByte();
/* 147 */             battery += (buf.readUnsignedByte() << 8);
/* 148 */             position.set("battery", Double.valueOf(battery * 0.01D));
/*     */             
/* 150 */             buf.skipBytes(6);
/*     */           } 
/*     */           
/* 153 */           positions.add(position); continue;
/*     */         } 
/* 155 */         if (type == 243)
/*     */         {
/* 157 */           buf.skipBytes(13);
/*     */         }
/*     */       } 
/*     */       
/* 161 */       return positions;
/*     */     } 
/*     */     
/* 164 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PiligrimProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */