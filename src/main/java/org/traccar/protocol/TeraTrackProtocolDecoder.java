/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import java.io.StringReader;
/*    */ import java.net.SocketAddress;
/*    */ import java.text.DateFormat;
/*    */ import java.text.SimpleDateFormat;
/*    */ import java.util.TimeZone;
/*    */ import javax.json.Json;
/*    */ import javax.json.JsonArray;
/*    */ import javax.json.JsonObject;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.NetworkMessage;
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
/*    */ 
/*    */ public class TeraTrackProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public TeraTrackProtocolDecoder(Protocol protocol) {
/* 38 */     super(protocol);
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 45 */     JsonObject json = Json.createReader(new StringReader((String)msg)).readObject();
/*    */     
/* 47 */     String deviceId = json.getString("MDeviceID");
/* 48 */     String imei = json.getString("IMEI");
/* 49 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { deviceId, imei });
/* 50 */     if (deviceSession == null) {
/* 51 */       return null;
/*    */     }
/*    */     
/* 54 */     Position position = new Position(getProtocolName());
/* 55 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 57 */     DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
/* 58 */     dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/* 59 */     position.setTime(dateFormat.parse(json.getString("DateTime")));
/*    */     
/* 61 */     position.setValid(true);
/* 62 */     position.setLatitude(Double.parseDouble(json.getString("Latitude")));
/* 63 */     position.setLongitude(Double.parseDouble(json.getString("Longitude")));
/* 64 */     position.setSpeed(UnitsConverter.knotsFromKph(Integer.parseInt(json.getString("Speed"))));
/*    */     
/* 66 */     position.set("odometer", Integer.valueOf(Integer.parseInt(json.getString("Mileage"))));
/* 67 */     position.set("blocked", Boolean.valueOf(json.getString("LockOpen").equals("0")));
/* 68 */     position.set("driverUniqueId", json.getString("CardNo"));
/* 69 */     position.set("alarm", json.getString("LowPower").equals("1") ? "lowPower" : null);
/* 70 */     position.set("batteryLevel", Integer.valueOf(Integer.parseInt(json.getString("Power"))));
/* 71 */     position.set("rssi", Integer.valueOf(Integer.parseInt(json.getString("GSM"))));
/*    */     
/* 73 */     position.set("lockCutOff", json.getString("LockCutOff"));
/* 74 */     position.set("sealTampered", json.getString("SealTampered"));
/* 75 */     position.set("lockRope", json.getString("LockRope"));
/* 76 */     position.set("lockStatus", json.getString("LockStatus"));
/* 77 */     position.set("illegalCard", json.getString("IllegalCard"));
/* 78 */     position.set("coverStatus", json.getString("CoverStatus"));
/*    */     
/* 80 */     JsonArray slaves = json.getJsonArray("Slave");
/* 81 */     for (int i = 0; i < slaves.size(); i++) {
/* 82 */       JsonObject slave = slaves.getJsonObject(i);
/* 83 */       position.set("s" + (i + 1) + "DeviceId", slave.getString("SDeviceId"));
/* 84 */       position.set("s" + (i + 1) + "Power", slave.getString("SPower"));
/* 85 */       position.set("s" + (i + 1) + "LockCutOff", slave.getString("SLockCutOff"));
/* 86 */       position.set("s" + (i + 1) + "LockOpen", slave.getString("SLockOpen"));
/* 87 */       position.set("s" + (i + 1) + "CoverStatus", slave.getString("SCoverStatus"));
/* 88 */       position.set("s" + (i + 1) + "TimeOut", slave.getString("STimeOut"));
/* 89 */       position.set("s" + (i + 1) + "LockRope", slave.getString("SLockRope"));
/* 90 */       position.set("s" + (i + 1) + "SealTempered", slave.getString("SSealTempered"));
/*    */     } 
/*    */     
/* 93 */     if (channel != null && json.getString("MessageAck").equals("1")) {
/* 94 */       channel.writeAndFlush(new NetworkMessage("{01}", remoteAddress));
/*    */     }
/*    */     
/* 97 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TeraTrackProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */