///*    */ package org.traccar.protocol;
///*    */
///*    */ import io.netty.channel.ChannelHandler;
///*    */ import io.netty.handler.codec.string.StringDecoder;
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
///*    */ public class SabertekProtocol
///*    */   extends BaseProtocol
///*    */ {
///*    */   public SabertekProtocol() {
///* 26 */     addServer(new TrackerServer(false, getName())
///*    */         {
///*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
///* 29 */             pipeline.addLast((ChannelHandler)new SabertekFrameDecoder());
///* 30 */             pipeline.addLast((ChannelHandler)new StringDecoder());
///* 31 */             pipeline.addLast((ChannelHandler)new SabertekProtocolDecoder((Protocol)SabertekProtocol.this));
///*    */           }
///*    */         });
///*    */   }
///*    */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\SabertekProtocol.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */