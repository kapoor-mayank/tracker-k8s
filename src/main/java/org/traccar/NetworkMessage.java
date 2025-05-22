/*    */ package org.traccar;
/*    */ 
/*    */ import java.net.SocketAddress;
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
/*    */ public class NetworkMessage
/*    */ {
/*    */   private final SocketAddress remoteAddress;
/*    */   private final Object message;

    @Override
    public String toString() {
        return "NetworkMessage{" +
                "remoteAddress=" + remoteAddress +
                ", message=" + message +
                '}';
    }

    /*    */
/*    */   public NetworkMessage(Object message, SocketAddress remoteAddress) {
/* 26 */     this.message = message;
/* 27 */     this.remoteAddress = remoteAddress;
/*    */   }
/*    */   
/*    */   public SocketAddress getRemoteAddress() {
/* 31 */     return this.remoteAddress;
/*    */   }
/*    */   
/*    */   public Object getMessage() {
/* 35 */     return this.message;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\NetworkMessage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */