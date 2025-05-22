/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.ByteBuf;
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import java.nio.charset.StandardCharsets;
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
/*    */ public class RitiProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public RitiProtocolDecoder(Protocol protocol) {
/* 35 */     super(protocol);
/*    */   }
/*    */   
/* 38 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 39 */     .text("$GPRMC,")
/* 40 */     .number("(dd)(dd)(dd).?d*,")
/* 41 */     .expression("([AV]),")
/* 42 */     .number("(dd)(dd.d+),")
/* 43 */     .expression("([NS]),")
/* 44 */     .number("(ddd)(dd.d+),")
/* 45 */     .expression("([EW]),")
/* 46 */     .number("(d+.?d*)?,")
/* 47 */     .number("(d+.?d*)?,")
/* 48 */     .number("(dd)(dd)(dd)")
/* 49 */     .any()
/* 50 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 56 */     ByteBuf buf = (ByteBuf)msg;
/*    */     
/* 58 */     buf.skipBytes(2);
/*    */     
/* 60 */     Position position = new Position(getProtocolName());
/*    */     
/* 62 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { String.valueOf(buf.readUnsignedShort()) });
/* 63 */     if (deviceSession == null) {
/* 64 */       return null;
/*    */     }
/* 66 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 68 */     position.set("mode", Short.valueOf(buf.readUnsignedByte()));
/* 69 */     position.set("command", Short.valueOf(buf.readUnsignedByte()));
/* 70 */     position.set("power", Double.valueOf(buf.readUnsignedShortLE() * 0.001D));
/*    */     
/* 72 */     buf.skipBytes(5);
/* 73 */     buf.readUnsignedShortLE();
/* 74 */     buf.readUnsignedShortLE();
/*    */     
/* 76 */     position.set("distance", Long.valueOf(buf.readUnsignedIntLE()));
/* 77 */     position.set("tripOdometer", Long.valueOf(buf.readUnsignedIntLE()));
/*    */ 
/*    */     
/* 80 */     int end = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte)42);
/* 81 */     String gprmc = buf.toString(buf.readerIndex(), end - buf.readerIndex(), StandardCharsets.US_ASCII);
/* 82 */     Parser parser = new Parser(PATTERN, gprmc);
/* 83 */     if (!parser.matches()) {
/* 84 */       return null;
/*    */     }
/*    */ 
/*    */     
/* 88 */     DateBuilder dateBuilder = (new DateBuilder()).setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/*    */     
/* 90 */     position.setValid(parser.next().equals("A"));
/* 91 */     position.setLatitude(parser.nextCoordinate());
/* 92 */     position.setLongitude(parser.nextCoordinate());
/* 93 */     position.setSpeed(parser.nextDouble(0.0D));
/* 94 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 96 */     dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
/* 97 */     position.setTime(dateBuilder.getDate());
/*    */     
/* 99 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\RitiProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */