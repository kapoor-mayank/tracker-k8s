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
/*     */ public class FoxProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public FoxProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .number("(d+),")
/*  38 */     .expression("([AV]),")
/*  39 */     .number("(dd)(dd)(dd),")
/*  40 */     .number("(dd)(dd)(dd),")
/*  41 */     .number("(dd)(dd.d+),")
/*  42 */     .expression("([NS]),")
/*  43 */     .number("(ddd)(dd.d+),")
/*  44 */     .expression("([EW]),")
/*  45 */     .number("(d+.?d*)?,")
/*  46 */     .number("(d+.?d*)?,")
/*  47 */     .expression("[^,]*,")
/*  48 */     .number("([01]+) ")
/*  49 */     .number("(d+) ")
/*  50 */     .number("(d+) ")
/*  51 */     .number("(d+) ")
/*  52 */     .number("(d+) ")
/*  53 */     .number("(d+) ")
/*  54 */     .number("(d+) ")
/*  55 */     .number("([01]+) ")
/*  56 */     .number("(d+),")
/*  57 */     .expression("(.+)")
/*  58 */     .compile();
/*     */   
/*     */   private String getAttribute(String xml, String key) {
/*  61 */     int start = xml.indexOf(key + "=\"");
/*  62 */     if (start != -1) {
/*  63 */       start += key.length() + 2;
/*  64 */       int end = xml.indexOf("\"", start);
/*  65 */       if (end != -1) {
/*  66 */         return xml.substring(start, end);
/*     */       }
/*     */     } 
/*  69 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  76 */     String xml = (String)msg;
/*  77 */     String id = getAttribute(xml, "id");
/*  78 */     String data = getAttribute(xml, "data");
/*     */     
/*  80 */     if (id != null && data != null) {
/*     */       
/*  82 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id });
/*  83 */       if (deviceSession == null) {
/*  84 */         return null;
/*     */       }
/*     */       
/*  87 */       Parser parser = new Parser(PATTERN, data);
/*  88 */       if (!parser.matches()) {
/*  89 */         return null;
/*     */       }
/*     */       
/*  92 */       Position position = new Position(getProtocolName());
/*  93 */       position.setDeviceId(deviceSession.getDeviceId());
/*     */       
/*  95 */       position.set("status", Integer.valueOf(parser.nextInt(0)));
/*     */       
/*  97 */       position.setValid(parser.next().equals("A"));
/*     */       
/*  99 */       position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/* 100 */       position.setLatitude(parser.nextCoordinate());
/* 101 */       position.setLongitude(parser.nextCoordinate());
/* 102 */       position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/* 103 */       position.setCourse(parser.nextDouble(0.0D));
/*     */       
/* 105 */       position.set("input", Integer.valueOf(parser.nextBinInt(0)));
/* 106 */       position.set("power", Double.valueOf(parser.nextDouble(0.0D) * 0.1D));
/* 107 */       position.set("temp1", Integer.valueOf(parser.nextInt(0)));
/* 108 */       position.set("rpm", Integer.valueOf(parser.nextInt(0)));
/* 109 */       position.set("fuel", Integer.valueOf(parser.nextInt(0)));
/* 110 */       position.set("adc1", Integer.valueOf(parser.nextInt(0)));
/* 111 */       position.set("adc2", Integer.valueOf(parser.nextInt(0)));
/* 112 */       position.set("output", Integer.valueOf(parser.nextBinInt(0)));
/* 113 */       position.set("odometer", Integer.valueOf(parser.nextInt(0)));
/*     */       
/* 115 */       position.set("statusData", parser.next());
/*     */       
/* 117 */       return position;
/*     */     } 
/*     */ 
/*     */     
/* 121 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FoxProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */