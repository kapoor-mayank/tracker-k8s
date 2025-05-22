/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import io.netty.handler.codec.http.FullHttpRequest;
/*     */ import io.netty.handler.codec.http.HttpResponseStatus;
/*     */ import io.netty.handler.codec.http.QueryStringDecoder;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseHttpProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
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
/*     */ public class OpenGtsProtocolDecoder
/*     */   extends BaseHttpProtocolDecoder
/*     */ {
/*  37 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  38 */     .text("$GPRMC,")
/*  39 */     .number("(dd)(dd)(dd)(?:.d+)?,")
/*  40 */     .expression("([AV]),")
/*  41 */     .number("(d+)(dd.d+),")
/*  42 */     .expression("([NS]),")
/*  43 */     .number("(d+)(dd.d+),")
/*  44 */     .expression("([EW]),")
/*  45 */     .number("(d+.d+),")
/*  46 */     .number("(d+.d+)?,")
/*  47 */     .number("(dd)(dd)(dd),")
/*  48 */     .any()
/*  49 */     .compile();
/*     */   
/*     */   public OpenGtsProtocolDecoder(Protocol protocol) {
/*  52 */     super(protocol);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  59 */     FullHttpRequest request = (FullHttpRequest)msg;
/*  60 */     QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
/*  61 */     Map<String, List<String>> params = decoder.parameters();
/*     */     
/*  63 */     Position position = new Position(getProtocolName());
/*     */     
/*  65 */     for (Map.Entry<String, List<String>> entry : params.entrySet()) {
/*  66 */       DeviceSession deviceSession; Parser parser; DateBuilder dateBuilder; String value = ((List<String>)entry.getValue()).get(0);
/*  67 */       switch ((String)entry.getKey()) {
/*     */         case "id":
/*  69 */           deviceSession = getDeviceSession(channel, remoteAddress, new String[] { value });
/*  70 */           if (deviceSession == null) {
/*  71 */             sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
/*  72 */             return null;
/*     */           } 
/*  74 */           position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/*     */         case "gprmc":
/*  77 */           parser = new Parser(PATTERN, value);
/*  78 */           if (!parser.matches()) {
/*  79 */             sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
/*  80 */             return null;
/*     */           } 
/*     */ 
/*     */           
/*  84 */           dateBuilder = (new DateBuilder()).setTime(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*     */           
/*  86 */           position.setValid(parser.next().equals("A"));
/*  87 */           position.setLatitude(parser.nextCoordinate());
/*  88 */           position.setLongitude(parser.nextCoordinate());
/*  89 */           position.setSpeed(parser.nextDouble().doubleValue());
/*  90 */           position.setCourse(parser.nextDouble(0.0D));
/*     */           
/*  92 */           dateBuilder.setDateReverse(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*  93 */           position.setTime(dateBuilder.getDate());
/*     */         
/*     */         case "alt":
/*  96 */           position.setAltitude(Double.parseDouble(value));
/*     */         
/*     */         case "batt":
/*  99 */           position.set("batteryLevel", Double.valueOf(Double.parseDouble(value)));
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     } 
/* 106 */     if (position.getDeviceId() != 0L) {
/* 107 */       sendResponse(channel, HttpResponseStatus.OK);
/* 108 */       return position;
/*     */     } 
/* 110 */     sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
/* 111 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OpenGtsProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */