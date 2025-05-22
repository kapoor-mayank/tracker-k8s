/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ public class FreematicsProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public FreematicsProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodeEvent(Channel channel, SocketAddress remoteAddress, String sentence) {
/*  42 */     DeviceSession deviceSession = null;
/*  43 */     String event = null;
/*  44 */     String time = null;
/*     */     
/*  46 */     for (String pair : sentence.split(",")) {
/*  47 */       String[] data = pair.split("=");
/*  48 */       String key = data[0];
/*  49 */       String value = data[1];
/*  50 */       switch (key) {
/*     */         case "ID":
/*     */         case "VIN":
/*  53 */           if (deviceSession == null) {
/*  54 */             deviceSession = getDeviceSession(channel, remoteAddress, new String[] { value });
/*     */           }
/*     */           break;
/*     */         case "EV":
/*  58 */           event = value;
/*     */           break;
/*     */         case "TS":
/*  61 */           time = value;
/*     */           break;
/*     */       } 
/*     */ 
/*     */ 
/*     */     
/*     */     } 
/*  68 */     if (channel != null && deviceSession != null && event != null && time != null) {
/*  69 */       String message = String.format("1#EV=%s,RX=1,TS=%s", new Object[] { event, time });
/*  70 */       message = message + '*' + Checksum.sum(message);
/*  71 */       channel.writeAndFlush(new NetworkMessage(message, remoteAddress));
/*     */     } 
/*     */     
/*  74 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Object decodePosition(Channel channel, SocketAddress remoteAddress, String sentence) throws Exception {
/*  80 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  81 */     if (deviceSession == null) {
/*  82 */       return null;
/*     */     }
/*     */     
/*  85 */     List<Position> positions = new LinkedList<>();
/*  86 */     Position position = null;
/*  87 */     DateBuilder dateBuilder = null;
/*     */     
/*  89 */     for (String pair : sentence.split(",")) {
/*  90 */       String[] data = pair.split("[=:]");
/*  91 */       int key = Integer.parseInt(data[0], 16);
/*  92 */       String value = data[1];
/*  93 */       switch (key) {
/*     */         case 0:
/*  95 */           if (position != null) {
/*  96 */             position.setTime(dateBuilder.getDate());
/*  97 */             positions.add(position);
/*     */           } 
/*  99 */           position = new Position(getProtocolName());
/* 100 */           position.setDeviceId(deviceSession.getDeviceId());
/* 101 */           position.setValid(true);
/* 102 */           dateBuilder = new DateBuilder(new Date());
/*     */           break;
/*     */         case 17:
/* 105 */           value = ("000000" + value).substring(value.length());
/* 106 */           dateBuilder.setDateReverse(
/* 107 */               Integer.parseInt(value.substring(0, 2)), 
/* 108 */               Integer.parseInt(value.substring(2, 4)), 
/* 109 */               Integer.parseInt(value.substring(4)));
/*     */           break;
/*     */         case 16:
/* 112 */           value = ("00000000" + value).substring(value.length());
/* 113 */           dateBuilder.setTime(
/* 114 */               Integer.parseInt(value.substring(0, 2)), 
/* 115 */               Integer.parseInt(value.substring(2, 4)), 
/* 116 */               Integer.parseInt(value.substring(4, 6)), 
/* 117 */               Integer.parseInt(value.substring(6)) * 10);
/*     */           break;
/*     */         case 10:
/* 120 */           position.setLatitude(Double.parseDouble(value));
/*     */           break;
/*     */         case 11:
/* 123 */           position.setLongitude(Double.parseDouble(value));
/*     */           break;
/*     */         case 12:
/* 126 */           position.setAltitude(Double.parseDouble(value));
/*     */           break;
/*     */         case 13:
/* 129 */           position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(value)));
/*     */           break;
/*     */         case 14:
/* 132 */           position.setCourse(Integer.parseInt(value));
/*     */           break;
/*     */         case 15:
/* 135 */           position.set("sat", Integer.valueOf(Integer.parseInt(value)));
/*     */           break;
/*     */         case 32:
/* 138 */           position.set("acceleration", value);
/*     */           break;
/*     */         case 36:
/* 141 */           position.set("battery", Double.valueOf(Integer.parseInt(value) * 0.01D));
/*     */           break;
/*     */         case 129:
/* 144 */           position.set("rssi", Integer.valueOf(Integer.parseInt(value)));
/*     */           break;
/*     */         case 130:
/* 147 */           position.set("deviceTemp", Double.valueOf(Integer.parseInt(value) * 0.1D));
/*     */           break;
/*     */         default:
/* 150 */           position.set(data[0], value);
/*     */           break;
/*     */       } 
/*     */     
/*     */     } 
/* 155 */     if (position != null) {
/* 156 */       position.setTime(dateBuilder.getDate());
/* 157 */       positions.add(position);
/*     */     } 
/*     */     
/* 160 */     return positions;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 167 */     String sentence = (String)msg;
/* 168 */     int startIndex = sentence.indexOf('#');
/* 169 */     int endIndex = sentence.indexOf('*');
/*     */     
/* 171 */     if (startIndex > 0 && endIndex > 0) {
/* 172 */       sentence = sentence.substring(startIndex + 1, endIndex);
/*     */       
/* 174 */       if (sentence.startsWith("EV")) {
/* 175 */         return decodeEvent(channel, remoteAddress, sentence);
/*     */       }
/* 177 */       return decodePosition(channel, remoteAddress, sentence);
/*     */     } 
/*     */ 
/*     */     
/* 181 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FreematicsProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */