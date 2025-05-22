/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import java.util.regex.Pattern;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.helper.DateBuilder;
/*    */ import org.traccar.helper.Parser;
/*    */ import org.traccar.helper.PatternBuilder;
/*    */ import org.traccar.model.Position;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class CarscopProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public CarscopProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .text("*")
/* 38 */     .any()
/* 39 */     .number("(dd)(dd)(dd)")
/* 40 */     .expression("([AV])")
/* 41 */     .number("(dd)(dd.dddd)")
/* 42 */     .expression("([NS])")
/* 43 */     .number("(ddd)(dd.dddd)")
/* 44 */     .expression("([EW])")
/* 45 */     .number("(ddd.d)")
/* 46 */     .number("(dd)(dd)(dd)")
/* 47 */     .number("(ddd.dd)")
/* 48 */     .groupBegin()
/* 49 */     .number("(d{8})")
/* 50 */     .number("L(d{6})")
/* 51 */     .groupEnd("?")
/* 52 */     .compile();
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*    */     DeviceSession deviceSession;
/* 58 */     String sentence = (String)msg;
/*    */ 
/*    */     
/* 61 */     int index = sentence.indexOf("UB05");
/* 62 */     if (index != -1) {
/* 63 */       String imei = sentence.substring(index + 4, index + 4 + 15);
/* 64 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*    */     } else {
/* 66 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*    */     } 
/* 68 */     if (deviceSession == null) {
/* 69 */       return null;
/*    */     }
/*    */     
/* 72 */     Parser parser = new Parser(PATTERN, sentence);
/* 73 */     if (!parser.matches()) {
/* 74 */       return null;
/*    */     }
/*    */     
/* 77 */     Position position = new Position(getProtocolName());
/* 78 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */ 
/*    */     
/* 81 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*    */     
/* 83 */     position.setValid(parser.next().equals("A"));
/* 84 */     position.setLatitude(parser.nextCoordinate());
/* 85 */     position.setLongitude(parser.nextCoordinate());
/* 86 */     position.setSpeed(parser.nextDouble(0.0D));
/*    */     
/* 88 */     dateBuilder.setDate(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 89 */     position.setTime(dateBuilder.getDate());
/*    */     
/* 91 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 93 */     if (parser.hasNext(2)) {
/* 94 */       position.set("status", parser.next());
/* 95 */       position.set("odometer", Integer.valueOf(parser.nextInt(0)));
/*    */     } 
/*    */     
/* 98 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CarscopProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */