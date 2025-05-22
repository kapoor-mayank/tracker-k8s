/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import java.util.TimeZone;
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
/*    */ public class AustinNbProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public AustinNbProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */   
/* 36 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 37 */     .number("(d+);")
/* 38 */     .number("(dddd)-(dd)-(dd) ")
/* 39 */     .number("(dd):(dd):(dd);")
/* 40 */     .number("(-?d+,d+);")
/* 41 */     .number("(-?d+,d+);")
/* 42 */     .number("(d+);")
/* 43 */     .number("(d+);")
/* 44 */     .number("(d+);")
/* 45 */     .number("(d+);")
/* 46 */     .expression("(.*)")
/* 47 */     .any()
/* 48 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 54 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 55 */     if (!parser.matches()) {
/* 56 */       return null;
/*    */     }
/*    */     
/* 59 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 60 */     if (deviceSession == null) {
/* 61 */       return null;
/*    */     }
/*    */     
/* 64 */     Position position = new Position(getProtocolName());
/* 65 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 67 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.YMD_HMS, TimeZone.getDefault().getID()));
/*    */     
/* 69 */     position.setValid(true);
/* 70 */     position.setLatitude(Double.parseDouble(parser.next().replace(',', '.')));
/* 71 */     position.setLongitude(Double.parseDouble(parser.next().replace(',', '.')));
/* 72 */     position.setCourse(parser.nextInt().intValue());
/* 73 */     position.set("angle", parser.nextInt());
/* 74 */     position.set("range", parser.nextInt());
/* 75 */     position.set("outOfRange", parser.nextInt());
/* 76 */     position.set("carrier", parser.next());
/*    */     
/* 78 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AustinNbProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */