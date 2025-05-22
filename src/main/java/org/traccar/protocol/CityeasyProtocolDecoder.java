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
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
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
/*     */ public class CityeasyProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public CityeasyProtocolDecoder(Protocol protocol) {
/*  38 */     super(protocol);
/*     */   }
/*     */   
/*  41 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  42 */     .groupBegin()
/*  43 */     .number("(dddd)(dd)(dd)")
/*  44 */     .number("(dd)(dd)(dd),")
/*  45 */     .number("([AV]),")
/*  46 */     .number("(d+),")
/*  47 */     .number("([NS]),(d+.d+),")
/*  48 */     .number("([EW]),(d+.d+),")
/*  49 */     .number("(d+.d),")
/*  50 */     .number("(d+.d),")
/*  51 */     .number("(d+.d)")
/*  52 */     .groupEnd("?").text(";")
/*  53 */     .number("(d+),")
/*  54 */     .number("(d+),")
/*  55 */     .number("(d+),")
/*  56 */     .number("(d+)")
/*  57 */     .any()
/*  58 */     .compile();
/*     */   
/*     */   public static final int MSG_ADDRESS_REQUEST = 1;
/*     */   
/*     */   public static final int MSG_STATUS = 2;
/*     */   
/*     */   public static final int MSG_LOCATION_REPORT = 3;
/*     */   
/*     */   public static final int MSG_LOCATION_REQUEST = 4;
/*     */   public static final int MSG_LOCATION_INTERVAL = 5;
/*     */   public static final int MSG_PHONE_NUMBER = 6;
/*     */   public static final int MSG_MONITORING = 7;
/*     */   public static final int MSG_TIMEZONE = 8;
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  73 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  75 */     buf.skipBytes(2);
/*  76 */     buf.readUnsignedShort();
/*     */     
/*  78 */     String imei = ByteBufUtil.hexDump(buf.readSlice(7));
/*  79 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei, imei + 
/*  80 */           Checksum.luhn(Long.parseLong(imei)) });
/*  81 */     if (deviceSession == null) {
/*  82 */       return null;
/*     */     }
/*     */     
/*  85 */     int type = buf.readUnsignedShort();
/*     */     
/*  87 */     if (type == 3 || type == 4) {
/*     */       
/*  89 */       String sentence = buf.toString(buf.readerIndex(), buf.readableBytes() - 8, StandardCharsets.US_ASCII);
/*  90 */       Parser parser = new Parser(PATTERN, sentence);
/*  91 */       if (!parser.matches()) {
/*  92 */         return null;
/*     */       }
/*     */       
/*  95 */       Position position = new Position(getProtocolName());
/*  96 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  98 */       if (parser.hasNext(15)) {
/*     */         
/* 100 */         position.setTime(parser.nextDateTime());
/*     */         
/* 102 */         position.setValid(parser.next().equals("A"));
/* 103 */         position.set("sat", parser.nextInt());
/*     */         
/* 105 */         position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
/* 106 */         position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
/*     */         
/* 108 */         position.setSpeed(parser.nextDouble(0.0D));
/* 109 */         position.set("hdop", Double.valueOf(parser.nextDouble(0.0D)));
/* 110 */         position.setAltitude(parser.nextDouble(0.0D));
/*     */       }
/*     */       else {
/*     */         
/* 114 */         getLastLocation(position, null);
/*     */       } 
/*     */ 
/*     */       
/* 118 */       position.setNetwork(new Network(CellTower.from(parser
/* 119 */               .nextInt(0), parser.nextInt(0), parser.nextInt(0), parser.nextInt(0))));
/*     */       
/* 121 */       return position;
/*     */     } 
/*     */     
/* 124 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CityeasyProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */