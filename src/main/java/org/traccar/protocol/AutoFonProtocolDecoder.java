/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
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
/*     */ public class AutoFonProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_LOGIN = 16;
/*     */   public static final int MSG_LOCATION = 17;
/*     */   public static final int MSG_HISTORY = 18;
/*     */   public static final int MSG_45_LOGIN = 65;
/*     */   public static final int MSG_45_LOCATION = 2;
/*     */   
/*     */   public AutoFonProtocolDecoder(Protocol protocol) {
/*  41 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static double convertCoordinate(int raw) {
/*  52 */     int degrees = raw / 1000000;
/*  53 */     double minutes = (raw % 1000000) / 10000.0D;
/*  54 */     return degrees + minutes / 60.0D;
/*     */   }
/*     */   
/*     */   private static double convertCoordinate(short degrees, int minutes) {
/*  58 */     double value = degrees + BitUtil.from(minutes, 4) / 600000.0D;
/*  59 */     if (BitUtil.check(minutes, 0)) {
/*  60 */       return value;
/*     */     }
/*  62 */     return -value;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodePosition(DeviceSession deviceSession, ByteBuf buf, boolean history) {
/*  68 */     Position position = new Position(getProtocolName());
/*  69 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  71 */     if (!history) {
/*  72 */       buf.readUnsignedByte();
/*  73 */       buf.skipBytes(8);
/*     */     } 
/*  75 */     position.set("status", Short.valueOf(buf.readUnsignedByte()));
/*  76 */     if (!history) {
/*  77 */       buf.readUnsignedShort();
/*     */     }
/*  79 */     position.set("battery", Short.valueOf(buf.readUnsignedByte()));
/*  80 */     buf.skipBytes(6);
/*     */     
/*  82 */     if (!history) {
/*  83 */       for (int i = 0; i < 2; i++) {
/*  84 */         buf.skipBytes(5);
/*  85 */         buf.readUnsignedShort();
/*  86 */         buf.skipBytes(5);
/*     */       } 
/*     */     }
/*     */     
/*  90 */     position.set("temp1", Byte.valueOf(buf.readByte()));
/*     */     
/*  92 */     int rssi = buf.readUnsignedByte();
/*  93 */     CellTower cellTower = CellTower.from(buf
/*  94 */         .readUnsignedShort(), buf.readUnsignedShort(), buf
/*  95 */         .readUnsignedShort(), buf.readUnsignedShort(), rssi);
/*  96 */     position.setNetwork(new Network(cellTower));
/*     */     
/*  98 */     int valid = buf.readUnsignedByte();
/*  99 */     position.setValid(((valid & 0xC0) != 0));
/* 100 */     position.set("sat", Integer.valueOf(valid & 0x3F));
/*     */ 
/*     */ 
/*     */     
/* 104 */     DateBuilder dateBuilder = (new DateBuilder()).setDateReverse(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte()).setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
/* 105 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 107 */     position.setLatitude(convertCoordinate(buf.readInt()));
/* 108 */     position.setLongitude(convertCoordinate(buf.readInt()));
/* 109 */     position.setAltitude(buf.readShort());
/* 110 */     position.setSpeed(buf.readUnsignedByte());
/* 111 */     position.setCourse(buf.readUnsignedByte() * 2.0D);
/*     */     
/* 113 */     position.set("hdop", Integer.valueOf(buf.readUnsignedShort()));
/*     */     
/* 115 */     buf.readUnsignedShort();
/* 116 */     buf.readUnsignedByte();
/* 117 */     return position;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 124 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 126 */     int type = buf.readUnsignedByte();
/*     */     
/* 128 */     if (type == 16 || type == 65) {
/*     */       
/* 130 */       if (type == 16) {
/* 131 */         buf.readUnsignedByte();
/* 132 */         buf.readUnsignedByte();
/*     */       } 
/*     */       
/* 135 */       String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(1);
/* 136 */       DeviceSession deviceSession1 = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*     */       
/* 138 */       if (deviceSession1 != null && channel != null) {
/* 139 */         ByteBuf response = Unpooled.buffer();
/* 140 */         response.writeBytes("resp_crc=".getBytes(StandardCharsets.US_ASCII));
/* 141 */         response.writeByte(buf.getByte(buf.writerIndex() - 1));
/* 142 */         channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */       } 
/*     */       
/* 145 */       return null;
/*     */     } 
/*     */ 
/*     */     
/* 149 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 150 */     if (deviceSession == null) {
/* 151 */       return null;
/*     */     }
/*     */     
/* 154 */     if (type == 17)
/*     */     {
/* 156 */       return decodePosition(deviceSession, buf, false);
/*     */     }
/* 158 */     if (type == 18) {
/*     */       
/* 160 */       int count = buf.readUnsignedByte() & 0xF;
/* 161 */       buf.readUnsignedShort();
/* 162 */       List<Position> positions = new LinkedList<>();
/*     */       
/* 164 */       for (int i = 0; i < count; i++) {
/* 165 */         positions.add(decodePosition(deviceSession, buf, true));
/*     */       }
/*     */       
/* 168 */       return positions;
/*     */     } 
/* 170 */     if (type == 2) {
/*     */       
/* 172 */       Position position = new Position(getProtocolName());
/* 173 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 175 */       short status = buf.readUnsignedByte();
/* 176 */       if (BitUtil.check(status, 7)) {
/* 177 */         position.set("alarm", "general");
/*     */       }
/* 179 */       position.set("battery", Integer.valueOf(BitUtil.to(status, 7)));
/*     */       
/* 181 */       buf.skipBytes(2);
/*     */       
/* 183 */       position.set("temp1", Byte.valueOf(buf.readByte()));
/*     */       
/* 185 */       buf.skipBytes(2);
/* 186 */       buf.readByte();
/* 187 */       buf.readByte();
/*     */       
/* 189 */       buf.skipBytes(6);
/*     */       
/* 191 */       int valid = buf.readUnsignedByte();
/* 192 */       position.setValid((BitUtil.from(valid, 6) != 0));
/* 193 */       position.set("sat", Integer.valueOf(BitUtil.from(valid, 6)));
/*     */       
/* 195 */       int time = buf.readUnsignedMedium();
/* 196 */       int date = buf.readUnsignedMedium();
/*     */ 
/*     */ 
/*     */       
/* 200 */       DateBuilder dateBuilder = (new DateBuilder()).setTime(time / 10000, time / 100 % 100, time % 100).setDateReverse(date / 10000, date / 100 % 100, date % 100);
/* 201 */       position.setTime(dateBuilder.getDate());
/*     */       
/* 203 */       position.setLatitude(convertCoordinate(buf.readUnsignedByte(), buf.readUnsignedMedium()));
/* 204 */       position.setLongitude(convertCoordinate(buf.readUnsignedByte(), buf.readUnsignedMedium()));
/* 205 */       position.setSpeed(buf.readUnsignedByte());
/* 206 */       position.setCourse(buf.readUnsignedShort());
/*     */       
/* 208 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 212 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AutoFonProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */