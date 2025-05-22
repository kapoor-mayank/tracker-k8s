/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import org.traccar.BaseProtocolEncoder;
/*     */ import org.traccar.helper.DataConverter;
/*     */ import org.traccar.model.Command;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class EelinkProtocolEncoder
/*     */   extends BaseProtocolEncoder
/*     */ {
/*     */   private boolean connectionless;
/*     */   
/*     */   public EelinkProtocolEncoder(boolean connectionless) {
/*  32 */     this.connectionless = connectionless;
/*     */   }
/*     */   
/*     */   public static int checksum(ByteBuffer buf) {
/*  36 */     int sum = 0;
/*  37 */     while (buf.hasRemaining()) {
/*  38 */       sum = (sum << 1 | sum >> 15) + (buf.get() & 0xFF) & 0xFFFF;
/*     */     }
/*  40 */     return sum;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static ByteBuf encodeContent(boolean connectionless, String uniqueId, int type, int index, ByteBuf content) {
/*  46 */     ByteBuf buf = Unpooled.buffer();
/*     */     
/*  48 */     if (connectionless) {
/*  49 */       buf.writeBytes(DataConverter.parseHex('0' + uniqueId));
/*     */     }
/*     */     
/*  52 */     buf.writeByte(103);
/*  53 */     buf.writeByte(103);
/*  54 */     buf.writeByte(type);
/*  55 */     buf.writeShort(2 + ((content != null) ? content.readableBytes() : 0));
/*  56 */     buf.writeShort(index);
/*     */     
/*  58 */     if (content != null) {
/*  59 */       buf.writeBytes(content);
/*     */     }
/*     */     
/*  62 */     ByteBuf result = Unpooled.buffer();
/*     */     
/*  64 */     if (connectionless) {
/*  65 */       result.writeByte(69);
/*  66 */       result.writeByte(76);
/*  67 */       result.writeShort(2 + buf.readableBytes());
/*  68 */       result.writeShort(checksum(buf.nioBuffer()));
/*     */     } 
/*     */     
/*  71 */     result.writeBytes(buf);
/*  72 */     buf.release();
/*     */     
/*  74 */     return result;
/*     */   }
/*     */ 
/*     */   
/*     */   private ByteBuf encodeContent(long deviceId, String content) {
/*  79 */     ByteBuf buf = Unpooled.buffer();
/*     */     
/*  81 */     buf.writeByte(1);
/*  82 */     buf.writeInt(0);
/*  83 */     buf.writeBytes(content.getBytes(StandardCharsets.UTF_8));
/*     */     
/*  85 */     return encodeContent(this.connectionless, getUniqueId(deviceId), 128, 0, buf);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object encodeCommand(Command command) {
/*  91 */     switch (command.getType()) {
/*     */       case "custom":
/*  93 */         return encodeContent(command.getDeviceId(), command.getString("data"));
/*     */       case "positionSingle":
/*  95 */         return encodeContent(command.getDeviceId(), "WHERE#");
/*     */       case "engineStop":
/*  97 */         return encodeContent(command.getDeviceId(), "RELAY,1#");
/*     */       case "engineResume":
/*  99 */         return encodeContent(command.getDeviceId(), "RELAY,0#");
/*     */       case "rebootDevice":
/* 101 */         return encodeContent(command.getDeviceId(), "RESET#");
/*     */     } 
/* 103 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EelinkProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */