/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.ByteBufUtil;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import java.util.Date;
/*    */ import java.util.LinkedList;
/*    */ import java.util.List;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.NetworkMessage;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.helper.BitUtil;
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
/*    */ public class Dsf22ProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public Dsf22ProtocolDecoder(Protocol protocol) {
/* 38 */     super(protocol);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 45 */     ByteBuf buf = (ByteBuf)msg;
/*    */     
/* 47 */     buf.skipBytes(2);
/*    */     
/* 49 */     String id = ByteBufUtil.hexDump(buf.readSlice(2));
/* 50 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 51 */     if (deviceSession == null) {
/* 52 */       return null;
/*    */     }
/*    */     
/* 55 */     List<Position> positions = new LinkedList<>();
/* 56 */     int count = buf.readUnsignedByte();
/*    */     
/* 58 */     for (int i = 0; i < count; i++) {
/*    */       
/* 60 */       Position position = new Position(getProtocolName());
/* 61 */       position.setDeviceId(deviceSession.getDeviceId());
/*    */       
/* 63 */       position.setValid(true);
/* 64 */       position.setLatitude(buf.readInt());
/* 65 */       position.setLongitude(buf.readInt());
/* 66 */       position.setTime(new Date(946684800000L + buf.readUnsignedInt()));
/* 67 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*    */       
/* 69 */       position.set("fuel", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*    */       
/* 71 */       int status = buf.readUnsignedByte();
/* 72 */       position.set("ignition", Boolean.valueOf(BitUtil.check(status, 0)));
/* 73 */       position.set("in1", Boolean.valueOf(BitUtil.check(status, 1)));
/* 74 */       position.set("out1", Boolean.valueOf(BitUtil.check(status, 4)));
/* 75 */       position.set("alarm", BitUtil.check(status, 6) ? "jamming" : null);
/* 76 */       position.set("status", Integer.valueOf(status));
/*    */       
/* 78 */       positions.add(position);
/*    */     } 
/*    */ 
/*    */     
/* 82 */     if (channel != null) {
/* 83 */       byte[] response = { 1 };
/* 84 */       channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(response), remoteAddress));
/*    */     } 
/*    */     
/* 87 */     return positions;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Dsf22ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */