/*    */ package org.traccar.notificators;
/*    */ 
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ import org.traccar.model.Event;
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
/*    */ public final class NotificatorNull
/*    */   extends Notificator
/*    */ {
/* 26 */   private static final Logger LOGGER = LoggerFactory.getLogger(NotificatorNull.class);
/*    */ 
/*    */   
/*    */   public void sendAsync(long userId, Event event, Position position) {
/* 30 */     LOGGER.warn("You are using null notificatior, please check your configuration, notification not sent");
/*    */   }
/*    */ 
/*    */   
/*    */   public void sendSync(long userId, Event event, Position position) {
/* 35 */     LOGGER.warn("You are using null notificatior, please check your configuration, notification not sent");
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\notificators\NotificatorNull.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */