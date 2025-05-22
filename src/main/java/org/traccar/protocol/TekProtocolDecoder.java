/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufUtil;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
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
/*     */ public class TekProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TekProtocolDecoder(Protocol protocol) {
/*  37 */     super(protocol);
/*     */   }
/*     */   
/*  40 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  41 */     .number(",d+,")
/*  42 */     .number("(dd)(dd)(dd).d,")
/*  43 */     .number("(dd)(dd.d+)")
/*  44 */     .expression("([NS]),")
/*  45 */     .number("(ddd)(dd.d+)")
/*  46 */     .expression("([EW]),")
/*  47 */     .number("(d+.d+),")
/*  48 */     .number("(d+.d+),")
/*  49 */     .number("(d+),")
/*  50 */     .number("(d+.d+),")
/*  51 */     .number("d+.d+,")
/*  52 */     .number("(d+.d+),")
/*  53 */     .number("(dd)(dd)(dd),")
/*  54 */     .number("(d+),")
/*  55 */     .any()
/*  56 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  62 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  64 */     buf.readUnsignedByte();
/*  65 */     buf.readUnsignedByte();
/*  66 */     buf.readUnsignedByte();
/*  67 */     buf.readUnsignedByte();
/*  68 */     buf.readUnsignedByte();
/*  69 */     buf.readUnsignedByte();
/*  70 */     buf.readUnsignedByte();
/*     */     
/*  72 */     String imei = ByteBufUtil.hexDump(buf.readBytes(8)).substring(1);
/*  73 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  74 */     if (deviceSession == null) {
/*  75 */       return null;
/*     */     }
/*     */     
/*  78 */     int type = BitUtil.to(buf.readUnsignedByte(), 6);
/*  79 */     buf.readUnsignedByte();
/*     */     
/*  81 */     if (type == 4 || type == 8) {
/*     */       
/*  83 */       Position position = new Position(getProtocolName());
/*  84 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  86 */       int count = buf.readUnsignedShort();
/*  87 */       buf.readUnsignedByte();
/*  88 */       buf.readUnsignedByte();
/*  89 */       buf.readUnsignedByte();
/*  90 */       buf.readUnsignedByte();
/*  91 */       buf.readUnsignedByte();
/*  92 */       buf.readUnsignedByte();
/*     */       
/*  94 */       for (int i = 0; i < count; i++) {
/*  95 */         position.set("rssi" + (i + 1), Short.valueOf(buf.readUnsignedByte()));
/*  96 */         position.set("temp" + (i + 1), Integer.valueOf(buf.readUnsignedByte() - 30));
/*  97 */         int data = buf.readUnsignedShort();
/*  98 */         position.set("src" + (i + 1), Integer.valueOf(BitUtil.from(data, 10)));
/*  99 */         position.set("ullage" + (i + 1), Integer.valueOf(BitUtil.to(data, 10)));
/*     */       } 
/*     */       
/* 102 */       return position;
/*     */     } 
/* 104 */     if (type == 17) {
/*     */       
/* 106 */       String sentence = buf.toString(StandardCharsets.US_ASCII);
/*     */       
/* 108 */       Parser parser = new Parser(PATTERN, sentence);
/* 109 */       if (!parser.matches()) {
/* 110 */         return null;
/*     */       }
/*     */       
/* 113 */       Position position = new Position(getProtocolName());
/* 114 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */ 
/*     */       
/* 117 */       DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*     */       
/* 119 */       position.setLatitude(parser.nextCoordinate());
/* 120 */       position.setLongitude(parser.nextCoordinate());
/*     */       
/* 122 */       position.set("hdop", parser.nextDouble());
/*     */       
/* 124 */       position.setAltitude(parser.nextDouble().doubleValue());
/* 125 */       position.setValid((parser.nextInt().intValue() > 0));
/* 126 */       position.setCourse(parser.nextDouble().doubleValue());
/* 127 */       position.setSpeed(parser.nextDouble().doubleValue());
/*     */       
/* 129 */       dateBuilder.setDateReverse(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/* 130 */       position.setTime(dateBuilder.getDate());
/*     */       
/* 132 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 136 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TekProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */