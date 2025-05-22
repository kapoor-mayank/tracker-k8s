/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.Parser;
/*     */ import org.traccar.helper.PatternBuilder;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.Position;
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
/*     */ public class Ivt401ProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public Ivt401ProtocolDecoder(Protocol protocol) {
/*  33 */     super(protocol);
/*     */   }
/*     */   
/*  36 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  37 */     .text("(")
/*  38 */     .expression("TL[ABLN],")
/*  39 */     .number("(d+),")
/*  40 */     .number("(dd)(dd)(dd),")
/*  41 */     .number("(dd)(dd)(dd),")
/*  42 */     .number("([-+]d+.d+),")
/*  43 */     .number("([-+]d+.d+),")
/*  44 */     .number("(d+),")
/*  45 */     .number("(d+),")
/*  46 */     .number("(-?d+.?d*),")
/*  47 */     .number("d+,")
/*  48 */     .number("(d),")
/*  49 */     .number("(d+),")
/*  50 */     .number("(d+),")
/*  51 */     .number("(d+),")
/*  52 */     .number("(d+.d+),")
/*  53 */     .number("(d+.d+),")
/*  54 */     .number("(d+.d+),")
/*  55 */     .number("(-?d+.?d*),")
/*  56 */     .expression("([^,]+),")
/*  57 */     .number("(d+),")
/*  58 */     .number("(d+.d+),")
/*  59 */     .number("(-?d+),")
/*  60 */     .number("(d+),")
/*  61 */     .number("(d+),")
/*  62 */     .groupBegin()
/*  63 */     .number("([01]),")
/*  64 */     .number("[01],")
/*  65 */     .number("[01],")
/*  66 */     .number("[01],")
/*  67 */     .number("[0-2]+,")
/*  68 */     .number("([0-3]),")
/*  69 */     .number("[01],")
/*  70 */     .number("([01]),")
/*  71 */     .number("([01]),")
/*  72 */     .number("[01],")
/*  73 */     .number("([01]),")
/*  74 */     .number("[01],")
/*  75 */     .number("[128],")
/*  76 */     .expression("([^,]+)?,")
/*  77 */     .number("d+,")
/*  78 */     .groupEnd("?")
/*  79 */     .any()
/*  80 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  86 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  87 */     if (!parser.matches()) {
/*  88 */       return null;
/*     */     }
/*     */     
/*  91 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  92 */     if (deviceSession == null) {
/*  93 */       return null;
/*     */     }
/*     */     
/*  96 */     Position position = new Position(getProtocolName());
/*  97 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  99 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 101 */     position.setLatitude(parser.nextDouble().doubleValue());
/* 102 */     position.setLongitude(parser.nextDouble().doubleValue());
/* 103 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextInt().intValue()));
/* 104 */     position.setCourse(parser.nextInt().intValue());
/* 105 */     position.setAltitude(parser.nextDouble().doubleValue());
/* 106 */     position.setValid((parser.nextInt().intValue() > 0));
/*     */     
/* 108 */     position.set("rssi", parser.nextInt());
/*     */     
/* 110 */     String input = parser.next();
/* 111 */     for (int i = 0; i < input.length(); i++) {
/* 112 */       int value = Character.getNumericValue(input.charAt(i));
/* 113 */       if (value < 2) {
/* 114 */         position.set("in" + (i + 1), Boolean.valueOf((value > 0)));
/*     */       }
/*     */     } 
/*     */     
/* 118 */     String output = parser.next();
/* 119 */     for (int j = 0; j < output.length(); j++) {
/* 120 */       position.set("out" + (j + 1), Boolean.valueOf((Character.getNumericValue(output.charAt(j)) > 0)));
/*     */     }
/*     */     
/* 123 */     position.set("adc1", parser.nextDouble());
/* 124 */     position.set("power", parser.nextDouble());
/* 125 */     position.set("battery", parser.nextDouble());
/* 126 */     position.set("deviceTemp", parser.nextDouble());
/*     */     
/* 128 */     String temp = parser.next();
/* 129 */     if (temp.startsWith("M")) {
/* 130 */       int index = 1;
/* 131 */       int startIndex = 1;
/*     */       
/* 133 */       while (startIndex < temp.length()) {
/* 134 */         int endIndex = temp.indexOf('-', startIndex + 1);
/* 135 */         if (endIndex < 0) {
/* 136 */           endIndex = temp.indexOf('+', startIndex + 1);
/*     */         }
/* 138 */         if (endIndex < 0) {
/* 139 */           endIndex = temp.length();
/*     */         }
/* 141 */         if (endIndex > 0) {
/* 142 */           double value = Double.parseDouble(temp.substring(startIndex, endIndex));
/* 143 */           position.set("temp" + index++, Double.valueOf(value));
/*     */         } 
/* 145 */         startIndex = endIndex;
/*     */       } 
/*     */     } else {
/* 148 */       position.set("temp1", Double.valueOf(Double.parseDouble(temp)));
/*     */     } 
/*     */     
/* 151 */     position.set("motion", Boolean.valueOf((parser.nextInt().intValue() > 0)));
/* 152 */     position.set("acceleration", parser.nextDouble());
/*     */     
/* 154 */     parser.nextInt();
/* 155 */     parser.nextInt();
/*     */     
/* 157 */     position.set("odometer", parser.nextLong());
/*     */     
/* 159 */     if (parser.hasNext(6)) {
/* 160 */       position.set("alarm", (parser.nextInt().intValue() == 1) ? "overspeed" : null);
/* 161 */       switch (parser.nextInt().intValue()) {
/*     */         case 1:
/* 163 */           position.set("alarm", "hardAcceleration");
/*     */           break;
/*     */         case 2:
/* 166 */           position.set("alarm", "hardBraking");
/*     */           break;
/*     */         case 3:
/* 169 */           position.set("alarm", "hardCornering");
/*     */           break;
/*     */       } 
/*     */ 
/*     */       
/* 174 */       position.set("alarm", (parser.nextInt().intValue() == 1) ? "lowBattery" : null);
/* 175 */       position.set("alarm", (parser.nextInt().intValue() == 1) ? "powerCut" : null);
/* 176 */       position.set("alarm", (parser.nextInt().intValue() == 1) ? "tow" : null);
/* 177 */       position.set("driverUniqueId", parser.next());
/*     */     } 
/*     */     
/* 180 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\Ivt401ProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */