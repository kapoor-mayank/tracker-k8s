/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitBuffer;
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
/*     */ public class AisProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public AisProtocolDecoder(Protocol protocol) {
/*  39 */     super(protocol);
/*     */   }
/*     */   
/*  42 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  43 */     .text("!AIVDM,")
/*  44 */     .number("(d+),")
/*  45 */     .number("(d+),")
/*  46 */     .number("(d+)?,")
/*  47 */     .expression(".,")
/*  48 */     .expression("([^,]+),")
/*  49 */     .any()
/*  50 */     .compile();
/*     */ 
/*     */   
/*     */   private Position decodePayload(Channel channel, SocketAddress remoteAddress, BitBuffer buf) {
/*  54 */     int type = buf.readUnsigned(6);
/*  55 */     if (type == 1 || type == 2 || type == 3 || type == 18) {
/*     */       
/*  57 */       buf.readUnsigned(2);
/*  58 */       int mmsi = buf.readUnsigned(30);
/*     */       
/*  60 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(mmsi) });
/*  61 */       if (deviceSession == null) {
/*  62 */         return null;
/*     */       }
/*     */       
/*  65 */       Position position = new Position(getProtocolName());
/*  66 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  68 */       position.setTime(new Date());
/*     */       
/*  70 */       if (type == 18) {
/*  71 */         buf.readUnsigned(8);
/*     */       } else {
/*  73 */         position.set("status", Integer.valueOf(buf.readUnsigned(4)));
/*  74 */         position.set("turn", Integer.valueOf(buf.readSigned(8)));
/*     */       } 
/*     */       
/*  77 */       position.setSpeed(buf.readUnsigned(10) * 0.1D);
/*  78 */       position.setValid((buf.readUnsigned(1) != 0));
/*  79 */       position.setLongitude(buf.readSigned(28) * 1.0E-4D / 60.0D);
/*  80 */       position.setLatitude(buf.readSigned(27) * 1.0E-4D / 60.0D);
/*  81 */       position.setCourse(buf.readUnsigned(12) * 0.1D);
/*     */       
/*  83 */       position.set("heading", Integer.valueOf(buf.readUnsigned(9)));
/*     */       
/*  85 */       return position;
/*     */     } 
/*     */ 
/*     */     
/*  89 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  96 */     String[] sentences = ((String)msg).split("\\r\\n");
/*     */     
/*  98 */     List<Position> positions = new ArrayList<>();
/*  99 */     Map<Integer, BitBuffer> buffers = new HashMap<>();
/*     */     
/* 101 */     for (String sentence : sentences) {
/* 102 */       if (!sentence.isEmpty()) {
/* 103 */         Parser parser = new Parser(PATTERN, sentence);
/* 104 */         if (parser.matches()) {
/*     */           
/* 106 */           int count = parser.nextInt(0);
/* 107 */           int index = parser.nextInt(0);
/* 108 */           int id = parser.nextInt(0);
/*     */           
/* 110 */           Position position = null;
/*     */           
/* 112 */           if (count == 1) {
/* 113 */             BitBuffer bits = new BitBuffer();
/* 114 */             bits.writeEncoded(parser.next().getBytes(StandardCharsets.US_ASCII));
/* 115 */             position = decodePayload(channel, remoteAddress, bits);
/*     */           } else {
/* 117 */             BitBuffer bits = buffers.get(Integer.valueOf(id));
/* 118 */             if (bits == null) {
/* 119 */               bits = new BitBuffer();
/* 120 */               buffers.put(Integer.valueOf(id), bits);
/*     */             } 
/* 122 */             bits.writeEncoded(parser.next().getBytes(StandardCharsets.US_ASCII));
/* 123 */             if (count == index) {
/* 124 */               position = decodePayload(channel, remoteAddress, bits);
/* 125 */               buffers.remove(Integer.valueOf(id));
/*     */             } 
/*     */           } 
/*     */           
/* 129 */           if (position != null) {
/* 130 */             positions.add(position);
/*     */           }
/*     */         } 
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 137 */     return positions;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AisProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */