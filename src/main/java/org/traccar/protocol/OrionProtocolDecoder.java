/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
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
/*     */ public class OrionProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_USERLOG = 0;
/*     */   public static final int MSG_SYSLOG = 3;
/*     */   
/*     */   public OrionProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void sendResponse(Channel channel, ByteBuf buf) {
/*  42 */     if (channel != null) {
/*  43 */       ByteBuf response = Unpooled.buffer(4);
/*  44 */       response.writeByte(42);
/*  45 */       response.writeShort(buf.getUnsignedShort(buf.writerIndex() - 2));
/*  46 */       response.writeByte(buf.getUnsignedByte(buf.writerIndex() - 3));
/*  47 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */   
/*     */   private static double convertCoordinate(int raw) {
/*  52 */     int degrees = raw / 1000000;
/*  53 */     double minutes = (raw % 1000000) / 10000.0D;
/*  54 */     return degrees + minutes / 60.0D;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  61 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  63 */     buf.skipBytes(2);
/*  64 */     int type = buf.readUnsignedByte() & 0xF;
/*     */     
/*  66 */     if (type == 0) {
/*     */       
/*  68 */       int header = buf.readUnsignedByte();
/*     */       
/*  70 */       if ((header & 0x40) != 0) {
/*  71 */         sendResponse(channel, buf);
/*     */       }
/*     */       
/*  74 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] {
/*  75 */             String.valueOf(buf.readUnsignedInt()) });
/*  76 */       if (deviceSession == null) {
/*  77 */         return null;
/*     */       }
/*     */       
/*  80 */       List<Position> positions = new LinkedList<>();
/*     */       
/*  82 */       for (int i = 0; i < (header & 0xF); i++) {
/*     */         
/*  84 */         Position position = new Position(getProtocolName());
/*  85 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/*  87 */         position.set("event", Short.valueOf(buf.readUnsignedByte()));
/*  88 */         buf.readUnsignedByte();
/*  89 */         position.set("flags", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */         
/*  91 */         position.setLatitude(convertCoordinate(buf.readIntLE()));
/*  92 */         position.setLongitude(convertCoordinate(buf.readIntLE()));
/*  93 */         position.setAltitude(buf.readShortLE() / 10.0D);
/*  94 */         position.setCourse(buf.readUnsignedShortLE());
/*  95 */         position.setSpeed(buf.readUnsignedShortLE() * 0.0539957D);
/*     */ 
/*     */ 
/*     */         
/*  99 */         DateBuilder dateBuilder = (new DateBuilder()).setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/* 100 */         position.setTime(dateBuilder.getDate());
/*     */         
/* 102 */         int satellites = buf.readUnsignedByte();
/* 103 */         position.setValid((satellites >= 3));
/* 104 */         position.set("sat", Integer.valueOf(satellites));
/*     */         
/* 106 */         positions.add(position);
/*     */       } 
/*     */       
/* 109 */       return positions;
/*     */     } 
/*     */     
/* 112 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OrionProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */