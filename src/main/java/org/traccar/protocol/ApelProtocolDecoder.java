/*     */ package org.traccar.protocol;
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.Checksum;
import org.traccar.model.Position;
/*     */ 
/*     */ public class ApelProtocolDecoder extends BaseProtocolDecoder {
/*     */   private long lastIndex;
/*     */   private long newIndex;
/*     */   public static final short MSG_NULL = 0;
/*     */   public static final short MSG_REQUEST_TRACKER_ID = 10;
/*     */   public static final short MSG_TRACKER_ID = 11;
/*     */   public static final short MSG_TRACKER_ID_EXT = 12;
/*     */   public static final short MSG_DISCONNECT = 20;
/*     */   public static final short MSG_REQUEST_PASSWORD = 30;
/*     */   public static final short MSG_PASSWORD = 31;
/*     */   public static final short MSG_REQUEST_STATE_FULL_INFO = 90;
/*     */   public static final short MSG_STATE_FULL_INFO_T104 = 92;
/*     */   public static final short MSG_REQUEST_CURRENT_GPS_DATA = 100;
/*     */   public static final short MSG_CURRENT_GPS_DATA = 101;
/*     */   public static final short MSG_REQUEST_SENSORS_STATE = 110;
/*     */   public static final short MSG_SENSORS_STATE = 111;
/*     */   public static final short MSG_SENSORS_STATE_T100 = 112;
/*     */   public static final short MSG_SENSORS_STATE_T100_4 = 113;
/*     */   public static final short MSG_REQUEST_LAST_LOG_INDEX = 120;
/*     */   public static final short MSG_LAST_LOG_INDEX = 121;
/*     */   public static final short MSG_REQUEST_LOG_RECORDS = 130;
/*     */   public static final short MSG_LOG_RECORDS = 131;
/*     */   public static final short MSG_EVENT = 141;
/*     */   public static final short MSG_TEXT = 150;
/*     */   public static final short MSG_ACK_ALARM = 160;
/*     */   public static final short MSG_SET_TRACKER_MODE = 170;
/*     */   public static final short MSG_GPRS_COMMAND = 180;
/*     */   
/*     */   public ApelProtocolDecoder(Protocol protocol) {
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
/*     */   private void sendSimpleMessage(Channel channel, short type) {
/*  70 */     ByteBuf request = Unpooled.buffer(8);
/*  71 */     request.writeShortLE(type);
/*  72 */     request.writeShortLE(0);
/*  73 */     request.writeIntLE(Checksum.crc32(request.nioBuffer(0, 4)));
/*  74 */     channel.writeAndFlush(new NetworkMessage(request, channel.remoteAddress()));
/*     */   }
/*     */   
/*     */   private void requestArchive(Channel channel) {
/*  78 */     if (this.lastIndex == 0L) {
/*  79 */       this.lastIndex = this.newIndex;
/*  80 */     } else if (this.newIndex > this.lastIndex) {
/*  81 */       ByteBuf request = Unpooled.buffer(14);
/*  82 */       request.writeShortLE(130);
/*  83 */       request.writeShortLE(6);
/*  84 */       request.writeIntLE((int)this.lastIndex);
/*  85 */       request.writeShortLE(512);
/*  86 */       request.writeIntLE(Checksum.crc32(request.nioBuffer(0, 10)));
/*  87 */       channel.writeAndFlush(new NetworkMessage(request, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  95 */     ByteBuf buf = (ByteBuf)msg;
/*  96 */     int type = buf.readUnsignedShortLE();
/*  97 */     boolean alarm = ((type & 0x8000) != 0);
/*  98 */     type &= 0x7FFF;
/*  99 */     buf.readUnsignedShortLE();
/*     */     
/* 101 */     if (alarm) {
/* 102 */       sendSimpleMessage(channel, (short)160);
/*     */     }
/*     */     
/* 105 */     if (type == 11) {
/* 106 */       return null;
/*     */     }
/*     */     
/* 109 */     if (type == 12) {
/*     */       
/* 111 */       buf.readUnsignedIntLE();
/* 112 */       int length = buf.readUnsignedShortLE();
/* 113 */       buf.skipBytes(length);
/* 114 */       length = buf.readUnsignedShortLE();
/* 115 */       getDeviceSession(channel, remoteAddress, new String[] { buf.readSlice(length).toString(StandardCharsets.US_ASCII) });
/*     */     }
/* 117 */     else if (type == 121) {
/*     */       
/* 119 */       long index = buf.readUnsignedIntLE();
/* 120 */       if (index > 0L) {
/* 121 */         this.newIndex = index;
/* 122 */         requestArchive(channel);
/*     */       }
/*     */     
/* 125 */     } else if (type == 101 || type == 92 || type == 131) {
/*     */       
/* 127 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 128 */       if (deviceSession == null) {
/* 129 */         return null;
/*     */       }
/*     */       
/* 132 */       List<Position> positions = new LinkedList<>();
/*     */       
/* 134 */       int recordCount = 1;
/* 135 */       if (type == 131) {
/* 136 */         recordCount = buf.readUnsignedShortLE();
/*     */       }
/*     */       
/* 139 */       int j = 0; while (true) { Position position; if (j < recordCount)
/* 140 */         { position = new Position(getProtocolName());
/* 141 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */           
/* 143 */           int subtype = type;
/* 144 */           if (type == 131)
/* 145 */           { position.set("archive", Boolean.valueOf(true));
/* 146 */             this.lastIndex = buf.readUnsignedIntLE() + 1L;
/* 147 */             position.set("index", Long.valueOf(this.lastIndex));
/*     */             
/* 149 */             subtype = buf.readUnsignedShortLE();
/* 150 */             if (subtype != 101 && subtype != 92)
/* 151 */             { buf.skipBytes(buf.readUnsignedShortLE()); }
/*     */             else
/*     */             
/* 154 */             { buf.readUnsignedShortLE();
/*     */ 
/*     */               
/* 157 */               position.setTime(new Date(buf.readUnsignedIntLE() * 1000L));
/* 158 */               position.setLatitude(buf.readIntLE() * 180.0D / 2.147483647E9D);
/* 159 */               position.setLongitude(buf.readIntLE() * 180.0D / 2.147483647E9D); }  continue; }  } else { break; }  position.setTime(new Date(buf.readUnsignedIntLE() * 1000L)); position.setLatitude(buf.readIntLE() * 180.0D / 2.147483647E9D); position.setLongitude(buf.readIntLE() * 180.0D / 2.147483647E9D);
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
/*     */         j++; }
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
/* 196 */       buf.readUnsignedIntLE();
/*     */       
/* 198 */       if (type == 131) {
/* 199 */         requestArchive(channel);
/*     */       } else {
/* 201 */         sendSimpleMessage(channel, (short)120);
/*     */       } 
/*     */       
/* 204 */       return positions;
/*     */     } 
/*     */     
/* 207 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ApelProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */