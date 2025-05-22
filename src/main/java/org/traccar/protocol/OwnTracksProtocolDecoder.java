/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import io.netty.handler.codec.http.FullHttpRequest;
/*     */ import io.netty.handler.codec.http.HttpResponseStatus;
/*     */ import java.io.StringReader;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import javax.json.Json;
/*     */ import javax.json.JsonObject;
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
/*     */ 
/*     */ public class OwnTracksProtocolDecoder
/*     */   extends BaseHttpProtocolDecoder
/*     */ {
/*     */   public OwnTracksProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*     */     String uniqueId;
/*  45 */     FullHttpRequest request = (FullHttpRequest)msg;
/*     */     
/*  47 */     JsonObject root = Json.createReader(new StringReader(request.content().toString(StandardCharsets.US_ASCII))).readObject();
/*     */     
/*  49 */     if (!root.containsKey("_type")) {
/*  50 */       sendResponse(channel, HttpResponseStatus.OK);
/*  51 */       return null;
/*     */     } 
/*  53 */     if (!root.getString("_type").equals("location") && 
/*  54 */       !root.getString("_type").equals("lwt")) {
/*  55 */       sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
/*  56 */       return null;
/*     */     } 
/*     */     
/*  59 */     Position position = new Position(getProtocolName());
/*     */ 
/*     */     
/*  62 */     if (root.containsKey("topic")) {
/*  63 */       uniqueId = root.getString("topic");
/*  64 */       if (root.containsKey("tid")) {
/*  65 */         position.set("tid", root.getString("tid"));
/*     */       }
/*     */     } else {
/*  68 */       uniqueId = root.getString("tid");
/*     */     } 
/*  70 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { uniqueId });
/*  71 */     if (deviceSession == null) {
/*  72 */       sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
/*  73 */       return null;
/*     */     } 
/*     */     
/*  76 */     if (root.getString("_type").equals("lwt")) {
/*  77 */       sendResponse(channel, HttpResponseStatus.OK);
/*  78 */       return null;
/*     */     } 
/*     */     
/*  81 */     if (root.containsKey("t") && root.getString("t").equals("p")) {
/*  82 */       sendResponse(channel, HttpResponseStatus.OK);
/*  83 */       return null;
/*     */     } 
/*     */     
/*  86 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  88 */     position.setTime(new Date(root.getJsonNumber("tst").longValue() * 1000L));
/*  89 */     if (root.containsKey("sent")) {
/*  90 */       position.setDeviceTime(new Date(root.getJsonNumber("sent").longValue() * 1000L));
/*     */     }
/*     */     
/*  93 */     position.setValid(true);
/*     */     
/*  95 */     position.setLatitude(root.getJsonNumber("lat").doubleValue());
/*  96 */     position.setLongitude(root.getJsonNumber("lon").doubleValue());
/*     */     
/*  98 */     if (root.containsKey("vel")) {
/*  99 */       position.setSpeed(UnitsConverter.knotsFromKph(root.getInt("vel")));
/*     */     }
/* 101 */     if (root.containsKey("alt")) {
/* 102 */       position.setAltitude(root.getInt("alt"));
/*     */     }
/* 104 */     if (root.containsKey("cog")) {
/* 105 */       position.setCourse(root.getInt("cog"));
/*     */     }
/* 107 */     if (root.containsKey("acc")) {
/* 108 */       position.setAccuracy(root.getInt("acc"));
/*     */     }
/* 110 */     if (root.containsKey("t")) {
/* 111 */       String trigger = root.getString("t");
/* 112 */       position.set("t", trigger);
/* 113 */       Integer reportType = Integer.valueOf(-1);
/* 114 */       if (root.containsKey("rty")) {
/* 115 */         reportType = Integer.valueOf(root.getInt("rty"));
/*     */       }
/* 117 */       setEventOrAlarm(position, trigger, reportType);
/*     */     } 
/* 119 */     if (root.containsKey("batt")) {
/* 120 */       position.set("batteryLevel", Integer.valueOf(root.getInt("batt")));
/*     */     }
/* 122 */     if (root.containsKey("uext")) {
/* 123 */       position.set("power", Double.valueOf(root.getJsonNumber("uext").doubleValue()));
/*     */     }
/* 125 */     if (root.containsKey("ubatt")) {
/* 126 */       position.set("battery", Double.valueOf(root.getJsonNumber("ubatt").doubleValue()));
/*     */     }
/* 128 */     if (root.containsKey("vin")) {
/* 129 */       position.set("vin", root.getString("vin"));
/*     */     }
/* 131 */     if (root.containsKey("name")) {
/* 132 */       position.set("vin", root.getString("name"));
/*     */     }
/* 134 */     if (root.containsKey("rpm")) {
/* 135 */       position.set("rpm", Integer.valueOf(root.getInt("rpm")));
/*     */     }
/* 137 */     if (root.containsKey("ign")) {
/* 138 */       position.set("ignition", Boolean.valueOf(root.getBoolean("ign")));
/*     */     }
/* 140 */     if (root.containsKey("motion")) {
/* 141 */       position.set("motion", Boolean.valueOf(root.getBoolean("motion")));
/*     */     }
/* 143 */     if (root.containsKey("odometer")) {
/* 144 */       position.set("odometer", Double.valueOf(root.getJsonNumber("odometer").doubleValue() * 1000.0D));
/*     */     }
/* 146 */     if (root.containsKey("hmc")) {
/* 147 */       position.set("hours", Double.valueOf(root.getJsonNumber("hmc").doubleValue() / 3600.0D));
/*     */     }
/*     */     
/* 150 */     if (root.containsKey("anum")) {
/* 151 */       Integer numberOfAnalogueInputs = Integer.valueOf(root.getInt("anum"));
/* 152 */       for (Integer i = Integer.valueOf(0); i.intValue() < numberOfAnalogueInputs.intValue(); i = Integer.valueOf(i.intValue() + 1)) {
/* 153 */         Integer integer1, integer2; String indexString = String.format("%02d", new Object[] { i });
/* 154 */         if (root.containsKey("adda-" + indexString)) {
/* 155 */           position.set("adc" + (i.intValue() + 1), root.getString("adda-" + indexString));
/*     */         }
/* 157 */         if (root.containsKey("temp_c-" + indexString)) {
/* 158 */           position.set("temp" + (i.intValue() + 1), 
/* 159 */               Double.valueOf(root.getJsonNumber("temp_c-" + indexString).doubleValue()));
/*     */         }
/*     */       } 
/*     */     } 
/*     */     
/* 164 */     sendResponse(channel, HttpResponseStatus.OK);
/* 165 */     return position;
/*     */   }
/*     */   
/*     */   private void setEventOrAlarm(Position position, String trigger, Integer reportType) {
/* 169 */     switch (trigger) {
/*     */       case "9":
/* 171 */         position.set("alarm", "lowBattery");
/*     */         break;
/*     */       case "1":
/* 174 */         position.set("alarm", "powerOn");
/*     */         break;
/*     */       case "i":
/* 177 */         position.set("ignition", Boolean.valueOf(true));
/*     */         break;
/*     */       case "I":
/* 180 */         position.set("ignition", Boolean.valueOf(false));
/*     */         break;
/*     */       case "E":
/* 183 */         position.set("alarm", "powerRestored");
/*     */         break;
/*     */       case "e":
/* 186 */         position.set("alarm", "powerCut");
/*     */         break;
/*     */       case "!":
/* 189 */         position.set("alarm", "tow");
/*     */         break;
/*     */       case "s":
/* 192 */         position.set("alarm", "overspeed");
/*     */         break;
/*     */       case "h":
/* 195 */         switch (reportType.intValue()) {
/*     */           case 0:
/*     */           case 3:
/* 198 */             position.set("alarm", "hardBraking");
/*     */             break;
/*     */           case 1:
/*     */           case 4:
/* 202 */             position.set("alarm", "hardAcceleration");
/*     */             break;
/*     */         } 
/*     */ 
/*     */         
/* 207 */         position.set("alarm", "hardCornering");
/*     */         break;
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OwnTracksProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */