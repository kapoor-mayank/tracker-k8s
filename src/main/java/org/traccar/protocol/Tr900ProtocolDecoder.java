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
/*    */ public class Tr900ProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public Tr900ProtocolDecoder(Protocol protocol) {
/* 32 */     super(protocol);
/*    */   }
/*    */   
/* 35 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 36 */     .number(">(d+),")
/* 37 */     .number("d+,")
/* 38 */     .number("(d),")
/* 39 */     .number("(dd)(dd)(dd),")
/* 40 */     .number("(dd)(dd)(dd),")
/* 41 */     .expression("([EW])")
/* 42 */     .number("(ddd)(dd.d+),")
/* 43 */     .expression("([NS])")
/* 44 */     .number("(dd)(dd.d+),")
/* 45 */     .expression("[^,]*,")
/* 46 */     .number("(d+.?d*),")
/* 47 */     .number("(d+.?d*),")
/* 48 */     .number("(d+),")
/* 49 */     .number("(d+),")
/* 50 */     .number("(d+)-")
/* 51 */     .number("(d+),")
/* 52 */     .number("d+,")
/* 53 */     .number("(d+),")
/* 54 */     .number("(d+)")
/* 55 */     .any()
/* 56 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 62 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 63 */     if (!parser.matches()) {
/* 64 */       return null;
/*    */     }
/*    */     
/* 67 */     Position position = new Position(getProtocolName());
/*    */     
/* 69 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 70 */     if (deviceSession == null) {
/* 71 */       return null;
/*    */     }
/* 73 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 75 */     position.setValid((parser.nextInt(0) == 1));
/*    */     
/* 77 */     position.setTime(parser.nextDateTime());
/*    */     
/* 79 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 80 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 81 */     position.setSpeed(parser.nextDouble(0.0D));
/* 82 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 84 */     position.set("rssi", parser.nextDouble());
/* 85 */     position.set("event", Integer.valueOf(parser.nextInt(0)));
/* 86 */     position.set("adc1", Integer.valueOf(parser.nextInt(0)));
/* 87 */     position.set("battery", Integer.valueOf(parser.nextInt(0)));
/* 88 */     position.set("input", parser.next());
/* 89 */     position.set("status", parser.next());
/*    */     
/* 91 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Tr900ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */