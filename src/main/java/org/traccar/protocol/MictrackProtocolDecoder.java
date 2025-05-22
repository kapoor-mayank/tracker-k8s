/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.text.DateFormat;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
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
/*     */ public class MictrackProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public MictrackProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
/*     */   }
/*     */   
/*     */   private Date decodeTime(String data) throws ParseException {
/*  42 */     DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
/*  43 */     dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/*  44 */     return dateFormat.parse(data);
/*     */   }
/*     */   
/*     */   private void decodeLocation(Position position, String data) throws ParseException {
/*  48 */     int index = 0;
/*  49 */     String[] values = data.split("\\+");
/*     */     
/*  51 */     position.set("sat", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */     
/*  53 */     position.setValid(true);
/*  54 */     position.setTime(decodeTime(values[index++]));
/*  55 */     position.setLatitude(Double.parseDouble(values[index++]));
/*  56 */     position.setLongitude(Double.parseDouble(values[index++]));
/*  57 */     position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[index++])));
/*  58 */     position.setCourse(Integer.parseInt(values[index++]));
/*     */     
/*  60 */     position.set("event", Integer.valueOf(Integer.parseInt(values[index++])));
/*  61 */     position.set("battery", Double.valueOf(Integer.parseInt(values[index++]) * 0.001D));
/*     */   }
/*     */   
/*     */   private void decodeCell(Network network, String data) {
/*  65 */     String[] values = data.split(",");
/*  66 */     int length = (values.length % 5 == 0) ? 5 : 4;
/*  67 */     for (int i = 0; i < values.length / length; i++) {
/*  68 */       int mnc = Integer.parseInt(values[i * length]);
/*  69 */       int cid = Integer.parseInt(values[i * length + 1]);
/*  70 */       int lac = Integer.parseInt(values[i * length + 2]);
/*  71 */       int mcc = Integer.parseInt(values[i * length + 3]);
/*  72 */       network.addCellTower(CellTower.from(mcc, mnc, lac, cid));
/*     */     } 
/*     */   }
/*     */   
/*     */   private void decodeWifi(Network network, String data) {
/*  77 */     String[] values = data.split(",");
/*  78 */     for (int i = 0; i < values.length / 2; i++) {
/*  79 */       network.addWifiAccessPoint(WifiAccessPoint.from(values[i * 2], Integer.parseInt(values[i * 2 + 1])));
/*     */     }
/*     */   }
/*     */   
/*     */   private void decodeNetwork(Position position, String data, boolean hasWifi, boolean hasCell) throws ParseException {
/*  84 */     int index = 0;
/*  85 */     String[] values = data.split("\\+");
/*     */     
/*  87 */     getLastLocation(position, decodeTime(values[index++]));
/*     */     
/*  89 */     Network network = new Network();
/*     */     
/*  91 */     if (hasWifi) {
/*  92 */       decodeWifi(network, values[index++]);
/*     */     }
/*     */     
/*  95 */     if (hasCell) {
/*  96 */       decodeCell(network, values[index++]);
/*     */     }
/*     */     
/*  99 */     position.setNetwork(network);
/*     */     
/* 101 */     position.set("event", Integer.valueOf(Integer.parseInt(values[index++])));
/* 102 */     position.set("battery", Double.valueOf(Integer.parseInt(values[index++]) * 0.001D));
/*     */   }
/*     */   
/*     */   private void decodeStatus(Position position, String data) throws ParseException {
/* 106 */     int index = 0;
/* 107 */     String[] values = data.split("\\+");
/*     */     
/* 109 */     position.set("sat", Integer.valueOf(Integer.parseInt(values[index++])));
/*     */     
/* 111 */     getLastLocation(position, decodeTime(values[index++]));
/*     */     
/* 113 */     index += 4;
/*     */     
/* 115 */     position.set("event", Integer.valueOf(Integer.parseInt(values[index++])));
/* 116 */     position.set("battery", Double.valueOf(Integer.parseInt(values[index++]) * 0.001D));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 123 */     String[] fragments = ((String)msg).split(";");
/*     */     
/* 125 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { fragments[2] });
/* 126 */     if (deviceSession == null) {
/* 127 */       return null;
/*     */     }
/*     */     
/* 130 */     Position position = new Position(getProtocolName());
/* 131 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 133 */     switch (fragments[3]) {
/*     */       case "R0":
/* 135 */         decodeLocation(position, fragments[4]);
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
/*     */ 
/*     */         
/* 155 */         return position;case "R1": decodeNetwork(position, fragments[4], true, false); return position;case "R2": case "R3": decodeNetwork(position, fragments[4], false, true); return position;case "R12": case "R13": decodeNetwork(position, fragments[4], true, true); return position;case "RH": decodeStatus(position, fragments[4]); return position;
/*     */     } 
/*     */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MictrackProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */