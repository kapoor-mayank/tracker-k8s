/*    */ package org.traccar;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.handler.codec.mqtt.MqttConnAckMessage;
/*    */ import io.netty.handler.codec.mqtt.MqttConnectMessage;
/*    */ import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
/*    */ import io.netty.handler.codec.mqtt.MqttMessage;
/*    */ import io.netty.handler.codec.mqtt.MqttMessageBuilders;
/*    */ import io.netty.handler.codec.mqtt.MqttPublishMessage;
/*    */ import io.netty.handler.codec.mqtt.MqttSubAckMessage;
/*    */ import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
/*    */ import java.net.SocketAddress;
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
/*    */ public abstract class BaseMqttProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   public BaseMqttProtocolDecoder(Protocol protocol) {
/* 32 */     super(protocol);
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   protected abstract Object decode(DeviceSession paramDeviceSession, MqttPublishMessage paramMqttPublishMessage) throws Exception;
/*    */ 
/*    */   
/*    */   protected final Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 41 */     if (msg instanceof MqttConnectMessage) {
/*    */       
/* 43 */       MqttConnectMessage message = (MqttConnectMessage)msg;
/*    */       
/* 45 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { message
/* 46 */             .payload().clientIdentifier() });
/*    */       
/* 48 */       MqttConnectReturnCode returnCode = (deviceSession != null) ? MqttConnectReturnCode.CONNECTION_ACCEPTED : MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
/*    */ 
/*    */ 
/*    */       
/* 52 */       MqttConnAckMessage mqttConnAckMessage = MqttMessageBuilders.connAck().returnCode(returnCode).build();
/*    */       
/* 54 */       if (channel != null) {
/* 55 */         channel.writeAndFlush(new NetworkMessage(mqttConnAckMessage, remoteAddress));
/*    */       }
/*    */     }
/* 58 */     else if (msg instanceof MqttSubscribeMessage) {
/*    */       
/* 60 */       MqttSubscribeMessage message = (MqttSubscribeMessage)msg;
/*    */ 
/*    */ 
/*    */       
/* 64 */       MqttSubAckMessage mqttSubAckMessage = MqttMessageBuilders.subAck().packetId(message.variableHeader().messageId()).build();
/*    */       
/* 66 */       if (channel != null) {
/* 67 */         channel.writeAndFlush(new NetworkMessage(mqttSubAckMessage, remoteAddress));
/*    */       }
/*    */     }
/* 70 */     else if (msg instanceof MqttPublishMessage) {
/*    */       
/* 72 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[0]);
/* 73 */       if (deviceSession == null) {
/* 74 */         return null;
/*    */       }
/*    */       
/* 77 */       MqttPublishMessage message = (MqttPublishMessage)msg;
/*    */       
/* 79 */       Object result = decode(deviceSession, message);
/*    */ 
/*    */ 
/*    */       
/* 83 */       MqttMessage response = MqttMessageBuilders.pubAck().packetId(message.variableHeader().packetId()).build();
/*    */       
/* 85 */       if (channel != null) {
/* 86 */         channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
/*    */       }
/*    */       
/* 89 */       return result;
/*    */     } 
/*    */ 
/*    */     
/* 93 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\BaseMqttProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */