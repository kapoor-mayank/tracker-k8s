/*    */ package org.traccar;
/*    */ 
/*    */ import io.netty.util.HashedWheelTimer;
/*    */ import io.netty.util.Timer;
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
/*    */ public final class GlobalTimer
/*    */ {
/* 23 */   private static Timer instance = null;
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static void release() {
/* 29 */     if (instance != null) {
/* 30 */       instance.stop();
/*    */     }
/* 32 */     instance = null;
/*    */   }
/*    */   
/*    */   public static Timer getTimer() {
/* 36 */     if (instance == null) {
/* 37 */       instance = (Timer)new HashedWheelTimer();
/*    */     }
/* 39 */     return instance;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\GlobalTimer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */