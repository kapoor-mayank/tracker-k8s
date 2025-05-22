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
/*    */ public class GpsMarkerProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public GpsMarkerProtocolDecoder(Protocol protocol) {
/* 32 */     super(protocol);
/*    */   }
/*    */   
/* 35 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 36 */     .text("$GM")
/* 37 */     .number("d")
/* 38 */     .number("(?:xx)?")
/* 39 */     .number("(d{15})")
/* 40 */     .number("T(dd)(dd)(dd)")
/* 41 */     .number("(dd)(dd)(dd)?")
/* 42 */     .expression("([NS])")
/* 43 */     .number("(dd)(dd)(dddd)")
/* 44 */     .expression("([EW])")
/* 45 */     .number("(ddd)(dd)(dddd)")
/* 46 */     .number("(ddd)")
/* 47 */     .number("(ddd)")
/* 48 */     .number("(x)")
/* 49 */     .number("(dd)")
/* 50 */     .number("(d)")
/* 51 */     .number("(d)")
/* 52 */     .number("(ddd)")
/* 53 */     .any()
/* 54 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 60 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 61 */     if (!parser.matches()) {
/* 62 */       return null;
/*    */     }
/*    */     
/* 65 */     Position position = new Position(getProtocolName());
/*    */     
/* 67 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 68 */     if (deviceSession == null) {
/* 69 */       return null;
/*    */     }
/* 71 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 73 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*    */     
/* 75 */     position.setValid(true);
/* 76 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_MIN));
/* 77 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_MIN));
/* 78 */     position.setSpeed(parser.nextDouble(0.0D));
/* 79 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 81 */     position.set("sat", Integer.valueOf(parser.nextHexInt(0)));
/* 82 */     position.set("batteryLevel", Integer.valueOf(parser.nextInt(0)));
/* 83 */     position.set("input", parser.next());
/* 84 */     position.set("output", parser.next());
/* 85 */     position.set("temp1", parser.next());
/*    */     
/* 87 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GpsMarkerProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */