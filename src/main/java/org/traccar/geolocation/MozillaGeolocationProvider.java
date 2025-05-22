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
/*    */ public class MozillaGeolocationProvider
/*    */   extends UniversalGeolocationProvider
/*    */ {
/*    */   private static final String URL = "https://location.services.mozilla.com/v1/geolocate";
/*    */   
/*    */   public MozillaGeolocationProvider(String key) {
/* 23 */     super("https://location.services.mozilla.com/v1/geolocate", (key != null) ? key : "test");
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geolocation\MozillaGeolocationProvider.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */