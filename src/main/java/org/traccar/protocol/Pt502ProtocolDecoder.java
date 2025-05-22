/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.DateBuilder;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
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
/*     */ 
/*     */ public class Pt502ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private static final int MAX_CHUNK_SIZE = 960;
/*     */   private ByteBuf photo;
/*     */   
/*     */   public Pt502ProtocolDecoder(Protocol protocol) {
/*  43 */     super(protocol);
/*     */   }
/*     */   
/*  46 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  47 */     .any().text("$")
/*  48 */     .expression("([^,]+),")
/*  49 */     .number("(d+),")
/*  50 */     .number("(dd)(dd)(dd).(ddd),")
/*  51 */     .expression("([AV]),")
/*  52 */     .number("(d+)(dd.dddd),")
/*  53 */     .expression("([NS]),")
/*  54 */     .number("(d+)(dd.dddd),")
/*  55 */     .expression("([EW]),")
/*  56 */     .number("(d+.d+)?,")
/*  57 */     .number("(d+.d+)?,")
/*  58 */     .number("(dd)(dd)(dd),,,?")
/*  59 */     .expression(".?/")
/*  60 */     .expression("([01])+,")
/*  61 */     .expression("([01])+/")
/*  62 */     .expression("([^/]+)?/")
/*  63 */     .number("(d+)")
/*  64 */     .expression("/([^/]+)?/")
/*  65 */     .number("(xxx)").optional(2)
/*  66 */     .any()
/*  67 */     .compile();
/*     */   
/*     */   private String decodeAlarm(String value) {
/*  70 */     switch (value) {
/*     */       case "IN1":
/*  72 */         return "sos";
/*     */       case "GOF":
/*  74 */         return "geofence";
/*     */       case "TOW":
/*  76 */         return "tow";
/*     */       case "HDA":
/*  78 */         return "hardAcceleration";
/*     */       case "HDB":
/*  80 */         return "hardBraking";
/*     */       case "FDA":
/*  82 */         return "fatigueDriving";
/*     */       case "SKA":
/*  84 */         return "vibration";
/*     */       case "PMA":
/*  86 */         return "movement";
/*     */       case "CPA":
/*  88 */         return "powerCut";
/*     */     } 
/*  90 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private Position decodePosition(Channel channel, SocketAddress remoteAddress, String sentence) {
/*  96 */     Parser parser = new Parser(PATTERN, sentence);
/*  97 */     if (!parser.matches()) {
/*  98 */       return null;
/*     */     }
/*     */     
/* 101 */     Position position = new Position(getProtocolName());
/* 102 */     position.set("alarm", decodeAlarm(parser.next()));
/*     */     
/* 104 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 105 */     if (deviceSession == null) {
/* 106 */       return null;
/*     */     }
/* 108 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */     
/* 111 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*     */     
/* 113 */     position.setValid(parser.next().equals("A"));
/* 114 */     position.setLatitude(parser.nextCoordinate());
/* 115 */     position.setLongitude(parser.nextCoordinate());
/* 116 */     position.setSpeed(parser.nextDouble(0.0D));
/* 117 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 119 */     dateBuilder.setDateReverse(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/* 120 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 122 */     position.set("input", parser.next());
/* 123 */     position.set("output", parser.next());
/*     */     
/* 125 */     if (parser.hasNext()) {
/* 126 */       String[] values = parser.next().split(",");
/* 127 */       for (int i = 0; i < values.length; i++) {
/* 128 */         position.set("adc" + (i + 1), Integer.valueOf(Integer.parseInt(values[i], 16)));
/*     */       }
/*     */     } 
/*     */     
/* 132 */     position.set("odometer", Integer.valueOf(parser.nextInt(0)));
/* 133 */     position.set("driverUniqueId", parser.next());
/*     */     
/* 135 */     if (parser.hasNext()) {
/* 136 */       int value = parser.nextHexInt(0);
/* 137 */       position.set("battery", Integer.valueOf(value >> 8));
/* 138 */       position.set("rssi", Integer.valueOf(value >> 4 & 0xF));
/* 139 */       position.set("sat", Integer.valueOf(value & 0xF));
/*     */     } 
/*     */     
/* 142 */     return position;
/*     */   }
/*     */   
/*     */   private void requestPhotoFragment(Channel channel) {
/* 146 */     if (channel != null) {
/* 147 */       int offset = this.photo.writerIndex();
/* 148 */       int size = Math.min(this.photo.writableBytes(), 960);
/* 149 */       channel.writeAndFlush(new NetworkMessage("#PHD" + offset + "," + size + "\r\n", channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 157 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/* 159 */     int typeEndIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)44);
/* 160 */     String type = buf.toString(buf.readerIndex(), typeEndIndex - buf.readerIndex(), StandardCharsets.US_ASCII);
/*     */     
/* 162 */     if (type.startsWith("$PHD")) {
/*     */       
/* 164 */       int dataIndex = buf.indexOf(typeEndIndex + 1, buf.writerIndex(), (byte)44) + 1;
/* 165 */       buf.readerIndex(dataIndex);
/*     */       
/* 167 */       if (this.photo != null)
/*     */       {
/* 169 */         this.photo.writeBytes(buf.readSlice(buf.readableBytes()));
/*     */         
/* 171 */         if (this.photo.writableBytes() > 0)
/*     */         {
/* 173 */           requestPhotoFragment(channel);
/*     */         }
/*     */         else
/*     */         {
/* 177 */           DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 178 */           String uniqueId = Context.getIdentityManager().getById(deviceSession.getDeviceId()).getUniqueId();
/*     */           
/* 180 */           Position position = new Position(getProtocolName());
/* 181 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */           
/* 183 */           getLastLocation(position, null);
/*     */           
/* 185 */           position.set("image", Context.getMediaManager().writeFile(uniqueId, this.photo, "jpg"));
/* 186 */           this.photo.release();
/* 187 */           this.photo = null;
/*     */           
/* 189 */           return position;
/*     */         }
/*     */       
/*     */       }
/*     */     
/*     */     }
/*     */     else {
/*     */       
/* 197 */       if (type.startsWith("$PHO")) {
/* 198 */         int size = Integer.parseInt(type.split("-")[0].substring(4));
/* 199 */         if (size > 0) {
/* 200 */           this.photo = Unpooled.buffer(size);
/* 201 */           requestPhotoFragment(channel);
/*     */         } 
/*     */       } 
/*     */       
/* 205 */       return decodePosition(channel, remoteAddress, buf.toString(StandardCharsets.US_ASCII));
/*     */     } 
/*     */ 
/*     */     
/* 209 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Pt502ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */