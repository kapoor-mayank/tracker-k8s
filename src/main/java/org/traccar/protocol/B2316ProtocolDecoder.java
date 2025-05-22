/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.io.StringReader;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import javax.json.Json;
/*     */ import javax.json.JsonArray;
/*     */ import javax.json.JsonObject;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
/*     */ import org.traccar.model.Position;
/*     */ import org.traccar.model.WifiAccessPoint;
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
/*     */ public class B2316ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public B2316ProtocolDecoder(Protocol protocol) {
/*  39 */     super(protocol);
/*     */   }
/*     */   
/*     */   private String decodeAlarm(int value) {
/*  43 */     switch (value) {
/*     */       case 1:
/*  45 */         return "lowBattery";
/*     */       case 2:
/*  47 */         return "sos";
/*     */       case 3:
/*  49 */         return "powerOff";
/*     */       case 4:
/*  51 */         return "removing";
/*     */     } 
/*  53 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private Integer decodeBattery(int value) {
/*  58 */     switch (value) {
/*     */       case 0:
/*  60 */         return Integer.valueOf(10);
/*     */       case 1:
/*  62 */         return Integer.valueOf(30);
/*     */       case 2:
/*  64 */         return Integer.valueOf(60);
/*     */       case 3:
/*  66 */         return Integer.valueOf(80);
/*     */       case 4:
/*  68 */         return Integer.valueOf(100);
/*     */     } 
/*  70 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  78 */     JsonObject root = Json.createReader(new StringReader((String)msg)).readObject();
/*     */     
/*  80 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { root.getString("imei") });
/*  81 */     if (deviceSession == null) {
/*  82 */       return null;
/*     */     }
/*     */     
/*  85 */     List<Position> positions = new LinkedList<>();
/*  86 */     JsonArray data = root.getJsonArray("data");
/*  87 */     for (int i = 0; i < data.size(); i++) {
/*     */       
/*  89 */       Position position = new Position(getProtocolName());
/*  90 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  92 */       Network network = new Network();
/*     */       
/*  94 */       JsonObject item = data.getJsonObject(i);
/*  95 */       Date time = new Date(item.getJsonNumber("tm").longValue() * 1000L);
/*     */       
/*  97 */       if (item.containsKey("gp")) {
/*  98 */         String[] coordinates = item.getString("gp").split(",");
/*  99 */         position.setLongitude(Double.parseDouble(coordinates[0]));
/* 100 */         position.setLatitude(Double.parseDouble(coordinates[1]));
/* 101 */         position.setValid(true);
/* 102 */         position.setTime(time);
/*     */       } else {
/* 104 */         getLastLocation(position, time);
/*     */       } 
/*     */       
/* 107 */       if (item.containsKey("ci")) {
/* 108 */         String[] cell = item.getString("ci").split(",");
/* 109 */         network.addCellTower(CellTower.from(
/* 110 */               Integer.parseInt(cell[0]), Integer.parseInt(cell[1]), 
/* 111 */               Integer.parseInt(cell[2]), Integer.parseInt(cell[3]), 
/* 112 */               Integer.parseInt(cell[4])));
/*     */       } 
/*     */       
/* 115 */       if (item.containsKey("wi")) {
/* 116 */         String[] points = item.getString("wi").split(";");
/* 117 */         for (String point : points) {
/* 118 */           String[] values = point.split(",");
/* 119 */           network.addWifiAccessPoint(WifiAccessPoint.from(values[0]
/* 120 */                 .replaceAll("(..)", "$1:"), Integer.parseInt(values[1])));
/*     */         } 
/*     */       } 
/*     */       
/* 124 */       if (item.containsKey("wn")) {
/* 125 */         position.set("alarm", decodeAlarm(item.getInt("wn")));
/*     */       }
/* 127 */       if (item.containsKey("ic")) {
/* 128 */         position.set("iccid", item.getString("ic"));
/*     */       }
/* 130 */       if (item.containsKey("ve")) {
/* 131 */         position.set("versionFw", item.getString("ve"));
/*     */       }
/* 133 */       if (item.containsKey("te")) {
/* 134 */         String[] temperatures = item.getString("te").split(",");
/* 135 */         for (int j = 0; j < temperatures.length; j++) {
/* 136 */           position.set("temp" + (j + 1), Double.valueOf(Integer.parseInt(temperatures[j]) * 0.1D));
/*     */         }
/*     */       } 
/* 139 */       if (item.containsKey("st")) {
/* 140 */         position.set("steps", Integer.valueOf(item.getInt("st")));
/*     */       }
/* 142 */       if (item.containsKey("ba")) {
/* 143 */         position.set("batteryLevel", decodeBattery(item.getInt("ba")));
/*     */       }
/* 145 */       if (item.containsKey("sn")) {
/* 146 */         position.set("rssi", Integer.valueOf(item.getInt("sn")));
/*     */       }
/* 148 */       if (item.containsKey("hr")) {
/* 149 */         position.set("heartRate", Integer.valueOf(item.getInt("hr")));
/*     */       }
/*     */       
/* 152 */       if (network.getCellTowers() != null || network.getWifiAccessPoints() != null) {
/* 153 */         position.setNetwork(network);
/*     */       }
/*     */       
/* 156 */       positions.add(position);
/*     */     } 
/*     */     
/* 159 */     return positions.isEmpty() ? null : positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\B2316ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */