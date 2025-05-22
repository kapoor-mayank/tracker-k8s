/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import java.util.regex.Pattern;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.helper.BitUtil;
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
/*    */ public class NtoProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public NtoProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .text("^NB,")
/* 38 */     .number("(d+),")
/* 39 */     .expression("(...),")
/* 40 */     .number("(dd)(dd)(dd),")
/* 41 */     .number("(dd)(dd)(dd),")
/* 42 */     .expression("([AVM]),")
/* 43 */     .number("([NS]),(dd)(dd.d+),")
/* 44 */     .number("([EW]),(ddd)(dd.d+),")
/* 45 */     .number("(d+.?d*),")
/* 46 */     .number("(d+),")
/* 47 */     .number("(x+),")
/* 48 */     .any()
/* 49 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 55 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 56 */     if (!parser.matches()) {
/* 57 */       return null;
/*    */     }
/*    */     
/* 60 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 61 */     if (deviceSession == null) {
/* 62 */       return null;
/*    */     }
/*    */     
/* 65 */     Position position = new Position(getProtocolName());
/* 66 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 68 */     position.set("type", parser.next());
/*    */     
/* 70 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*    */     
/* 72 */     position.setValid(parser.next().equals("A"));
/* 73 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 74 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 75 */     position.setSpeed(parser.nextDouble().doubleValue());
/* 76 */     position.setCourse(parser.nextInt().intValue());
/*    */     
/* 78 */     long status = parser.nextHexLong().longValue();
/* 79 */     position.set("status", Long.valueOf(status));
/* 80 */     position.set("alarm", BitUtil.check(status, 1) ? "jamming" : null);
/* 81 */     position.set("alarm", BitUtil.check(status, 25) ? "powerCut" : null);
/* 82 */     position.set("alarm", BitUtil.check(status, 26) ? "overspeed" : null);
/* 83 */     position.set("alarm", BitUtil.check(status, 27) ? "vibration" : null);
/* 84 */     position.set("alarm", BitUtil.check(status, 28) ? "geofenceEnter" : null);
/* 85 */     position.set("alarm", BitUtil.check(status, 29) ? "geofenceExit" : null);
/* 86 */     position.set("alarm", BitUtil.check(status, 32) ? "lowBattery" : null);
/* 87 */     position.set("alarm", BitUtil.check(status, 36) ? "door" : null);
/*    */     
/* 89 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NtoProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */