/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import io.netty.handler.codec.http.FullHttpRequest;
/*     */ import io.netty.handler.codec.http.HttpResponseStatus;
/*     */ import java.io.StringReader;
/*     */ import java.net.SocketAddress;
/*     */ import java.net.URLDecoder;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import javax.json.Json;
/*     */ import javax.json.JsonNumber;
/*     */ import javax.json.JsonObject;
/*     */ import javax.json.JsonString;
/*     */ import javax.json.JsonValue;
/*     */ import org.traccar.BaseHttpProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.BufferUtil;
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
/*     */ 
/*     */ public class SigfoxProtocolDecoder
/*     */   extends BaseHttpProtocolDecoder
/*     */ {
/*     */   public SigfoxProtocolDecoder(Protocol protocol) {
/*  48 */     super(protocol);
/*     */   }
/*     */   
/*     */   private boolean jsonContains(JsonObject json, String key) {
/*  52 */     if (json.containsKey(key)) {
/*  53 */       JsonValue value = (JsonValue)json.get(key);
/*  54 */       if (value.getValueType() == JsonValue.ValueType.STRING) {
/*  55 */         return !((JsonString)value).getString().equals("null");
/*     */       }
/*     */       
/*  58 */       return true;
/*     */     } 
/*     */     
/*  61 */     return false;
/*     */   }
/*     */   
/*     */   private boolean getJsonBoolean(JsonObject json, String key) {
/*  65 */     JsonValue value = (JsonValue)json.get(key);
/*  66 */     if (value != null) {
/*  67 */       if (value.getValueType() == JsonValue.ValueType.STRING) {
/*  68 */         return Boolean.parseBoolean(((JsonString)value).getString());
/*     */       }
/*  70 */       return (value.getValueType() == JsonValue.ValueType.TRUE);
/*     */     } 
/*     */     
/*  73 */     return false;
/*     */   }
/*     */   
/*     */   private int getJsonInt(JsonObject json, String key) {
/*  77 */     JsonValue value = (JsonValue)json.get(key);
/*  78 */     if (value != null) {
/*  79 */       if (value.getValueType() == JsonValue.ValueType.NUMBER)
/*  80 */         return ((JsonNumber)value).intValue(); 
/*  81 */       if (value.getValueType() == JsonValue.ValueType.STRING) {
/*  82 */         return Integer.parseInt(((JsonString)value).getString());
/*     */       }
/*     */     } 
/*  85 */     return 0;
/*     */   }
/*     */   
/*     */   private double getJsonDouble(JsonObject json, String key) {
/*  89 */     JsonValue value = (JsonValue)json.get(key);
/*  90 */     if (value != null) {
/*  91 */       if (value.getValueType() == JsonValue.ValueType.NUMBER)
/*  92 */         return ((JsonNumber)value).doubleValue(); 
/*  93 */       if (value.getValueType() == JsonValue.ValueType.STRING) {
/*  94 */         return Double.parseDouble(((JsonString)value).getString());
/*     */       }
/*     */     } 
/*  97 */     return 0.0D;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*     */     String deviceId;
/* 104 */     FullHttpRequest request = (FullHttpRequest)msg;
/* 105 */     String content = request.content().toString(StandardCharsets.UTF_8);
/* 106 */     if (!content.startsWith("{")) {
/* 107 */       content = URLDecoder.decode(content.split("=")[0], "UTF-8");
/*     */     }
/* 109 */     JsonObject json = Json.createReader(new StringReader(content)).readObject();
/*     */ 
/*     */     
/* 112 */     if (json.containsKey("device")) {
/* 113 */       deviceId = json.getString("device");
/*     */     } else {
/* 115 */       deviceId = json.getString("deviceId");
/*     */     } 
/*     */     
/* 118 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { deviceId });
/* 119 */     if (deviceSession == null) {
/* 120 */       sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
/* 121 */       return null;
/*     */     } 
/*     */     
/* 124 */     Position position = new Position(getProtocolName());
/* 125 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 127 */     if (jsonContains(json, "time")) {
/* 128 */       position.setTime(new Date(getJsonInt(json, "time") * 1000L));
/* 129 */     } else if (jsonContains(json, "positionTime")) {
/* 130 */       position.setTime(new Date(getJsonInt(json, "positionTime") * 1000L));
/*     */     } else {
/* 132 */       position.setTime(new Date());
/*     */     } 
/*     */     
/* 135 */     if (jsonContains(json, "lastSeen")) {
/* 136 */       position.setDeviceTime(new Date(getJsonInt(json, "lastSeen") * 1000L));
/*     */     }
/*     */     
/* 139 */     if (jsonContains(json, "location") || (
/* 140 */       jsonContains(json, "lat") && jsonContains(json, "lng") && !jsonContains(json, "data")) || (
/* 141 */       jsonContains(json, "latitude") && jsonContains(json, "longitude") && !jsonContains(json, "data"))) {
/*     */       JsonObject location;
/*     */       
/* 144 */       if (jsonContains(json, "location")) {
/* 145 */         location = json.getJsonObject("location");
/*     */       } else {
/* 147 */         location = json;
/*     */       } 
/*     */       
/* 150 */       position.setValid(true);
/* 151 */       position.setLatitude(getJsonDouble(location, jsonContains(location, "lat") ? "lat" : "latitude"));
/* 152 */       position.setLongitude(getJsonDouble(location, jsonContains(location, "lng") ? "lng" : "longitude"));
/*     */     }
/* 154 */     else if (jsonContains(json, "data") || jsonContains(json, "payload")) {
/*     */       
/* 156 */       String data = json.getString(jsonContains(json, "data") ? "data" : "payload");
/* 157 */       ByteBuf buf = Unpooled.wrappedBuffer(DataConverter.parseHex(data));
/*     */       try {
/* 159 */         int header = buf.readUnsignedByte();
/* 160 */         if ("Amber".equals(getDeviceModel(deviceSession)))
/*     */         {
/* 162 */           int flags = buf.readUnsignedByte();
/* 163 */           position.set("motion", Boolean.valueOf(BitUtil.check(flags, 1)));
/*     */           
/* 165 */           position.set("battery", Double.valueOf(buf.readUnsignedByte() * 0.02D));
/* 166 */           position.set("temp1", Integer.valueOf(buf.readByte()));
/*     */           
/* 168 */           position.setValid(true);
/* 169 */           position.setLatitude(buf.readInt() / 60000.0D);
/* 170 */           position.setLongitude(buf.readInt() / 60000.0D);
/*     */         }
/* 172 */         else if (header == 15 || header == 31)
/*     */         {
/* 174 */           position.setValid((header >> 4 > 0));
/* 175 */           position.setLatitude(BufferUtil.readSignedMagnitudeInt(buf) * 1.0E-6D);
/* 176 */           position.setLongitude(BufferUtil.readSignedMagnitudeInt(buf) * 1.0E-6D);
/*     */           
/* 178 */           position.set("battery", Integer.valueOf(buf.readUnsignedByte()));
/*     */         }
/* 180 */         else if (header >> 4 <= 3 && buf.writerIndex() == 12)
/*     */         {
/* 182 */           if (BitUtil.to(header, 4) == 0) {
/* 183 */             position.setValid(true);
/* 184 */             position.setLatitude(buf.readIntLE() * 1.0E-7D);
/* 185 */             position.setLongitude(buf.readIntLE() * 1.0E-7D);
/* 186 */             position.setCourse((buf.readUnsignedByte() * 2));
/* 187 */             position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*     */           } 
/*     */           
/* 190 */           position.set("battery", Double.valueOf(buf.readUnsignedByte() * 0.025D));
/*     */         }
/*     */         else
/*     */         {
/* 194 */           position.set("event", Integer.valueOf(header));
/* 195 */           if (header == 34 || header == 98) {
/* 196 */             position.set("alarm", "sos");
/*     */           }
/*     */           
/* 199 */           while (buf.isReadable()) {
/* 200 */             int type = buf.readUnsignedByte();
/* 201 */             switch (type) {
/*     */               case 1:
/* 203 */                 position.setValid(true);
/* 204 */                 position.setLatitude(buf.readMedium());
/* 205 */                 position.setLongitude(buf.readMedium());
/*     */                 continue;
/*     */               case 2:
/* 208 */                 position.setValid(true);
/* 209 */                 position.setLatitude(buf.readFloat());
/* 210 */                 position.setLongitude(buf.readFloat());
/*     */                 continue;
/*     */               case 3:
/* 213 */                 position.set("temp1", Double.valueOf(buf.readByte() * 0.5D));
/*     */                 continue;
/*     */               case 4:
/* 216 */                 position.set("battery", Double.valueOf(buf.readUnsignedByte() * 0.1D));
/*     */                 continue;
/*     */               case 5:
/* 219 */                 position.set("batteryLevel", Short.valueOf(buf.readUnsignedByte()));
/*     */                 continue;
/*     */               case 6:
/* 222 */                 ByteBufUtil.hexDump(buf.readSlice(6)).replaceAll("(..)", "$1:");
/*     */                 continue;
/*     */               case 7:
/* 225 */                 buf.skipBytes(10);
/*     */                 continue;
/*     */               case 8:
/* 228 */                 buf.skipBytes(6);
/*     */                 continue;
/*     */               case 9:
/* 231 */                 position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
/*     */                 continue;
/*     */             } 
/* 234 */             buf.readUnsignedByte();
/*     */           }
/*     */         
/*     */         }
/*     */       
/*     */       } finally {
/*     */         
/* 241 */         buf.release();
/*     */       } 
/*     */     } 
/*     */     
/* 245 */     if (position.getLatitude() == 0.0D && position.getLongitude() == 0.0D) {
/* 246 */       getLastLocation(position, position.getDeviceTime());
/*     */     }
/*     */     
/* 249 */     if (jsonContains(json, "moving")) {
/* 250 */       position.set("motion", Boolean.valueOf(getJsonBoolean(json, "moving")));
/*     */     }
/* 252 */     if (jsonContains(json, "magStatus")) {
/* 253 */       position.set("blocked", Boolean.valueOf(getJsonBoolean(json, "magStatus")));
/*     */     }
/* 255 */     if (jsonContains(json, "temperature")) {
/* 256 */       position.set("deviceTemp", Double.valueOf(getJsonDouble(json, "temperature")));
/*     */     }
/* 258 */     if (jsonContains(json, "rssi")) {
/* 259 */       position.set("rssi", Double.valueOf(getJsonDouble(json, "rssi")));
/*     */     }
/* 261 */     if (jsonContains(json, "seqNumber")) {
/* 262 */       position.set("index", Integer.valueOf(getJsonInt(json, "seqNumber")));
/*     */     }
/*     */     
/* 265 */     sendResponse(channel, HttpResponseStatus.OK);
/* 266 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SigfoxProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */