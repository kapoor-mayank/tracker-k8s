/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import java.util.Date;
/*    */ import java.util.regex.Pattern;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.NetworkMessage;
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
/*    */ public class GpsmtaProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public GpsmtaProtocolDecoder(Protocol protocol) {
/* 34 */     super(protocol);
/*    */   }
/*    */   
/* 37 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 38 */     .expression("([^ ]+) ")
/* 39 */     .number("(d+) ")
/* 40 */     .number("(-?d+.d+) ")
/* 41 */     .number("(-?d+.d+) ")
/* 42 */     .number("(d+) ")
/* 43 */     .number("(d+) ")
/* 44 */     .number("(d+) ")
/* 45 */     .number("(d+) ")
/* 46 */     .number("(d+) ")
/* 47 */     .number("(d+) ")
/* 48 */     .number("(d+) ")
/* 49 */     .number("(d)")
/* 50 */     .any()
/* 51 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 57 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 58 */     if (!parser.matches()) {
/* 59 */       return null;
/*    */     }
/*    */     
/* 62 */     Position position = new Position(getProtocolName());
/*    */     
/* 64 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 65 */     if (deviceSession == null) {
/* 66 */       return null;
/*    */     }
/* 68 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 70 */     String time = parser.next();
/* 71 */     position.setTime(new Date(Long.parseLong(time) * 1000L));
/*    */     
/* 73 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 74 */     position.setLongitude(parser.nextDouble().doubleValue());
/* 75 */     position.setSpeed(parser.nextInt().intValue());
/* 76 */     position.setCourse(parser.nextInt().intValue());
/* 77 */     position.setAccuracy(parser.nextInt().intValue());
/* 78 */     position.setAltitude(parser.nextInt().intValue());
/*    */     
/* 80 */     position.set("status", parser.nextInt());
/* 81 */     position.set("batteryLevel", parser.nextInt());
/* 82 */     position.set("temp1", parser.nextInt());
/* 83 */     position.set("charge", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/*    */     
/* 85 */     if (channel != null) {
/* 86 */       channel.writeAndFlush(new NetworkMessage(time, remoteAddress));
/*    */     }
/*    */     
/* 89 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GpsmtaProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */