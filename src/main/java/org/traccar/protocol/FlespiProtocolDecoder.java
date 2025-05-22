/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import io.netty.handler.codec.http.FullHttpRequest;
/*     */ import io.netty.handler.codec.http.HttpResponseStatus;
/*     */ import java.io.StringReader;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import javax.json.Json;
/*     */ import javax.json.JsonArray;
/*     */ import javax.json.JsonNumber;
/*     */ import javax.json.JsonObject;
/*     */ import javax.json.JsonString;
/*     */ import javax.json.JsonValue;
/*     */ import org.traccar.BaseHttpProtocolDecoder;
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
/*     */ 
/*     */ public class FlespiProtocolDecoder
/*     */   extends BaseHttpProtocolDecoder
/*     */ {
/*     */   public FlespiProtocolDecoder(Protocol protocol) {
/*  44 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  51 */     FullHttpRequest request = (FullHttpRequest)msg;
/*     */     
/*  53 */     JsonArray result = Json.createReader(new StringReader(request.content().toString(StandardCharsets.UTF_8))).readArray();
/*  54 */     List<Position> positions = new LinkedList<>();
/*  55 */     for (int i = 0; i < result.size(); i++) {
/*  56 */       JsonObject message = result.getJsonObject(i);
/*  57 */       JsonString ident = message.getJsonString("ident");
/*  58 */       if (ident != null) {
/*     */ 
/*     */         
/*  61 */         DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { ident.getString() });
/*  62 */         if (deviceSession != null) {
/*     */ 
/*     */           
/*  65 */           Position position = new Position(getProtocolName());
/*  66 */           position.setDeviceId(deviceSession.getDeviceId());
/*  67 */           decodePosition(message, position);
/*  68 */           positions.add(position);
/*     */         } 
/*     */       } 
/*  71 */     }  sendResponse(channel, HttpResponseStatus.OK);
/*  72 */     return positions;
/*     */   }
/*     */   
/*     */   private void decodePosition(JsonObject object, Position position) {
/*  76 */     for (Map.Entry<String, JsonValue> param : (Iterable<Map.Entry<String, JsonValue>>)object.entrySet()) {
/*  77 */       String paramName = param.getKey();
/*  78 */       JsonValue paramValue = param.getValue();
/*  79 */       int index = -1;
/*  80 */       if (paramName.contains("#")) {
/*  81 */         String[] parts = paramName.split("#");
/*  82 */         paramName = parts[0];
/*  83 */         index = Integer.parseInt(parts[1]);
/*     */       } 
/*  85 */       if (!decodeParam(paramName, index, paramValue, position)) {
/*  86 */         decodeUnknownParam(param.getKey(), param.getValue(), position);
/*     */       }
/*     */     } 
/*  89 */     if (position.getLatitude() == 0.0D && position.getLongitude() == 0.0D) {
/*  90 */       getLastLocation(position, position.getDeviceTime());
/*     */     }
/*     */   }
/*     */   
/*     */   private boolean decodeParam(String name, int index, JsonValue value, Position position) {
/*  95 */     switch (name) {
/*     */       case "timestamp":
/*  97 */         position.setTime(new Date(((JsonNumber)value).longValue() * 1000L));
/*  98 */         return true;
/*     */       case "position.latitude":
/* 100 */         position.setLatitude(((JsonNumber)value).doubleValue());
/* 101 */         return true;
/*     */       case "position.longitude":
/* 103 */         position.setLongitude(((JsonNumber)value).doubleValue());
/* 104 */         return true;
/*     */       case "position.speed":
/* 106 */         position.setSpeed(UnitsConverter.knotsFromKph(((JsonNumber)value).doubleValue()));
/* 107 */         return true;
/*     */       case "position.direction":
/* 109 */         position.setCourse(((JsonNumber)value).doubleValue());
/* 110 */         return true;
/*     */       case "position.altitude":
/* 112 */         position.setAltitude(((JsonNumber)value).doubleValue());
/* 113 */         return true;
/*     */       case "position.satellites":
/* 115 */         position.set("sat", Integer.valueOf(((JsonNumber)value).intValue()));
/* 116 */         return true;
/*     */       case "position.valid":
/* 118 */         position.setValid((value == JsonValue.TRUE));
/* 119 */         return true;
/*     */       case "position.hdop":
/* 121 */         position.set("hdop", Double.valueOf(((JsonNumber)value).doubleValue()));
/* 122 */         return true;
/*     */       case "position.pdop":
/* 124 */         position.set("pdop", Double.valueOf(((JsonNumber)value).doubleValue()));
/* 125 */         return true;
/*     */       case "din":
/*     */       case "dout":
/* 128 */         position.set(name.equals("din") ? "input" : "output", 
/* 129 */             Integer.valueOf(((JsonNumber)value).intValue()));
/* 130 */         return true;
/*     */       case "gps.vehicle.mileage":
/* 132 */         position.set("odometer", Double.valueOf(((JsonNumber)value).doubleValue()));
/* 133 */         return true;
/*     */       case "external.powersource.voltage":
/* 135 */         position.set("power", Double.valueOf(((JsonNumber)value).doubleValue()));
/* 136 */         return true;
/*     */       case "battery.voltage":
/* 138 */         position.set("battery", Double.valueOf(((JsonNumber)value).doubleValue()));
/* 139 */         return true;
/*     */       case "fuel.level":
/*     */       case "can.fuel.level":
/* 142 */         position.set("fuel", Double.valueOf(((JsonNumber)value).doubleValue()));
/* 143 */         return true;
/*     */       case "engine.rpm":
/*     */       case "can.engine.rpm":
/* 146 */         position.set("rpm", Double.valueOf(((JsonNumber)value).doubleValue()));
/* 147 */         return true;
/*     */       case "can.engine.temperature":
/* 149 */         position.set("temp" + ((index > 0) ? index : 0), Double.valueOf(((JsonNumber)value).doubleValue()));
/* 150 */         return true;
/*     */       case "engine.ignition.status":
/* 152 */         position.set("ignition", Boolean.valueOf((value == JsonValue.TRUE)));
/* 153 */         return true;
/*     */       case "movement.status":
/* 155 */         position.set("motion", Boolean.valueOf((value == JsonValue.TRUE)));
/* 156 */         return true;
/*     */       case "device.temperature":
/* 158 */         position.set("deviceTemp", Double.valueOf(((JsonNumber)value).doubleValue()));
/* 159 */         return true;
/*     */       case "ibutton.code":
/* 161 */         position.set("driverUniqueId", ((JsonString)value).getString());
/* 162 */         return true;
/*     */       case "vehicle.vin":
/* 164 */         position.set("vin", ((JsonString)value).getString());
/* 165 */         return true;
/*     */       case "alarm.event.trigger":
/* 167 */         if (value == JsonValue.TRUE) {
/* 168 */           position.set("alarm", "general");
/*     */         }
/* 170 */         return true;
/*     */       case "towing.event.trigger":
/*     */       case "towing.alarm.status":
/* 173 */         if (value == JsonValue.TRUE) {
/* 174 */           position.set("alarm", "tow");
/*     */         }
/* 176 */         return true;
/*     */       case "geofence.event.enter":
/* 178 */         if (value == JsonValue.TRUE) {
/* 179 */           position.set("alarm", "geofenceEnter");
/*     */         }
/* 181 */         return true;
/*     */       case "geofence.event.exit":
/* 183 */         if (value == JsonValue.TRUE) {
/* 184 */           position.set("alarm", "geofenceExit");
/*     */         }
/* 186 */         return true;
/*     */       case "shock.event.trigger":
/* 188 */         if (value == JsonValue.TRUE) {
/* 189 */           position.set("alarm", "shock");
/*     */         }
/* 191 */         return true;
/*     */       case "overspeeding.event.trigger":
/* 193 */         if (value == JsonValue.TRUE) {
/* 194 */           position.set("alarm", "overspeed");
/*     */         }
/* 196 */         return true;
/*     */       case "harsh.acceleration.event.trigger":
/* 198 */         if (value == JsonValue.TRUE) {
/* 199 */           position.set("alarm", "hardAcceleration");
/*     */         }
/* 201 */         return true;
/*     */       case "harsh.braking.event.trigger":
/* 203 */         if (value == JsonValue.TRUE) {
/* 204 */           position.set("alarm", "hardBraking");
/*     */         }
/* 206 */         return true;
/*     */       case "harsh.cornering.event.trigger":
/* 208 */         if (value == JsonValue.TRUE) {
/* 209 */           position.set("alarm", "hardCornering");
/*     */         }
/* 211 */         return true;
/*     */       case "gnss.antenna.cut.status":
/* 213 */         if (value == JsonValue.TRUE) {
/* 214 */           position.set("alarm", "gpsAntennaCut");
/*     */         }
/* 216 */         return true;
/*     */       case "gsm.jamming.event.trigger":
/* 218 */         if (value == JsonValue.TRUE) {
/* 219 */           position.set("alarm", "jamming");
/*     */         }
/* 221 */         return true;
/*     */       case "hood.open.status":
/* 223 */         if (value == JsonValue.TRUE) {
/* 224 */           position.set("alarm", "bonnet");
/*     */         }
/* 226 */         return true;
/*     */     } 
/* 228 */     return false;
/*     */   }
/*     */ 
/*     */   
/*     */   private void decodeUnknownParam(String name, JsonValue value, Position position) {
/* 233 */     if (value instanceof JsonNumber) {
/* 234 */       if (((JsonNumber)value).isIntegral()) {
/* 235 */         position.set(name, Long.valueOf(((JsonNumber)value).longValue()));
/*     */       } else {
/* 237 */         position.set(name, Double.valueOf(((JsonNumber)value).doubleValue()));
/*     */       } 
/* 239 */       position.set(name, Double.valueOf(((JsonNumber)value).doubleValue()));
/* 240 */     } else if (value instanceof JsonString) {
/* 241 */       position.set(name, ((JsonString)value).getString());
/* 242 */     } else if (value == JsonValue.TRUE || value == JsonValue.FALSE) {
/* 243 */       position.set(name, Boolean.valueOf((value == JsonValue.TRUE)));
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FlespiProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */