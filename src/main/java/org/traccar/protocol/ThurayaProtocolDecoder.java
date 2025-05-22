/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ public class ThurayaProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_EVENT = 20737;
/*     */   public static final int MSG_PERIODIC_REPORT = 28929;
/*     */   public static final int MSG_SETTING_RESPONSE = 33045;
/*     */   public static final int MSG_ACK = 39169;
/*     */   
/*     */   public ThurayaProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static int checksum(ByteBuffer buf) {
/*  47 */     int crc = 0;
/*  48 */     while (buf.hasRemaining()) {
/*  49 */       crc += buf.get();
/*     */     }
/*  51 */     crc ^= 0xFFFFFFFF;
/*  52 */     crc++;
/*  53 */     return crc;
/*     */   }
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, long id, int type) {
/*  57 */     if (channel != null) {
/*  58 */       ByteBuf response = Unpooled.buffer();
/*  59 */       response.writeCharSequence("#T", StandardCharsets.US_ASCII);
/*  60 */       response.writeShort(15);
/*  61 */       response.writeShort(39169);
/*  62 */       response.writeInt((int)id);
/*  63 */       response.writeShort(type);
/*  64 */       response.writeShort(1);
/*  65 */       response.writeShort(checksum(response.nioBuffer()));
/*  66 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeLocation(ByteBuf buf, Position position) {
/*  72 */     position.setValid(true);
/*     */     
/*  74 */     DateBuilder dateBuilder = new DateBuilder();
/*     */     
/*  76 */     int date = buf.readInt();
/*  77 */     dateBuilder.setDay(date % 100);
/*  78 */     date /= 100;
/*  79 */     dateBuilder.setMonth(date % 100);
/*  80 */     date /= 100;
/*  81 */     dateBuilder.setYear(date);
/*     */     
/*  83 */     int time = buf.readInt();
/*  84 */     dateBuilder.setSecond(time % 100);
/*  85 */     time /= 100;
/*  86 */     dateBuilder.setMinute(time % 100);
/*  87 */     time /= 100;
/*  88 */     dateBuilder.setHour(time);
/*     */     
/*  90 */     position.setTime(dateBuilder.getDate());
/*     */     
/*  92 */     position.setLongitude(buf.readInt() / 1000000.0D);
/*  93 */     position.setLatitude(buf.readInt() / 1000000.0D);
/*     */     
/*  95 */     int data = buf.readUnsignedShort();
/*     */     
/*  97 */     int ignition = BitUtil.from(data, 12);
/*  98 */     if (ignition == 1) {
/*  99 */       position.set("ignition", Boolean.valueOf(true));
/* 100 */     } else if (ignition == 2) {
/* 101 */       position.set("ignition", Boolean.valueOf(false));
/*     */     } 
/*     */     
/* 104 */     position.setCourse(BitUtil.to(data, 12));
/* 105 */     position.setSpeed(buf.readShort());
/*     */     
/* 107 */     position.set("rpm", Short.valueOf(buf.readShort()));
/*     */     
/* 109 */     position.set("data", readString(buf));
/*     */   }
/*     */   
/*     */   private String decodeAlarm(int event) {
/* 113 */     switch (event) {
/*     */       case 10:
/* 115 */         return "vibration";
/*     */       case 11:
/* 117 */         return "overspeed";
/*     */       case 12:
/* 119 */         return "powerCut";
/*     */       case 13:
/* 121 */         return "lowBattery";
/*     */       case 18:
/* 123 */         return "gpsAntennaCut";
/*     */       case 20:
/* 125 */         return "hardAcceleration";
/*     */       case 21:
/* 127 */         return "hardBraking";
/*     */     } 
/* 129 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private String readString(ByteBuf buf) {
/* 134 */     int endIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)0);
/* 135 */     CharSequence value = buf.readCharSequence(endIndex - buf.readerIndex(), StandardCharsets.US_ASCII);
/* 136 */     buf.readUnsignedByte();
/* 137 */     return value.toString();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 144 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 146 */     buf.skipBytes(2);
/* 147 */     buf.readUnsignedShort();
/* 148 */     int type = buf.readUnsignedShort();
/* 149 */     long id = buf.readUnsignedInt();
/*     */     
/* 151 */     sendResponse(channel, remoteAddress, id, type);
/*     */     
/* 153 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(id) });
/* 154 */     if (deviceSession == null) {
/* 155 */       return null;
/*     */     }
/*     */     
/* 158 */     if (type == 20737) {
/*     */       
/* 160 */       Position position = new Position(getProtocolName());
/* 161 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 163 */       decodeLocation(buf, position);
/*     */       
/* 165 */       int event = buf.readUnsignedByte();
/* 166 */       position.set("alarm", decodeAlarm(event));
/* 167 */       position.set("event", Integer.valueOf(event));
/* 168 */       position.set("eventData", readString(buf));
/*     */       
/* 170 */       return position;
/*     */     } 
/* 172 */     if (type == 28929) {
/*     */       
/* 174 */       List<Position> positions = new LinkedList<>();
/*     */       
/* 176 */       int count = buf.readUnsignedByte();
/* 177 */       for (int i = 0; i < count; i++) {
/*     */         
/* 179 */         Position position = new Position(getProtocolName());
/* 180 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 182 */         decodeLocation(buf, position);
/*     */         
/* 184 */         positions.add(position);
/*     */       } 
/*     */ 
/*     */       
/* 188 */       return positions;
/*     */     } 
/*     */ 
/*     */     
/* 192 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ThurayaProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */