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
/*    */ public class RaveonProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public RaveonProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .text("$PRAVE,")
/* 38 */     .number("(d+),")
/* 39 */     .number("d+,")
/* 40 */     .number("(-?)(d+)(dd.d+),")
/* 41 */     .number("(-?)(d+)(dd.d+),")
/* 42 */     .number("(dd)(dd)(dd),")
/* 43 */     .number("(d),")
/* 44 */     .number("(d+),")
/* 45 */     .number("(-?d+),")
/* 46 */     .number("(-?d+),")
/* 47 */     .number("(d+.d+),")
/* 48 */     .number("(d+),")
/* 49 */     .number("(-?d+),")
/* 50 */     .number("(d+),")
/* 51 */     .number("(d+),")
/* 52 */     .expression("([PMACIVSX])?,")
/* 53 */     .any()
/* 54 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 60 */     String sentence = (String)msg;
/*    */     
/* 62 */     Parser parser = new Parser(PATTERN, sentence);
/* 63 */     if (!parser.matches()) {
/* 64 */       return null;
/*    */     }
/*    */     
/* 67 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 68 */     if (deviceSession == null) {
/* 69 */       return null;
/*    */     }
/*    */     
/* 72 */     Position position = new Position(getProtocolName());
/* 73 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 75 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 76 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/*    */     
/* 78 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS));
/*    */     
/* 80 */     position.setValid((parser.nextInt(0) != 0));
/*    */     
/* 82 */     position.set("sat", Integer.valueOf(parser.nextInt(0)));
/*    */     
/* 84 */     position.setAltitude(parser.nextInt(0));
/*    */     
/* 86 */     position.set("temp1", Integer.valueOf(parser.nextInt(0)));
/* 87 */     position.set("power", Double.valueOf(parser.nextDouble(0.0D)));
/* 88 */     position.set("input", Integer.valueOf(parser.nextInt(0)));
/* 89 */     position.set("rssi", Integer.valueOf(parser.nextInt(0)));
/*    */     
/* 91 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt(0)));
/* 92 */     position.setCourse(parser.nextInt(0));
/*    */     
/* 94 */     position.set("alarm", parser.next());
/*    */     
/* 96 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\RaveonProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */