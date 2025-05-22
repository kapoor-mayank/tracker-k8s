/*    */ package org.traccar;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import io.netty.channel.ChannelHandlerContext;
/*    */ import io.netty.channel.ChannelOutboundHandlerAdapter;
/*    */ import io.netty.channel.ChannelPromise;
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ import org.traccar.model.Command;
/*    */ import org.traccar.model.Device;
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
/*    */ public abstract class BaseProtocolEncoder
/*    */   extends ChannelOutboundHandlerAdapter
/*    */ {
/* 29 */   private static final Logger LOGGER = LoggerFactory.getLogger(BaseProtocolEncoder.class);
/*    */   
/*    */   private String modelOverride;
/*    */   
/*    */   protected String getUniqueId(long deviceId) {
/* 34 */     return Context.getIdentityManager().getById(deviceId).getUniqueId();
/*    */   }
/*    */   
/*    */   protected void initDevicePassword(Command command, String defaultPassword) {
/* 38 */     if (!command.getAttributes().containsKey("devicePassword")) {
/* 39 */       Device device = Context.getIdentityManager().getById(command.getDeviceId());
/* 40 */       String password = device.getString("devicePassword");
/* 41 */       if (password != null) {
/* 42 */         command.set("devicePassword", password);
/*    */       } else {
/* 44 */         command.set("devicePassword", defaultPassword);
/*    */       } 
/*    */     } 
/*    */   }
/*    */   
/*    */   public void setModelOverride(String modelOverride) {
/* 50 */     this.modelOverride = modelOverride;
/*    */   }
/*    */   
/*    */   public String getDeviceModel(long deviceId) {
/* 54 */     String model = ((Device)Context.getDeviceManager().getById(deviceId)).getModel();
/* 55 */     return (this.modelOverride != null) ? this.modelOverride : model;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
/* 61 */     NetworkMessage networkMessage = (NetworkMessage)msg;
/*    */     
/* 63 */     if (networkMessage.getMessage() instanceof Command) {
/*    */       
/* 65 */       Command command = (Command)networkMessage.getMessage();
/* 66 */       Object encodedCommand = encodeCommand(ctx.channel(), command);
/*    */       
/* 68 */       StringBuilder s = new StringBuilder();
/* 69 */       s.append("[").append(ctx.channel().id().asShortText()).append("] ");
/* 70 */       s.append("id: ").append(getUniqueId(command.getDeviceId())).append(", ");
/* 71 */       s.append("command type: ").append(command.getType()).append(" ");
/* 72 */       if (encodedCommand != null) {
/* 73 */         s.append("sent");
/*    */       } else {
/* 75 */         s.append("not sent");
/*    */       } 
/* 77 */       LOGGER.info(s.toString());
/*    */       
/* 79 */       ctx.write(new NetworkMessage(encodedCommand, networkMessage.getRemoteAddress()), promise);
/*    */     }
/*    */     else {
/*    */       
/* 83 */       super.write(ctx, msg, promise);
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   protected Object encodeCommand(Channel channel, Command command) {
/* 89 */     return encodeCommand(command);
/*    */   }
/*    */   
/*    */   protected Object encodeCommand(Command command) {
/* 93 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\BaseProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */