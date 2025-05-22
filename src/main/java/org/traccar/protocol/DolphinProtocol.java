///*    */ package org.traccar.protocol;
///*    */
///*    */ import io.netty.channel.ChannelHandler;
///*    */ import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
///*    */ import java.nio.ByteOrder;
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
///*    */ public class DolphinProtocol
///*    */   extends BaseProtocol
///*    */ {
///*    */   public DolphinProtocol() {
///* 28 */     addServer(new TrackerServer(false, getName())
///*    */         {
///*    */           protected void addProtocolHandlers(PipelineBuilder pipeline) {
///* 31 */             pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 4096, 20, 4, 4, 0, true));
///* 32 */             pipeline.addLast((ChannelHandler)new DolphinProtocolDecoder((Protocol)DolphinProtocol.this));
///*    */           }
///*    */         });
///*    */   }
///*    */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\DolphinProtocol.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */