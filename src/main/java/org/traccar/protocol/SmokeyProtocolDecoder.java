/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.time.Instant;
/*     */ import java.time.temporal.ChronoUnit;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DateBuilder;
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
/*     */ public class SmokeyProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_DATE_RECORD = 0;
/*     */   public static final int MSG_DATE_RECORD_ACK = 1;
/*     */   
/*     */   public SmokeyProtocolDecoder(Protocol protocol) {
/*  40 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void sendResponse(Channel channel, SocketAddress remoteAddress, ByteBuf id, int index, int report) {
/*  49 */     if (channel != null) {
/*  50 */       ByteBuf response = Unpooled.buffer();
/*  51 */       response.writeBytes("SM".getBytes(StandardCharsets.US_ASCII));
/*  52 */       response.writeByte(3);
/*  53 */       response.writeByte(1);
/*  54 */       response.writeBytes(id);
/*  55 */       response.writeInt(
/*  56 */           (int)ChronoUnit.SECONDS.between(Instant.parse("2000-01-01T00:00:00.00Z"), Instant.now()));
/*  57 */       response.writeByte(index);
/*  58 */       response.writeByte(report - 512);
/*     */       
/*  60 */       short checksum = -2656;
/*  61 */       for (int i = 0; i < response.readableBytes(); i += 2) {
/*  62 */         checksum = (short)(checksum ^ response.getShortLE(i));
/*     */       }
/*  64 */       response.writeShort(checksum);
/*     */       
/*  66 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  74 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  76 */     buf.skipBytes(2);
/*  77 */     buf.readUnsignedByte();
/*     */     
/*  79 */     int type = buf.readUnsignedByte();
/*     */     
/*  81 */     ByteBuf id = buf.readSlice(8);
/*  82 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { ByteBufUtil.hexDump(id) });
/*  83 */     if (deviceSession == null) {
/*  84 */       return null;
/*     */     }
/*     */     
/*  87 */     if (type == 0) {
/*     */       
/*  89 */       Position position = new Position(getProtocolName());
/*  90 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  92 */       position.set("versionFw", Integer.valueOf(buf.readUnsignedShort()));
/*     */       
/*  94 */       int status = buf.readUnsignedShort();
/*  95 */       position.set("status", Integer.valueOf(status));
/*     */ 
/*     */       
/*  98 */       DateBuilder dateBuilder = (new DateBuilder()).setDate(2000, 1, 1).addSeconds(buf.readUnsignedInt());
/*     */       
/* 100 */       getLastLocation(position, dateBuilder.getDate());
/*     */       
/* 102 */       int index = buf.readUnsignedByte();
/* 103 */       position.set("index", Integer.valueOf(index));
/*     */       
/* 105 */       int report = buf.readUnsignedShort();
/*     */       
/* 107 */       buf.readUnsignedShort();
/*     */       
/* 109 */       position.set("battery", Integer.valueOf(buf.readUnsignedShort()));
/*     */       
/* 111 */       Network network = new Network();
/*     */       
/* 113 */       if (report != 515) {
/*     */         
/* 115 */         int count = 1;
/* 116 */         if (report != 512) {
/* 117 */           count = buf.readUnsignedByte();
/*     */         }
/*     */         
/* 120 */         for (int i = 0; i < count; i++) {
/* 121 */           int mcc = buf.readUnsignedShort();
/* 122 */           int mnc = buf.readUnsignedShort();
/* 123 */           int lac = buf.readUnsignedShort();
/* 124 */           int cid = buf.readUnsignedShort();
/* 125 */           if (i == 0) {
/* 126 */             buf.readByte();
/*     */           }
/* 128 */           int rssi = buf.readByte();
/* 129 */           network.addCellTower(CellTower.from(mcc, mnc, lac, cid, rssi));
/*     */         } 
/*     */       } 
/*     */ 
/*     */       
/* 134 */       if (report == 514 || report == 515) {
/*     */         
/* 136 */         int count = buf.readUnsignedByte();
/*     */         
/* 138 */         for (int i = 0; i < count; i++) {
/* 139 */           buf.readerIndex(buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)0) + 1);
/*     */           
/* 141 */           String mac = String.format("%02x:%02x:%02x:%02x:%02x:%02x", new Object[] {
/* 142 */                 Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte()), 
/* 143 */                 Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte()), Short.valueOf(buf.readUnsignedByte())
/*     */               });
/* 145 */           network.addWifiAccessPoint(WifiAccessPoint.from(mac, buf.readByte()));
/*     */         } 
/*     */       } 
/*     */ 
/*     */       
/* 150 */       position.setNetwork(network);
/*     */       
/* 152 */       sendResponse(channel, remoteAddress, id, index, report);
/*     */       
/* 154 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 158 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SmokeyProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */