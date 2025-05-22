/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BcdUtil;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class GatorProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_HEARTBEAT = 33;
/*     */   public static final int MSG_POSITION_DATA = 128;
/*     */   public static final int MSG_ROLLCALL_RESPONSE = 129;
/*     */   public static final int MSG_ALARM_DATA = 130;
/*     */   public static final int MSG_TERMINAL_STATUS = 131;
/*     */   public static final int MSG_MESSAGE = 132;
/*     */   public static final int MSG_TERMINAL_ANSWER = 133;
/*     */   public static final int MSG_BLIND_AREA = 142;
/*     */   public static final int MSG_PICTURE_FRAME = 84;
/*     */   public static final int MSG_CAMERA_RESPONSE = 86;
/*     */   public static final int MSG_PICTURE_DATA = 87;
/*     */   
/*     */   public GatorProtocolDecoder(Protocol protocol) {
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String decodeId(int b1, int b2, int b3, int b4) {
/*  53 */     int d1 = 30 + (b1 >> 7 << 3) + (b2 >> 7 << 2) + (b3 >> 7 << 1) + (b4 >> 7);
/*  54 */     int d2 = b1 & 0x7F;
/*  55 */     int d3 = b2 & 0x7F;
/*  56 */     int d4 = b3 & 0x7F;
/*  57 */     int d5 = b4 & 0x7F;
/*     */     
/*  59 */     return String.format("%02d%02d%02d%02d%02d", new Object[] { Integer.valueOf(d1), Integer.valueOf(d2), Integer.valueOf(d3), Integer.valueOf(d4), Integer.valueOf(d5) });
/*     */   }
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, int type, int checksum) {
/*  63 */     if (channel != null) {
/*  64 */       ByteBuf response = Unpooled.buffer();
/*  65 */       response.writeShort(9252);
/*  66 */       response.writeByte(33);
/*  67 */       response.writeShort(5);
/*  68 */       response.writeByte(checksum);
/*  69 */       response.writeByte(type);
/*  70 */       response.writeByte(0);
/*  71 */       response.writeByte(Checksum.xor(response.nioBuffer(2, response.writerIndex())));
/*  72 */       response.writeByte(13);
/*  73 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  81 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  83 */     buf.skipBytes(2);
/*  84 */     int type = buf.readUnsignedByte();
/*  85 */     buf.readUnsignedShort();
/*     */     
/*  87 */     String id = decodeId(buf
/*  88 */         .readUnsignedByte(), buf.readUnsignedByte(), buf
/*  89 */         .readUnsignedByte(), buf.readUnsignedByte());
/*     */     
/*  91 */     sendResponse(channel, remoteAddress, type, buf.getByte(buf.writerIndex() - 2));
/*     */     
/*  93 */     if (type == 128 || type == 129 || type == 130 || type == 142) {
/*     */ 
/*     */       
/*  96 */       Position position = new Position(getProtocolName());
/*     */       
/*  98 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { "1" + id, id });
/*  99 */       if (deviceSession == null) {
/* 100 */         return null;
/*     */       }
/* 102 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 110 */       DateBuilder dateBuilder = (new DateBuilder()).setYear(BcdUtil.readInteger(buf, 2)).setMonth(BcdUtil.readInteger(buf, 2)).setDay(BcdUtil.readInteger(buf, 2)).setHour(BcdUtil.readInteger(buf, 2)).setMinute(BcdUtil.readInteger(buf, 2)).setSecond(BcdUtil.readInteger(buf, 2));
/* 111 */       position.setTime(dateBuilder.getDate());
/*     */       
/* 113 */       position.setLatitude(BcdUtil.readCoordinate(buf));
/* 114 */       position.setLongitude(BcdUtil.readCoordinate(buf));
/* 115 */       position.setSpeed(UnitsConverter.knotsFromKph(BcdUtil.readInteger(buf, 4)));
/* 116 */       position.setCourse(BcdUtil.readInteger(buf, 4));
/*     */       
/* 118 */       int flags = buf.readUnsignedByte();
/* 119 */       position.setValid(((flags & 0x80) != 0));
/* 120 */       position.set("sat", Integer.valueOf(flags & 0xF));
/*     */       
/* 122 */       position.set("status", Short.valueOf(buf.readUnsignedByte()));
/* 123 */       position.set("key", Short.valueOf(buf.readUnsignedByte()));
/*     */       
/* 125 */       position.set("adc1", Double.valueOf(buf.readUnsignedByte() + buf.readUnsignedByte() * 0.01D));
/* 126 */       position.set("adc2", Double.valueOf(buf.readUnsignedByte() + buf.readUnsignedByte() * 0.01D));
/*     */       
/* 128 */       position.set("odometer", Long.valueOf(buf.readUnsignedInt()));
/*     */       
/* 130 */       return position;
/*     */     } 
/*     */     
/* 133 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GatorProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */