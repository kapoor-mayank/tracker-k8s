/*    */ package org.traccar.notificators;
/*    */ 
/*    */ import org.traccar.Context;
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
/*    */ 
/*    */ public final class NotificatorWeb
/*    */   extends Notificator
/*    */ {
/*    */   public void sendSync(long userId, Event event, Position position) {
/* 27 */     Context.getConnectionManager().updateEvent(userId, event);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\notificators\NotificatorWeb.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */