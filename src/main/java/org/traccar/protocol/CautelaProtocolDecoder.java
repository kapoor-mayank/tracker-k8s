/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import java.util.regex.Pattern;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.helper.DateBuilder;
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
/*    */ public class CautelaProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public CautelaProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .number("(d+),")
/* 38 */     .number("(d+),")
/* 39 */     .number("(dd),(dd),(dd),")
/* 40 */     .number("(-?d+.d+),")
/* 41 */     .number("(-?d+.d+),")
/* 42 */     .number("(dd)(dd),")
/* 43 */     .any()
/* 44 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 50 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 51 */     if (!parser.matches()) {
/* 52 */       return null;
/*    */     }
/*    */     
/* 55 */     parser.next();
/*    */     
/* 57 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 58 */     if (deviceSession == null) {
/* 59 */       return null;
/*    */     }
/*    */     
/* 62 */     Position position = new Position(getProtocolName());
/* 63 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 65 */     DateBuilder dateBuilder = new DateBuilder();
/* 66 */     dateBuilder.setDateReverse(parser.nextInt().intValue(), parser.nextInt().intValue(), parser.nextInt().intValue());
/*    */     
/* 68 */     position.setValid(true);
/* 69 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 70 */     position.setLongitude(parser.nextDouble().doubleValue());
/*    */     
/* 72 */     dateBuilder.setHour(parser.nextInt().intValue()).setMinute(parser.nextInt().intValue());
/* 73 */     position.setTime(dateBuilder.getDate());
/*    */     
/* 75 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\CautelaProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */