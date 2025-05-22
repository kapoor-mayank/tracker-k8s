///*    */ package org.traccar.protocol;
///*    */
///*    */ import io.netty.buffer.ByteBuf;
///*    */ import io.netty.buffer.Unpooled;
///*    */ import java.text.SimpleDateFormat;
///*    */ import java.util.Date;
///*    */ import org.traccar.BaseProtocolEncoder;
///*    */ import org.traccar.Context;
///*    */ import org.traccar.helper.DataConverter;
///*    */ import org.traccar.model.Command;
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
///*    */
///*    */
///*    */
///*    */
///*    */ public class HuabaoProtocolEncoder
///*    */   extends BaseProtocolEncoder
///*    */ {
///*    */   protected Object encodeCommand(Command command) {
///* 33 */     boolean alternative = Context.getIdentityManager().lookupAttributeBoolean(command
///* 34 */         .getDeviceId(), "huabao.alternative", false, true);
///*    */
///* 36 */     ByteBuf id = Unpooled.wrappedBuffer(
///* 37 */         DataConverter.parseHex(getUniqueId(command.getDeviceId())));
///*    */     try {
///* 39 */       ByteBuf data = Unpooled.buffer();
///* 40 */       byte[] time = DataConverter.parseHex((new SimpleDateFormat("yyMMddHHmmss")).format(new Date()));
///*    */
///* 42 */       switch (command.getType()) {
///*    */         case "engineStop":
///* 44 */           if (alternative) {
///* 45 */             data.writeByte(1);
///* 46 */             data.writeBytes(time);
///* 47 */             return HuabaoProtocolDecoder.formatMessage(40966, id, false, data);
///*    */           }
///*    */
///* 50 */           data.writeByte(100);
///* 51 */           byteBuf1 = HuabaoProtocolDecoder.formatMessage(33029, id, false, data); return byteBuf1;
///*    */
///*    */
///*    */         case "engineResume":
///* 55 */           if (alternative) {
///* 56 */             data.writeByte(0);
///* 57 */             data.writeBytes(time);
///* 58 */             byteBuf1 = HuabaoProtocolDecoder.formatMessage(40966, id, false, data); return byteBuf1;
///*    */           }
///*    */
///* 61 */           data.writeByte(101);
///* 62 */           byteBuf1 = HuabaoProtocolDecoder.formatMessage(33029, id, false, data); return byteBuf1;
///*    */       }
///*    */
///*    */
///* 66 */       ByteBuf byteBuf1 = null; return byteBuf1;
///*    */     } finally {
///*    */
///* 69 */       id.release();
///*    */     }
///*    */   }
///*    */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\HuabaoProtocolEncoder.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */