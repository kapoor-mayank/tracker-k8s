/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BcdUtil;
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
/*     */ public class VnetProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_LOGIN = 0;
/*     */   public static final int MSG_LBS = 50;
/*     */   public static final int MSG_GPS = 51;
/*     */   
/*     */   public VnetProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
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
/*  47 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  49 */     buf.skipBytes(2);
/*  50 */     int type = BitUtil.to(buf.readUnsignedShortLE(), 15);
/*  51 */     buf.readUnsignedShortLE();
/*     */ 
/*     */ 
/*     */     
/*  55 */     DateBuilder dateBuilder = (new DateBuilder()).setDateReverse(BcdUtil.readInteger(buf, 2), BcdUtil.readInteger(buf, 2), BcdUtil.readInteger(buf, 2)).setTime(BcdUtil.readInteger(buf, 2), BcdUtil.readInteger(buf, 2), BcdUtil.readInteger(buf, 2));
/*     */     
/*  57 */     if (type == 0) {
/*     */       
/*  59 */       String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(0, 15);
/*  60 */       getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  61 */       if (channel != null) {
/*  62 */         channel.writeAndFlush(new NetworkMessage(buf
/*  63 */               .retainedSlice(0, buf.writerIndex()), channel.remoteAddress()));
/*     */       }
/*     */     }
/*  66 */     else if (type == 51) {
/*     */       
/*  68 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  69 */       if (deviceSession == null) {
/*  70 */         return null;
/*     */       }
/*     */       
/*  73 */       Position position = new Position(getProtocolName());
/*  74 */       position.setDeviceId(deviceSession.getDeviceId());
/*  75 */       position.setTime(dateBuilder.getDate());
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  80 */       int value = BcdUtil.readInteger(buf, 8);
/*  81 */       int degrees = value / 1000000;
/*  82 */       double lat = degrees + (value % 1000000) * 1.0E-4D / 60.0D;
/*     */       
/*  84 */       value = BcdUtil.readInteger(buf, 10);
/*  85 */       degrees = value / 10000000;
/*  86 */       double lon = degrees + (value % 10000000) * 1.0E-5D / 60.0D;
/*     */       
/*  88 */       int flags = buf.readUnsignedByte();
/*  89 */       position.setValid(BitUtil.check(flags, 0));
/*  90 */       position.setLatitude(BitUtil.check(flags, 1) ? lat : -lat);
/*  91 */       position.setLongitude(BitUtil.check(flags, 2) ? lon : -lon);
/*     */       
/*  93 */       position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*  94 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*  95 */       position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*  96 */       position.setCourse((buf.readUnsignedByte() * 2));
/*     */       
/*  98 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 102 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\VnetProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */