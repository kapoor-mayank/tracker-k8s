/*    */ package org.traccar.notification;
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
/*    */ public class FullMessage
/*    */ {
/*    */   private String subject;
/*    */   private String body;
/*    */   
/*    */   public FullMessage(String subject, String body) {
/* 25 */     this.subject = subject;
/* 26 */     this.body = body;
/*    */   }
/*    */   
/*    */   public String getSubject() {
/* 30 */     return this.subject;
/*    */   }
/*    */   
/*    */   public String getBody() {
/* 34 */     return this.body;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\notification\FullMessage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */