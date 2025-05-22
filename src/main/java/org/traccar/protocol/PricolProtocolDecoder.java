/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.NetworkMessage;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.helper.DateBuilder;
/*    */ import org.traccar.helper.UnitsConverter;
/*    */ import org.traccar.model.Position;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class PricolProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public PricolProtocolDecoder(Protocol protocol) {
/* 35 */     super(protocol);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 42 */     ByteBuf buf = (ByteBuf)msg;
/*    */     
/* 44 */     buf.readUnsignedByte();
/*    */     
/* 46 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { buf
/* 47 */           .readSlice(7).toString(StandardCharsets.US_ASCII) });
/* 48 */     if (deviceSession == null) {
/* 49 */       return null;
/*    */     }
/*    */     
/* 52 */     Position position = new Position(getProtocolName());
/* 53 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 55 */     position.set("eventType", Short.valueOf(buf.readUnsignedByte()));
/* 56 */     position.set("packetVersion", Short.valueOf(buf.readUnsignedByte()));
/* 57 */     position.set("status", Short.valueOf(buf.readUnsignedByte()));
/* 58 */     position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/* 59 */     position.set("gps", Short.valueOf(buf.readUnsignedByte()));
/*    */     
/* 61 */     position.setTime((new DateBuilder())
/* 62 */         .setDateReverse(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
/* 63 */         .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).getDate());
/*    */     
/* 65 */     position.setValid(true);
/*    */     
/* 67 */     double lat = (buf.getUnsignedShort(buf.readerIndex()) / 100);
/* 68 */     lat += (buf.readUnsignedShort() % 100 * 10000 + buf.readUnsignedShort()) / 600000.0D;
/* 69 */     position.setLatitude((buf.readUnsignedByte() == 83) ? -lat : lat);
/*    */     
/* 71 */     double lon = (buf.getUnsignedMedium(buf.readerIndex()) / 100);
/* 72 */     lon += (buf.readUnsignedMedium() % 100 * 10000 + buf.readUnsignedShort()) / 600000.0D;
/* 73 */     position.setLongitude((buf.readUnsignedByte() == 87) ? -lon : lon);
/*    */     
/* 75 */     position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*    */     
/* 77 */     position.set("input", Integer.valueOf(buf.readUnsignedShort()));
/* 78 */     position.set("output", Short.valueOf(buf.readUnsignedByte()));
/*    */     
/* 80 */     position.set("analogAlerts", Short.valueOf(buf.readUnsignedByte()));
/* 81 */     position.set("customAlertTypes", Integer.valueOf(buf.readUnsignedShort()));
/*    */     
/* 83 */     for (int i = 1; i <= 5; i++) {
/* 84 */       position.set("adc" + i, Integer.valueOf(buf.readUnsignedShort()));
/*    */     }
/*    */     
/* 87 */     position.set("odometer", Integer.valueOf(buf.readUnsignedMedium()));
/* 88 */     position.set("rpm", Integer.valueOf(buf.readUnsignedShort()));
/*    */     
/* 90 */     if (channel != null) {
/* 91 */       channel.writeAndFlush(new NetworkMessage(
/* 92 */             Unpooled.copiedBuffer("ACK", StandardCharsets.US_ASCII), remoteAddress));
/*    */     }
/*    */     
/* 95 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PricolProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */