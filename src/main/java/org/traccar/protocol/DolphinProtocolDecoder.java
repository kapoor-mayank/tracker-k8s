///*     */ package org.traccar.protocol;
///*     */
///*     */ import com.google.protobuf.InvalidProtocolBufferException;
///*     */ import io.netty.buffer.ByteBuf;
///*     */ import io.netty.buffer.ByteBufUtil;
///*     */ import io.netty.buffer.Unpooled;
///*     */ import io.netty.channel.Channel;
///*     */ import java.net.SocketAddress;
///*     */ import java.util.Date;
///*     */ import java.util.LinkedList;
///*     */ import java.util.List;
///*     */ import org.traccar.BaseProtocolDecoder;
///*     */ import org.traccar.DeviceSession;
///*     */ import org.traccar.NetworkMessage;
///*     */ import org.traccar.Protocol;
///*     */ import org.traccar.helper.UnitsConverter;
///*     */ import org.traccar.model.Position;
///*     */ import org.traccar.protobuf.dolphin.Messages.DolphinMessages;
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
///*     */ public class DolphinProtocolDecoder
///*     */   extends BaseProtocolDecoder
///*     */ {
///*     */   public DolphinProtocolDecoder(Protocol protocol) {
///*  39 */     super(protocol);
///*     */   }
///*     */
///*     */
///*     */
///*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, int index, DolphinMessages.DataPackResponseCode responseCode) {
///*  45 */     if (channel != null) {
///*     */
///*     */
///*     */
///*  49 */       byte[] responseData = DolphinMessages.DataPackResponse.newBuilder().setResponse(responseCode).build().toByteArray();
///*     */
///*  51 */       ByteBuf response = Unpooled.buffer();
///*  52 */       response.writeShort(43947);
///*  53 */       response.writeIntLE(index);
///*  54 */       response.writeShort(0);
///*  55 */       response.writeShortLE(DolphinMessages.MessageType.DataPack_Response.getNumber());
///*  56 */       response.writeIntLE(responseData.length);
///*  57 */       response.writeIntLE(0);
///*  58 */       response.writeBytes(responseData);
///*     */
///*  60 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
///*     */     }
///*     */   }
///*     */
///*     */
///*     */
///*     */
///*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
///*  68 */     ByteBuf buf = (ByteBuf)msg;
///*     */
///*  70 */     buf.readUnsignedShort();
///*  71 */     int index = (int)buf.readUnsignedIntLE();
///*  72 */     buf.readUnsignedShort();
///*  73 */     buf.readUnsignedShort();
///*  74 */     int type = buf.readUnsignedShortLE();
///*     */
///*  76 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(buf.readLongLE()) });
///*  77 */     if (deviceSession == null) {
///*  78 */       return null;
///*     */     }
///*     */
///*  81 */     int length = (int)buf.readUnsignedIntLE();
///*  82 */     buf.readUnsignedInt();
///*     */
///*  84 */     if (type == DolphinMessages.MessageType.DataPack_Request.getNumber()) {
///*     */
///*  86 */       DolphinMessages.DataPackRequest message = null;
///*     */
///*     */       try {
///*  89 */         message = DolphinMessages.DataPackRequest.parseFrom(
///*  90 */             ByteBufUtil.getBytes(buf, buf.readerIndex(), length, false));
///*  91 */       } catch (InvalidProtocolBufferException e) {
///*  92 */         sendResponse(channel, remoteAddress, index, DolphinMessages.DataPackResponseCode.DataPack_Decode_Error);
///*  93 */         return null;
///*     */       }
///*     */
///*  96 */       sendResponse(channel, remoteAddress, index, DolphinMessages.DataPackResponseCode.DataPack_OK);
///*     */
///*  98 */       List<Position> positions = new LinkedList<>();
///*     */
///* 100 */       for (int i = 0; i < message.getPointsCount(); i++) {
///*     */
///* 102 */         Position position = new Position(getProtocolName());
///* 103 */         position.setDeviceId(deviceSession.getDeviceId());
///*     */
///* 105 */         DolphinMessages.DataPoint point = message.getPoints(i);
///*     */
///* 107 */         position.setValid(true);
///* 108 */         position.setTime(new Date(point.getTimestamp() * 1000L));
///* 109 */         position.setLatitude(point.getLatitude());
///* 110 */         position.setLongitude(point.getLongitude());
///* 111 */         position.setAltitude(point.getAltitude());
///* 112 */         position.setSpeed(UnitsConverter.knotsFromKph(point.getSpeed()));
///* 113 */         position.setCourse(point.getBearing());
///*     */
///* 115 */         position.set("sat", Integer.valueOf(point.getSatellites()));
///* 116 */         position.set("hdop", Integer.valueOf(point.getHDOP()));
///*     */
///* 118 */         for (int j = 0; j < point.getIOListIDCount(); j++) {
///* 119 */           position.set("io" + point.getIOListIDValue(j), Long.valueOf(point.getIOListValue(j)));
///*     */         }
///*     */
///* 122 */         positions.add(position);
///*     */       }
///*     */
///*     */
///* 126 */       return positions;
///*     */     }
///*     */
///*     */
///* 130 */     return null;
///*     */   }
///*     */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\DolphinProtocolDecoder.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */