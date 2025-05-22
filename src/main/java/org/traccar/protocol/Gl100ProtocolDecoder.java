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
/*    */ public class Gl100ProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public Gl100ProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .text("+RESP:")
/* 38 */     .expression("GT...,")
/* 39 */     .number("(d{15}),")
/* 40 */     .groupBegin()
/* 41 */     .number("d+,")
/* 42 */     .number("d,")
/* 43 */     .number("d+")
/* 44 */     .or()
/* 45 */     .number("[^,]*")
/* 46 */     .groupEnd(",")
/* 47 */     .expression("([01]),")
/* 48 */     .number("(d+.d),")
/* 49 */     .number("(d+),")
/* 50 */     .number("(-?d+.d),")
/* 51 */     .number("d*,")
/* 52 */     .number("(-?d+.d+),")
/* 53 */     .number("(-?d+.d+),")
/* 54 */     .number("(dddd)(dd)(dd)")
/* 55 */     .number("(dd)(dd)(dd),")
/* 56 */     .any()
/* 57 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 63 */     String sentence = (String)msg;
/*    */     
/* 65 */     if (sentence.contains("AT+GTHBD=") && channel != null) {
/* 66 */       String response = "+RESP:GTHBD,GPRS ACTIVE,";
/* 67 */       response = response + sentence.substring(9, sentence.lastIndexOf(','));
/* 68 */       response = response + Character.MIN_VALUE;
/* 69 */       channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*    */     } 
/*    */     
/* 72 */     Parser parser = new Parser(PATTERN, sentence);
/* 73 */     if (!parser.matches()) {
/* 74 */       return null;
/*    */     }
/*    */     
/* 77 */     Position position = new Position(getProtocolName());
/*    */     
/* 79 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 80 */     if (deviceSession == null) {
/* 81 */       return null;
/*    */     }
/* 83 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 85 */     position.setValid((parser.nextInt(0) == 0));
/* 86 */     position.setSpeed(parser.nextDouble(0.0D));
/* 87 */     position.setCourse(parser.nextDouble(0.0D));
/* 88 */     position.setAltitude(parser.nextDouble(0.0D));
/* 89 */     position.setLongitude(parser.nextDouble(0.0D));
/* 90 */     position.setLatitude(parser.nextDouble(0.0D));
/*    */     
/* 92 */     position.setTime(parser.nextDateTime());
/*    */     
/* 94 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Gl100ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */