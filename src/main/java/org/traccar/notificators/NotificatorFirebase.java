/*    */ package org.traccar.notificators;
/*    */ 
/*    */ import com.fasterxml.jackson.annotation.JsonProperty;
/*    */ import javax.ws.rs.client.Entity;
/*    */ import javax.ws.rs.client.InvocationCallback;
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ import org.traccar.Context;
/*    */ import org.traccar.model.Event;
/*    */ import org.traccar.model.Position;
/*    */ import org.traccar.model.User;
/*    */ import org.traccar.notification.NotificationFormatter;
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
/*    */ public class NotificatorFirebase
/*    */   extends Notificator
/*    */ {
/* 33 */   private static final Logger LOGGER = LoggerFactory.getLogger(NotificatorFirebase.class);
/*    */   
/*    */   private static final String URL = "https://fcm.googleapis.com/fcm/send";
/*    */ 
/*    */   
/*    */   public static class Notification
/*    */   {
/*    */     @JsonProperty("body")
/*    */     private String body;
/*    */   }
/*    */ 
/*    */   
/*    */   public static class Message
/*    */   {
/*    */     @JsonProperty("registration_ids")
/*    */     private String[] tokens;
/*    */     @JsonProperty("notification")
/*    */     private NotificatorFirebase.Notification notification;
/*    */   }
/* 52 */   private String key = Context.getConfig().getString("notificator.firebase.key");
/*    */ 
/*    */ 
/*    */   
/*    */   public void sendSync(long userId, Event event, Position position) {
/* 57 */     User user = Context.getPermissionsManager().getUser(userId);
/* 58 */     if (user.getAttributes().containsKey("notificationTokens")) {
/*    */       
/* 60 */       Notification notification = new Notification();
/* 61 */       notification.body = NotificationFormatter.formatShortMessage(userId, event, position).trim();
/*    */       
/* 63 */       Message message = new Message();
/* 64 */       message.tokens = user.getString("notificationTokens").split("[, ]");
/* 65 */       message.notification = notification;
/*    */       
/* 67 */       Context.getClient().target("https://fcm.googleapis.com/fcm/send").request()
/* 68 */         .header("Authorization", "key=" + this.key)
/* 69 */         .async().post(Entity.json(message), new InvocationCallback<Object>()
/*    */           {
/*    */             public void completed(Object o) {}
/*    */ 
/*    */ 
/*    */             
/*    */             public void failed(Throwable throwable) {
/* 76 */               NotificatorFirebase.LOGGER.warn("Firebase notification error", throwable);
/*    */             }
/*    */           });
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   public void sendAsync(long userId, Event event, Position position) {
/* 84 */     sendSync(userId, event, position);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\notificators\NotificatorFirebase.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */