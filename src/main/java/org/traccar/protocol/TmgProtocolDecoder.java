/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.regex.Pattern;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
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
/*     */ public class TmgProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public TmgProtocolDecoder(Protocol protocol) {
/*  34 */     super(protocol);
/*     */   }
/*     */   
/*  37 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  38 */     .text("$")
/*  39 */     .expression("(...),")
/*  40 */     .expression("[LH],").optional()
/*  41 */     .number("(d+),")
/*  42 */     .number("(dd)(dd)(dddd),")
/*  43 */     .number("(dd)(dd)(dd),")
/*  44 */     .number("(d),")
/*  45 */     .number("(dd)(dd.d+),")
/*  46 */     .expression("([NS]),")
/*  47 */     .number("(ddd)(dd.d+),")
/*  48 */     .expression("([EW]),")
/*  49 */     .number("(d+.?d*),")
/*  50 */     .number("(d+.?d*),")
/*  51 */     .groupBegin()
/*  52 */     .number("(-?d+.?d*),")
/*  53 */     .number("(d+.d+),")
/*  54 */     .number("(d+),")
/*  55 */     .number("(d+),")
/*  56 */     .number("([^,]*),")
/*  57 */     .number("(d+),")
/*  58 */     .number("[^,]*,")
/*  59 */     .expression("([01]),")
/*  60 */     .number("(d+.?d*),")
/*  61 */     .number("(d+.?d*),")
/*  62 */     .expression("([01]+),")
/*  63 */     .expression("([01]+),")
/*  64 */     .expression("[01]+,")
/*  65 */     .number("(d+.?d*)[^,]*,")
/*  66 */     .number("(d+.?d*)[^,]*,")
/*  67 */     .number("d+.?d*,")
/*  68 */     .expression("([^,]*),")
/*  69 */     .expression("([^,]*),").optional()
/*  70 */     .or()
/*  71 */     .number("[^,]*,")
/*  72 */     .number("(d+),")
/*  73 */     .number("(d+),")
/*  74 */     .number("[^,]*,")
/*  75 */     .expression("([01]),")
/*  76 */     .expression("([LH]{4}),")
/*  77 */     .expression("[NT]{4},")
/*  78 */     .expression("([LH]{2}),")
/*  79 */     .number("(d+.d+),")
/*  80 */     .number("(d+.d+),")
/*  81 */     .number("[^,]*,")
/*  82 */     .number("(d+),")
/*  83 */     .groupEnd()
/*  84 */     .any()
/*  85 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  91 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  92 */     if (!parser.matches()) {
/*  93 */       return null;
/*     */     }
/*     */     
/*  96 */     String type = parser.next();
/*     */     
/*  98 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  99 */     if (deviceSession == null) {
/* 100 */       return null;
/*     */     }
/*     */     
/* 103 */     Position position = new Position(getProtocolName());
/* 104 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/* 106 */     switch (type) {
/*     */       case "rmv":
/* 108 */         position.set("alarm", "powerCut");
/*     */         break;
/*     */       case "ebl":
/* 111 */         position.set("alarm", "lowPower");
/*     */         break;
/*     */       case "ibl":
/* 114 */         position.set("alarm", "lowBattery");
/*     */         break;
/*     */       case "tmp":
/*     */       case "smt":
/*     */       case "btt":
/* 119 */         position.set("alarm", "tampering");
/*     */         break;
/*     */       case "ion":
/* 122 */         position.set("ignition", Boolean.valueOf(true));
/*     */         break;
/*     */       case "iof":
/* 125 */         position.set("ignition", Boolean.valueOf(false));
/*     */         break;
/*     */     } 
/*     */ 
/*     */ 
/*     */     
/* 131 */     position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/* 133 */     position.setValid((parser.nextInt().intValue() > 0));
/* 134 */     position.setLatitude(parser.nextCoordinate());
/* 135 */     position.setLongitude(parser.nextCoordinate());
/* 136 */     position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble().doubleValue()));
/* 137 */     position.setCourse(parser.nextDouble().doubleValue());
/*     */     
/* 139 */     if (parser.hasNext(15)) {
/*     */       
/* 141 */       position.setAltitude(parser.nextDouble().doubleValue());
/*     */       
/* 143 */       position.set("hdop", parser.nextDouble());
/* 144 */       position.set("sat", parser.nextInt());
/* 145 */       position.set("satVisible", parser.nextInt());
/* 146 */       position.set("operator", parser.next());
/* 147 */       position.set("rssi", parser.nextInt());
/* 148 */       position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/* 149 */       position.set("battery", parser.nextDouble());
/* 150 */       position.set("power", parser.nextDouble());
/*     */       
/* 152 */       int input = parser.nextBinInt().intValue();
/* 153 */       int output = parser.nextBinInt().intValue();
/*     */       
/* 155 */       if (!BitUtil.check(input, 0)) {
/* 156 */         position.set("alarm", "sos");
/*     */       }
/*     */       
/* 159 */       position.set("input", Integer.valueOf(input));
/* 160 */       position.set("output", Integer.valueOf(output));
/*     */       
/* 162 */       position.set("adc1", parser.nextDouble());
/* 163 */       position.set("adc2", parser.nextDouble());
/* 164 */       position.set("versionFw", parser.next());
/* 165 */       position.set("driverUniqueId", parser.next());
/*     */     } 
/*     */ 
/*     */     
/* 169 */     if (parser.hasNext(6)) {
/*     */       
/* 171 */       position.set("rssi", parser.nextInt());
/* 172 */       position.set("sat", parser.nextInt());
/* 173 */       position.set("ignition", Boolean.valueOf((parser.nextInt().intValue() == 1)));
/*     */       
/* 175 */       char[] input = parser.next().toCharArray();
/* 176 */       for (int i = 0; i < input.length; i++) {
/* 177 */         position.set("in" + (i + 1), Boolean.valueOf((input[i] == 'H')));
/*     */       }
/*     */       
/* 180 */       char[] output = parser.next().toCharArray();
/* 181 */       for (int j = 0; j < output.length; j++) {
/* 182 */         position.set("out" + (j + 1), Boolean.valueOf((output[j] == 'H')));
/*     */       }
/*     */       
/* 185 */       position.set("adc1", parser.nextDouble());
/* 186 */       position.set("adc2", parser.nextDouble());
/* 187 */       position.set("odometer", parser.nextInt());
/*     */     } 
/*     */ 
/*     */     
/* 191 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\TmgProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */