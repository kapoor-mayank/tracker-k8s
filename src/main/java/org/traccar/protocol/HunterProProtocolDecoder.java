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
/*    */ public class HunterProProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public HunterProProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .number(">(d+)<")
/* 38 */     .text("$GPRMC,")
/* 39 */     .number("(dd)(dd)(dd).?d*,")
/* 40 */     .expression("([AV]),")
/* 41 */     .number("(dd)(dd.d+),")
/* 42 */     .expression("([NS]),")
/* 43 */     .number("(ddd)(dd.d+),")
/* 44 */     .expression("([EW]),")
/* 45 */     .number("(d+.?d*)?,")
/* 46 */     .number("(d+.?d*)?,")
/* 47 */     .number("(dd)(dd)(dd)")
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
/* 60 */     Position position = new Position(getProtocolName());
/*    */     
/* 62 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 63 */     if (deviceSession == null) {
/* 64 */       return null;
/*    */     }
/* 66 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 68 */     DateBuilder dateBuilder = new DateBuilder();
/* 69 */     dateBuilder.setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*    */     
/* 71 */     position.setValid(parser.next().equals("A"));
/* 72 */     position.setLatitude(parser.nextCoordinate());
/* 73 */     position.setLongitude(parser.nextCoordinate());
/* 74 */     position.setSpeed(parser.nextDouble(0.0D));
/* 75 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 77 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 78 */     position.setTime(dateBuilder.getDate());
/*    */     
/* 80 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\HunterProProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */