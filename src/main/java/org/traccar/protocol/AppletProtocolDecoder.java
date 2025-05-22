/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.handler.codec.http.FullHttpRequest;
/*    */ import io.netty.handler.codec.http.HttpResponseStatus;
/*    */ import java.net.SocketAddress;
/*    */ import org.traccar.BaseHttpProtocolDecoder;
/*    */ import org.traccar.DeviceSession;
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
/*    */ public class AppletProtocolDecoder
/*    */   extends BaseHttpProtocolDecoder
/*    */ {
/*    */   public AppletProtocolDecoder(Protocol protocol) {
/* 30 */     super(protocol);
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 36 */     FullHttpRequest request = (FullHttpRequest)msg;
/*    */     
/* 38 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { request.headers().get("From") });
/* 39 */     if (deviceSession != null) {
/* 40 */       sendResponse(channel, HttpResponseStatus.OK);
/*    */     } else {
/* 42 */       sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
/*    */     } 
/*    */     
/* 45 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AppletProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */