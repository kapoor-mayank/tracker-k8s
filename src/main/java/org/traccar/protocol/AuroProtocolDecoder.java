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
/*    */ public class AuroProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public AuroProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .number("M(dddd)")
/* 38 */     .number("Td+")
/* 39 */     .number("I(d+)")
/* 40 */     .number("Ed+W")
/* 41 */     .text("*****")
/* 42 */     .number("d{8}d{4}")
/* 43 */     .expression(".{8}#.{8}")
/* 44 */     .number("d{10}")
/* 45 */     .number("([-+])(ddd)(dd)(dddd)")
/* 46 */     .number("([-+])(ddd)(dd)(dddd)")
/* 47 */     .number("(dd)(dd)(dddd)")
/* 48 */     .number("(dd)(dd)(dd)")
/* 49 */     .number("(ddd)")
/* 50 */     .number("d{6}")
/* 51 */     .number("(ddd)")
/* 52 */     .number("d")
/* 53 */     .number("(dd)")
/* 54 */     .expression("([01])")
/* 55 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 61 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 62 */     if (!parser.matches()) {
/* 63 */       return null;
/*    */     }
/*    */     
/* 66 */     Position position = new Position(getProtocolName());
/*    */     
/* 68 */     position.set("index", Integer.valueOf(parser.nextInt(0)));
/*    */     
/* 70 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 71 */     if (deviceSession == null) {
/* 72 */       return null;
/*    */     }
/* 74 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 76 */     position.setValid(true);
/* 77 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_MIN));
/* 78 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_MIN));
/*    */     
/* 80 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*    */     
/* 82 */     position.setCourse(parser.nextDouble(0.0D));
/* 83 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/*    */     
/* 85 */     position.set("battery", Integer.valueOf(parser.nextInt(0)));
/* 86 */     position.set("charge", Boolean.valueOf((parser.nextInt(0) == 1)));
/*    */     
/* 88 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AuroProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */