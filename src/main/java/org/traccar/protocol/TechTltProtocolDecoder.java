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
/*    */ import org.traccar.model.CellTower;
/*    */ import org.traccar.model.Network;
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
/*    */ public class TechTltProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public TechTltProtocolDecoder(Protocol protocol) {
/* 35 */     super(protocol);
/*    */   }
/*    */   
/* 38 */   private static final Pattern PATTERN_POSITION = (new PatternBuilder())
/* 39 */     .number("(d+)")
/* 40 */     .text("*POS=Y,")
/* 41 */     .number("(dd):(dd):(dd),")
/* 42 */     .number("(dd)/(dd)/(dd),")
/* 43 */     .number("(dd)(dd.d+)")
/* 44 */     .expression("([NS]),")
/* 45 */     .number("(ddd)(dd.d+)")
/* 46 */     .expression("([EW]),")
/* 47 */     .number("(d+.d+),")
/* 48 */     .number("(d+.d+),")
/* 49 */     .number("(d+.d+),")
/* 50 */     .number("(d+),")
/* 51 */     .number("(d+),")
/* 52 */     .number("(d+)")
/* 53 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 59 */     Parser parser = new Parser(PATTERN_POSITION, (String)msg);
/* 60 */     if (!parser.matches()) {
/* 61 */       return null;
/*    */     }
/*    */     
/* 64 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 65 */     if (deviceSession == null) {
/* 66 */       return null;
/*    */     }
/*    */     
/* 69 */     Position position = new Position(getProtocolName());
/* 70 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 72 */     position.setValid(true);
/* 73 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));
/* 74 */     position.setLatitude(parser.nextCoordinate());
/* 75 */     position.setLongitude(parser.nextCoordinate());
/* 76 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/* 77 */     position.setCourse(parser.nextDouble().doubleValue());
/* 78 */     position.setAltitude(parser.nextDouble().doubleValue());
/*    */     
/* 80 */     position.set("sat", parser.nextInt());
/*    */     
/* 82 */     position.setNetwork(new Network(CellTower.fromLacCid(parser.nextInt().intValue(), parser.nextInt().intValue())));
/*    */     
/* 84 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TechTltProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */