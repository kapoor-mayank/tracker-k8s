/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.TimeZone;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.UnitsConverter;
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
/*     */ public class S168ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public S168ProtocolDecoder(Protocol protocol) {
/*  36 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  43 */     String sentence = (String)msg;
/*  44 */     String[] values = sentence.split("#");
/*     */     
/*  46 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { values[1] });
/*  47 */     if (deviceSession == null) {
/*  48 */       return null;
/*     */     }
/*     */     
/*  51 */     Position position = new Position(getProtocolName());
/*  52 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  54 */     Network network = new Network();
/*     */     
/*  56 */     String content = values[4];
/*  57 */     String[] fragments = content.split(";");
/*  58 */     for (String fragment : fragments) {
/*  59 */       if (!fragment.isEmpty()) {
/*     */         DateFormat dateFormat;
/*     */ 
/*     */         
/*  63 */         int cellCount, mcc, mnc, i, wifiCount, j, dataIndex = fragment.indexOf(':');
/*  64 */         String type = fragment.substring(0, dataIndex);
/*  65 */         values = fragment.substring(dataIndex + 1).split(",");
/*  66 */         int index = 0;
/*     */         
/*  68 */         switch (type) {
/*     */           case "GDATA":
/*  70 */             position.setValid(values[index++].equals("A"));
/*  71 */             position.set("sat", Integer.valueOf(Integer.parseInt(values[index++])));
/*  72 */             dateFormat = new SimpleDateFormat("yyMMddHHmmss");
/*  73 */             dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/*  74 */             position.setTime(dateFormat.parse(values[index++]));
/*  75 */             position.setLatitude(Double.parseDouble(values[index++]));
/*  76 */             position.setLongitude(Double.parseDouble(values[index++]));
/*  77 */             position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[index++])));
/*  78 */             position.setCourse(Integer.parseInt(values[index++]));
/*  79 */             position.setAltitude(Integer.parseInt(values[index++]));
/*     */             break;
/*     */           case "CELL":
/*  82 */             cellCount = Integer.parseInt(values[index++]);
/*  83 */             mcc = Integer.parseInt(values[index++], 16);
/*  84 */             mnc = Integer.parseInt(values[index++], 16);
/*  85 */             for (i = 0; i < cellCount; i++) {
/*  86 */               network.addCellTower(CellTower.from(mcc, mnc, 
/*  87 */                     Integer.parseInt(values[index++], 16), Integer.parseInt(values[index++], 16), 
/*  88 */                     Integer.parseInt(values[index++], 16)));
/*     */             }
/*     */             break;
/*     */           case "WIFI":
/*  92 */             wifiCount = Integer.parseInt(values[index++]);
/*  93 */             for (j = 0; j < wifiCount; j++) {
/*  94 */               network.addWifiAccessPoint(WifiAccessPoint.from(values[index++]
/*  95 */                     .replace('-', ':'), Integer.parseInt(values[index++])));
/*     */             }
/*     */             break;
/*     */           case "STATUS":
/*  99 */             position.set("batteryLevel", Integer.valueOf(Integer.parseInt(values[index++])));
/* 100 */             position.set("rssi", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */             break;
/*     */         } 
/*     */ 
/*     */       
/*     */       } 
/*     */     } 
/* 107 */     if (network.getCellTowers() != null || network.getWifiAccessPoints() != null) {
/* 108 */       position.setNetwork(network);
/*     */     }
/* 110 */     if (!position.getAttributes().containsKey("sat")) {
/* 111 */       getLastLocation(position, null);
/*     */     }
/*     */     
/* 114 */     if (position.getNetwork() != null || !position.getAttributes().isEmpty()) {
/* 115 */       return position;
/*     */     }
/* 117 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\S168ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */