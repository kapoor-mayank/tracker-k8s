/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.text.SimpleDateFormat;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
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
/*     */ public class StarcomProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public StarcomProtocolDecoder(StarcomProtocol protocol) {
/*  30 */     super((Protocol)protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  37 */     String sentence = (String)msg;
/*  38 */     sentence = sentence.substring(sentence.indexOf('|') + 1, sentence.lastIndexOf('|'));
/*     */     
/*  40 */     Position position = new Position();
/*  41 */     position.setProtocol(getProtocolName());
/*     */     
/*  43 */     for (String entry : sentence.split(",")) {
/*  44 */       DeviceSession deviceSession; int delimiter = entry.indexOf('=');
/*  45 */       String key = entry.substring(0, delimiter);
/*  46 */       String value = entry.substring(delimiter + 1);
/*  47 */       switch (key) {
/*     */         case "unit":
/*  49 */           deviceSession = getDeviceSession(channel, remoteAddress, new String[] { value });
/*  50 */           if (deviceSession != null) {
/*  51 */             position.setDeviceId(deviceSession.getDeviceId());
/*     */           }
/*     */           break;
/*     */         case "gps_valid":
/*  55 */           position.setValid((Integer.parseInt(value) != 0));
/*     */           break;
/*     */         case "datetime_actual":
/*  58 */           position.setTime((new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).parse(value));
/*     */           break;
/*     */         case "latitude":
/*  61 */           position.setLatitude(Double.parseDouble(value));
/*     */           break;
/*     */         case "longitude":
/*  64 */           position.setLongitude(Double.parseDouble(value));
/*     */           break;
/*     */         case "altitude":
/*  67 */           position.setAltitude(Double.parseDouble(value));
/*     */           break;
/*     */         case "velocity":
/*  70 */           position.setSpeed(UnitsConverter.knotsFromKph(Integer.parseInt(value)));
/*     */           break;
/*     */         case "heading":
/*  73 */           position.setCourse(Integer.parseInt(value));
/*     */           break;
/*     */         case "eventid":
/*  76 */           position.set("event", Integer.valueOf(Integer.parseInt(value)));
/*     */           break;
/*     */         case "odometer":
/*  79 */           position.set("odometer", Long.valueOf((long)(Double.parseDouble(value) * 1000.0D)));
/*     */           break;
/*     */         case "satellites":
/*  82 */           position.set("sat", Integer.valueOf(Integer.parseInt(value)));
/*     */           break;
/*     */         case "ignition":
/*  85 */           position.set("ignition", Boolean.valueOf((Integer.parseInt(value) != 0)));
/*     */           break;
/*     */         case "door":
/*  88 */           position.set("door", Boolean.valueOf((Integer.parseInt(value) != 0)));
/*     */           break;
/*     */         case "arm":
/*  91 */           position.set("armed", Boolean.valueOf((Integer.parseInt(value) != 0)));
/*     */           break;
/*     */         case "fuel":
/*  94 */           position.set("fuel", Integer.valueOf(Integer.parseInt(value)));
/*     */           break;
/*     */         case "rpm":
/*  97 */           position.set("rpm", Integer.valueOf(Integer.parseInt(value)));
/*     */           break;
/*     */         case "main_voltage":
/* 100 */           position.set("power", Double.valueOf(Double.parseDouble(value)));
/*     */           break;
/*     */         case "backup_voltage":
/* 103 */           position.set("battery", Double.valueOf(Double.parseDouble(value)));
/*     */           break;
/*     */         case "analog1":
/*     */         case "analog2":
/*     */         case "analog3":
/* 108 */           position.set("adc" + (key.charAt(key.length() - 1) - 48), Double.valueOf(Double.parseDouble(value)));
/*     */           break;
/*     */ 
/*     */ 
/*     */         
/*     */         default:
/* 114 */           position.set(key, value);
/*     */           break;
/*     */       } 
/*     */     
/*     */     } 
/* 119 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\StarcomProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */