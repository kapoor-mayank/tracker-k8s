/*    */ package org.traccar.notificators;
/*    */ 
/*    */ import org.traccar.Context;
/*    */ import org.traccar.Main;
/*    */ import org.traccar.database.StatisticsManager;
/*    */ import org.traccar.model.Event;
/*    */ import org.traccar.model.Position;
/*    */ import org.traccar.model.User;
/*    */ import org.traccar.notification.MessageException;
/*    */ import org.traccar.notification.NotificationFormatter;
/*    */ import org.traccar.sms.SmsManager;
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
/*    */ public final class NotificatorSms
/*    */   extends Notificator
/*    */ {
/*    */   private final SmsManager smsManager;
/*    */   
/*    */   public NotificatorSms() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
/* 34 */     String smsClass = Context.getConfig().getString("notificator.sms.manager.class", "");
/* 35 */     if (smsClass.length() > 0) {
/* 36 */       this.smsManager = (SmsManager)Class.forName(smsClass).newInstance();
/*    */     } else {
/* 38 */       this.smsManager = Context.getSmsManager();
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   public void sendAsync(long userId, Event event, Position position) {
/* 44 */     User user = Context.getPermissionsManager().getUser(userId);
/* 45 */     if (user.getPhone() != null) {
/* 46 */       ((StatisticsManager)Main.getInjector().getInstance(StatisticsManager.class)).registerSms();
/* 47 */       this.smsManager.sendMessageAsync(user.getPhone(), 
/* 48 */           NotificationFormatter.formatShortMessage(userId, event, position), false);
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   public void sendSync(long userId, Event event, Position position) throws MessageException, InterruptedException {
/* 54 */     User user = Context.getPermissionsManager().getUser(userId);
/* 55 */     if (user.getPhone() != null) {
/* 56 */       ((StatisticsManager)Main.getInjector().getInstance(StatisticsManager.class)).registerSms();
/* 57 */       this.smsManager.sendMessageSync(user.getPhone(), 
/* 58 */           NotificationFormatter.formatShortMessage(userId, event, position), false);
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\notificators\NotificatorSms.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */