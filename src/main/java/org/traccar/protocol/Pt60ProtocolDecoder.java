/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
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
/*     */ public class Pt60ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_G_TRACK = 6;
/*     */   public static final int MSG_G_STEP_COUNT = 13;
/*     */   public static final int MSG_G_HEART_RATE = 14;
/*     */   public static final int MSG_B_POSITION = 1;
/*     */   
/*     */   public Pt60ProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  46 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  47 */     .expression("@(.)#@[,|]")
/*  48 */     .number("V?dd[,|]")
/*  49 */     .number("(d+)[,|]")
/*  50 */     .number("(d+)[,|]")
/*  51 */     .number("d+[,|]")
/*  52 */     .groupBegin()
/*  53 */     .expression("[^,|]+[,|]").optional()
/*  54 */     .number("[01][,|]")
/*  55 */     .number("d+[,|]")
/*  56 */     .groupEnd("?")
/*  57 */     .number("(dddd)(dd)(dd)")
/*  58 */     .number("(dd)(dd)(dd)[,|]")
/*  59 */     .expression("(.*)")
/*  60 */     .expression("[,|]")
/*  61 */     .compile();
/*     */   
/*     */   private void sendResponse(Channel channel, SocketAddress remoteAddress, String format, int type, String imei) {
/*  64 */     if (channel != null) {
/*     */       
/*  66 */       String message, time = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
/*  67 */       if (format.equals("G")) {
/*  68 */         message = String.format("@G#@,V01,38,%s,@R#@", new Object[] { time });
/*     */       } else {
/*  70 */         message = String.format("@B#@|01|%03d|%s|0|%s|@E#@", new Object[] { Integer.valueOf(type + 1), imei, time });
/*     */       } 
/*  72 */       channel.writeAndFlush(new NetworkMessage(message, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  80 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  81 */     if (!parser.matches()) {
/*  82 */       return null;
/*     */     }
/*     */     
/*  85 */     String format = parser.next();
/*  86 */     int type = parser.nextInt().intValue();
/*  87 */     String imei = parser.next();
/*     */     
/*  89 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  90 */     if (deviceSession == null) {
/*  91 */       return null;
/*     */     }
/*     */     
/*  94 */     sendResponse(channel, remoteAddress, format, type, imei);
/*     */     
/*  96 */     if (format.equals("G")) {
/*     */       
/*  98 */       if (type != 6 && type != 13 && type != 14) {
/*  99 */         return null;
/*     */       }
/*     */       
/* 102 */       Position position1 = new Position(getProtocolName());
/* 103 */       position1.setDeviceId(deviceSession.getDeviceId());
/* 104 */       position1.setDeviceTime(parser.nextDateTime());
/*     */       
/* 106 */       String[] arrayOfString = parser.next().split(",");
/*     */       
/* 108 */       if (type == 6) {
/*     */         
/* 110 */         position1.setValid(true);
/* 111 */         position1.setFixTime(position1.getDeviceTime());
/*     */         
/* 113 */         String[] coordinates = arrayOfString[0].split(";");
/* 114 */         position1.setLatitude(Double.parseDouble(coordinates[0]));
/* 115 */         position1.setLongitude(Double.parseDouble(coordinates[1]));
/*     */       }
/*     */       else {
/*     */         
/* 119 */         getLastLocation(position1, position1.getDeviceTime());
/*     */         
/* 121 */         switch (type) {
/*     */           case 13:
/* 123 */             position1.set("steps", Integer.valueOf(Integer.parseInt(arrayOfString[0])));
/*     */             break;
/*     */           case 14:
/* 126 */             position1.set("heartRate", Integer.valueOf(Integer.parseInt(arrayOfString[0])));
/* 127 */             position1.set("battery", Integer.valueOf(Integer.parseInt(arrayOfString[1])));
/*     */             break;
/*     */         } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*     */       } 
/* 135 */       return position1;
/*     */     } 
/*     */ 
/*     */     
/* 139 */     if (type != 1) {
/* 140 */       return null;
/*     */     }
/*     */     
/* 143 */     Position position = new Position(getProtocolName());
/* 144 */     position.setDeviceId(deviceSession.getDeviceId());
/* 145 */     position.setDeviceTime(parser.nextDateTime());
/*     */     
/* 147 */     String[] values = parser.next().split("\\|");
/*     */     
/* 149 */     if (Integer.parseInt(values[values.length - 1]) == 2) {
/*     */       
/* 151 */       getLastLocation(position, position.getDeviceTime());
/*     */       
/* 153 */       Network network = new Network();
/*     */       
/* 155 */       for (int i = 0; i < values.length - 1; i++) {
/* 156 */         String[] cellValues = values[i].split(",");
/* 157 */         CellTower tower = new CellTower();
/* 158 */         tower.setCellId(Long.valueOf(Long.parseLong(cellValues[0])));
/* 159 */         tower.setLocationAreaCode(Integer.valueOf(Integer.parseInt(cellValues[1])));
/* 160 */         tower.setMobileNetworkCode(Integer.valueOf(Integer.parseInt(cellValues[2])));
/* 161 */         tower.setMobileCountryCode(Integer.valueOf(Integer.parseInt(cellValues[3])));
/* 162 */         tower.setSignalStrength(Integer.valueOf(Integer.parseInt(cellValues[4])));
/* 163 */         network.addCellTower(tower);
/*     */       } 
/*     */       
/* 166 */       position.setNetwork(network);
/*     */     
/*     */     }
/*     */     else {
/*     */       
/* 171 */       position.setValid(true);
/* 172 */       position.setFixTime(position.getDeviceTime());
/*     */       
/* 174 */       position.setLatitude(Double.parseDouble(values[0]));
/* 175 */       position.setLongitude(Double.parseDouble(values[1]));
/*     */     } 
/*     */ 
/*     */     
/* 179 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Pt60ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */