/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Date;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BufferUtil;
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
/*     */ public class EnforaProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public EnforaProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */   
/*  40 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  41 */     .text("GPRMC,")
/*  42 */     .number("(dd)(dd)(dd).?d*,")
/*  43 */     .expression("([AV]),")
/*  44 */     .number("(dd)(dd.d+),")
/*  45 */     .expression("([NS]),")
/*  46 */     .number("(ddd)(dd.d+),")
/*  47 */     .expression("([EW]),")
/*  48 */     .number("(d+.d+)?,")
/*  49 */     .number("(d+.d+)?,")
/*  50 */     .number("(dd)(dd)(dd),")
/*  51 */     .any()
/*  52 */     .compile();
/*     */ 
/*     */   
/*     */   public static final int IMEI_LENGTH = 15;
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*     */     String id;
/*  60 */     ByteBuf buf = (ByteBuf)msg;
/*     */ 
/*     */ 
/*     */     
/*  64 */     int index = -1; int i;
/*  65 */     for (i = buf.readerIndex(); i < buf.writerIndex() - 15; i++) {
/*  66 */       index = i;
/*  67 */       for (int j = i; j < i + 15; j++) {
/*  68 */         if (!Character.isDigit((char)buf.getByte(j))) {
/*  69 */           index = -1;
/*     */           break;
/*     */         } 
/*     */       } 
/*  73 */       if (index > 0) {
/*     */         break;
/*     */       }
/*     */     } 
/*  77 */     if (index < 0) {
/*  78 */       for (i = buf.readerIndex(); i < buf.writerIndex(); i++) {
/*  79 */         if (Character.isDigit((char)buf.getByte(i))) {
/*  80 */           index = i;
/*     */           break;
/*     */         } 
/*     */       } 
/*  84 */       if (index < 0) {
/*  85 */         return null;
/*     */       }
/*  87 */       while (Character.isDigit((char)buf.getByte(index)) || buf.getByte(index) == 32) {
/*  88 */         index++;
/*     */       }
/*  90 */       id = buf.toString(index, buf
/*  91 */           .indexOf(index, buf.writerIndex(), (byte)32) - index, StandardCharsets.US_ASCII);
/*     */     } else {
/*     */       
/*  94 */       id = buf.toString(index, 15, StandardCharsets.US_ASCII);
/*     */     } 
/*     */     
/*  97 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/*  98 */     if (deviceSession == null) {
/*  99 */       return null;
/*     */     }
/*     */ 
/*     */     
/* 103 */     int start = BufferUtil.indexOf("GPRMC", buf);
/* 104 */     if (start == -1) {
/* 105 */       return null;
/*     */     }
/*     */     
/* 108 */     String sentence = buf.toString(start, buf.readableBytes() - start, StandardCharsets.US_ASCII);
/* 109 */     Parser parser = new Parser(PATTERN, sentence);
/* 110 */     if (!parser.matches()) {
/* 111 */       return null;
/*     */     }
/*     */     
/* 114 */     Position position = new Position(getProtocolName());
/* 115 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */     
/* 118 */     DateBuilder dateBuilder = (new DateBuilder(new Date())).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0), 0);
/*     */     
/* 120 */     position.setValid(parser.next().equals("A"));
/* 121 */     position.setLatitude(parser.nextCoordinate());
/* 122 */     position.setLongitude(parser.nextCoordinate());
/* 123 */     position.setSpeed(parser.nextDouble(0.0D));
/* 124 */     position.setCourse(parser.nextDouble(0.0D));
/*     */     
/* 126 */     position.setTime(dateBuilder.getDate());
/*     */     
/* 128 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EnforaProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */