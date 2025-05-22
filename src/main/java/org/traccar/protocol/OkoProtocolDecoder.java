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
/*    */ public class OkoProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public OkoProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .text("{")
/* 38 */     .number("(d{15}),").optional()
/* 39 */     .number("(dd)(dd)(dd).d+,")
/* 40 */     .expression("([AV]),")
/* 41 */     .number("(dd)(dd.d+),")
/* 42 */     .expression("([NS]),")
/* 43 */     .number("(ddd)(dd.d+),")
/* 44 */     .expression("([EW]),")
/* 45 */     .number("(d+.?d*)?,")
/* 46 */     .number("(d+.?d*)?,")
/* 47 */     .number("(dd)(dd)(dd),")
/* 48 */     .number("(d+),")
/* 49 */     .number("(d+.d+),")
/* 50 */     .number("(xx),")
/* 51 */     .number("(d+.d+),")
/* 52 */     .number("d,")
/* 53 */     .number("(xx)")
/* 54 */     .any()
/* 55 */     .compile();
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*    */     DeviceSession deviceSession;
/* 61 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 62 */     if (!parser.matches()) {
/* 63 */       return null;
/*    */     }
/*    */ 
/*    */     
/* 67 */     if (parser.hasNext()) {
/* 68 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*    */     } else {
/* 70 */       deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/*    */     } 
/* 72 */     if (deviceSession == null) {
/* 73 */       return null;
/*    */     }
/*    */     
/* 76 */     Position position = new Position(getProtocolName());
/* 77 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */ 
/*    */     
/* 80 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*    */     
/* 82 */     position.setValid(parser.next().equals("A"));
/* 83 */     position.setLatitude(parser.nextCoordinate());
/* 84 */     position.setLongitude(parser.nextCoordinate());
/* 85 */     position.setSpeed(parser.nextDouble(0.0D));
/* 86 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 88 */     dateBuilder.setDateReverse(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/* 89 */     position.setTime(dateBuilder.getDate());
/*    */     
/* 91 */     position.set("sat", parser.nextInt());
/* 92 */     position.set("adc1", parser.nextDouble());
/* 93 */     position.set("event", parser.next());
/* 94 */     position.set("power", parser.nextDouble());
/* 95 */     position.set("input", parser.nextHexInt());
/*    */     
/* 97 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OkoProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */