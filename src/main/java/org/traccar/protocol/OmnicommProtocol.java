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
///*    */ public class OmnicommProtocol
///*    */   extends BaseProtocol
///*    */ {
///*    */   public OmnicommProtocol() {
///* 25 */     addServer(new TrackerServer(false, getName())
///*    */         {
///*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
///* 28 */             pipeline.addLast((ChannelHandler)new OmnicommFrameDecoder());
///* 29 */             pipeline.addLast((ChannelHandler)new OmnicommProtocolDecoder((Protocol)OmnicommProtocol.this));
///*    */           }
///*    */         });
///*    */   }
///*    */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OmnicommProtocol.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */