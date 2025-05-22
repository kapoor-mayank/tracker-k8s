/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BcdUtil;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.BufferUtil;
/*     */ import org.traccar.helper.Checksum;
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
/*     */ 
/*     */ public class NiotProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_RESPONSE = 33;
/*     */   public static final int MSG_POSITION_DATA = 128;
/*     */   
/*     */   public NiotProtocolDecoder(Protocol protocol) {
/*  40 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, int type, int checksum) {
/*  47 */     if (channel != null) {
/*  48 */       ByteBuf response = Unpooled.buffer();
/*  49 */       response.writeShort(22616);
/*  50 */       response.writeByte(33);
/*  51 */       response.writeShort(5);
/*  52 */       response.writeByte(checksum);
/*  53 */       response.writeByte(type);
/*  54 */       response.writeByte(0);
/*  55 */       response.writeByte(Checksum.xor(response.nioBuffer(2, response.writerIndex())));
/*  56 */       response.writeByte(13);
/*  57 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  65 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  67 */     buf.skipBytes(2);
/*  68 */     int type = buf.readUnsignedByte();
/*  69 */     buf.readUnsignedShort();
/*     */     
/*  71 */     String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(1);
/*     */     
/*  73 */     sendResponse(channel, remoteAddress, type, buf.getByte(buf.writerIndex() - 2));
/*     */     
/*  75 */     if (type == 128) {
/*     */       
/*  77 */       Position position = new Position(getProtocolName());
/*     */       
/*  79 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  80 */       if (deviceSession == null) {
/*  81 */         return null;
/*     */       }
/*  83 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  91 */       DateBuilder dateBuilder = (new DateBuilder()).setYear(BcdUtil.readInteger(buf, 2)).setMonth(BcdUtil.readInteger(buf, 2)).setDay(BcdUtil.readInteger(buf, 2)).setHour(BcdUtil.readInteger(buf, 2)).setMinute(BcdUtil.readInteger(buf, 2)).setSecond(BcdUtil.readInteger(buf, 2));
/*  92 */       position.setTime(dateBuilder.getDate());
/*     */       
/*  94 */       position.setLatitude(BufferUtil.readSignedMagnitudeInt(buf) / 1800000.0D);
/*  95 */       position.setLongitude(BufferUtil.readSignedMagnitudeInt(buf) / 1800000.0D);
/*  96 */       BcdUtil.readInteger(buf, 4);
/*  97 */       position.setCourse(BcdUtil.readInteger(buf, 4));
/*     */       
/*  99 */       int statusX = buf.readUnsignedByte();
/* 100 */       position.setValid(BitUtil.check(statusX, 7));
/* 101 */       switch (BitUtil.between(statusX, 3, 5)) {
/*     */         case 2:
/* 103 */           position.set("alarm", "powerCut");
/*     */           break;
/*     */         case 1:
/* 106 */           position.set("alarm", "lowPower");
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 112 */       position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*     */       
/* 114 */       int statusA = buf.readUnsignedByte();
/* 115 */       position.set("ignition", Boolean.valueOf(!BitUtil.check(statusA, 7)));
/* 116 */       if (!BitUtil.check(statusA, 6)) {
/* 117 */         position.set("alarm", "overspeed");
/*     */       }
/*     */       
/* 120 */       buf.readUnsignedByte();
/* 121 */       buf.readUnsignedByte();
/* 122 */       position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/* 123 */       position.set("battery", Double.valueOf(buf.readUnsignedByte() * 0.1D));
/* 124 */       position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.1D));
/* 125 */       buf.readUnsignedByte();
/* 126 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/* 127 */       buf.readUnsignedByte();
/* 128 */       buf.readUnsignedByte();
/* 129 */       buf.readUnsignedByte();
/*     */       
/* 131 */       while (buf.readableBytes() > 4) {
/* 132 */         int statusD, extendedLength = buf.readUnsignedShort();
/* 133 */         int extendedType = buf.readUnsignedShort();
/* 134 */         switch (extendedType) {
/*     */           case 1:
/* 136 */             position.set("iccid", buf
/* 137 */                 .readCharSequence(20, StandardCharsets.US_ASCII).toString());
/*     */             continue;
/*     */           case 2:
/* 140 */             statusD = buf.readUnsignedByte();
/* 141 */             position.set("alarm", BitUtil.check(statusD, 5) ? "removing" : null);
/* 142 */             position.set("alarm", BitUtil.check(statusD, 4) ? "tampering" : null);
/* 143 */             buf.readUnsignedByte();
/* 144 */             buf.readUnsignedByte();
/*     */             continue;
/*     */         } 
/* 147 */         buf.skipBytes(extendedLength - 2);
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 153 */       return position;
/*     */     } 
/*     */     
/* 156 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NiotProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */