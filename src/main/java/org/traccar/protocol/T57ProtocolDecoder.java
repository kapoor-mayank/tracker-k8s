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
/*    */ public class T57ProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public T57ProtocolDecoder(Protocol protocol) {
/* 32 */     super(protocol);
/*    */   }
/*    */   
/* 35 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 36 */     .text("*T57#")
/* 37 */     .number("Fd#")
/* 38 */     .number("([^#]+)#")
/* 39 */     .number("(dd)(dd)(dd)#")
/* 40 */     .number("(dd)(dd)(dd)#")
/* 41 */     .number("(dd)(dd.d+)#")
/* 42 */     .expression("([NS])#")
/* 43 */     .number("(ddd)(dd.d+)#")
/* 44 */     .expression("([EW])#")
/* 45 */     .expression("[^#]+#")
/* 46 */     .number("(d+.d+)#")
/* 47 */     .number("(d+.d+)#")
/* 48 */     .expression("([AV])")
/* 49 */     .number("d#")
/* 50 */     .number("(d+.d+)#")
/* 51 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 57 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 58 */     if (!parser.matches()) {
/* 59 */       return null;
/*    */     }
/*    */     
/* 62 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 63 */     if (deviceSession == null) {
/* 64 */       return null;
/*    */     }
/*    */     
/* 67 */     Position position = new Position(getProtocolName());
/* 68 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 70 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*    */     
/* 72 */     position.setLatitude(parser.nextCoordinate());
/* 73 */     position.setLongitude(parser.nextCoordinate());
/* 74 */     position.setSpeed(parser.nextDouble().doubleValue());
/* 75 */     position.setAltitude(parser.nextDouble().doubleValue());
/*    */     
/* 77 */     position.setValid(parser.next().equals("A"));
/*    */     
/* 79 */     position.set("battery", parser.nextDouble());
/*    */     
/* 81 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\T57ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */