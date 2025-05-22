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
/*    */ public class DishaProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public DishaProtocolDecoder(Protocol protocol) {
/* 32 */     super(protocol);
/*    */   }
/*    */   
/* 35 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 36 */     .text("$A#A#")
/* 37 */     .number("(d+)#")
/* 38 */     .expression("([AVMX])#")
/* 39 */     .number("(dd)(dd)(dd)#")
/* 40 */     .number("(dd)(dd)(dd)#")
/* 41 */     .number("(dd)(dd.d+)#")
/* 42 */     .expression("([NS])#")
/* 43 */     .number("(ddd)(dd.d+)#")
/* 44 */     .expression("([EW])#")
/* 45 */     .number("(d+.d+)#")
/* 46 */     .number("(d+.d+)#")
/* 47 */     .number("(d+)#")
/* 48 */     .number("(d+.d+)#")
/* 49 */     .number("(d+)#")
/* 50 */     .expression("([012])#")
/* 51 */     .number("(d+)#")
/* 52 */     .number("(d+)#")
/* 53 */     .number("(d+)#")
/* 54 */     .number("d+.d+#")
/* 55 */     .number("(d+.d+)#")
/* 56 */     .expression("([01]+)")
/* 57 */     .text("*")
/* 58 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 64 */     Parser parser = new Parser(PATTERN, (String)msg);
/* 65 */     if (!parser.matches()) {
/* 66 */       return null;
/*    */     }
/*    */     
/* 69 */     Position position = new Position(getProtocolName());
/*    */     
/* 71 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/* 72 */     if (deviceSession == null) {
/* 73 */       return null;
/*    */     }
/* 75 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 77 */     position.setValid(parser.next().equals("A"));
/*    */     
/* 79 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.HMS_DMY));
/*    */     
/* 81 */     position.setLatitude(parser.nextCoordinate());
/* 82 */     position.setLongitude(parser.nextCoordinate());
/*    */     
/* 84 */     position.setSpeed(parser.nextDouble(0.0D));
/* 85 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 87 */     position.set("sat", parser.nextInt());
/* 88 */     position.set("hdop", parser.nextDouble());
/* 89 */     position.set("rssi", parser.nextDouble());
/* 90 */     position.set("charge", Boolean.valueOf((parser.nextInt(0) == 2)));
/* 91 */     position.set("batteryLevel", Integer.valueOf(parser.nextInt(0)));
/*    */     
/* 93 */     position.set("adc1", Integer.valueOf(parser.nextInt(0)));
/* 94 */     position.set("adc2", Integer.valueOf(parser.nextInt(0)));
/*    */     
/* 96 */     position.set("odometer", Double.valueOf(parser.nextDouble(0.0D) * 1000.0D));
/* 97 */     position.set("input", parser.next());
/*    */     
/* 99 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\DishaProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */