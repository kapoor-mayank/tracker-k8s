/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import java.util.Date;
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
/*    */ public class CradlepointProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public CradlepointProtocolDecoder(Protocol protocol) {
/* 34 */     super(protocol);
/*    */   }
/*    */   
/* 37 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 38 */     .expression("([^,]+),")
/* 39 */     .number("(d{1,6}),")
/* 40 */     .number("(d+)(dd.d+),")
/* 41 */     .expression("([NS]),")
/* 42 */     .number("(d+)(dd.d+),")
/* 43 */     .expression("([EW]),")
/* 44 */     .number("(d+.d+)?,")
/* 45 */     .number("(d+.d+)?,")
/* 46 */     .expression("([^,]+)?,")
/* 47 */     .expression("([^,]+)?,")
/* 48 */     .number("(-?d+)?,")
/* 49 */     .number("(-?d+)?,")
/* 50 */     .number("(-?d+)?,")
/* 51 */     .expression("([^,]+)?,")
/* 52 */     .expression("([^,]+)?")
/* 53 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 59 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 60 */     if (!parser.matches()) {
/* 61 */       return null;
/*    */     }
/*    */     
/* 64 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 65 */     if (deviceSession == null) {
/* 66 */       return null;
/*    */     }
/*    */     
/* 69 */     Position position = new Position(getProtocolName());
/* 70 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 72 */     int time = parser.nextInt().intValue();
/* 73 */     DateBuilder dateBuilder = new DateBuilder(new Date());
/* 74 */     dateBuilder.setHour(time / 100 / 100);
/* 75 */     dateBuilder.setMinute(time / 100 % 100);
/* 76 */     dateBuilder.setSecond(time % 100);
/* 77 */     position.setTime(dateBuilder.getDate());
/*    */     
/* 79 */     position.setValid(true);
/* 80 */     position.setLatitude(parser.nextCoordinate());
/* 81 */     position.setLongitude(parser.nextCoordinate());
/* 82 */     position.setSpeed(parser.nextDouble(0.0D));
/* 83 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 85 */     position.set("carrid", parser.next());
/* 86 */     position.set("serdis", parser.next());
/* 87 */     position.set("rsrp", parser.nextInt());
/* 88 */     position.set("rssi", parser.nextInt());
/* 89 */     position.set("rsrq", parser.nextInt());
/* 90 */     position.set("ecio", parser.next());
/*    */     
/* 92 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CradlepointProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */