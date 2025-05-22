/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.Checksum;
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
/*     */ public class SatsolProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public SatsolProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  45 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  47 */     buf.readUnsignedShortLE();
/*  48 */     buf.readUnsignedShortLE();
/*  49 */     long id = buf.readUnsignedIntLE();
/*  50 */     buf.readUnsignedShortLE();
/*     */     
/*  52 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(id) });
/*  53 */     if (deviceSession == null) {
/*  54 */       return null;
/*     */     }
/*     */     
/*  57 */     List<Position> positions = new LinkedList<>();
/*     */     
/*  59 */     while (buf.isReadable()) {
/*     */       
/*  61 */       buf.readUnsignedShortLE();
/*  62 */       buf.readUnsignedShortLE();
/*  63 */       buf.readUnsignedShortLE();
/*  64 */       int length = buf.readUnsignedShortLE();
/*     */       
/*  66 */       Position position = new Position(getProtocolName());
/*  67 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  69 */       position.setTime(new Date(buf.readUnsignedIntLE() * 1000L));
/*  70 */       position.setLatitude(buf.readUnsignedIntLE() * 1.0E-6D);
/*  71 */       position.setLongitude(buf.readUnsignedIntLE() * 1.0E-6D);
/*  72 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShortLE() * 0.01D));
/*  73 */       position.setAltitude(buf.readShortLE());
/*  74 */       position.setCourse(buf.readUnsignedShortLE());
/*  75 */       position.setValid((buf.readUnsignedByte() > 0));
/*     */       
/*  77 */       position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*  78 */       position.set("event", Short.valueOf(buf.readUnsignedByte()));
/*     */       
/*  80 */       if (BitUtil.check(buf.readUnsignedByte(), 0)) {
/*  81 */         position.set("archive", Boolean.valueOf(true));
/*     */       }
/*     */       
/*  84 */       positions.add(position);
/*     */       
/*  86 */       buf.skipBytes(length);
/*     */     } 
/*     */ 
/*     */     
/*  90 */     if (channel != null) {
/*  91 */       ByteBuf response = Unpooled.buffer();
/*  92 */       response.writeShortLE(0);
/*  93 */       response.writeShortLE(19647);
/*  94 */       response.writeIntLE((int)id);
/*  95 */       response.writeShortLE(0);
/*  96 */       response.setShortLE(0, Checksum.crc16(Checksum.CRC16_CCITT_FALSE, response
/*  97 */             .nioBuffer(2, response.readableBytes() - 2)));
/*  98 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */     
/* 101 */     return positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SatsolProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */