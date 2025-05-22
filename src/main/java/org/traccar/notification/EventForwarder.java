/*    */ package org.traccar.notification;
/*    */ 
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ import java.util.Set;
/*    */ import javax.ws.rs.client.AsyncInvoker;
/*    */ import javax.ws.rs.client.Invocation;
/*    */ import org.traccar.Context;
/*    */ import org.traccar.model.Device;
/*    */ import org.traccar.model.Event;
/*    */ import org.traccar.model.Geofence;
/*    */ import org.traccar.model.Maintenance;
/*    */ import org.traccar.model.Position;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ public abstract class EventForwarder
/*    */ {
/* 37 */   private final String url = Context.getConfig().getString("event.forward.url", "http://localhost/");
/* 38 */   private final String header = Context.getConfig().getString("event.forward.header");
/*    */   
/*    */   private static final String KEY_POSITION = "position";
/*    */   
/*    */   private static final String KEY_EVENT = "event";
/*    */   
/*    */   private static final String KEY_GEOFENCE = "geofence";
/*    */   private static final String KEY_DEVICE = "device";
/*    */   private static final String KEY_MAINTENANCE = "maintenance";
/*    */   private static final String KEY_USERS = "users";
/*    */   
/*    */   public final void forwardEvent(Event event, Position position, Set<Long> users) {
/* 50 */     Invocation.Builder requestBuilder = Context.getClient().target(this.url).request();
/*    */     
/* 52 */     if (this.header != null && !this.header.isEmpty()) {
/* 53 */       for (String line : this.header.split("\\r?\\n")) {
/* 54 */         String[] values = line.split(":", 2);
/* 55 */         requestBuilder.header(values[0].trim(), values[1].trim());
/*    */       } 
/*    */     }
/*    */     
/* 59 */     executeRequest(event, position, users, requestBuilder.async());
/*    */   }
/*    */   
/*    */   protected Map<String, Object> preparePayload(Event event, Position position, Set<Long> users) {
/* 63 */     Map<String, Object> data = new HashMap<>();
/* 64 */     data.put("event", event);
/* 65 */     if (position != null) {
/* 66 */       data.put("position", position);
/*    */     }
/* 68 */     Device device = Context.getIdentityManager().getById(event.getDeviceId());
/* 69 */     if (device != null) {
/* 70 */       data.put("device", device);
/*    */     }
/* 72 */     if (event.getGeofenceId() != 0L) {
/* 73 */       Geofence geofence = (Geofence)Context.getGeofenceManager().getById(event.getGeofenceId());
/* 74 */       if (geofence != null) {
/* 75 */         data.put("geofence", geofence);
/*    */       }
/*    */     } 
/* 78 */     if (event.getMaintenanceId() != 0L) {
/* 79 */       Maintenance maintenance = (Maintenance)Context.getMaintenancesManager().getById(event.getMaintenanceId());
/* 80 */       if (maintenance != null) {
/* 81 */         data.put("maintenance", maintenance);
/*    */       }
/*    */     } 
/* 84 */     data.put("users", Context.getUsersManager().getItems(users));
/* 85 */     return data;
/*    */   }
/*    */   
/*    */   protected abstract void executeRequest(Event paramEvent, Position paramPosition, Set<Long> paramSet, AsyncInvoker paramAsyncInvoker);
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\notification\EventForwarder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */