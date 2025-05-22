/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.buffer.Unpooled;
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.handler.codec.http.DefaultFullHttpRequest;
/*    */ import io.netty.handler.codec.http.HttpHeaderNames;
/*    */ import io.netty.handler.codec.http.HttpMethod;
/*    */ import io.netty.handler.codec.http.HttpVersion;
/*    */ import io.netty.handler.codec.http.QueryStringEncoder;
/*    */ import java.net.SocketAddress;
/*    */ import java.text.DateFormat;
/*    */ import java.text.SimpleDateFormat;
/*    */ import java.util.Date;
/*    */ import java.util.TimeZone;
/*    */ import org.traccar.BaseProtocolPoller;
/*    */ import org.traccar.Context;
/*    */ import org.traccar.Protocol;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ public class OrbcommProtocolPoller
/*    */   extends BaseProtocolPoller
/*    */ {
/*    */   private final String accessId;
/*    */   private final String password;
/*    */   private final String host;
/* 43 */   private Date startTime = new Date();
/*    */   
/*    */   public void setStartTime(Date startTime) {
/* 46 */     this.startTime = startTime;
/*    */   }
/*    */   
/*    */   public OrbcommProtocolPoller(Protocol protocol) {
/* 50 */     super(protocol);
/* 51 */     this.accessId = Context.getConfig().getString("orbcomm.accessId");
/* 52 */     this.password = Context.getConfig().getString("orbcomm.password");
/* 53 */     this.host = Context.getConfig().getString("orbcomm.address");
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected void sendRequest(Channel channel, SocketAddress remoteAddress) {
/* 59 */     QueryStringEncoder encoder = new QueryStringEncoder("/GLGW/2/RestMessages.svc/JSON/get_return_messages/");
/* 60 */     encoder.addParam("access_id", this.accessId);
/* 61 */     encoder.addParam("password", this.password);
/*    */     
/* 63 */     DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
/* 64 */     dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
/* 65 */     encoder.addParam("start_utc", dateFormat.format(this.startTime));
/*    */ 
/*    */     
/* 68 */     DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, encoder.toString(), Unpooled.buffer());
/* 69 */     defaultFullHttpRequest.headers().add((CharSequence)HttpHeaderNames.HOST, this.host);
/* 70 */     defaultFullHttpRequest.headers().add((CharSequence)HttpHeaderNames.CONTENT_LENGTH, Integer.valueOf(0));
/* 71 */     channel.writeAndFlush(defaultFullHttpRequest);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OrbcommProtocolPoller.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */