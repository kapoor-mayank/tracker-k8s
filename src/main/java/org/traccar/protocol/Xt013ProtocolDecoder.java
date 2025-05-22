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
/*    */ public class Xt013ProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public Xt013ProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .number("HI,d+").optional()
/* 38 */     .text("TK,")
/* 39 */     .number("(d+),")
/* 40 */     .number("(dd)(dd)(dd)")
/* 41 */     .number("(dd)(dd)(dd),")
/* 42 */     .number("([+-]d+.d+),")
/* 43 */     .number("([+-]d+.d+),")
/* 44 */     .number("(d+),")
/* 45 */     .number("(d+),")
/* 46 */     .number("d+,")
/* 47 */     .number("(d+),")
/* 48 */     .expression("([FL]),")
/* 49 */     .number("d+,")
/* 50 */     .number("(d+),")
/* 51 */     .number("x+,")
/* 52 */     .number("x+,")
/* 53 */     .number("(d+),")
/* 54 */     .expression("[^,]*,")
/* 55 */     .number("(d+.d+),")
/* 56 */     .number("(d),")
/* 57 */     .any()
/* 58 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 64 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 65 */     if (!parser.matches()) {
/* 66 */       return null;
/*    */     }
/*    */     
/* 69 */     Position position = new Position(getProtocolName());
/*    */     
/* 71 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 72 */     if (deviceSession == null) {
/* 73 */       return null;
/*    */     }
/* 75 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 77 */     position.setTime(parser.nextDateTime());
/*    */     
/* 79 */     position.setLatitude(parser.nextDouble(0.0D));
/* 80 */     position.setLongitude(parser.nextDouble(0.0D));
/* 81 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/* 82 */     position.setCourse(parser.nextDouble(0.0D));
/* 83 */     position.setAltitude(parser.nextDouble(0.0D));
/* 84 */     position.setValid(parser.next().equals("F"));
/*    */     
/* 86 */     position.set("sat", parser.nextInt());
/* 87 */     position.set("rssi", parser.nextDouble());
/* 88 */     position.set("battery", Double.valueOf(parser.nextDouble(0.0D)));
/* 89 */     position.set("charge", Boolean.valueOf(parser.next().equals("1")));
/*    */     
/* 91 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Xt013ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */