/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.io.StringReader;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Date;
/*     */ import java.util.Map;
/*     */ import javax.json.Json;
/*     */ import javax.json.JsonNumber;
/*     */ import javax.json.JsonObject;
/*     */ import javax.json.JsonValue;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class FlexApiProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public FlexApiProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  45 */     String message = (String)msg;
/*  46 */     JsonObject root = Json.createReader(new StringReader(message.substring(1, message.length() - 2))).readObject();
/*     */     
/*  48 */     String topic = root.getString("topic");
/*  49 */     String clientId = topic.substring(3, topic.indexOf('/', 3));
/*  50 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { clientId });
/*  51 */     if (deviceSession == null) {
/*  52 */       return null;
/*     */     }
/*     */     
/*  55 */     Position position = new Position(getProtocolName());
/*  56 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  58 */     JsonObject payload = root.getJsonObject("payload");
/*     */     
/*  60 */     if (topic.contains("/gnss/")) {
/*     */       
/*  62 */       position.setValid(true);
/*     */       
/*  64 */       if (payload.containsKey("time")) {
/*  65 */         position.setTime(new Date(payload.getInt("time") * 1000L));
/*  66 */         position.setLatitude(payload.getJsonNumber("lat").doubleValue());
/*  67 */         position.setLongitude(payload.getJsonNumber("log").doubleValue());
/*     */       } else {
/*  69 */         position.setTime(new Date(payload.getInt("gnss.ts") * 1000L));
/*  70 */         position.setLatitude(payload.getJsonNumber("gnss.latitude").doubleValue());
/*  71 */         position.setLongitude(payload.getJsonNumber("gnss.longitude").doubleValue());
/*     */       } 
/*     */       
/*  74 */       position.setValid((payload.getInt("gnss.fix") > 0));
/*  75 */       position.setAltitude(payload.getJsonNumber("gnss.altitude").doubleValue());
/*  76 */       position.setSpeed(payload.getJsonNumber("gnss.speed").doubleValue());
/*  77 */       position.setCourse(payload.getJsonNumber("gnss.heading").doubleValue());
/*     */       
/*  79 */       position.set("sat", Integer.valueOf(payload.getInt("gnss.num_sv")));
/*  80 */       position.set("hdop", Double.valueOf(payload.getJsonNumber("gnss.hdop").doubleValue()));
/*     */     }
/*  82 */     else if (topic.contains("/cellular1/")) {
/*     */       
/*  84 */       getLastLocation(position, new Date(payload.getInt("modem1.ts") * 1000L));
/*     */       
/*  86 */       position.set("imei", payload.getString("modem1.imei"));
/*  87 */       position.set("imsi", payload.getString("modem1.imsi"));
/*  88 */       position.set("iccid", payload.getString("modem1.iccid"));
/*     */       
/*  90 */       position.set("rsrp", Integer.valueOf(payload.getInt("modem1.rsrp")));
/*  91 */       position.set("rsrq", Integer.valueOf(payload.getInt("modem1.rsrq")));
/*  92 */       position.set("regStatus", Integer.valueOf(payload.getInt("modem1.reg_status")));
/*     */       
/*  94 */       position.set("cellular1.status", Integer.valueOf(payload.getInt("cellular1.status")));
/*  95 */       position.set("cellular1.ip", payload.getString("cellular1.ip"));
/*  96 */       position.set("cellular1.netmask", payload.getString("cellular1.netmask"));
/*  97 */       position.set("cellular1.gateway", payload.getString("cellular1.gateway"));
/*  98 */       position.set("cellular1.dns1", payload.getString("cellular1.dns1"));
/*  99 */       position.set("cellular1.up_at", Integer.valueOf(payload.getInt("cellular1.up_at")));
/*     */       
/* 101 */       String operator = payload.getString("modem1.operator");
/* 102 */       if (!operator.isEmpty()) {
/* 103 */         CellTower cellTower = CellTower.from(
/* 104 */             Integer.parseInt(operator.substring(0, 3)), 
/* 105 */             Integer.parseInt(operator.substring(3)), 
/* 106 */             Integer.parseInt(payload.getString("modem1.lac"), 16), 
/* 107 */             Integer.parseInt(payload.getString("modem1.cell_id"), 16), payload
/* 108 */             .getInt("modem1.rssi"));
/* 109 */         switch (payload.getInt("modem1.network")) {
/*     */           case 1:
/* 111 */             cellTower.setRadioType("gsm");
/*     */             break;
/*     */           case 2:
/* 114 */             cellTower.setRadioType("wcdma");
/*     */             break;
/*     */           case 3:
/* 117 */             cellTower.setRadioType("lte");
/*     */             break;
/*     */         } 
/*     */ 
/*     */         
/* 122 */         position.setNetwork(new Network(cellTower));
/*     */       }
/*     */     
/* 125 */     } else if (topic.contains("/obd/")) {
/*     */       
/* 127 */       getLastLocation(position, new Date(payload.getInt("obd.ts") * 1000L));
/*     */       
/* 129 */       for (Map.Entry<String, JsonValue> entry : (Iterable<Map.Entry<String, JsonValue>>)payload.entrySet()) {
/* 130 */         if (((JsonValue)entry.getValue()).getValueType() == JsonValue.ValueType.NUMBER) {
/* 131 */           position.set(entry.getKey(), Double.valueOf(((JsonNumber)entry.getValue()).doubleValue()));
/*     */         }
/*     */       }
/*     */     
/* 135 */     } else if (topic.contains("/motion/")) {
/*     */       
/* 137 */       getLastLocation(position, new Date(payload.getInt("motion.ts") * 1000L));
/*     */       
/* 139 */       position.set("ax", Double.valueOf(payload.getJsonNumber("motion.ax").doubleValue()));
/* 140 */       position.set("ay", Double.valueOf(payload.getJsonNumber("motion.ay").doubleValue()));
/* 141 */       position.set("az", Double.valueOf(payload.getJsonNumber("motion.az").doubleValue()));
/* 142 */       position.set("gx", Double.valueOf(payload.getJsonNumber("motion.gx").doubleValue()));
/* 143 */       position.set("gy", Double.valueOf(payload.getJsonNumber("motion.gy").doubleValue()));
/* 144 */       position.set("gz", Double.valueOf(payload.getJsonNumber("motion.gz").doubleValue()));
/*     */     }
/* 146 */     else if (topic.contains("/io/")) {
/*     */       
/* 148 */       getLastLocation(position, new Date(payload.getInt("io.ts") * 1000L));
/*     */       
/* 150 */       if (payload.containsKey("io.IGT")) {
/* 151 */         position.set("ignition", Boolean.valueOf((payload.getInt("io.IGT") > 0)));
/*     */       }
/*     */       
/* 154 */       for (String key : payload.keySet()) {
/* 155 */         if (key.startsWith("io.AI")) {
/* 156 */           position.set("adc" + key.substring(5), Double.valueOf(payload.getJsonNumber(key).doubleValue())); continue;
/* 157 */         }  if (key.startsWith("io.DI") && !key.endsWith("_pullup")) {
/* 158 */           position.set("in" + key.substring(5), Boolean.valueOf((payload.getInt(key) > 0))); continue;
/* 159 */         }  if (key.startsWith("io.DI") && key.endsWith("_pullup")) {
/* 160 */           position.set("in" + key.substring(5) + "Pullup", Boolean.valueOf((payload.getInt(key) > 0))); continue;
/* 161 */         }  if (key.startsWith("io.DO")) {
/* 162 */           position.set("out" + key.substring(5), Boolean.valueOf((payload.getInt(key) > 0)));
/*     */         }
/*     */       }
/*     */     
/* 166 */     } else if (topic.contains("/sysinfo/")) {
/*     */       
/* 168 */       getLastLocation(position, new Date(payload.getInt("sysinfo.ts") * 1000L));
/*     */       
/* 170 */       position.set("serial", payload.getString("sysinfo.serial_number"));
/* 171 */       position.set("versionFw", payload.getString("sysinfo.firmware_version"));
/*     */     }
/*     */     else {
/*     */       
/* 175 */       return null;
/*     */     } 
/*     */ 
/*     */     
/* 179 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FlexApiProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */