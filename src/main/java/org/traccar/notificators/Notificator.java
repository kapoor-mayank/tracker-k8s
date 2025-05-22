/*    */ package org.traccar.notificators;
/*    */ 
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ import org.traccar.model.Event;
/*    */ import org.traccar.model.Position;
/*    */ import org.traccar.notification.MessageException;
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
/*    */ public abstract class Notificator
/*    */ {
/* 27 */   private static final Logger LOGGER = LoggerFactory.getLogger(Notificator.class);
/*    */   
/*    */   public void sendAsync(final long userId, final Event event, final Position position) {
/* 30 */     (new Thread(new Runnable() {
/*    */           public void run() {
/*    */             try {
/* 33 */               Notificator.this.sendSync(userId, event, position);
/* 34 */             } catch (MessageException|InterruptedException error) {
/* 35 */               Notificator.LOGGER.warn("Event send error", error);
/*    */             } 
/*    */           }
/* 38 */         })).start();
/*    */   }
/*    */   
/*    */   public abstract void sendSync(long paramLong, Event paramEvent, Position paramPosition) throws MessageException, InterruptedException;
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\notificators\Notificator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */