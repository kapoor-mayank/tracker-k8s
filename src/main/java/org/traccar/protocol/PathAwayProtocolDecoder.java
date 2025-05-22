/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelFutureListener;
/*    */ import io.netty.handler.codec.http.DefaultFullHttpResponse;
/*    */ import io.netty.handler.codec.http.FullHttpRequest;
/*    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*    */ import io.netty.handler.codec.http.HttpVersion;
/*    */ import io.netty.handler.codec.http.QueryStringDecoder;
/*    */ import io.netty.util.concurrent.GenericFutureListener;
/*    */ import java.net.SocketAddress;
/*    */ import java.util.List;
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
/*    */ public class PathAwayProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public PathAwayProtocolDecoder(Protocol protocol) {
/* 40 */     super(protocol);
/*    */   }
/*    */   
/* 43 */   private static final Pattern PATTERN = (new PatternBuilder())
/* 44 */     .text("$PWS,")
/* 45 */     .number("d+,")
/* 46 */     .expression("[^,]*,")
/* 47 */     .expression("[^,]*,")
/* 48 */     .expression("[^,]*,")
/* 49 */     .number("(dd)(dd)(dd),")
/* 50 */     .number("(dd)(dd)(dd),")
/* 51 */     .number("(-?d+.d+),")
/* 52 */     .number("(-?d+.d+),")
/* 53 */     .number("(-?d+.?d*),")
/* 54 */     .number("(-?d+.?d*),")
/* 55 */     .number("(-?d+.?d*),")
/* 56 */     .any()
/* 57 */     .compile();
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 63 */     FullHttpRequest request = (FullHttpRequest)msg;
/* 64 */     QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
/*    */     
/* 66 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { ((List<String>)decoder
/* 67 */           .parameters().get("UserName")).get(0) });
/* 68 */     if (deviceSession == null) {
/* 69 */       return null;
/*    */     }
/*    */     
/* 72 */     Parser parser = new Parser(PATTERN, ((List<String>)decoder.parameters().get("LOC")).get(0));
/* 73 */     if (!parser.matches()) {
/* 74 */       return null;
/*    */     }
/*    */     
/* 77 */     Position position = new Position(getProtocolName());
/* 78 */     position.setDeviceId(deviceSession.getDeviceId());
/*    */     
/* 80 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*    */     
/* 82 */     position.setValid(true);
/* 83 */     position.setLatitude(parser.nextDouble(0.0D));
/* 84 */     position.setLongitude(parser.nextDouble(0.0D));
/* 85 */     position.setAltitude(parser.nextDouble(0.0D));
/* 86 */     position.setSpeed(parser.nextDouble(0.0D));
/* 87 */     position.setCourse(parser.nextDouble(0.0D));
/*    */     
/* 89 */     if (channel != null) {
/* 90 */       DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
/* 91 */       channel.writeAndFlush(new NetworkMessage(defaultFullHttpResponse, remoteAddress)).addListener((GenericFutureListener)ChannelFutureListener.CLOSE);
/*    */     } 
/*    */     
/* 94 */     return position;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\PathAwayProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */