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
///*    */
///*    */ public class HuabaoProtocol
///*    */   extends BaseProtocol
///*    */ {
///*    */   public HuabaoProtocol() {
///* 26 */     setSupportedDataCommands(new String[] { "engineStop", "engineResume" });
///*    */
///*    */
///* 29 */     addServer(new TrackerServer(false, getName())
///*    */         {
///*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
///* 32 */             pipeline.addLast((ChannelHandler)new HuabaoFrameDecoder());
///* 33 */             pipeline.addLast((ChannelHandler)new HuabaoProtocolEncoder());
///* 34 */             pipeline.addLast((ChannelHandler)new HuabaoProtocolDecoder((Protocol)HuabaoProtocol.this));
///*    */           }
///*    */         });
///*    */   }
///*    */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\HuabaoProtocol.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */