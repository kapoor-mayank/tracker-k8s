/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.handler.codec.http.FullHttpRequest;
/*    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*    */ import java.io.StringReader;
/*    */ import java.net.SocketAddress;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import java.util.Date;
/*    */ import java.util.Map;
/*    */ import javax.json.Json;
/*    */ import javax.json.JsonNumber;
/*    */ import javax.json.JsonObject;
/*    */ import javax.json.JsonString;
/*    */ import javax.json.JsonValue;
/*    */ import org.traccar.BaseHttpProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.model.Position;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class OutsafeProtocolDecoder
/*    */   extends BaseHttpProtocolDecoder
/*    */ {
/*    */   public OutsafeProtocolDecoder(Protocol protocol) {
/* 40 */     super(protocol);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 47 */     FullHttpRequest request = (FullHttpRequest)msg;
/* 48 */     String content = request.content().toString(StandardCharsets.UTF_8);
/* 49 */     JsonObject json = Json.createReader(new StringReader(content)).readObject();
/*    */     
/* 51 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { json.getString("device") });
/* 52 */     if (deviceSession == null) {
/* 53 */       sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
/* 54 */       return null;
/*    */     } 
/*    */     
/* 57 */     Position position = new Position(getProtocolName());
/* 58 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 60 */     position.setTime(new Date());
/* 61 */     position.setValid(true);
/* 62 */     position.setLatitude(json.getJsonNumber("latitude").doubleValue());
/* 63 */     position.setLongitude(json.getJsonNumber("longitude").doubleValue());
/* 64 */     position.setAltitude(json.getJsonNumber("altitude").doubleValue());
/* 65 */     position.setCourse(json.getJsonNumber("heading").intValue());
/*    */     
/* 67 */     position.set("rssi", Integer.valueOf(json.getJsonNumber("rssi").intValue()));
/* 68 */     position.set("origin", json.getString("origin"));
/*    */     
/* 70 */     JsonObject data = json.getJsonObject("data");
/* 71 */     for (Map.Entry<String, JsonValue> entry : (Iterable<Map.Entry<String, JsonValue>>)data.entrySet()) {
/* 72 */       decodeUnknownParam(entry.getKey(), entry.getValue(), position);
/*    */     }
/*    */     
/* 75 */     sendResponse(channel, HttpResponseStatus.OK);
/* 76 */     return position;
/*    */   }
/*    */   
/*    */   private void decodeUnknownParam(String name, JsonValue value, Position position) {
/* 80 */     if (value instanceof JsonNumber) {
/* 81 */       position.set(name, Double.valueOf(((JsonNumber)value).doubleValue()));
/* 82 */     } else if (value instanceof JsonString) {
/* 83 */       position.set(name, ((JsonString)value).getString());
/* 84 */     } else if (value == JsonValue.TRUE || value == JsonValue.FALSE) {
/* 85 */       position.set(name, Boolean.valueOf((value == JsonValue.TRUE)));
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OutsafeProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */