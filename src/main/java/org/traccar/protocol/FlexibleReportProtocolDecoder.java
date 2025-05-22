/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Date;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
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
/*     */ 
/*     */ public class FlexibleReportProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_GENERAL = 0;
/*     */   
/*     */   public FlexibleReportProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, int index) {
/*  43 */     if (channel != null) {
/*  44 */       ByteBuf response = Unpooled.buffer();
/*  45 */       response.writeByte(126);
/*  46 */       response.writeShort(2);
/*  47 */       response.writeByte(224);
/*  48 */       response.writeByte(BitUtil.check(index, 0) ? 79 : 15);
/*  49 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */   
/*     */   private Date decodeTime(ByteBuf buf) {
/*  54 */     int timestamp = buf.readInt();
/*  55 */     return (new DateBuilder())
/*  56 */       .setSecond(timestamp % 60)
/*  57 */       .setMinute(timestamp / 60 % 60)
/*  58 */       .setHour(timestamp / 3600 % 24)
/*  59 */       .setDay(1 + timestamp / 86400 % 31)
/*  60 */       .setMonth(1 + timestamp / 2678400 % 12)
/*  61 */       .setYear(2000 + timestamp / 32140800)
/*  62 */       .getDate();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  69 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  71 */     buf.readUnsignedByte();
/*  72 */     int flags = buf.readUnsignedByte();
/*     */     
/*  74 */     String imei = ByteBufUtil.hexDump(buf.readSlice(8)).substring(1);
/*  75 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  76 */     if (deviceSession == null) {
/*  77 */       return null;
/*     */     }
/*     */     
/*  80 */     int index = buf.readUnsignedShort();
/*     */     
/*  82 */     if (BitUtil.to(flags, 2) > 0) {
/*  83 */       sendResponse(channel, remoteAddress, index);
/*     */     }
/*     */     
/*  86 */     Date time = decodeTime(buf);
/*  87 */     int event = buf.readUnsignedByte();
/*     */     
/*  89 */     buf.readUnsignedByte();
/*     */     
/*  91 */     int type = buf.readUnsignedByte();
/*     */     
/*  93 */     if (type == 0) {
/*     */       
/*  95 */       Position position = new Position(getProtocolName());
/*  96 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  98 */       position.setDeviceTime(time);
/*     */       
/* 100 */       position.set("event", Integer.valueOf(event));
/*     */       
/* 102 */       buf.readUnsignedByte();
/* 103 */       long mask = buf.readUnsignedInt();
/*     */       
/* 105 */       if (BitUtil.check(mask, 0)) {
/* 106 */         buf.readUnsignedByte();
/*     */       }
/* 108 */       if (BitUtil.check(mask, 1)) {
/* 109 */         position.setFixTime(decodeTime(buf));
/*     */       }
/* 111 */       if (BitUtil.check(mask, 2)) {
/* 112 */         position.setValid(true);
/* 113 */         position.setLatitude(buf.readUnsignedInt() / 1000000.0D - 90.0D);
/* 114 */         position.setLongitude(buf.readUnsignedInt() / 1000000.0D - 180.0D);
/*     */       } 
/* 116 */       if (BitUtil.check(mask, 3)) {
/* 117 */         position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/* 118 */         position.setCourse(buf.readUnsignedShort());
/*     */       } 
/* 120 */       if (BitUtil.check(mask, 4)) {
/* 121 */         position.setAltitude(buf.readShort());
/*     */       }
/* 123 */       if (BitUtil.check(mask, 5)) {
/* 124 */         buf.readUnsignedShort();
/*     */       }
/* 126 */       if (BitUtil.check(mask, 6)) {
/* 127 */         position.set("power", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*     */       }
/* 129 */       if (BitUtil.check(mask, 7)) {
/* 130 */         position.set("battery", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*     */       }
/* 132 */       if (BitUtil.check(mask, 8)) {
/* 133 */         position.set("auxPower", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*     */       }
/* 135 */       if (BitUtil.check(mask, 9)) {
/* 136 */         position.set("solarPower", Double.valueOf(buf.readUnsignedShort() * 0.001D));
/*     */       }
/* 138 */       if (BitUtil.check(mask, 10)) {
/* 139 */         int cellService = buf.readUnsignedByte();
/* 140 */         position.set("roaming", Boolean.valueOf(BitUtil.check(cellService, 7)));
/* 141 */         position.set("service", Integer.valueOf(BitUtil.to(cellService, 7)));
/* 142 */         buf.skipBytes(4);
/*     */       } 
/* 144 */       if (BitUtil.check(mask, 11)) {
/* 145 */         buf.readUnsignedByte();
/*     */       }
/* 147 */       if (BitUtil.check(mask, 12)) {
/* 148 */         int inputs = buf.readUnsignedByte();
/* 149 */         position.set("ignition", Boolean.valueOf(BitUtil.check(inputs, 0)));
/* 150 */         position.set("io1", Integer.valueOf(inputs));
/*     */       } 
/* 152 */       if (BitUtil.check(mask, 13)) {
/* 153 */         position.set("io2", Short.valueOf(buf.readUnsignedByte()));
/*     */       }
/* 155 */       if (BitUtil.check(mask, 14)) {
/* 156 */         position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 1000L));
/*     */       }
/* 158 */       if (BitUtil.check(mask, 15)) {
/* 159 */         position.set("temp1", Double.valueOf(buf.readUnsignedShort() * 0.01D));
/*     */       }
/*     */       
/* 162 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 166 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FlexibleReportProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */