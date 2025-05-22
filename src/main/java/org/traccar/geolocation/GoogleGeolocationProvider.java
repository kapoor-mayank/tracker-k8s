/*    */ package org.traccar.geolocation;
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
/*    */ public class GoogleGeolocationProvider
/*    */   extends UniversalGeolocationProvider
/*    */ {
/*    */   private static final String URL = "https://www.googleapis.com/geolocation/v1/geolocate";
/*    */   
/*    */   public GoogleGeolocationProvider(String key) {
/* 23 */     super("https://www.googleapis.com/geolocation/v1/geolocate", key);
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geolocation\GoogleGeolocationProvider.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */