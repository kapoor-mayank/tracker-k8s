/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Arrays;
/*     */ import java.util.Date;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BcdUtil;
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
/*     */ 
/*     */ 
/*     */ public class Vt200ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public Vt200ProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */   
/*     */   private static double decodeCoordinate(int value) {
/*  41 */     int degrees = value / 1000000;
/*  42 */     int minutes = value % 1000000;
/*  43 */     return degrees + minutes * 1.0E-4D / 60.0D;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Date decodeDate(ByteBuf buf) {
/*  49 */     DateBuilder dateBuilder = (new DateBuilder()).setDateReverse(BcdUtil.readInteger(buf, 2), BcdUtil.readInteger(buf, 2), BcdUtil.readInteger(buf, 2)).setTime(BcdUtil.readInteger(buf, 2), BcdUtil.readInteger(buf, 2), BcdUtil.readInteger(buf, 2));
/*  50 */     return dateBuilder.getDate();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  57 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  59 */     buf.skipBytes(1);
/*     */     
/*  61 */     String id = ByteBufUtil.hexDump(buf.readSlice(6));
/*  62 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/*  63 */     if (deviceSession == null) {
/*  64 */       return null;
/*     */     }
/*     */     
/*  67 */     int type = buf.readUnsignedShort();
/*  68 */     buf.readUnsignedShort();
/*     */     
/*  70 */     if (type == 8326 || type == 8324 || type == 8322) {
/*     */       
/*  72 */       Position position = new Position(getProtocolName());
/*  73 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  75 */       buf.readUnsignedByte();
/*  76 */       buf.readUnsignedShort();
/*     */       
/*  78 */       position.setTime(decodeDate(buf));
/*     */       
/*  80 */       position.setLatitude(decodeCoordinate(BcdUtil.readInteger(buf, 8)));
/*  81 */       position.setLongitude(decodeCoordinate(BcdUtil.readInteger(buf, 9)));
/*     */       
/*  83 */       int flags = buf.readUnsignedByte();
/*  84 */       position.setValid(BitUtil.check(flags, 0));
/*  85 */       if (!BitUtil.check(flags, 1)) {
/*  86 */         position.setLatitude(-position.getLatitude());
/*     */       }
/*  88 */       if (!BitUtil.check(flags, 2)) {
/*  89 */         position.setLongitude(-position.getLongitude());
/*     */       }
/*     */       
/*  92 */       position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*  93 */       position.setCourse((buf.readUnsignedByte() * 2));
/*     */       
/*  95 */       position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*  96 */       position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*  97 */       position.set("odometer", Long.valueOf(buf.readUnsignedInt() * 1000L));
/*  98 */       position.set("status", Long.valueOf(buf.readUnsignedInt()));
/*     */ 
/*     */ 
/*     */       
/* 102 */       return position;
/*     */     } 
/* 104 */     if (type == 12424) {
/*     */       
/* 106 */       Position position = new Position(getProtocolName());
/* 107 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/* 109 */       getLastLocation(position, null);
/*     */       
/* 111 */       buf.readUnsignedShort();
/* 112 */       buf.skipBytes(8);
/* 113 */       buf.skipBytes(8);
/*     */       
/* 115 */       position.set("tripStart", Long.valueOf(decodeDate(buf).getTime()));
/* 116 */       position.set("tripEnd", Long.valueOf(decodeDate(buf).getTime()));
/* 117 */       position.set("drivingTime", Integer.valueOf(buf.readUnsignedShort()));
/*     */       
/* 119 */       position.set("fuelConsumption", Long.valueOf(buf.readUnsignedInt()));
/* 120 */       position.set("tripOdometer", Long.valueOf(buf.readUnsignedInt()));
/*     */       
/* 122 */       position.set("maxSpeed", Double.valueOf(UnitsConverter.knotsFromKph(buf.readUnsignedByte())));
/* 123 */       position.set("maxRpm", Integer.valueOf(buf.readUnsignedShort()));
/* 124 */       position.set("maxTemp", Integer.valueOf(buf.readUnsignedByte() - 40));
/* 125 */       position.set("hardAccelerationCount", Short.valueOf(buf.readUnsignedByte()));
/* 126 */       position.set("hardBrakingCount", Short.valueOf(buf.readUnsignedByte()));
/*     */       
/* 128 */       for (String speedType : Arrays.<String>asList(new String[] { "over", "high", "normal", "low" })) {
/* 129 */         position.set(speedType + "SpeedTime", Integer.valueOf(buf.readUnsignedShort()));
/* 130 */         position.set(speedType + "SpeedDistance", Long.valueOf(buf.readUnsignedInt()));
/* 131 */         position.set(speedType + "SpeedFuel", Long.valueOf(buf.readUnsignedInt()));
/*     */       } 
/*     */       
/* 134 */       position.set("idleTime", Integer.valueOf(buf.readUnsignedShort()));
/* 135 */       position.set("idleFuel", Long.valueOf(buf.readUnsignedInt()));
/*     */       
/* 137 */       position.set("hardCorneringCount", Short.valueOf(buf.readUnsignedByte()));
/* 138 */       position.set("overspeedCount", Short.valueOf(buf.readUnsignedByte()));
/* 139 */       position.set("overheatCount", Integer.valueOf(buf.readUnsignedShort()));
/* 140 */       position.set("laneChangeCount", Short.valueOf(buf.readUnsignedByte()));
/* 141 */       position.set("emergencyRefueling", Short.valueOf(buf.readUnsignedByte()));
/*     */       
/* 143 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 147 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Vt200ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */