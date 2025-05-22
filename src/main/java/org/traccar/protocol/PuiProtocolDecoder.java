/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.handler.codec.mqtt.MqttPublishMessage;
/*    */ import java.io.InputStream;
/*    */ import java.text.DateFormat;
/*    */ import java.text.SimpleDateFormat;
/*    */ import javax.json.Json;
/*    */ import javax.json.JsonObject;
/*    */ import org.apache.kafka.common.utils.ByteBufferInputStream;
/*    */ import org.traccar.BaseMqttProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.helper.UnitsConverter;
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
/*    */ public class PuiProtocolDecoder
/*    */   extends BaseMqttProtocolDecoder
/*    */ {
/*    */   public PuiProtocolDecoder(Protocol protocol) {
/* 34 */     super(protocol);
/*    */   }
/*    */   protected Object decode(DeviceSession deviceSession, MqttPublishMessage message) throws Exception {
/*    */     JsonObject json;
/*    */     Position position;
/*    */     DateFormat dateFormat;
/*    */     JsonObject location;
/* 41 */     try (ByteBufferInputStream inputStream = new ByteBufferInputStream(message.payload().nioBuffer())) {
/* 42 */       json = Json.createReader((InputStream)inputStream).readObject();
/*    */     } 
/*    */     
/* 45 */     String type = json.getString("rpt");
/* 46 */     switch (type) {
/*    */       case "hf":
/*    */       case "loc":
/* 49 */         position = new Position(getProtocolName());
/* 50 */         position.setDeviceId(deviceSession.getDeviceId());
/*    */         
/* 52 */         position.setValid(true);
/*    */         
/* 54 */         dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
/* 55 */         position.setTime(dateFormat.parse(json.getString("ts")));
/*    */         
/* 57 */         location = json.getJsonObject("location");
/* 58 */         position.setLatitude(location.getJsonNumber("lat").doubleValue());
/* 59 */         position.setLongitude(location.getJsonNumber("lon").doubleValue());
/*    */         
/* 61 */         position.setCourse(json.getInt("bear"));
/* 62 */         position.setSpeed(UnitsConverter.knotsFromCps(json.getInt("spd")));
/*    */         
/* 64 */         position.set("ignition", Boolean.valueOf(json.getString("ign").equals("on")));
/*    */         
/* 66 */         return position;
/*    */     } 
/*    */     
/* 69 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PuiProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */