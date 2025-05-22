/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.buffer.ByteBufUtil;
/*    */ import io.netty.buffer.Unpooled;
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import java.util.Date;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.helper.DataConverter;
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
/*    */ public class DingtekProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public DingtekProtocolDecoder(Protocol protocol) {
/* 34 */     super(protocol);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 41 */     String sentence = (String)msg;
/*    */     
/* 43 */     int type = Integer.parseInt(sentence.substring(6, 8), 16);
/*    */     
/* 45 */     if (type == 1 || type == 2 || type == 4) {
/*    */       
/* 47 */       ByteBuf buf = Unpooled.wrappedBuffer(DataConverter.parseHex(sentence));
/*    */       
/* 49 */       buf.readUnsignedByte();
/* 50 */       buf.readUnsignedByte();
/* 51 */       buf.readUnsignedByte();
/* 52 */       buf.readUnsignedByte();
/* 53 */       buf.readUnsignedByte();
/*    */       
/* 55 */       String imei = ByteBufUtil.hexDump(buf.slice(buf.writerIndex() - 9, 8)).substring(1);
/* 56 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 57 */       if (deviceSession == null) {
/* 58 */         return null;
/*    */       }
/*    */       
/* 61 */       Position position = new Position(getProtocolName());
/* 62 */       position.setDeviceId(deviceSession.getDeviceId());
/* 63 */       position.setTime(new Date());
/*    */       
/* 65 */       position.set("height", Integer.valueOf(buf.readUnsignedShort()));
/*    */       
/* 67 */       position.setValid((buf.readUnsignedByte() > 0));
/* 68 */       position.setLongitude(buf.readFloat());
/* 69 */       position.setLatitude(buf.readFloat());
/*    */       
/* 71 */       position.set("temp1", Short.valueOf(buf.readUnsignedByte()));
/* 72 */       position.set("status", Long.valueOf(buf.readUnsignedInt()));
/* 73 */       position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/* 74 */       position.set("rssi", Float.valueOf(buf.readFloat()));
/* 75 */       position.set("index", Integer.valueOf(buf.readUnsignedShort()));
/*    */       
/* 77 */       return position;
/*    */     } 
/*    */     
/* 80 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\DingtekProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */