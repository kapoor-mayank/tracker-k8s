/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
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
/*     */ public class RecodaProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_HEARTBEAT = 4097;
/*     */   public static final int MSG_REQUEST_RESPONSE = 536870913;
/*     */   public static final int MSG_SIGNAL_LINK_REGISTRATION = 536875009;
/*     */   public static final int MSG_EVENT_NOTICE = 536879105;
/*     */   public static final int MSG_GPS_DATA = 536875025;
/*     */   
/*     */   public RecodaProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
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
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  47 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  49 */     int type = buf.readIntLE();
/*  50 */     buf.readUnsignedIntLE();
/*     */     
/*  52 */     if (type != 4097) {
/*  53 */       buf.readUnsignedShortLE();
/*  54 */       buf.readUnsignedShortLE();
/*     */     } 
/*     */     
/*  57 */     if (type == 536875009) {
/*     */       
/*  59 */       getDeviceSession(channel, remoteAddress, new String[] { buf.readSlice(12).toString(StandardCharsets.US_ASCII) });
/*     */     }
/*  61 */     else if (type == 536875025) {
/*     */       
/*  63 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  64 */       if (deviceSession == null) {
/*  65 */         return null;
/*     */       }
/*     */       
/*  68 */       Position position = new Position(getProtocolName());
/*  69 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  71 */       position.setTime(new Date(buf.readLongLE()));
/*     */       
/*  73 */       int flags = buf.readUnsignedByte();
/*     */       
/*  75 */       if (BitUtil.check(flags, 0)) {
/*     */         
/*  77 */         buf.readUnsignedShortLE();
/*     */         
/*  79 */         position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShortLE()));
/*     */         
/*  81 */         position.setLongitude(buf.readUnsignedByte() + buf.readUnsignedByte() / 60.0D);
/*  82 */         position.setLatitude(buf.readUnsignedByte() + buf.readUnsignedByte() / 60.0D);
/*     */         
/*  84 */         position.setLongitude(position.getLongitude() + buf.readUnsignedIntLE() / 3600.0D);
/*  85 */         position.setLatitude(position.getLatitude() + buf.readUnsignedIntLE() / 3600.0D);
/*     */         
/*  87 */         int status = buf.readUnsignedByte();
/*     */         
/*  89 */         position.setValid(BitUtil.check(status, 0));
/*  90 */         if (BitUtil.check(status, 1)) {
/*  91 */           position.setLongitude(-position.getLongitude());
/*     */         }
/*  93 */         if (!BitUtil.check(status, 2)) {
/*  94 */           position.setLatitude(-position.getLatitude());
/*     */         }
/*     */       }
/*     */       else {
/*     */         
/*  99 */         getLastLocation(position, position.getDeviceTime());
/*     */       } 
/*     */ 
/*     */       
/* 103 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 107 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\RecodaProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */