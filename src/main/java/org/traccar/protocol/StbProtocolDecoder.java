/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import com.fasterxml.jackson.annotation.JsonProperty;
/*    */ import io.netty.channel.Channel;
/*    */ import java.io.StringReader;
/*    */ import java.net.SocketAddress;
/*    */ import javax.json.Json;
/*    */ import javax.json.JsonObject;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.Context;
/*    */ import org.traccar.NetworkMessage;
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
/*    */ public class StbProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public StbProtocolDecoder(Protocol protocol) {
/* 33 */     super(protocol);
/*    */   }
/*    */ 
/*    */   
/*    */   public static class Response
/*    */   {
/*    */     @JsonProperty("msgType")
/*    */     private int type;
/*    */     
/*    */     @JsonProperty("devId")
/*    */     private String deviceId;
/*    */     @JsonProperty("result")
/*    */     private int result;
/*    */     @JsonProperty("txnNo")
/*    */     private String transaction;
/*    */   }
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 51 */     JsonObject root = Json.createReader(new StringReader((String)msg)).readObject();
/*    */     
/* 53 */     Response response = new Response();
/* 54 */     response.type = root.getInt("msgType");
/* 55 */     response.deviceId = root.getString("devId");
/* 56 */     response.result = 1;
/* 57 */     response.transaction = root.getString("txnNo");
/* 58 */     if (channel != null) {
/* 59 */       channel.writeAndFlush(new NetworkMessage(
/* 60 */             Context.getObjectMapper().writeValueAsString(response), remoteAddress));
/*    */     }
/*    */     
/* 63 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\StbProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */