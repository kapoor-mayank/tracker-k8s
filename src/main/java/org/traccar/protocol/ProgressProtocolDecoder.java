/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ProgressProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private long lastIndex;
/*     */   private long newIndex;
/*     */   public static final int MSG_NULL = 0;
/*     */   public static final int MSG_IDENT = 1;
/*     */   public static final int MSG_IDENT_FULL = 2;
/*     */   public static final int MSG_POINT = 10;
/*     */   public static final int MSG_LOG_SYNC = 100;
/*     */   public static final int MSG_LOGMSG = 101;
/*     */   public static final int MSG_TEXT = 102;
/*     */   public static final int MSG_ALARM = 200;
/*     */   public static final int MSG_ALARM_RECIEVED = 201;
/*     */   
/*     */   public ProgressProtocolDecoder(Protocol protocol) {
/*  41 */     super(protocol);
/*     */   }
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
/*     */   private void requestArchive(Channel channel) {
/*  55 */     if (this.lastIndex == 0L) {
/*  56 */       this.lastIndex = this.newIndex;
/*  57 */     } else if (this.newIndex > this.lastIndex) {
/*  58 */       ByteBuf request = Unpooled.buffer(12);
/*  59 */       request.writeShortLE(100);
/*  60 */       request.writeShortLE(4);
/*  61 */       request.writeIntLE((int)this.lastIndex);
/*  62 */       request.writeIntLE(0);
/*  63 */       channel.writeAndFlush(new NetworkMessage(request, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  71 */     ByteBuf buf = (ByteBuf)msg;
/*  72 */     int type = buf.readUnsignedShortLE();
/*  73 */     buf.readUnsignedShortLE();
/*     */     
/*  75 */     if (type == 1 || type == 2) {
/*     */       
/*  77 */       buf.readUnsignedIntLE();
/*  78 */       int length = buf.readUnsignedShortLE();
/*  79 */       buf.skipBytes(length);
/*  80 */       length = buf.readUnsignedShortLE();
/*  81 */       buf.skipBytes(length);
/*  82 */       length = buf.readUnsignedShortLE();
/*  83 */       String imei = buf.readSlice(length).toString(StandardCharsets.US_ASCII);
/*  84 */       getDeviceSession(channel, remoteAddress, new String[] { imei });
/*     */     }
/*  86 */     else if (type == 10 || type == 200 || type == 101) {
/*     */       
/*  88 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  89 */       if (deviceSession == null) {
/*  90 */         return null;
/*     */       }
/*     */       
/*  93 */       List<Position> positions = new LinkedList<>();
/*     */       
/*  95 */       int recordCount = 1;
/*  96 */       if (type == 101) {
/*  97 */         recordCount = buf.readUnsignedShortLE();
/*     */       }
/*     */       
/* 100 */       for (int j = 0; j < recordCount; j++) {
/* 101 */         Position position = new Position(getProtocolName());
/* 102 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 104 */         if (type == 101) {
/* 105 */           position.set("archive", Boolean.valueOf(true));
/* 106 */           int subtype = buf.readUnsignedShortLE();
/* 107 */           if (subtype == 200) {
/* 108 */             position.set("alarm", "general");
/*     */           }
/* 110 */           if (buf.readUnsignedShortLE() > buf.readableBytes()) {
/* 111 */             this.lastIndex++;
/*     */             break;
/*     */           } 
/* 114 */           this.lastIndex = buf.readUnsignedIntLE();
/* 115 */           position.set("index", Long.valueOf(this.lastIndex));
/*     */         } else {
/* 117 */           this.newIndex = buf.readUnsignedIntLE();
/*     */         } 
/*     */         
/* 120 */         position.setTime(new Date(buf.readUnsignedIntLE() * 1000L));
/* 121 */         position.setLatitude(buf.readIntLE() * 180.0D / 2.147483647E9D);
/* 122 */         position.setLongitude(buf.readIntLE() * 180.0D / 2.147483647E9D);
/* 123 */         position.setSpeed(buf.readUnsignedIntLE() * 0.01D);
/* 124 */         position.setCourse(buf.readUnsignedShortLE() * 0.01D);
/* 125 */         position.setAltitude(buf.readUnsignedShortLE() * 0.01D);
/*     */         
/* 127 */         int satellites = buf.readUnsignedByte();
/* 128 */         position.setValid((satellites >= 3));
/* 129 */         position.set("sat", Integer.valueOf(satellites));
/*     */         
/* 131 */         position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/* 132 */         position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*     */         
/* 134 */         long extraFlags = buf.readLongLE();
/*     */         
/* 136 */         if (BitUtil.check(extraFlags, 0)) {
/* 137 */           int count = buf.readUnsignedShortLE();
/* 138 */           for (int i = 1; i <= count; i++) {
/* 139 */             position.set("adc" + i, Integer.valueOf(buf.readUnsignedShortLE()));
/*     */           }
/*     */         } 
/*     */         
/* 143 */         if (BitUtil.check(extraFlags, 1)) {
/* 144 */           int size = buf.readUnsignedShortLE();
/* 145 */           position.set("can", buf.toString(buf.readerIndex(), size, StandardCharsets.US_ASCII));
/* 146 */           buf.skipBytes(size);
/*     */         } 
/*     */         
/* 149 */         if (BitUtil.check(extraFlags, 2)) {
/* 150 */           position.set("passenger", ByteBufUtil.hexDump(buf.readSlice(buf.readUnsignedShortLE())));
/*     */         }
/*     */         
/* 153 */         if (type == 200) {
/* 154 */           position.set("alarm", Boolean.valueOf(true));
/* 155 */           byte[] response = { -55, 0, 0, 0, 0, 0, 0, 0 };
/* 156 */           channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(response), remoteAddress));
/*     */         } 
/*     */         
/* 159 */         buf.readUnsignedIntLE();
/*     */         
/* 161 */         positions.add(position);
/*     */       } 
/*     */       
/* 164 */       requestArchive(channel);
/*     */       
/* 166 */       return positions;
/*     */     } 
/*     */     
/* 169 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ProgressProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */