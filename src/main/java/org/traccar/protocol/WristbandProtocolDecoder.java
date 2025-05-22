/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
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
/*     */ public class WristbandProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public WristbandProtocolDecoder(Protocol protocol) {
/*  45 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel, String imei, String version, int type, String data) {
/*  51 */     if (channel != null) {
/*  52 */       String sentence = String.format("YX%s|%s|0|{F%02d#%s}\r\n", new Object[] { imei, version, Integer.valueOf(type), data });
/*  53 */       ByteBuf response = Unpooled.buffer();
/*  54 */       if (type != 91) {
/*  55 */         response.writeBytes(new byte[] { 0, 1, 2 });
/*  56 */         response.writeShort(sentence.length());
/*     */       } 
/*  58 */       response.writeCharSequence(sentence, StandardCharsets.US_ASCII);
/*  59 */       if (type != 91) {
/*  60 */         response.writeBytes(new byte[] { -1, -2, -4 });
/*     */       }
/*  62 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */   
/*  66 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  67 */     .expression("..")
/*  68 */     .number("(d+)|")
/*  69 */     .number("([vV]d+.d+)|")
/*  70 */     .number("d+|")
/*  71 */     .text("{")
/*  72 */     .number("F(d+)")
/*  73 */     .groupBegin()
/*  74 */     .text("#")
/*  75 */     .expression("(.*)")
/*  76 */     .groupEnd("?")
/*  77 */     .text("}")
/*  78 */     .text("\r\n")
/*  79 */     .compile();
/*     */ 
/*     */   
/*     */   private Position decodePosition(DeviceSession deviceSession, String sentence) throws ParseException {
/*  83 */     Position position = new Position(getProtocolName());
/*  84 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  86 */     String[] values = sentence.split(",");
/*     */     
/*  88 */     position.setValid(true);
/*  89 */     position.setLongitude(Double.parseDouble(values[0]));
/*  90 */     position.setLatitude(Double.parseDouble(values[1]));
/*  91 */     position.setTime((new SimpleDateFormat("yyyyMMddHHmm")).parse(values[2]));
/*  92 */     position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[3])));
/*     */     
/*  94 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeStatus(DeviceSession deviceSession, String sentence) {
/*  99 */     Position position = new Position(getProtocolName());
/* 100 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 102 */     getLastLocation(position, null);
/*     */     
/* 104 */     position.set("batteryLevel", Integer.valueOf(Integer.parseInt(sentence.split(",")[0])));
/*     */     
/* 106 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Position decodeNetwork(DeviceSession deviceSession, String sentence, boolean wifi) {
/* 111 */     Position position = new Position(getProtocolName());
/* 112 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 114 */     getLastLocation(position, null);
/*     */     
/* 116 */     Network network = new Network();
/* 117 */     String[] fragments = sentence.split("\\|");
/*     */     
/* 119 */     if (wifi) {
/* 120 */       for (String item : fragments[0].split("_")) {
/* 121 */         String[] values = item.split(",");
/* 122 */         network.addWifiAccessPoint(WifiAccessPoint.from(values[0], Integer.parseInt(values[1])));
/*     */       } 
/*     */     }
/*     */     
/* 126 */     for (String item : fragments[wifi ? 1 : 0].split(":")) {
/* 127 */       String[] values = item.split(",");
/* 128 */       int lac = Integer.parseInt(values[0]);
/* 129 */       int mnc = Integer.parseInt(values[1]);
/* 130 */       int mcc = Integer.parseInt(values[2]);
/* 131 */       int cid = Integer.parseInt(values[3]);
/* 132 */       int rssi = Integer.parseInt(values[4]);
/* 133 */       network.addCellTower(CellTower.from(mcc, mnc, lac, cid, rssi));
/*     */     } 
/*     */     
/* 136 */     position.setNetwork(network);
/*     */     
/* 138 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private List<Position> decodeMessage(Channel channel, SocketAddress remoteAddress, String sentence) throws ParseException {
/*     */     String time;
/* 144 */     Parser parser = new Parser(PATTERN, sentence);
/* 145 */     if (!parser.matches()) {
/* 146 */       return null;
/*     */     }
/*     */     
/* 149 */     String imei = parser.next();
/* 150 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 151 */     if (deviceSession == null) {
/* 152 */       return null;
/*     */     }
/*     */     
/* 155 */     String version = parser.next();
/* 156 */     int type = parser.nextInt().intValue();
/*     */     
/* 158 */     List<Position> positions = new LinkedList<>();
/* 159 */     String data = parser.next();
/*     */     
/* 161 */     switch (type) {
/*     */       case 90:
/* 163 */         sendResponse(channel, imei, version, type, getServer(channel, ','));
/*     */         break;
/*     */       case 91:
/* 166 */         time = (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date());
/* 167 */         sendResponse(channel, imei, version, type, time + "|" + getServer(channel, ','));
/*     */         break;
/*     */       case 1:
/* 170 */         positions.add(decodeStatus(deviceSession, data));
/* 171 */         sendResponse(channel, imei, version, type, data.split(",")[1]);
/*     */         break;
/*     */       case 2:
/* 174 */         for (String fragment : data.split("\\|")) {
/* 175 */           positions.add(decodePosition(deviceSession, fragment));
/*     */         }
/*     */         break;
/*     */       case 3:
/*     */       case 4:
/* 180 */         positions.add(decodeNetwork(deviceSession, data, (type == 3)));
/*     */         break;
/*     */       case 64:
/* 183 */         sendResponse(channel, imei, version, type, data);
/*     */         break;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 189 */     return positions.isEmpty() ? null : positions;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 196 */     ByteBuf buf = (ByteBuf)msg;
/* 197 */     buf.skipBytes(3);
/* 198 */     buf.readUnsignedShort();
/*     */     
/* 200 */     String sentence = buf.toString(buf.readerIndex(), buf.readableBytes() - 3, StandardCharsets.US_ASCII);
/*     */     
/* 202 */     buf.skipBytes(3);
/*     */     
/* 204 */     return decodeMessage(channel, remoteAddress, sentence);
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WristbandProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */