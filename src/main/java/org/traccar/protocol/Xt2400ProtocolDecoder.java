/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DataConverter;
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
/*     */ public class Xt2400ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public Xt2400ProtocolDecoder(Protocol protocol) {
/*  39 */     super(protocol);
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  94 */     this.formats = (Map)new HashMap<>();
/*     */     String config = Context.getConfig().getString(getProtocolName() + ".config");
/*     */     if (config != null)
/*  97 */       setConfig(config);  } public void setConfig(String configString) { Pattern pattern = Pattern.compile(":wycfg pcr\\[\\d+] ([0-9a-fA-F]{2})[0-9a-fA-F]{2}([0-9a-fA-F]+)");
/*  98 */     Matcher matcher = pattern.matcher(configString);
/*  99 */     while (matcher.find()) {
/* 100 */       this.formats.put(Short.valueOf(Short.parseShort(matcher.group(1), 16)), DataConverter.parseHex(matcher.group(2)));
/*     */     } }
/*     */ 
/*     */   
/*     */   private static final Map<Integer, Integer> TAG_LENGTH_MAP = new HashMap<>();
/*     */   private Map<Short, byte[]> formats;
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 108 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 110 */     byte[] format = null;
/* 111 */     if (this.formats.size() > 1) {
/* 112 */       format = this.formats.get(Short.valueOf(buf.getUnsignedByte(buf.readerIndex())));
/* 113 */     } else if (!this.formats.isEmpty()) {
/* 114 */       format = this.formats.values().iterator().next();
/*     */     } 
/*     */     
/* 117 */     if (format == null) {
/* 118 */       return null;
/*     */     }
/*     */     
/* 121 */     Position position = new Position(getProtocolName());
/*     */     
/* 123 */     for (byte b : format) {
/* 124 */       DeviceSession deviceSession; int ecuCount, i, tag = b & 0xFF;
/* 125 */       switch (tag) {
/*     */         case 3:
/* 127 */           deviceSession = getDeviceSession(channel, remoteAddress, new String[] {
/* 128 */                 String.valueOf(buf.readUnsignedInt()) });
/* 129 */           if (deviceSession == null) {
/* 130 */             return null;
/*     */           }
/* 132 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */           break;
/*     */         case 4:
/* 135 */           position.set("event", Short.valueOf(buf.readUnsignedByte()));
/*     */           break;
/*     */         case 5:
/* 138 */           position.set("index", Integer.valueOf(buf.readUnsignedShort()));
/*     */           break;
/*     */         case 6:
/* 141 */           position.setTime(new Date(buf.readUnsignedInt() * 1000L));
/*     */           break;
/*     */         case 7:
/* 144 */           position.setLatitude(buf.readInt() * 1.0E-6D);
/*     */           break;
/*     */         case 8:
/* 147 */           position.setLongitude(buf.readInt() * 1.0E-6D);
/*     */           break;
/*     */         case 9:
/* 150 */           position.setAltitude(buf.readShort() * 0.1D);
/*     */           break;
/*     */         case 10:
/* 153 */           position.setCourse(buf.readShort() * 0.1D);
/*     */           break;
/*     */         case 11:
/* 156 */           position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*     */           break;
/*     */         case 16:
/* 159 */           position.set("tripOdometer", Long.valueOf(buf.readUnsignedInt()));
/*     */           break;
/*     */         case 18:
/* 162 */           position.set("hdop", Double.valueOf(buf.readUnsignedByte() * 0.1D));
/*     */           break;
/*     */         case 19:
/* 165 */           position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */           break;
/*     */         case 20:
/* 168 */           position.set("rssi", Short.valueOf(buf.readShort()));
/*     */           break;
/*     */         case 22:
/* 171 */           position.set("battery", Double.valueOf(buf.readUnsignedByte() * 0.1D));
/*     */           break;
/*     */         case 23:
/* 174 */           position.set("power", Double.valueOf(buf.readUnsignedByte() * 0.1D));
/*     */           break;
/*     */         case 87:
/* 177 */           position.set("obdSpeed", Double.valueOf(UnitsConverter.knotsFromKph(buf.readUnsignedShort())));
/*     */           break;
/*     */         case 101:
/* 180 */           position.set("vin", buf.readSlice(17).toString(StandardCharsets.US_ASCII));
/*     */           break;
/*     */         case 108:
/* 183 */           buf.readUnsignedByte();
/* 184 */           ecuCount = buf.readUnsignedByte();
/* 185 */           for (i = 0; i < ecuCount; i++) {
/* 186 */             buf.readUnsignedByte();
/* 187 */             buf.skipBytes(buf.readUnsignedByte() * 6);
/*     */           } 
/*     */           break;
/*     */         case 115:
/* 191 */           position.set("versionFw", buf.readSlice(16).toString(StandardCharsets.US_ASCII).trim());
/*     */           break;
/*     */         default:
/* 194 */           buf.skipBytes(getTagLength(tag));
/*     */           break;
/*     */       } 
/*     */     
/*     */     } 
/* 199 */     if (position.getLatitude() != 0.0D && position.getLongitude() != 0.0D) {
/* 200 */       position.setValid(true);
/*     */     } else {
/* 202 */       getLastLocation(position, position.getDeviceTime());
/*     */     } 
/*     */     
/* 205 */     return position;
/*     */   }
/*     */   
/*     */   static {
/*     */     int[] l1 = { 
/*     */         1, 2, 4, 11, 12, 13, 18, 19, 22, 23, 
/*     */         28, 31, 35, 44, 45, 48, 49, 50, 51, 52, 
/*     */         53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 
/*     */         64, 65, 83, 102, 105, 106, 147, 148, 150 };
/*     */     int[] l2 = { 
/*     */         5, 9, 10, 20, 21, 29, 30, 36, 38, 66, 
/*     */         67, 68, 69, 70, 71, 72, 73, 87, 88, 89, 
/*     */         90, 107, 111, 122, 123, 124, 125, 126, 127, 128, 
/*     */         129, 130, 131, 132, 133, 134, 200 };
/*     */     int[] l4 = { 
/*     */         3, 6, 7, 8, 14, 15, 16, 17, 24, 25, 
/*     */         26, 27, 32, 33, 34, 46, 47, 74, 75, 76, 
/*     */         77, 78, 79, 80, 81, 82, 84, 85, 86, 91, 
/*     */         92, 93, 94, 95, 96, 97, 98, 104, 110, 113, 
/*     */         114, 116, 117, 118, 119, 120, 121, 135, 136, 137, 
/*     */         138, 139, 140, 141 };
/*     */     for (int i : l1)
/*     */       TAG_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(1)); 
/*     */     for (int i : l2)
/*     */       TAG_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(2)); 
/*     */     for (int i : l4)
/*     */       TAG_LENGTH_MAP.put(Integer.valueOf(i), Integer.valueOf(4)); 
/*     */     TAG_LENGTH_MAP.put(Integer.valueOf(149), Integer.valueOf(24));
/*     */     TAG_LENGTH_MAP.put(Integer.valueOf(208), Integer.valueOf(21));
/*     */   }
/*     */   
/*     */   private static int getTagLength(int tag) {
/*     */     Integer length = TAG_LENGTH_MAP.get(Integer.valueOf(tag));
/*     */     if (length == null)
/*     */       throw new IllegalArgumentException(String.format("Unknown tag: 0x%02X", new Object[] { Integer.valueOf(tag) })); 
/*     */     return length.intValue();
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Xt2400ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */