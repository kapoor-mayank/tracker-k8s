/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
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
/*     */ public class Pt215ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_LOGIN = 1;
/*     */   public static final int MSG_HEARTBEAT = 8;
/*     */   public static final int MSG_GPS_REALTIME = 16;
/*     */   public static final int MSG_GPS_OFFLINE = 17;
/*     */   public static final int MSG_STATUS = 19;
/*     */   
/*     */   public Pt215ProtocolDecoder(Protocol protocol) {
/*  35 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, int type, ByteBuf content) {
/*  46 */     if (channel != null) {
/*  47 */       ByteBuf response = Unpooled.buffer();
/*  48 */       response.writeByte(88);
/*  49 */       response.writeByte(88);
/*  50 */       response.writeByte((content != null) ? (1 + content.readableBytes()) : 1);
/*  51 */       response.writeByte(type);
/*  52 */       if (content != null) {
/*  53 */         response.writeBytes(content);
/*  54 */         content.release();
/*     */       } 
/*  56 */       response.writeByte(13);
/*  57 */       response.writeByte(10);
/*  58 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  66 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  68 */     buf.skipBytes(2);
/*  69 */     buf.readUnsignedByte();
/*  70 */     int type = buf.readUnsignedByte();
/*     */     
/*  72 */     if (type == 1) {
/*     */       
/*  74 */       getDeviceSession(channel, remoteAddress, new String[] { ByteBufUtil.hexDump(buf.readSlice(8)).substring(1) });
/*  75 */       sendResponse(channel, remoteAddress, type, (ByteBuf)null);
/*     */     }
/*  77 */     else if (type == 17 || type == 16) {
/*     */       
/*  79 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  80 */       if (deviceSession == null) {
/*  81 */         return null;
/*     */       }
/*     */       
/*  84 */       Position position = new Position(getProtocolName());
/*  85 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  87 */       sendResponse(channel, remoteAddress, type, buf.retainedSlice(buf.readerIndex(), 6));
/*     */ 
/*     */ 
/*     */       
/*  91 */       DateBuilder dateBuilder = (new DateBuilder()).setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/*  92 */       position.setTime(dateBuilder.getDate());
/*     */       
/*  94 */       double latitude = buf.readUnsignedInt() / 60.0D / 30000.0D;
/*  95 */       double longitude = buf.readUnsignedInt() / 60.0D / 30000.0D;
/*     */       
/*  97 */       int flags = buf.readUnsignedShort();
/*  98 */       position.setCourse(BitUtil.to(flags, 10));
/*  99 */       position.setValid(BitUtil.check(flags, 12));
/*     */       
/* 101 */       if (!BitUtil.check(flags, 10)) {
/* 102 */         latitude = -latitude;
/*     */       }
/* 104 */       if (BitUtil.check(flags, 11)) {
/* 105 */         longitude = -longitude;
/*     */       }
/*     */       
/* 108 */       position.setLatitude(latitude);
/* 109 */       position.setLongitude(longitude);
/*     */       
/* 111 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 115 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Pt215ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */