/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.handler.codec.http.FullHttpRequest;
/*    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*    */ import java.io.StringReader;
/*    */ import java.net.SocketAddress;
/*    */ import java.nio.charset.StandardCharsets;
/*    */ import java.util.Date;
/*    */ import javax.json.Json;
/*    */ import javax.json.JsonObject;
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
/*    */ public class PolteProtocolDecoder
/*    */   extends BaseHttpProtocolDecoder
/*    */ {
/*    */   public PolteProtocolDecoder(Protocol protocol) {
/* 36 */     super(protocol);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 43 */     FullHttpRequest request = (FullHttpRequest)msg;
/* 44 */     String content = request.content().toString(StandardCharsets.UTF_8);
/* 45 */     JsonObject json = Json.createReader(new StringReader(content)).readObject();
/*    */     
/* 47 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { json.getString("ueToken") });
/* 48 */     if (deviceSession == null) {
/* 49 */       sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
/* 50 */       return null;
/*    */     } 
/*    */     
/* 53 */     if (json.containsKey("location")) {
/*    */       
/* 55 */       Position position = new Position(getProtocolName());
/* 56 */       position.setDeviceId(deviceSession.getDeviceId());
/*    */       
/* 58 */       JsonObject location = json.getJsonObject("location");
/*    */       
/* 60 */       position.setValid(true);
/* 61 */       position.setTime(new Date(location.getInt("detected_at") * 1000L));
/* 62 */       position.setLatitude(location.getJsonNumber("latitude").doubleValue());
/* 63 */       position.setLongitude(location.getJsonNumber("longitude").doubleValue());
/* 64 */       position.setAltitude(location.getJsonNumber("altitude").doubleValue());
/*    */       
/* 66 */       if (json.containsKey("report")) {
/* 67 */         JsonObject report = json.getJsonObject("report");
/* 68 */         position.set("event", Integer.valueOf(report.getInt("event")));
/* 69 */         if (report.containsKey("battery")) {
/* 70 */           JsonObject battery = report.getJsonObject("battery");
/* 71 */           position.set("batteryLevel", Integer.valueOf(battery.getInt("level")));
/* 72 */           position.set("battery", Double.valueOf(battery.getJsonNumber("voltage").doubleValue()));
/*    */         } 
/*    */       } 
/*    */       
/* 76 */       return position;
/*    */     } 
/*    */ 
/*    */     
/* 80 */     sendResponse(channel, HttpResponseStatus.OK);
/* 81 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PolteProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */