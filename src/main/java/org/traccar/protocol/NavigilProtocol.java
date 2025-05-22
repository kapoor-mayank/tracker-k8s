///*    */ package org.traccar.protocol;
///*    */
///*    */ import io.netty.channel.ChannelHandler;
///*    */ import org.traccar.BaseProtocol;
///*    */ import org.traccar.PipelineBuilder;
///*    */ import org.traccar.Protocol;
///*    */ import org.traccar.TrackerServer;
///*    */
///*    */
///*    */
///*    */
///*    */
///*    */
///*    */
///*    */
///*    */
///*    */
///*    */
///*    */
///*    */
///*    */ public class NavigilProtocol
///*    */   extends BaseProtocol
///*    */ {
///*    */   public NavigilProtocol() {
///* 25 */     addServer(new TrackerServer(false, getName())
///*    */         {
///*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
///* 28 */             pipeline.addLast((ChannelHandler)new NavigilFrameDecoder());
///* 29 */             pipeline.addLast((ChannelHandler)new NavigilProtocolDecoder((Protocol)NavigilProtocol.this));
///*    */           }
///*    */         });
///*    */   }
///*    */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NavigilProtocol.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */