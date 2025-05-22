/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
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
/*    */ public class NeosProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public NeosProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .text(">")
/* 38 */     .number("(d{8}),")
/* 39 */     .number("d+,")
/* 40 */     .number("([01]),")
/* 41 */     .number("(dd)(dd)(dd),")
/* 42 */     .number("(dd)(dd)(dd),")
/* 43 */     .expression("([EW])")
/* 44 */     .number("(d+)(dd.d+),")
/* 45 */     .expression("([NS])")
/* 46 */     .number("(d+)(dd.d+),")
/* 47 */     .expression("[^,]*,")
/* 48 */     .number("(d+),")
/* 49 */     .number("(d+),")
/* 50 */     .number("(d+),")
/* 51 */     .expression("[^,]*,")
/* 52 */     .number("(d+)-")
/* 53 */     .number("(d+),")
/* 54 */     .number("0,")
/* 55 */     .number("d,")
/* 56 */     .number("([01]{8})")
/* 57 */     .text("*")
/* 58 */     .number("xx!")
/* 59 */     .any()
/* 60 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 66 */     if (channel != null) {
/* 67 */       channel.writeAndFlush(new NetworkMessage("$OK!", remoteAddress));
/*    */     }
/*    */     
/* 70 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 71 */     if (!parser.matches()) {
/* 72 */       return null;
/*    */     }
/*    */     
/* 75 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 76 */     if (deviceSession == null) {
/* 77 */       return null;
/*    */     }
/*    */     
/* 80 */     Position position = new Position(getProtocolName());
/* 81 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 83 */     position.setValid((parser.nextInt().intValue() > 0));
/* 84 */     position.setTime(parser.nextDateTime());
/* 85 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 86 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/* 87 */     position.setSpeed(parser.nextInt().intValue());
/* 88 */     position.setCourse(parser.nextInt().intValue());
/*    */     
/* 90 */     position.set("rssi", parser.nextInt());
/* 91 */     position.set("adc1", parser.nextInt());
/* 92 */     position.set("batteryLevel", parser.nextInt());
/* 93 */     position.set("input", parser.nextBinInt());
/*    */     
/* 95 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NeosProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */