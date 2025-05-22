/*     */ package org.traccar.geolocation;
/*     */ 
/*     */ import com.fasterxml.jackson.annotation.JsonIgnore;
/*     */ import com.fasterxml.jackson.annotation.JsonProperty;
/*     */ import com.fasterxml.jackson.databind.ObjectMapper;
/*     */ import com.fasterxml.jackson.databind.node.ObjectNode;
/*     */ import java.util.Collection;
/*     */ import javax.json.JsonObject;
/*     */ import javax.ws.rs.client.Entity;
/*     */ import javax.ws.rs.client.InvocationCallback;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.model.CellTower;
/*     */ import org.traccar.model.Network;
/*     */ import org.traccar.model.WifiAccessPoint;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class UnwiredGeolocationProvider
/*     */   implements GeolocationProvider
/*     */ {
/*     */   private String url;
/*     */   private String key;
/*     */   private ObjectMapper objectMapper;
/*     */   
/*     */   private static abstract class NetworkMixIn
/*     */   {
/*     */     @JsonProperty("mcc")
/*     */     abstract Integer getHomeMobileCountryCode();
/*     */     
/*     */     @JsonProperty("mnc")
/*     */     abstract Integer getHomeMobileNetworkCode();
/*     */     
/*     */     @JsonProperty("radio")
/*     */     abstract String getRadioType();
/*     */     
/*     */     @JsonIgnore
/*     */     abstract String getCarrier();
/*     */     
/*     */     @JsonIgnore
/*     */     abstract Boolean getConsiderIp();
/*     */     
/*     */     @JsonProperty("cells")
/*     */     abstract Collection<CellTower> getCellTowers();
/*     */     
/*     */     @JsonProperty("wifi")
/*     */     abstract Collection<WifiAccessPoint> getWifiAccessPoints();
/*     */   }
/*     */   
/*     */   private static abstract class CellTowerMixIn
/*     */   {
/*     */     @JsonProperty("radio")
/*     */     abstract String getRadioType();
/*     */     
/*     */     @JsonProperty("mcc")
/*     */     abstract Integer getMobileCountryCode();
/*     */     
/*     */     @JsonProperty("mnc")
/*     */     abstract Integer getMobileNetworkCode();
/*     */     
/*     */     @JsonProperty("lac")
/*     */     abstract Integer getLocationAreaCode();
/*     */     
/*     */     @JsonProperty("cid")
/*     */     abstract Long getCellId();
/*     */   }
/*     */   
/*     */   private static abstract class WifiAccessPointMixIn
/*     */   {
/*     */     @JsonProperty("bssid")
/*     */     abstract String getMacAddress();
/*     */     
/*     */     @JsonProperty("signal")
/*     */     abstract Integer getSignalStrength();
/*     */   }
/*     */   
/*     */   public UnwiredGeolocationProvider(String url, String key) {
/*  77 */     this.url = url;
/*  78 */     this.key = key;
/*     */     
/*  80 */     this.objectMapper = new ObjectMapper();
/*  81 */     this.objectMapper.addMixIn(Network.class, NetworkMixIn.class);
/*  82 */     this.objectMapper.addMixIn(CellTower.class, CellTowerMixIn.class);
/*  83 */     this.objectMapper.addMixIn(WifiAccessPoint.class, WifiAccessPointMixIn.class);
/*     */   }
/*     */ 
/*     */   
/*     */   public void getLocation(Network network, final GeolocationProvider.LocationProviderCallback callback) {
/*  88 */     ObjectNode json = (ObjectNode)this.objectMapper.valueToTree(network);
/*  89 */     json.put("token", this.key);
/*     */     
/*  91 */     Context.getClient().target(this.url).request().async().post(Entity.json(json), new InvocationCallback<JsonObject>()
/*     */         {
/*     */           public void completed(JsonObject json) {
/*  94 */             if (json.getString("status").equals("error")) {
/*  95 */               callback.onFailure(new GeolocationException(json.getString("message")));
/*     */             } else {
/*  97 */               callback.onSuccess(json
/*  98 */                   .getJsonNumber("lat").doubleValue(), json
/*  99 */                   .getJsonNumber("lon").doubleValue(), json
/* 100 */                   .getJsonNumber("accuracy").doubleValue());
/*     */             } 
/*     */           }
/*     */ 
/*     */           
/*     */           public void failed(Throwable throwable) {
/* 106 */             callback.onFailure(throwable);
/*     */           }
/*     */         });
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geolocation\UnwiredGeolocationProvider.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */