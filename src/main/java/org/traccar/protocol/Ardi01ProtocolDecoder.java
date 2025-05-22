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
/*    */ public class Ardi01ProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public Ardi01ProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .number("(d+),")
/* 38 */     .number("(dddd)(dd)(dd)")
/* 39 */     .number("(dd)(dd)(dd),")
/* 40 */     .number("(-?d+.d+),")
/* 41 */     .number("(-?d+.d+),")
/* 42 */     .number("(d+.?d*),")
/* 43 */     .number("(d+.?d*),")
/* 44 */     .number("(-?d+.?d*),")
/* 45 */     .number("(d+),")
/* 46 */     .number("(d+),")
/* 47 */     .number("(d+),")
/* 48 */     .number("(-?d+)")
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
/* 68 */     position.setTime(parser.nextDateTime());
/*    */     
/* 70 */     position.setLongitude(parser.nextDouble(0.0D));
/* 71 */     position.setLatitude(parser.nextDouble(0.0D));
/* 72 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/* 73 */     position.setCourse(parser.nextDouble(0.0D));
/* 74 */     position.setAltitude(parser.nextDouble(0.0D));
/*    */     
/* 76 */     int satellites = parser.nextInt(0);
/* 77 */     position.setValid((satellites >= 3));
/* 78 */     position.set("sat", Integer.valueOf(satellites));
/*    */     
/* 80 */     position.set("event", parser.next());
/* 81 */     position.set("batteryLevel", Integer.valueOf(parser.nextInt(0)));
/* 82 */     position.set("temp1", parser.next());
/*    */     
/* 84 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Ardi01ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */