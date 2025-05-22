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
/*    */ public class SmartSoleProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public SmartSoleProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .text("#GTXRP=")
/* 38 */     .number("(d+),")
/* 39 */     .number("d+,")
/* 40 */     .number("(dd)(dd)(dd)")
/* 41 */     .number("(dd)(dd)(dd),")
/* 42 */     .number("(-?d+.d+),")
/* 43 */     .number("(-?d+.d+),")
/* 44 */     .number("(-?d+),")
/* 45 */     .number("(d+),")
/* 46 */     .number("([01]),")
/* 47 */     .number("(d+),")
/* 48 */     .number("(d+.d+),")
/* 49 */     .number("(dd)(dd)(dd)")
/* 50 */     .number("(dd)(dd)(dd),")
/* 51 */     .number("(d+.d+),")
/* 52 */     .number("(d+)")
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
/* 64 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 65 */     if (deviceSession == null) {
/* 66 */       return null;
/*    */     }
/*    */     
/* 69 */     Position position = new Position(getProtocolName());
/* 70 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 72 */     position.setFixTime(parser.nextDateTime());
/*    */     
/* 74 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 75 */     position.setLongitude(parser.nextDouble().doubleValue());
/* 76 */     position.setAltitude(parser.nextInt().intValue());
/* 77 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/* 78 */     position.setValid((parser.nextInt().intValue() == 1));
/*    */     
/* 80 */     position.set("sat", parser.nextInt());
/* 81 */     position.set("hdop", parser.nextDouble());
/*    */     
/* 83 */     position.setDeviceTime(parser.nextDateTime());
/*    */     
/* 85 */     position.set("battery", parser.nextDouble());
/* 86 */     position.set("status", parser.nextInt());
/*    */     
/* 88 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SmartSoleProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */