/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.text.DecimalFormat;
/*     */ import java.text.DecimalFormatSymbols;
/*     */ import java.util.HashMap;
/*     */ import java.util.Locale;
/*     */ import java.util.Map;
/*     */ import java.util.TimeZone;
/*     */ import org.traccar.StringProtocolEncoder;
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
/*     */ public class WatchProtocolEncoder
/*     */   extends StringProtocolEncoder
/*     */   implements StringProtocolEncoder.ValueFormatter
/*     */ {
/*     */   public String formatValue(String key, Object value) {
/*  37 */     if (key.equals("timezone")) {
/*  38 */       double offset = TimeZone.getTimeZone((String)value).getRawOffset() / 3600000.0D;
/*  39 */       DecimalFormat fmt = new DecimalFormat("+#.##;-#.##", DecimalFormatSymbols.getInstance(Locale.US));
/*  40 */       return fmt.format(offset);
/*  41 */     }  if (key.equals("message"))
/*  42 */       return DataConverter.printHex(value.toString().getBytes(StandardCharsets.UTF_16BE)); 
/*  43 */     if (key.equals("enable")) {
/*  44 */       return ((Boolean)value).booleanValue() ? "1" : "0";
/*     */     }
/*     */     
/*  47 */     return null;
/*     */   }
/*     */   
/*     */   protected ByteBuf formatTextCommand(Channel channel, Command command, String format, String... keys) {
/*  51 */     String content = formatCommand(command, format, this, keys);
/*  52 */     ByteBuf buf = Unpooled.copiedBuffer(content, StandardCharsets.US_ASCII);
/*     */     
/*  54 */     return formatBinaryCommand(channel, command, "", buf);
/*     */   }
/*     */   
/*     */   protected ByteBuf formatBinaryCommand(Channel channel, Command command, String textPrefix, ByteBuf data) {
/*  58 */     boolean hasIndex = false;
/*  59 */     String manufacturer = "CS";
/*  60 */     if (channel != null) {
/*  61 */       WatchProtocolDecoder decoder = (WatchProtocolDecoder)channel.pipeline().get(WatchProtocolDecoder.class);
/*  62 */       if (decoder != null) {
/*  63 */         hasIndex = decoder.getHasIndex();
/*  64 */         manufacturer = decoder.getManufacturer();
/*  65 */         if (manufacturer.equals("3G")) {
/*  66 */           manufacturer = "SG";
/*     */         }
/*     */       } 
/*     */     } 
/*     */     
/*  71 */     ByteBuf buf = Unpooled.buffer();
/*  72 */     buf.writeByte(91);
/*  73 */     buf.writeCharSequence(manufacturer, StandardCharsets.US_ASCII);
/*  74 */     buf.writeByte(42);
/*  75 */     buf.writeCharSequence(getUniqueId(command.getDeviceId()), StandardCharsets.US_ASCII);
/*  76 */     buf.writeByte(42);
/*  77 */     if (hasIndex) {
/*  78 */       buf.writeCharSequence("0001", StandardCharsets.US_ASCII);
/*  79 */       buf.writeByte(42);
/*     */     } 
/*  81 */     buf.writeCharSequence(String.format("%04x", new Object[] { Integer.valueOf(data.readableBytes() + textPrefix.length()) }), StandardCharsets.US_ASCII);
/*     */     
/*  83 */     buf.writeByte(42);
/*  84 */     buf.writeCharSequence(textPrefix, StandardCharsets.US_ASCII);
/*  85 */     buf.writeBytes(data);
/*  86 */     buf.writeByte(93);
/*     */     
/*  88 */     return buf;
/*     */   }
/*     */   
/*  91 */   private static Map<Byte, Byte> mapping = new HashMap<>();
/*     */   
/*     */   static {
/*  94 */     mapping.put(Byte.valueOf((byte)125), Byte.valueOf((byte)1));
/*  95 */     mapping.put(Byte.valueOf((byte)91), Byte.valueOf((byte)2));
/*  96 */     mapping.put(Byte.valueOf((byte)93), Byte.valueOf((byte)3));
/*  97 */     mapping.put(Byte.valueOf((byte)44), Byte.valueOf((byte)4));
/*  98 */     mapping.put(Byte.valueOf((byte)42), Byte.valueOf((byte)5));
/*     */   }
/*     */   
/*     */   private ByteBuf getBinaryData(Command command) {
/* 102 */     byte[] data = DataConverter.parseHex(command.getString("data"));
/*     */     
/* 104 */     int encodedLength = data.length;
/* 105 */     for (byte b : data) {
/* 106 */       if (mapping.containsKey(Byte.valueOf(b))) {
/* 107 */         encodedLength++;
/*     */       }
/*     */     } 
/*     */     
/* 111 */     int index = 0;
/* 112 */     byte[] encodedData = new byte[encodedLength];
/*     */     
/* 114 */     for (byte b : data) {
/* 115 */       Byte replacement = mapping.get(Byte.valueOf(b));
/* 116 */       if (replacement != null) {
/* 117 */         encodedData[index] = 125;
/* 118 */         index++;
/* 119 */         encodedData[index] = replacement.byteValue();
/*     */       } else {
/* 121 */         encodedData[index] = b;
/*     */       } 
/* 123 */       index++;
/*     */     } 
/*     */     
/* 126 */     return Unpooled.copiedBuffer(encodedData);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object encodeCommand(Channel channel, Command command) {
/* 132 */     switch (command.getType()) {
/*     */       case "custom":
/* 134 */         return formatTextCommand(channel, command, command.getString("data"), new String[0]);
/*     */       case "positionSingle":
/* 136 */         return formatTextCommand(channel, command, "RG", new String[0]);
/*     */       case "sosNumber":
/* 138 */         return formatTextCommand(channel, command, "SOS{%s},{%s}", new String[] { "index", "phone" });
/*     */       case "alarmSos":
/* 140 */         return formatTextCommand(channel, command, "SOSSMS,{%s}", new String[] { "enable" });
/*     */       case "alarmBattery":
/* 142 */         return formatTextCommand(channel, command, "LOWBAT,{%s}", new String[] { "enable" });
/*     */       case "rebootDevice":
/* 144 */         return formatTextCommand(channel, command, "RESET", new String[0]);
/*     */       case "powerOff":
/* 146 */         return formatTextCommand(channel, command, "POWEROFF", new String[0]);
/*     */       case "alarmRemove":
/* 148 */         return formatTextCommand(channel, command, "REMOVE,{%s}", new String[] { "enable" });
/*     */       case "silenceTime":
/* 150 */         return formatTextCommand(channel, command, "SILENCETIME,{%s}", new String[] { "data" });
/*     */       case "alarmClock":
/* 152 */         return formatTextCommand(channel, command, "REMIND,{%s}", new String[] { "data" });
/*     */       case "setPhonebook":
/* 154 */         return formatTextCommand(channel, command, "PHB,{%s}", new String[] { "data" });
/*     */       case "message":
/* 156 */         return formatTextCommand(channel, command, "MESSAGE,{%s}", new String[] { "message" });
/*     */       case "voiceMessage":
/* 158 */         return formatBinaryCommand(channel, command, "TK,", getBinaryData(command));
/*     */       case "positionPeriodic":
/* 160 */         return formatTextCommand(channel, command, "UPLOAD,{%s}", new String[] { "frequency" });
/*     */       case "setTimezone":
/* 162 */         return formatTextCommand(channel, command, "LZ,,{%s}", new String[] { "timezone" });
/*     */       case "setIndicator":
/* 164 */         return formatTextCommand(channel, command, "FLOWER,{%s}", new String[] { "data" });
/*     */     } 
/* 166 */     return null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\WatchProtocolEncoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */