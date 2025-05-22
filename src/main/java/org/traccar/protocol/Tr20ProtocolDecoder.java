/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.Position;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Tr20ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public Tr20ProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN_PING = (new PatternBuilder())
/*  38 */     .text("%%")
/*  39 */     .expression("[^,]+,")
/*  40 */     .number("(d+)")
/*  41 */     .compile();
/*     */   
/*  43 */   private static final Pattern PATTERN_DATA = (new PatternBuilder())
/*  44 */     .text("%%")
/*  45 */     .expression("([^,]+),")
/*  46 */     .expression("([AL]),")
/*  47 */     .number("(dd)(dd)(dd)")
/*  48 */     .number("(dd)(dd)(dd),")
/*  49 */     .expression("([NS])")
/*  50 */     .number("(dd)(dd.d+)")
/*  51 */     .expression("([EW])")
/*  52 */     .number("(ddd)(dd.d+),")
/*  53 */     .number("(d+),")
/*  54 */     .number("(d+),")
/*  55 */     .number("(?:NA|[BFC]?(-?d+)[^,]*),")
/*  56 */     .number("(x{8}),")
/*  57 */     .number("(d+)")
/*  58 */     .any()
/*  59 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  65 */     Parser parser = new Parser(PATTERN_PING, (String)msg);
/*  66 */     if (parser.matches()) {
/*  67 */       if (channel != null) {
/*  68 */         channel.writeAndFlush(new NetworkMessage("&&" + parser
/*  69 */               .next() + "\r\n", remoteAddress));
/*     */       }
/*  71 */       return null;
/*     */     } 
/*     */     
/*  74 */     parser = new Parser(PATTERN_DATA, (String)msg);
/*  75 */     if (!parser.matches()) {
/*  76 */       return null;
/*     */     }
/*     */     
/*  79 */     Position position = new Position(getProtocolName());
/*     */     
/*  81 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  82 */     if (deviceSession == null) {
/*  83 */       return null;
/*     */     }
/*  85 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  87 */     position.setValid(parser.next().equals("A"));
/*     */     
/*  89 */     position.setTime(parser.nextDateTime());
/*     */     
/*  91 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/*  92 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN));
/*  93 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/*  94 */     position.setCourse(parser.nextDouble().doubleValue());
/*     */     
/*  96 */     position.set("temp1", parser.nextInt());
/*  97 */     position.set("status", parser.nextHexLong());
/*  98 */     position.set("event", parser.nextInt());
/*     */     
/* 100 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Tr20ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */