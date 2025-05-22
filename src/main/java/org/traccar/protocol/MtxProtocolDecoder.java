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
/*    */ public class MtxProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public MtxProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .text("#MTX,")
/* 38 */     .number("(d+),")
/* 39 */     .number("(dddd)(dd)(dd),")
/* 40 */     .number("(dd)(dd)(dd),")
/* 41 */     .number("(-?d+.d+),")
/* 42 */     .number("(-?d+.d+),")
/* 43 */     .number("(d+.?d*),")
/* 44 */     .number("(d+),")
/* 45 */     .number("(d+.?d*),")
/* 46 */     .groupBegin()
/* 47 */     .number("d+")
/* 48 */     .or()
/* 49 */     .text("X")
/* 50 */     .groupEnd()
/* 51 */     .text(",")
/* 52 */     .expression("(?:[01]|X),")
/* 53 */     .expression("([01]+),")
/* 54 */     .expression("([01]+),")
/* 55 */     .number("(d+),")
/* 56 */     .number("(d+)")
/* 57 */     .any()
/* 58 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 64 */     if (channel != null) {
/* 65 */       channel.writeAndFlush(new NetworkMessage("#ACK", remoteAddress));
/*    */     }
/*    */     
/* 68 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 69 */     if (!parser.matches()) {
/* 70 */       return null;
/*    */     }
/*    */     
/* 73 */     Position position = new Position(getProtocolName());
/*    */     
/* 75 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 76 */     if (deviceSession == null) {
/* 77 */       return null;
/*    */     }
/* 79 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 81 */     position.setTime(parser.nextDateTime());
/*    */     
/* 83 */     position.setValid(true);
/* 84 */     position.setLatitude(parser.nextDouble(0.0D));
/* 85 */     position.setLongitude(parser.nextDouble(0.0D));
/* 86 */     position.setSpeed(parser.nextDouble(0.0D));
/* 87 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 89 */     position.set("odometer", Double.valueOf(parser.nextDouble(0.0D) * 1000.0D));
/* 90 */     position.set("input", parser.next());
/* 91 */     position.set("output", parser.next());
/* 92 */     position.set("adc1", parser.next());
/* 93 */     position.set("adc2", parser.next());
/*    */     
/* 95 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MtxProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */