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
/*    */ public class HomtecsProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public HomtecsProtocolDecoder(Protocol protocol) {
/* 32 */     super(protocol);
/*    */   }
/*    */   
/* 35 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 36 */     .expression("([^_]+)")
/* 37 */     .text("_R")
/* 38 */     .number("(x{8}),")
/* 39 */     .number("(dd)(dd)(dd),")
/* 40 */     .number("(dd)(dd)(dd).d+,")
/* 41 */     .number("(d+),")
/* 42 */     .number("(dd)(dd.d+),")
/* 43 */     .expression("([NS]),")
/* 44 */     .number("(ddd)(dd.d+),")
/* 45 */     .expression("([EW]),")
/* 46 */     .number("(d+.?d*)?,")
/* 47 */     .number("(d+.?d*)?,")
/* 48 */     .number("(d),")
/* 49 */     .number("(d+.?d*)?,")
/* 50 */     .number("(d+.?d*)?")
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
/* 62 */     String id = parser.next();
/* 63 */     String mac = parser.next();
/*    */     
/* 65 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { id, id + "_R" + mac });
/* 66 */     if (deviceSession == null) {
/* 67 */       return null;
/*    */     }
/*    */     
/* 70 */     Position position = new Position(getProtocolName());
/* 71 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 73 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.YMD_HMS));
/*    */     
/* 75 */     position.set("sat", Integer.valueOf(parser.nextInt(0)));
/*    */     
/* 77 */     position.setLatitude(parser.nextCoordinate());
/* 78 */     position.setLongitude(parser.nextCoordinate());
/* 79 */     position.setSpeed(parser.nextDouble(0.0D));
/* 80 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 82 */     position.setValid((parser.nextInt(0) > 0));
/*    */     
/* 84 */     position.set("hdop", Double.valueOf(parser.nextDouble(0.0D)));
/*    */     
/* 86 */     position.setAltitude(parser.nextDouble(0.0D));
/*    */     
/* 88 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\HomtecsProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */