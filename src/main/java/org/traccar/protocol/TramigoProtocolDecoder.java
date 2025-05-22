/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Calendar;
/*     */ import java.util.Date;
/*     */ import java.util.Locale;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DateUtil;
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
/*     */ public class TramigoProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_COMPACT = 256;
/*     */   public static final int MSG_FULL = 254;
/*     */   
/*     */   public TramigoProtocolDecoder(Protocol protocol) {
/*  42 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  48 */   private static final String[] DIRECTIONS = new String[] { "N", "NE", "E", "SE", "S", "SW", "W", "NW" };
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  54 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  56 */     int protocol = buf.readUnsignedByte();
/*  57 */     boolean legacy = (protocol == 128);
/*     */     
/*  59 */     buf.readUnsignedByte();
/*  60 */     int index = legacy ? buf.readUnsignedShort() : buf.readUnsignedShortLE();
/*  61 */     int type = legacy ? buf.readUnsignedShort() : buf.readUnsignedShortLE();
/*  62 */     buf.readUnsignedShort();
/*  63 */     buf.readUnsignedShort();
/*  64 */     buf.readUnsignedShort();
/*  65 */     long id = legacy ? buf.readUnsignedInt() : buf.readUnsignedIntLE();
/*  66 */     buf.readUnsignedInt();
/*     */     
/*  68 */     Position position = new Position(getProtocolName());
/*  69 */     position.set("index", Integer.valueOf(index));
/*  70 */     position.setValid(true);
/*     */     
/*  72 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(id) });
/*  73 */     if (deviceSession == null) {
/*  74 */       return null;
/*     */     }
/*  76 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  78 */     if (protocol == 1 && (type == 256 || type == 254)) {
/*     */ 
/*     */ 
/*     */       
/*  82 */       buf.readUnsignedShortLE();
/*  83 */       buf.readUnsignedShortLE();
/*     */       
/*  85 */       position.setLatitude(buf.readUnsignedIntLE() * 1.0E-7D);
/*  86 */       position.setLongitude(buf.readUnsignedIntLE() * 1.0E-7D);
/*     */       
/*  88 */       position.set("rssi", Integer.valueOf(buf.readUnsignedShortLE()));
/*  89 */       position.set("sat", Integer.valueOf(buf.readUnsignedShortLE()));
/*  90 */       position.set("satVisible", Integer.valueOf(buf.readUnsignedShortLE()));
/*  91 */       position.set("gpsAntennaStatus", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */       
/*  93 */       position.setSpeed(buf.readUnsignedShortLE() * 0.194384D);
/*  94 */       position.setCourse(buf.readUnsignedShortLE());
/*     */       
/*  96 */       position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*     */       
/*  98 */       position.set("battery", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */       
/* 100 */       position.set("charge", Integer.valueOf(buf.readUnsignedShortLE()));
/*     */       
/* 102 */       position.setTime(new Date(buf.readUnsignedIntLE() * 1000L));
/*     */ 
/*     */ 
/*     */       
/* 106 */       return position;
/*     */     } 
/* 108 */     if (legacy) {
/*     */       
/* 110 */       if (channel != null) {
/* 111 */         channel.writeAndFlush(new NetworkMessage(
/* 112 */               Unpooled.copiedBuffer("gprs,ack," + index, StandardCharsets.US_ASCII), remoteAddress));
/*     */       }
/*     */       
/* 115 */       String sentence = buf.toString(StandardCharsets.US_ASCII);
/*     */       
/* 117 */       Pattern pattern = Pattern.compile("(-?\\d+\\.\\d+), (-?\\d+\\.\\d+)");
/* 118 */       Matcher matcher = pattern.matcher(sentence);
/* 119 */       if (!matcher.find()) {
/* 120 */         return null;
/*     */       }
/* 122 */       position.setLatitude(Double.parseDouble(matcher.group(1)));
/* 123 */       position.setLongitude(Double.parseDouble(matcher.group(2)));
/*     */       
/* 125 */       pattern = Pattern.compile("([NSWE]{1,2}) with speed (\\d+) km/h");
/* 126 */       matcher = pattern.matcher(sentence);
/* 127 */       if (matcher.find()) {
/* 128 */         for (int i = 0; i < DIRECTIONS.length; i++) {
/* 129 */           if (matcher.group(1).equals(DIRECTIONS[i])) {
/* 130 */             position.setCourse(i * 45.0D);
/*     */             break;
/*     */           } 
/*     */         } 
/* 134 */         position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(matcher.group(2))));
/*     */       } 
/*     */       
/* 137 */       pattern = Pattern.compile("(\\d{1,2}:\\d{2}(:\\d{2})? \\w{3} \\d{1,2})");
/* 138 */       matcher = pattern.matcher(sentence);
/* 139 */       if (!matcher.find()) {
/* 140 */         return null;
/*     */       }
/*     */       
/* 143 */       DateFormat dateFormat = new SimpleDateFormat((matcher.group(2) != null) ? "HH:mm:ss MMM d yyyy" : "HH:mm MMM d yyyy", Locale.ENGLISH);
/* 144 */       position.setTime(DateUtil.correctYear(dateFormat
/* 145 */             .parse(matcher.group(1) + " " + Calendar.getInstance().get(1))));
/*     */       
/* 147 */       if (sentence.contains("Ignition on detected")) {
/* 148 */         position.set("ignition", Boolean.valueOf(true));
/* 149 */       } else if (sentence.contains("Ignition off detected")) {
/* 150 */         position.set("ignition", Boolean.valueOf(false));
/*     */       } 
/*     */       
/* 153 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 157 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TramigoProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */