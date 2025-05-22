/*    */ package org.traccar.notificators;
/*    */ 
/*    */ import javax.mail.MessagingException;
/*    */ import org.traccar.Context;
/*    */ import org.traccar.model.Event;
/*    */ import org.traccar.model.Position;
/*    */ import org.traccar.notification.FullMessage;
/*    */ import org.traccar.notification.MessageException;
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
/*    */ 
/*    */ public final class NotificatorMail
/*    */   extends Notificator
/*    */ {
/*    */   public void sendSync(long userId, Event event, Position position) throws MessageException {
/*    */     try {
/* 33 */       FullMessage message = NotificationFormatter.formatFullMessage(userId, event, position);
/* 34 */       Context.getMailManager().sendMessage(userId, message.getSubject(), message.getBody());
/* 35 */     } catch (MessagingException e) {
/* 36 */       throw new MessageException(e);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\notificators\NotificatorMail.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */