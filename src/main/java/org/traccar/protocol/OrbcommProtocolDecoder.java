/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import io.netty.handler.codec.http.FullHttpResponse;
/*     */ import java.io.StringReader;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.TimeZone;
/*     */ import javax.json.Json;
/*     */ import javax.json.JsonArray;
/*     */ import javax.json.JsonObject;
/*     */ import javax.json.JsonValue;
/*     */ import org.traccar.BasePipelineFactory;
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
/*     */ 
/*     */ public class OrbcommProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public OrbcommProtocolDecoder(Protocol protocol) {
/*  43 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  50 */     FullHttpResponse response = (FullHttpResponse)msg;
/*  51 */     String content = response.content().toString(StandardCharsets.UTF_8);
/*  52 */     JsonObject json = Json.createReader(new StringReader(content)).readObject();
/*     */     
/*  54 */     if (channel != null && !json.getString("NextStartUTC").isEmpty()) {
/*     */       
/*  56 */       OrbcommProtocolPoller poller = (OrbcommProtocolPoller)BasePipelineFactory.getHandler(channel.pipeline(), OrbcommProtocolPoller.class);
/*  57 */       if (poller != null) {
/*  58 */         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
/*  59 */         dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/*  60 */         poller.setStartTime(dateFormat.parse(json.getString("NextStartUTC")));
/*     */       } 
/*     */     } 
/*     */     
/*  64 */     if (((JsonValue)json.get("Messages")).getValueType() == JsonValue.ValueType.NULL) {
/*  65 */       return null;
/*     */     }
/*     */     
/*  68 */     LinkedList<Position> positions = new LinkedList<>();
/*     */     
/*  70 */     JsonArray messages = json.getJsonArray("Messages");
/*  71 */     for (int i = 0; i < messages.size(); i++) {
/*  72 */       JsonObject message = messages.getJsonObject(i);
/*  73 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, true, new String[] { message
/*  74 */             .getString("MobileID") });
/*  75 */       if (deviceSession != null) {
/*     */         
/*  77 */         Position position = new Position(getProtocolName());
/*  78 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/*  80 */         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
/*  81 */         dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/*  82 */         position.setDeviceTime(dateFormat.parse(message.getString("MessageUTC")));
/*     */         
/*  84 */         JsonArray fields = message.getJsonObject("Payload").getJsonArray("Fields");
/*  85 */         for (int j = 0; j < fields.size(); j++) {
/*  86 */           int heading; JsonObject field = fields.getJsonObject(j);
/*  87 */           String value = field.getString("Value");
/*  88 */           switch (field.getString("Name").toLowerCase()) {
/*     */             case "eventtime":
/*  90 */               position.setDeviceTime(new Date(Long.parseLong(value) * 1000L));
/*     */               break;
/*     */             case "latitude":
/*  93 */               position.setLatitude(Integer.parseInt(value) / 60000.0D);
/*     */               break;
/*     */             case "longitude":
/*  96 */               position.setLongitude(Integer.parseInt(value) / 60000.0D);
/*     */               break;
/*     */             case "speed":
/*  99 */               position.setSpeed(UnitsConverter.knotsFromKph(Integer.parseInt(value)));
/*     */               break;
/*     */             case "heading":
/* 102 */               heading = Integer.parseInt(value);
/* 103 */               position.setCourse((heading <= 360) ? heading : 0.0D);
/*     */               break;
/*     */           } 
/*     */ 
/*     */ 
/*     */         
/*     */         } 
/* 110 */         if (position.getLatitude() != 0.0D && position.getLongitude() != 0.0D) {
/* 111 */           position.setValid(true);
/* 112 */           position.setFixTime(position.getDeviceTime());
/*     */         } else {
/* 114 */           getLastLocation(position, position.getDeviceTime());
/*     */         } 
/*     */         
/* 117 */         positions.add(position);
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 122 */     return positions.isEmpty() ? null : positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OrbcommProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */