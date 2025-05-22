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
/*    */ public class ArknavProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public ArknavProtocolDecoder(Protocol protocol) {
/* 32 */     super(protocol);
/*    */   }
/*    */   
/* 35 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 36 */     .number("(d+),")
/* 37 */     .expression(".{6},")
/* 38 */     .number("ddd,")
/* 39 */     .number("Lddd,")
/* 40 */     .expression("([AV]),")
/* 41 */     .number("(dd)(dd.d+),")
/* 42 */     .expression("([NS]),")
/* 43 */     .number("(ddd)(dd.d+),")
/* 44 */     .expression("([EW]),")
/* 45 */     .number("(d+.?d*),")
/* 46 */     .number("(d+.?d*),")
/* 47 */     .number("(d+.?d*),")
/* 48 */     .number("(dd):(dd):(dd) ")
/* 49 */     .number("(dd)-(dd)-(dd),")
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
/* 70 */     position.setValid(parser.next().equals("A"));
/* 71 */     position.setLatitude(parser.nextCoordinate());
/* 72 */     position.setLongitude(parser.nextCoordinate());
/* 73 */     position.setSpeed(parser.nextDouble(0.0D));
/* 74 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 76 */     position.set("hdop", Double.valueOf(parser.nextDouble(0.0D)));
/*    */     
/* 78 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));
/*    */     
/* 80 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\ArknavProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */