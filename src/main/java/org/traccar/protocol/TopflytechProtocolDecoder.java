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
/*    */ public class TopflytechProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public TopflytechProtocolDecoder(Protocol protocol) {
/* 32 */     super(protocol);
/*    */   }
/*    */   
/* 35 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 36 */     .text("(")
/* 37 */     .number("(d+)")
/* 38 */     .any()
/* 39 */     .number("(dd)(dd)(dd)")
/* 40 */     .number("(dd)(dd)(dd)")
/* 41 */     .expression("([AV])")
/* 42 */     .number("(dd)(dd.dddd)([NS])")
/* 43 */     .number("(ddd)(dd.dddd)([EW])")
/* 44 */     .number("(ddd.d)")
/* 45 */     .number("(d+.d+)")
/* 46 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 52 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 53 */     if (!parser.matches()) {
/* 54 */       return null;
/*    */     }
/*    */     
/* 57 */     Position position = new Position(getProtocolName());
/*    */     
/* 59 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 60 */     if (deviceSession == null) {
/* 61 */       return null;
/*    */     }
/* 63 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 65 */     position.setTime(parser.nextDateTime());
/*    */     
/* 67 */     position.setValid(parser.next().equals("A"));
/* 68 */     position.setLatitude(parser.nextCoordinate());
/* 69 */     position.setLongitude(parser.nextCoordinate());
/* 70 */     position.setSpeed(parser.nextDouble(0.0D));
/* 71 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 73 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TopflytechProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */