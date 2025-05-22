/*    */ package org.traccar.notification;
/*    */ 
/*    */ import java.util.Set;
/*    */ import javax.ws.rs.client.AsyncInvoker;
/*    */ import javax.ws.rs.client.Entity;
/*    */ import org.traccar.model.Event;
/*    */ import org.traccar.model.Position;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class JsonTypeEventForwarder
/*    */   extends EventForwarder
/*    */ {
/*    */   protected void executeRequest(Event event, Position position, Set<Long> users, AsyncInvoker invoker) {
/* 15 */     invoker.post(Entity.json(preparePayload(event, position, users)));
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\notification\JsonTypeEventForwarder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */