/*    */ package org.traccar.geolocation;
/*    */ 
/*    */ import javax.json.JsonObject;
/*    */ import javax.ws.rs.client.AsyncInvoker;
/*    */ import javax.ws.rs.client.Entity;
/*    */ import javax.ws.rs.client.InvocationCallback;
/*    */ import org.traccar.Context;
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
/*    */ public class UniversalGeolocationProvider
/*    */   implements GeolocationProvider
/*    */ {
/*    */   private String url;
/*    */   
/*    */   public UniversalGeolocationProvider(String url, String key) {
/* 31 */     this.url = url + "?key=" + key;
/*    */   }
/*    */ 
/*    */   
/*    */   public void getLocation(Network network, final GeolocationProvider.LocationProviderCallback callback) {
/* 36 */     AsyncInvoker invoker = Context.getClient().target(this.url).request().async();
/* 37 */     invoker.post(Entity.json(network), new InvocationCallback<JsonObject>()
/*    */         {
/*    */           public void completed(JsonObject json) {
/* 40 */             if (json.containsKey("error")) {
/* 41 */               callback.onFailure(new GeolocationException(json.getJsonObject("error").getString("message")));
/*    */             } else {
/* 43 */               JsonObject location = json.getJsonObject("location");
/* 44 */               callback.onSuccess(location
/* 45 */                   .getJsonNumber("lat").doubleValue(), location
/* 46 */                   .getJsonNumber("lng").doubleValue(), json
/* 47 */                   .getJsonNumber("accuracy").doubleValue());
/*    */             } 
/*    */           }
/*    */ 
/*    */           
/*    */           public void failed(Throwable throwable) {
/* 53 */             callback.onFailure(throwable);
/*    */           }
/*    */         });
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geolocation\UniversalGeolocationProvider.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */