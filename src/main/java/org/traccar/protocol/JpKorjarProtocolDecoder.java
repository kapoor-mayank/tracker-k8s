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
/*    */ 
/*    */ public class JpKorjarProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public JpKorjarProtocolDecoder(Protocol protocol) {
/* 35 */     super(protocol);
/*    */   }
/*    */   
/* 38 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 39 */     .text("KORJAR.PL,")
/* 40 */     .number("(d+),")
/* 41 */     .number("(dd)(dd)(dd)")
/* 42 */     .number("(dd)(dd)(dd),")
/* 43 */     .number("(d+.d+)([NS]),")
/* 44 */     .number("(d+.d+)([EW]),")
/* 45 */     .number("(d+.d+),")
/* 46 */     .number("(d+),")
/* 47 */     .number("[FL]:(d+.d+)V,")
/* 48 */     .number("([01]) ")
/* 49 */     .number("(d+) ")
/* 50 */     .number("(d+) ")
/* 51 */     .number("(x+) ")
/* 52 */     .number("(x+),")
/* 53 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 59 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 60 */     if (!parser.matches()) {
/* 61 */       return null;
/*    */     }
/*    */     
/* 64 */     Position position = new Position(getProtocolName());
/*    */     
/* 66 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 67 */     if (deviceSession == null) {
/* 68 */       return null;
/*    */     }
/* 70 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 72 */     position.setTime(parser.nextDateTime());
/*    */     
/* 74 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 75 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
/* 76 */     position.setSpeed(parser.nextDouble(0.0D));
/* 77 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 79 */     position.set("battery", Double.valueOf(parser.nextDouble(0.0D)));
/*    */     
/* 81 */     position.setValid((parser.nextInt(0) == 1));
/*    */     
/* 83 */     position.setNetwork(new Network(CellTower.from(parser
/* 84 */             .nextInt(0), parser.nextInt(0), parser.nextHexInt(0), parser.nextHexInt(0))));
/*    */     
/* 86 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\JpKorjarProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */