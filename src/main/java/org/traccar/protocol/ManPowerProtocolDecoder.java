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
/*    */ public class ManPowerProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public ManPowerProtocolDecoder(Protocol protocol) {
/* 32 */     super(protocol);
/*    */   }
/*    */   
/* 35 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 36 */     .text("simei:")
/* 37 */     .number("(d+),")
/* 38 */     .expression("[^,]*,[^,]*,")
/* 39 */     .expression("([^,]*),")
/* 40 */     .number("d+,d+,d+.?d*,")
/* 41 */     .number("(dd)(dd)(dd)")
/* 42 */     .number("(dd)(dd)(dd),")
/* 43 */     .expression("([AV]),")
/* 44 */     .number("(dd)(dd.dddd),")
/* 45 */     .expression("([NS]),")
/* 46 */     .number("(ddd)(dd.dddd),")
/* 47 */     .expression("([EW])?,")
/* 48 */     .number("(d+.?d*),")
/* 49 */     .any()
/* 50 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 56 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 57 */     if (!parser.matches()) {
/* 58 */       return null;
/*    */     }
/*    */     
/* 61 */     Position position = new Position(getProtocolName());
/*    */     
/* 63 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 64 */     if (deviceSession == null) {
/* 65 */       return null;
/*    */     }
/* 67 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 69 */     position.set("status", parser.next());
/*    */     
/* 71 */     position.setTime(parser.nextDateTime());
/*    */     
/* 73 */     position.setValid(parser.next().equals("A"));
/* 74 */     position.setLatitude(parser.nextCoordinate());
/* 75 */     position.setLongitude(parser.nextCoordinate());
/* 76 */     position.setSpeed(parser.nextDouble(0.0D));
/*    */     
/* 78 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ManPowerProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */