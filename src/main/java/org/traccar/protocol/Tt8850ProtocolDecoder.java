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
/*    */ import org.traccar.model.CellTower;
/*    */ import org.traccar.model.Network;
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
/*    */ public class Tt8850ProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public Tt8850ProtocolDecoder(Protocol protocol) {
/* 35 */     super(protocol);
/*    */   }
/*    */   
/* 38 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 39 */     .binary("0004,")
/* 40 */     .number("xxxx,")
/* 41 */     .expression("[01],")
/* 42 */     .expression("GT...,")
/* 43 */     .number("(?:[0-9A-Z]{2}xxxx)?,")
/* 44 */     .expression("([^,]+),")
/* 45 */     .any()
/* 46 */     .number("(d{1,2})?,")
/* 47 */     .number("(d{1,3}.d)?,")
/* 48 */     .number("(d{1,3})?,")
/* 49 */     .number("(-?d{1,5}.d)?,")
/* 50 */     .number("(-?d{1,3}.d{6}),")
/* 51 */     .number("(-?d{1,2}.d{6}),")
/* 52 */     .number("(dddd)(dd)(dd)")
/* 53 */     .number("(dd)(dd)(dd),")
/* 54 */     .number("(0ddd)?,")
/* 55 */     .number("(0ddd)?,")
/* 56 */     .number("(xxxx)?,")
/* 57 */     .number("(xxxx)?,")
/* 58 */     .any()
/* 59 */     .number("(dddd)(dd)(dd)")
/* 60 */     .number("(dd)(dd)(dd),")
/* 61 */     .number("(xxxx)")
/* 62 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
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
/* 81 */     position.setValid(true);
/* 82 */     position.setAccuracy(parser.nextInt(0));
/* 83 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0.0D)));
/* 84 */     position.setCourse(parser.nextDouble(0.0D));
/* 85 */     position.setAltitude(parser.nextDouble(0.0D));
/* 86 */     position.setLongitude(parser.nextDouble(0.0D));
/* 87 */     position.setLatitude(parser.nextDouble(0.0D));
/*    */     
/* 89 */     position.setTime(parser.nextDateTime());
/*    */     
/* 91 */     if (parser.hasNext(4)) {
/* 92 */       position.setNetwork(new Network(
/* 93 */             CellTower.from(parser.nextInt(0), parser.nextInt(0), parser.nextHexInt(0), parser.nextHexInt(0))));
/*    */     }
/*    */     
/* 96 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Tt8850ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */