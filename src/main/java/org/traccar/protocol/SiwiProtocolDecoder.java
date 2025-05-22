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
/*    */ public class SiwiProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public SiwiProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .text("$").expression("[A-Z]+,")
/* 38 */     .number("(d+),")
/* 39 */     .number("d+,")
/* 40 */     .expression("([A-Z]),")
/* 41 */     .number("d+,")
/* 42 */     .number("[^,]*,")
/* 43 */     .expression("([01]),")
/* 44 */     .expression("[01],")
/* 45 */     .expression("[01],")
/* 46 */     .number("d+,")
/* 47 */     .number("(d+),")
/* 48 */     .number("(d+),")
/* 49 */     .number("(d+),")
/* 50 */     .expression("([AV]),")
/* 51 */     .number("(-?d+.d+),")
/* 52 */     .number("(-?d+.d+),")
/* 53 */     .number("(-?d+),")
/* 54 */     .number("(d+),")
/* 55 */     .number("(dd)(dd)(dd),")
/* 56 */     .number("(dd)(dd)(dd),")
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
/* 69 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 70 */     if (deviceSession == null) {
/* 71 */       return null;
/*    */     }
/*    */     
/* 74 */     Position position = new Position(getProtocolName());
/* 75 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 77 */     position.set("event", parser.next());
/* 78 */     position.set("ignition", Boolean.valueOf(parser.next().equals("1")));
/* 79 */     position.set("odometer", Integer.valueOf(parser.nextInt(0)));
/*    */     
/* 81 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt(0)));
/*    */     
/* 83 */     position.set("sat", Integer.valueOf(parser.nextInt(0)));
/*    */     
/* 85 */     position.setValid(parser.next().equals("A"));
/* 86 */     position.setLatitude(parser.nextDouble(0.0D));
/* 87 */     position.setLongitude(parser.nextDouble(0.0D));
/* 88 */     position.setAltitude(parser.nextDouble(0.0D));
/* 89 */     position.setCourse(parser.nextInt(0));
/*    */     
/* 91 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY, "IST"));
/*    */     
/* 93 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SiwiProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */