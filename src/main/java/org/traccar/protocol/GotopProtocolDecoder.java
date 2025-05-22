/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import java.util.regex.Pattern;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.helper.Parser;
/*    */ import org.traccar.helper.PatternBuilder;
/*    */ import org.traccar.helper.UnitsConverter;
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
/*    */ public class GotopProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public GotopProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .number("(d+),")
/* 38 */     .expression("([^,]+),")
/* 39 */     .expression("([AV]),")
/* 40 */     .number("DATE:(dd)(dd)(dd),")
/* 41 */     .number("TIME:(dd)(dd)(dd),")
/* 42 */     .number("LAT:(d+.d+)([NS]),")
/* 43 */     .number("LO[NT]:(d+.d+)([EW]),")
/* 44 */     .text("Speed:").number("(d+.d+),")
/* 45 */     .expression("([^,]+),")
/* 46 */     .number("(d+)?")
/* 47 */     .any()
/* 48 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 54 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 55 */     if (!parser.matches()) {
/* 56 */       return null;
/*    */     }
/*    */     
/* 59 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 60 */     if (deviceSession == null) {
/* 61 */       return null;
/*    */     }
/*    */     
/* 64 */     Position position = new Position(getProtocolName());
/* 65 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 67 */     String type = parser.next();
/* 68 */     if (type.equals("CMD-KEY")) {
/* 69 */       position.set("alarm", "sos");
/* 70 */     } else if (type.startsWith("ALM-B")) {
/*    */       String alarm;
/* 72 */       if (Character.getNumericValue(type.charAt(5)) % 2 > 0) {
/* 73 */         alarm = "geofenceEnter";
/*    */       } else {
/* 75 */         alarm = "geofenceExit";
/*    */       } 
/* 77 */       position.set("alarm", alarm + '_' + type.substring(type.lastIndexOf('-') + 1));
/*    */     } 
/*    */     
/* 80 */     position.setValid(parser.next().equals("A"));
/*    */     
/* 82 */     position.setTime(parser.nextDateTime());
/*    */     
/* 84 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 85 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 86 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/*    */     
/* 88 */     position.set("status", parser.next());
/*    */     
/* 90 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 92 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GotopProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */