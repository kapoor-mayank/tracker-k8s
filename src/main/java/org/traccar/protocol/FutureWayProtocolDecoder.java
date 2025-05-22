/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DataConverter;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
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
/*     */ public class FutureWayProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public FutureWayProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
/*     */   }
/*     */   
/*  41 */   private static final Pattern PATTERN_GPS = (new PatternBuilder())
/*  42 */     .text("GPS:")
/*  43 */     .expression("([AV]),")
/*  44 */     .number("(dd)(dd)(dd)")
/*  45 */     .number("(dd)(dd)(dd),")
/*  46 */     .groupBegin()
/*  47 */     .number("(dd)(dd.d+)([NS]),")
/*  48 */     .number("(ddd)(dd.d+)([EW]),")
/*  49 */     .or()
/*  50 */     .number("(d+.d+)([NS]),")
/*  51 */     .number("(d+.d+)([EW]),")
/*  52 */     .groupEnd()
/*  53 */     .number("(d+.d+),")
/*  54 */     .number("(d+.d+)")
/*  55 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  61 */     String sentence = (String)msg;
/*     */     
/*  63 */     ByteBuf header = Unpooled.wrappedBuffer(DataConverter.parseHex(sentence.substring(0, 16)));
/*  64 */     sentence = sentence.substring(16, sentence.length() - 4);
/*     */     
/*  66 */     header.readUnsignedByte();
/*  67 */     header.readUnsignedInt();
/*  68 */     int type = header.readUnsignedByte();
/*  69 */     header.readUnsignedShort();
/*     */     
/*  71 */     if (type == 32) {
/*     */       
/*  73 */       getDeviceSession(channel, remoteAddress, new String[] { sentence.split(",")[1].substring(5) });
/*     */     }
/*  75 */     else if (type == 160) {
/*     */       
/*  77 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  78 */       if (deviceSession == null) {
/*  79 */         return null;
/*     */       }
/*     */       
/*  82 */       Position position = new Position(getProtocolName());
/*  83 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  85 */       Network network = new Network();
/*     */       
/*  87 */       for (String line : sentence.split("\r\n")) {
/*     */         
/*  89 */         if (line.startsWith("GPS")) {
/*     */           
/*  91 */           Parser parser = new Parser(PATTERN_GPS, line);
/*  92 */           if (!parser.matches()) {
/*  93 */             return null;
/*     */           }
/*     */           
/*  96 */           position.setValid(parser.next().equals("A"));
/*  97 */           position.setTime(parser.nextDateTime());
/*     */           
/*  99 */           if (parser.hasNext(6)) {
/* 100 */             position.setLatitude(parser.nextCoordinate());
/* 101 */             position.setLongitude(parser.nextCoordinate());
/*     */           } 
/*     */           
/* 104 */           if (parser.hasNext(4)) {
/* 105 */             position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 106 */             position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/*     */           } 
/*     */           
/* 109 */           position.setSpeed(parser.nextDouble().doubleValue());
/* 110 */           position.setCourse(parser.nextDouble().doubleValue());
/*     */         }
/* 112 */         else if (line.startsWith("WIFI")) {
/*     */           
/* 114 */           if (line.contains(",")) {
/* 115 */             for (String item : line.substring(line.indexOf(',') + 1).split("&")) {
/* 116 */               String[] values = item.split("\\|");
/* 117 */               network.addWifiAccessPoint(
/* 118 */                   WifiAccessPoint.from(values[1].replace('-', ':'), Integer.parseInt(values[2])));
/*     */             }
/*     */           
/*     */           }
/* 122 */         } else if (line.startsWith("LBS")) {
/*     */           int lac, cid;
/* 124 */           String[] values = line.substring("LBS:".length()).split(",");
/*     */           
/* 126 */           if (Integer.parseInt(values[2]) > 65535) {
/* 127 */             cid = Integer.parseInt(values[2]);
/* 128 */             lac = Integer.parseInt(values[3]);
/*     */           } else {
/* 130 */             lac = Integer.parseInt(values[2]);
/* 131 */             cid = Integer.parseInt(values[3]);
/*     */           } 
/* 133 */           network.addCellTower(CellTower.from(
/* 134 */                 Integer.parseInt(values[0]), 
/* 135 */                 Integer.parseInt(values[1]), lac, cid));
/*     */         } 
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/* 141 */       if (!network.getCellTowers().isEmpty() || !network.getWifiAccessPoints().isEmpty()) {
/* 142 */         position.setNetwork(network);
/*     */       }
/*     */       
/* 145 */       if (position.getFixTime() == null) {
/* 146 */         getLastLocation(position, null);
/*     */       }
/*     */       
/* 149 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 153 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FutureWayProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */