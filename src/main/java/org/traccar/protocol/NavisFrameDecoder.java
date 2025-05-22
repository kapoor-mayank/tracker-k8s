///*     */ package org.traccar.protocol;
///*     */
///*     */ import io.netty.buffer.ByteBuf;
///*     */ import io.netty.channel.Channel;
///*     */ import io.netty.channel.ChannelHandlerContext;
///*     */ import java.nio.charset.StandardCharsets;
///*     */ import org.traccar.BaseFrameDecoder;
///*     */ import org.traccar.BasePipelineFactory;
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */
///*     */ public class NavisFrameDecoder
///*     */   extends BaseFrameDecoder
///*     */ {
///*     */   private static final int NTCB_HEADER_LENGTH = 16;
///*     */   private static final int NTCB_LENGTH_OFFSET = 12;
///*     */   private static final int FLEX_HEADER_LENGTH = 2;
///*     */   private int flexDataSize;
///*     */
///*     */   public void setFlexDataSize(int flexDataSize) {
///*  34 */     this.flexDataSize = flexDataSize;
///*     */   }
///*     */
///*     */
///*     */
///*     */
///*     */   protected Object decode(ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {
///*  41 */     if (buf.getByte(buf.readerIndex()) == Byte.MAX_VALUE) {
///*  42 */       return buf.readRetainedSlice(1);
///*     */     }
///*     */
///*  45 */     if (ctx != null && this.flexDataSize == 0) {
///*     */
///*  47 */       NavisProtocolDecoder protocolDecoder = (NavisProtocolDecoder)BasePipelineFactory.getHandler(ctx.pipeline(), NavisProtocolDecoder.class);
///*  48 */       if (protocolDecoder != null) {
///*  49 */         this.flexDataSize = protocolDecoder.getFlexDataSize();
///*     */       }
///*     */     }
///*     */
///*  53 */     if (this.flexDataSize > 0) {
///*     */
///*  55 */       if (buf.readableBytes() > 2) {
///*  56 */         int i, length = 0;
///*  57 */         String type = buf.toString(buf.readerIndex(), 2, StandardCharsets.US_ASCII);
///*  58 */         switch (type) {
///*     */
///*     */           case "~A":
///*  61 */             length = this.flexDataSize * buf.getByte(buf.readerIndex() + 2) + 1 + 1;
///*     */             break;
///*     */           case "~T":
///*  64 */             length = this.flexDataSize + 4 + 1;
///*     */             break;
///*     */           case "~C":
///*  67 */             length = this.flexDataSize + 1;
///*     */             break;
///*     */
///*     */           case "~E":
///*  71 */             length++;
///*  72 */             for (i = 0; i < buf.getByte(buf.readerIndex() + 2); i++) {
///*  73 */               if (buf.readableBytes() > 2 + length + 1) {
///*  74 */                 length += buf.getUnsignedShort(length + 2) + 2;
///*     */               } else {
///*  76 */                 return null;
///*     */               }
///*     */             }
///*  79 */             length++;
///*     */             break;
///*     */           case "~X":
///*  82 */             length = buf.getUnsignedShortLE(buf.readerIndex() + 2) + 4 + 1;
///*     */             break;
///*     */           default:
///*  85 */             return null;
///*     */         }
///*     */
///*  88 */         if (buf.readableBytes() >= 2 + length) {
///*  89 */           return buf.readRetainedSlice(buf.readableBytes());
///*     */         }
///*     */       }
///*     */
///*     */     } else {
///*     */
///*  95 */       if (buf.readableBytes() < 16) {
///*  96 */         return null;
///*     */       }
///*     */
///*  99 */       int length = 16 + buf.getUnsignedShortLE(buf.readerIndex() + 12);
///* 100 */       if (buf.readableBytes() >= length) {
///* 101 */         return buf.readRetainedSlice(length);
///*     */       }
///*     */     }
///*     */
///*     */
///* 106 */     return null;
///*     */   }
///*     */ }
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NavisFrameDecoder.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */