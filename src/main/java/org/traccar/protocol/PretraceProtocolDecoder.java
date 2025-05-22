/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
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
/*     */ public class PretraceProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public PretraceProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .text("(")
/*  38 */     .number("(d{15})")
/*  39 */     .number("Uddd")
/*  40 */     .number("d")
/*  41 */     .expression("([AV])")
/*  42 */     .number("(dd)(dd)(dd)")
/*  43 */     .number("(dd)(dd)(dd)")
/*  44 */     .number("(dd)(dd.dddd)")
/*  45 */     .expression("([NS])")
/*  46 */     .number("(ddd)(dd.dddd)")
/*  47 */     .expression("([EW])")
/*  48 */     .number("(ddd)")
/*  49 */     .number("(ddd)")
/*  50 */     .number("(xxx)")
/*  51 */     .number("(x{8})")
/*  52 */     .number("(x)")
/*  53 */     .number("(dd)")
/*  54 */     .number("(dd)")
/*  55 */     .expression("(.{8}),&")
/*  56 */     .expression("(.+)?")
/*  57 */     .text("^")
/*  58 */     .number("xx")
/*  59 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  65 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  66 */     if (!parser.matches()) {
/*  67 */       return null;
/*     */     }
/*     */     
/*  70 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  71 */     if (deviceSession == null) {
/*  72 */       return null;
/*     */     }
/*     */     
/*  75 */     Position position = new Position(getProtocolName());
/*  76 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  78 */     position.setValid(parser.next().equals("A"));
/*     */     
/*  80 */     position.setTime(parser.nextDateTime());
/*     */     
/*  82 */     position.setLatitude(parser.nextCoordinate());
/*  83 */     position.setLongitude(parser.nextCoordinate());
/*  84 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt(0)));
/*  85 */     position.setCourse(parser.nextInt(0));
/*  86 */     position.setAltitude(parser.nextHexInt(0));
/*     */     
/*  88 */     position.set("odometer", Integer.valueOf(parser.nextHexInt(0)));
/*  89 */     position.set("sat", Integer.valueOf(parser.nextHexInt(0)));
/*  90 */     position.set("hdop", Integer.valueOf(parser.nextInt(0)));
/*  91 */     position.set("rssi", Integer.valueOf(parser.nextInt(0)));
/*     */     
/*  93 */     parser.next();
/*     */     
/*  95 */     if (parser.hasNext()) {
/*  96 */       for (String value : parser.next().split(",")) {
/*  97 */         double temperature; switch (value.charAt(0)) {
/*     */           case 'P':
/*  99 */             if (value.charAt(1) == '1') {
/* 100 */               if (value.charAt(4) == '%') {
/* 101 */                 position.set("batteryLevel", Integer.valueOf(Integer.parseInt(value.substring(2, 4)))); break;
/*     */               } 
/* 103 */               position.set("battery", Double.valueOf(Integer.parseInt(value.substring(2), 16) * 0.01D));
/*     */               break;
/*     */             } 
/* 106 */             position.set("power", Double.valueOf(Integer.parseInt(value.substring(2), 16) * 0.01D));
/*     */             break;
/*     */           
/*     */           case 'T':
/* 110 */             temperature = Integer.parseInt(value.substring(2), 16) * 0.25D;
/* 111 */             if (value.charAt(1) == '1') {
/* 112 */               position.set("deviceTemp", Double.valueOf(temperature)); break;
/*     */             } 
/* 114 */             position.set("temp" + (value.charAt(1) - 48), Double.valueOf(temperature));
/*     */             break;
/*     */           
/*     */           case 'F':
/* 118 */             position.set("fuel" + (value.charAt(1) - 48), Double.valueOf(Integer.parseInt(value.substring(2), 16) * 0.01D));
/*     */             break;
/*     */           case 'R':
/* 121 */             position.set("driverUniqueId", value.substring(3));
/*     */             break;
/*     */         } 
/*     */ 
/*     */ 
/*     */       
/*     */       } 
/*     */     }
/* 129 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PretraceProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */