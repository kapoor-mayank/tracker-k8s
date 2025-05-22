///*     */ package org.traccar.protocol;
///*     */
///*     */ import io.netty.buffer.ByteBuf;
///*     */ import io.netty.buffer.Unpooled;
///*     */ import io.netty.channel.Channel;
///*     */ import java.net.SocketAddress;
///*     */ import java.nio.charset.StandardCharsets;
///*     */ import java.util.Date;
///*     */ import org.traccar.BaseProtocolDecoder;
///*     */ import org.traccar.DeviceSession;
///*     */ import org.traccar.NetworkMessage;
///*     */ import org.traccar.Protocol;
///*     */ import org.traccar.model.Position;
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */ public class RetranslatorProtocolDecoder
///*     */   extends BaseProtocolDecoder
///*     */ {
///*     */   public RetranslatorProtocolDecoder(Protocol protocol) {
///*  34 */     super(protocol);
///*     */   }
///*     */
///*     */
///*     */
///*     */
///*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
///*  41 */     if (channel != null) {
///*  42 */       channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(new byte[] { 17 }, ), remoteAddress));
///*     */     }
///*     */
///*  45 */     ByteBuf buf = (ByteBuf)msg;
///*     */
///*  47 */     buf.readUnsignedInt();
///*     */
///*  49 */     int idLength = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)0) - buf.readerIndex();
///*  50 */     String id = buf.readBytes(idLength).toString(StandardCharsets.US_ASCII);
///*  51 */     buf.readByte();
///*  52 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
///*  53 */     if (deviceSession == null) {
///*  54 */       return null;
///*     */     }
///*     */
///*  57 */     Position position = new Position(getProtocolName());
///*  58 */     position.setDeviceId(deviceSession.getDeviceId());
///*  59 */     position.setTime(new Date(buf.readUnsignedInt() * 1000L));
///*     */
///*  61 */     buf.readUnsignedInt();
///*     */
///*  63 */     while (buf.isReadable()) {
///*     */
///*  65 */       buf.readUnsignedShort();
///*  66 */       int blockEnd = buf.readInt() + buf.readerIndex();
///*  67 */       buf.readUnsignedByte();
///*  68 */       int dataType = buf.readUnsignedByte();
///*     */
///*  70 */       int nameLength = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)0) - buf.readerIndex();
///*  71 */       String name = buf.readBytes(nameLength).toString(StandardCharsets.US_ASCII);
///*  72 */       buf.readByte();
///*     */
///*  74 */       if (name.equals("posinfo")) {
///*  75 */         position.setValid(true);
///*  76 */         position.setLongitude(buf.readDoubleLE());
///*  77 */         position.setLatitude(buf.readDoubleLE());
///*  78 */         position.setAltitude(buf.readDoubleLE());
///*  79 */         position.setSpeed(buf.readShort());
///*  80 */         position.setCourse(buf.readShort());
///*  81 */         position.set("sat", Byte.valueOf(buf.readByte()));
///*     */       } else {
///*  83 */         int len; switch (dataType) {
///*     */           case 1:
///*  85 */             len = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)0) - buf.readerIndex();
///*  86 */             position.set(name, buf.readBytes(len).toString(StandardCharsets.US_ASCII));
///*  87 */             buf.readByte();
///*     */             break;
///*     */           case 3:
///*  90 */             position.set(name, Integer.valueOf(buf.readInt()));
///*     */             break;
///*     */           case 4:
///*  93 */             position.set(name, Double.valueOf(buf.readDoubleLE()));
///*     */             break;
///*     */           case 5:
///*  96 */             position.set(name, Long.valueOf(buf.readLong()));
///*     */             break;
///*     */         }
///*     */
///*     */
///*     */
///*     */       }
///* 103 */       buf.readerIndex(blockEnd);
///*     */     }
///*     */
///*     */
///* 107 */     if (position.getLatitude() == 0.0D && position.getLongitude() == 0.0D) {
///* 108 */       getLastLocation(position, position.getDeviceTime());
///*     */     }
///*     */
///* 111 */     return position;
///*     */   }
///*     */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\RetranslatorProtocolDecoder.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */