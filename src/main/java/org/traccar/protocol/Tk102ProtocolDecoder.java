/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
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
/*     */ public class Tk102ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public static final int MSG_LOGIN_REQUEST = 128;
/*     */   public static final int MSG_LOGIN_REQUEST_2 = 33;
/*     */   public static final int MSG_LOGIN_RESPONSE = 0;
/*     */   public static final int MSG_HEARTBEAT_REQUEST = 240;
/*     */   public static final int MSG_HEARTBEAT_RESPONSE = 255;
/*     */   public static final int MSG_REPORT_ONCE = 144;
/*     */   public static final int MSG_REPORT_INTERVAL = 147;
/*     */   public static final int MODE_GPRS = 48;
/*     */   public static final int MODE_GPRS_SMS = 51;
/*     */   
/*     */   public Tk102ProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
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
/*  51 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  52 */     .text("(")
/*  53 */     .expression("[A-Z]+")
/*  54 */     .number("(dd)(dd)(dd)")
/*  55 */     .expression("([AV])")
/*  56 */     .number("(dd)(dd.dddd)([NS])")
/*  57 */     .number("(ddd)(dd.dddd)([EW])")
/*  58 */     .number("(ddd.ddd)")
/*  59 */     .number("(dd)(dd)(dd)")
/*  60 */     .any()
/*  61 */     .text(")")
/*  62 */     .compile();
/*     */   
/*     */   private void sendResponse(Channel channel, int type, ByteBuf dataSequence, ByteBuf content) {
/*  65 */     if (channel != null) {
/*  66 */       ByteBuf response = Unpooled.buffer();
/*  67 */       response.writeByte(91);
/*  68 */       response.writeByte(type);
/*  69 */       response.writeBytes(dataSequence);
/*  70 */       response.writeByte(content.readableBytes());
/*  71 */       response.writeBytes(content);
/*  72 */       content.release();
/*  73 */       response.writeByte(93);
/*  74 */       channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  82 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  84 */     buf.skipBytes(1);
/*  85 */     int type = buf.readUnsignedByte();
/*  86 */     ByteBuf dataSequence = buf.readSlice(10);
/*  87 */     int length = buf.readUnsignedByte();
/*     */     
/*  89 */     if (type == 128 || type == 33) {
/*     */       String id;
/*  91 */       ByteBuf data = buf.readSlice(length);
/*     */ 
/*     */       
/*  94 */       if (type == 128) {
/*  95 */         id = data.toString(StandardCharsets.US_ASCII);
/*     */       } else {
/*  97 */         id = data.copy(1, 15).toString(StandardCharsets.US_ASCII);
/*     */       } 
/*     */       
/* 100 */       if (getDeviceSession(channel, remoteAddress, new String[] { id }) != null) {
/* 101 */         ByteBuf response = Unpooled.buffer();
/* 102 */         response.writeByte(48);
/* 103 */         response.writeBytes(data);
/* 104 */         sendResponse(channel, 0, dataSequence, response);
/*     */       }
/*     */     
/* 107 */     } else if (type == 240) {
/*     */       
/* 109 */       sendResponse(channel, 255, dataSequence, buf.readRetainedSlice(length));
/*     */     }
/*     */     else {
/*     */       
/* 113 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 114 */       if (deviceSession == null) {
/* 115 */         return null;
/*     */       }
/*     */       
/* 118 */       Parser parser = new Parser(PATTERN, buf.readSlice(length).toString(StandardCharsets.US_ASCII));
/* 119 */       if (!parser.matches()) {
/* 120 */         return null;
/*     */       }
/*     */       
/* 123 */       Position position = new Position(getProtocolName());
/* 124 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */       
/* 127 */       DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*     */       
/* 129 */       position.setValid(parser.next().equals("A"));
/* 130 */       position.setLatitude(parser.nextCoordinate());
/* 131 */       position.setLongitude(parser.nextCoordinate());
/* 132 */       position.setSpeed(parser.nextDouble(0.0D));
/*     */       
/* 134 */       dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 135 */       position.setTime(dateBuilder.getDate());
/*     */       
/* 137 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 141 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Tk102ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */