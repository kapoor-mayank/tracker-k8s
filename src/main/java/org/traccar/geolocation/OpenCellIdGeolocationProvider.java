/*    */ package org.traccar.geolocation;
/*    */ 
/*    */ import javax.json.JsonObject;
/*    */ import javax.ws.rs.client.InvocationCallback;
/*    */ import org.traccar.Context;
/*    */ import org.traccar.model.CellTower;
/*    */ import org.traccar.model.Network;
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
/*    */ public class OpenCellIdGeolocationProvider
/*    */   implements GeolocationProvider
/*    */ {
/*    */   private String url;
/*    */   
/*    */   public OpenCellIdGeolocationProvider(String key) {
/* 30 */     this("http://opencellid.org/cell/get", key);
/*    */   }
/*    */   
/*    */   public OpenCellIdGeolocationProvider(String url, String key) {
/* 34 */     this.url = url + "?format=json&mcc=%d&mnc=%d&lac=%d&cellid=%d&key=" + key;
/*    */   }
/*    */ 
/*    */   
/*    */   public void getLocation(Network network, final GeolocationProvider.LocationProviderCallback callback) {
/* 39 */     if (network.getCellTowers() != null && !network.getCellTowers().isEmpty()) {
/*    */       
/* 41 */       CellTower cellTower = network.getCellTowers().iterator().next();
/* 42 */       String request = String.format(this.url, new Object[] { cellTower.getMobileCountryCode(), cellTower.getMobileNetworkCode(), cellTower
/* 43 */             .getLocationAreaCode(), cellTower.getCellId() });
/*    */       
/* 45 */       Context.getClient().target(request).request().async().get(new InvocationCallback<JsonObject>()
/*    */           {
/*    */             public void completed(JsonObject json) {
/* 48 */               if (json.containsKey("lat") && json.containsKey("lon")) {
/* 49 */                 callback.onSuccess(json
/* 50 */                     .getJsonNumber("lat").doubleValue(), json
/* 51 */                     .getJsonNumber("lon").doubleValue(), 0.0D);
/*    */               } else {
/* 53 */                 callback.onFailure(new GeolocationException("Coordinates are missing"));
/*    */               } 
/*    */             }
/*    */ 
/*    */             
/*    */             public void failed(Throwable throwable) {
/* 59 */               callback.onFailure(throwable);
/*    */             }
/*    */           });
/*    */     } else {
/*    */       
/* 64 */       callback.onFailure(new GeolocationException("No network information"));
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geolocation\OpenCellIdGeolocationProvider.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */