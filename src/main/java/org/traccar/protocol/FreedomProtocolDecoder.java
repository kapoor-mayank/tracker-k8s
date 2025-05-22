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
/*    */ public class FreedomProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public FreedomProtocolDecoder(Protocol protocol) {
/* 32 */     super(protocol);
/*    */   }
/*    */   
/* 35 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 36 */     .text("IMEI,")
/* 37 */     .number("(d+),")
/* 38 */     .number("(dddd)/(dd)/(dd), ")
/* 39 */     .number("(dd):(dd):(dd), ")
/* 40 */     .expression("([NS]), ")
/* 41 */     .number("Lat:(dd)(d+.d+), ")
/* 42 */     .expression("([EW]), ")
/* 43 */     .number("Lon:(ddd)(d+.d+), ")
/* 44 */     .text("Spd:").number("(d+.d+)")
/* 45 */     .any()
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
/* 65 */     position.setValid(true);
/*    */     
/* 67 */     position.setTime(parser.nextDateTime());
/*    */     
/* 69 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 70 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/*    */     
/* 72 */     position.setSpeed(parser.nextDouble(0.0D));
/*    */     
/* 74 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\FreedomProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */