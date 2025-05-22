/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class TelemaxProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TelemaxProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*     */   private String readValue(String sentence, int[] index, int length) {
/*  38 */     String value = sentence.substring(index[0], index[0] + length);
/*  39 */     index[0] = index[0] + length;
/*  40 */     return value;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  47 */     String sentence = (String)msg;
/*     */     
/*  49 */     if (sentence.startsWith("%")) {
/*  50 */       int length = Integer.parseInt(sentence.substring(1, 3));
/*  51 */       getDeviceSession(channel, remoteAddress, new String[] { sentence.substring(3, 3 + length) });
/*  52 */       return null;
/*     */     } 
/*     */     
/*  55 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*  56 */     if (deviceSession == null) {
/*  57 */       return null;
/*     */     }
/*     */     
/*  60 */     int[] index = { 0 };
/*     */     
/*  62 */     if (!readValue(sentence, index, 1).equals("Y")) {
/*  63 */       return null;
/*     */     }
/*     */     
/*  66 */     readValue(sentence, index, 8);
/*  67 */     readValue(sentence, index, 6);
/*  68 */     readValue(sentence, index, Integer.parseInt(readValue(sentence, index, 2), 16));
/*  69 */     readValue(sentence, index, 2);
/*     */     
/*  71 */     readValue(sentence, index, 2);
/*     */     
/*  73 */     int interval = Integer.parseInt(readValue(sentence, index, 4), 16);
/*     */     
/*  75 */     readValue(sentence, index, 2);
/*  76 */     readValue(sentence, index, 2);
/*     */     
/*  78 */     int count = Integer.parseInt(readValue(sentence, index, 2), 16);
/*     */     
/*  80 */     Date time = null;
/*  81 */     List<Position> positions = new LinkedList<>();
/*     */     
/*  83 */     for (int i = 0; i < count; i++) {
/*     */       
/*  85 */       Position position = new Position(getProtocolName());
/*  86 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  88 */       int speed = Integer.parseInt(readValue(sentence, index, 2), 16);
/*     */       
/*  90 */       position.setValid(BitUtil.check(speed, 7));
/*  91 */       position.setSpeed(BitUtil.to(speed, 7));
/*     */       
/*  93 */       position.setLongitude((Integer.parseInt(readValue(sentence, index, 6), 16) - 5400000) / 30000.0D);
/*  94 */       position.setLatitude((Integer.parseInt(readValue(sentence, index, 6), 16) - 5400000) / 30000.0D);
/*     */       
/*  96 */       if ((((i == 0) ? 1 : 0) | ((i == count - 1) ? 1 : 0)) != 0) {
/*  97 */         time = (new SimpleDateFormat("yyMMddHHmmss")).parse(readValue(sentence, index, 12));
/*  98 */         position.set("status", readValue(sentence, index, 8));
/*     */       } else {
/* 100 */         time = new Date(time.getTime() + (interval * 1000));
/*     */       } 
/*     */       
/* 103 */       position.setTime(time);
/*     */       
/* 105 */       positions.add(position);
/*     */     } 
/*     */ 
/*     */     
/* 109 */     return positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TelemaxProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */