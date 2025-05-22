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
/*    */ public class AppelloProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public AppelloProtocolDecoder(Protocol protocol) {
/* 32 */     super(protocol);
/*    */   }
/*    */   
/* 35 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 36 */     .text("FOLLOWIT,")
/* 37 */     .number("(d+),")
/* 38 */     .groupBegin()
/* 39 */     .number("(dd)(dd)(dd)")
/* 40 */     .number("(dd)(dd)(dd).?d*,")
/* 41 */     .or()
/* 42 */     .text("UTCTIME,")
/* 43 */     .groupEnd()
/* 44 */     .number("(-?d+.d+),")
/* 45 */     .number("(-?d+.d+),")
/* 46 */     .number("(d+),")
/* 47 */     .number("(d+),")
/* 48 */     .number("(d+),")
/* 49 */     .number("(-?d+),")
/* 50 */     .expression("([FL]),")
/* 51 */     .any()
/* 52 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 58 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 59 */     if (!parser.matches()) {
/* 60 */       return null;
/*    */     }
/*    */     
/* 63 */     String imei = parser.next();
/* 64 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/* 65 */     if (deviceSession == null) {
/* 66 */       return null;
/*    */     }
/*    */     
/* 69 */     Position position = new Position(getProtocolName());
/* 70 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 72 */     if (parser.hasNext(6)) {
/* 73 */       position.setTime(parser.nextDateTime());
/*    */     } else {
/* 75 */       getLastLocation(position, null);
/*    */     } 
/*    */     
/* 78 */     position.setLatitude(parser.nextDouble(0.0D));
/* 79 */     position.setLongitude(parser.nextDouble(0.0D));
/* 80 */     position.setSpeed(parser.nextDouble(0.0D));
/* 81 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 83 */     position.set("sat", Integer.valueOf(parser.nextInt(0)));
/*    */     
/* 85 */     position.setAltitude(parser.nextDouble(0.0D));
/*    */     
/* 87 */     position.setValid(parser.next().equals("F"));
/*    */     
/* 89 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AppelloProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */