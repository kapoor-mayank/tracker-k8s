/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import java.io.StringReader;
/*    */ import java.net.SocketAddress;
/*    */ import java.time.OffsetDateTime;
/*    */ import java.util.Date;
/*    */ import javax.json.Json;
/*    */ import javax.json.JsonObject;
/*    */ import org.traccar.BaseProtocolDecoder;
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
/*    */ public class HoopoProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public HoopoProtocolDecoder(Protocol protocol) {
/* 34 */     super(protocol);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 41 */     JsonObject json = Json.createReader(new StringReader((String)msg)).readObject();
/*    */     
/* 43 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { json.getString("deviceId") });
/* 44 */     if (deviceSession == null) {
/* 45 */       return null;
/*    */     }
/*    */     
/* 48 */     if (json.containsKey("eventData")) {
/*    */       
/* 50 */       JsonObject eventData = json.getJsonObject("eventData");
/*    */       
/* 52 */       Position position = new Position(getProtocolName());
/* 53 */       position.setDeviceId(deviceSession.getDeviceId());
/*    */       
/* 55 */       Date time = new Date(OffsetDateTime.parse(json.getString("eventTime")).toInstant().toEpochMilli());
/* 56 */       position.setTime(time);
/*    */       
/* 58 */       position.setValid(true);
/* 59 */       position.setLatitude(eventData.getJsonNumber("latitude").doubleValue());
/* 60 */       position.setLongitude(eventData.getJsonNumber("longitude").doubleValue());
/*    */       
/* 62 */       position.set("event", eventData.getString("eventType"));
/* 63 */       position.set("batteryLevel", Integer.valueOf(eventData.getInt("batteryLevel")));
/*    */       
/* 65 */       if (json.containsKey("movement")) {
/* 66 */         position.setSpeed(json.getJsonObject("movement").getInt("Speed"));
/*    */       }
/*    */       
/* 69 */       return position;
/*    */     } 
/*    */ 
/*    */     
/* 73 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\HoopoProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */