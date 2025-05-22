/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.helper.BcdUtil;
/*    */ import org.traccar.helper.BitUtil;
/*    */ import org.traccar.helper.DataConverter;
/*    */ import org.traccar.helper.DateBuilder;
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
/*    */ public class MotorProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public MotorProtocolDecoder(Protocol protocol) {
/* 35 */     super(protocol);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 42 */     String sentence = (String)msg;
/* 43 */     ByteBuf buf = Unpooled.wrappedBuffer(DataConverter.parseHex(sentence));
/*    */     
/* 45 */     String id = String.format("%08x", new Object[] { Long.valueOf(buf.readUnsignedIntLE()) });
/* 46 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/* 47 */     if (deviceSession == null) {
/* 48 */       return null;
/*    */     }
/*    */     
/* 51 */     Position position = new Position(getProtocolName());
/* 52 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 54 */     buf.skipBytes(2);
/*    */     
/* 56 */     position.set("status", Integer.valueOf(buf.readUnsignedShortLE()));
/*    */     
/* 58 */     buf.skipBytes(2);
/* 59 */     buf.readUnsignedMediumLE();
/*    */     
/* 61 */     int flags = buf.readUnsignedByte();
/* 62 */     position.setValid(BitUtil.check(flags, 7));
/* 63 */     if (BitUtil.check(flags, 0)) {
/* 64 */       position.set("alarm", "general");
/*    */     }
/*    */     
/* 67 */     position.setLatitude(BcdUtil.readInteger(buf, 2) + BcdUtil.readInteger(buf, 6) * 1.0E-4D / 60.0D);
/* 68 */     position.setLongitude(BcdUtil.readInteger(buf, 4) + BcdUtil.readInteger(buf, 6) * 1.0E-4D / 60.0D);
/* 69 */     position.setSpeed(BcdUtil.readInteger(buf, 4) * 0.1D);
/* 70 */     position.setCourse(BcdUtil.readInteger(buf, 4) * 0.1D);
/*    */     
/* 72 */     position.setTime((new DateBuilder())
/* 73 */         .setYear(BcdUtil.readInteger(buf, 2))
/* 74 */         .setMonth(BcdUtil.readInteger(buf, 2))
/* 75 */         .setDay(BcdUtil.readInteger(buf, 2))
/* 76 */         .setHour(BcdUtil.readInteger(buf, 2))
/* 77 */         .setMinute(BcdUtil.readInteger(buf, 2))
/* 78 */         .setSecond(BcdUtil.readInteger(buf, 2)).getDate());
/*    */     
/* 80 */     position.set("rssi", Integer.valueOf(BcdUtil.readInteger(buf, 2)));
/*    */     
/* 82 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MotorProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */