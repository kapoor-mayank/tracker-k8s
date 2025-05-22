/*    */ package org.traccar;
/*    */ 
/*    */ import java.util.TimeZone;
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
/*    */ public class DeviceSession
/*    */ {
/*    */   private final long deviceId;
/*    */   private TimeZone timeZone;
/*    */   
/*    */   public DeviceSession(long deviceId) {
/* 25 */     this.deviceId = deviceId;
/*    */   }
/*    */   
/*    */   public long getDeviceId() {
/* 29 */     return this.deviceId;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public void setTimeZone(TimeZone timeZone) {
/* 35 */     this.timeZone = timeZone;
/*    */   }
/*    */   
/*    */   public TimeZone getTimeZone() {
/* 39 */     return this.timeZone;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\DeviceSession.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */