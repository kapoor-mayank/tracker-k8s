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
/*     */ public class EnvotechProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   public EnvotechProtocolDecoder(Protocol protocol) {
/*  32 */     super(protocol);
/*     */   }
/*     */   
/*  35 */   private static final Pattern PATTERN = (new PatternBuilder())
/*  36 */     .text("$")
/*  37 */     .number("dd")
/*  38 */     .expression("...,")
/*  39 */     .number("(x+),")
/*  40 */     .number("x+,")
/*  41 */     .number("(x+),")
/*  42 */     .number("(dd)(dd)(dd)")
/*  43 */     .number("(dd)(dd)(dd),")
/*  44 */     .number("xx")
/*  45 */     .number("(dd)")
/*  46 */     .number("d{5},")
/*  47 */     .number("(ddd)")
/*  48 */     .number("(ddd),")
/*  49 */     .number("(xx)")
/*  50 */     .number("(xx),")
/*  51 */     .number("(xxx)?")
/*  52 */     .number("(xxx)?,")
/*  53 */     .number("(x{8}),")
/*  54 */     .expression("[^']*'")
/*  55 */     .number("(dd)(dd)(dd)")
/*  56 */     .number("(dd)(dd)(dd)")
/*  57 */     .number("(d)")
/*  58 */     .number("(d+)(d{5})([NS])")
/*  59 */     .number("(d+)(d{5})([EW])")
/*  60 */     .number("(ddd)")
/*  61 */     .number("(ddd)")
/*  62 */     .any()
/*  63 */     .compile();
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  69 */     Parser parser = new Parser(PATTERN, (String)msg);
/*  70 */     if (!parser.matches()) {
/*  71 */       return null;
/*     */     }
/*     */     
/*  74 */     Position position = new Position(getProtocolName());
/*     */     
/*  76 */     int event = parser.nextHexInt().intValue();
/*  77 */     switch (event) {
/*     */       case 96:
/*  79 */         position.set("alarm", "lock");
/*     */         break;
/*     */       case 97:
/*  82 */         position.set("alarm", "unlock");
/*     */         break;
/*     */     } 
/*     */ 
/*     */     
/*  87 */     position.set("event", Integer.valueOf(event));
/*     */     
/*  89 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { parser.next() });
/*  90 */     if (deviceSession == null) {
/*  91 */       return null;
/*     */     }
/*  93 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  95 */     position.setDeviceTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/*     */     
/*  97 */     position.set("rssi", parser.nextInt());
/*  98 */     position.set("power", Double.valueOf(parser.nextInt().intValue() * 0.01D));
/*  99 */     position.set("battery", Double.valueOf(parser.nextInt().intValue() * 0.01D));
/* 100 */     position.set("input", parser.nextHexInt());
/* 101 */     position.set("out", parser.nextHexInt());
/* 102 */     position.set("fuel", parser.nextHexInt());
/* 103 */     position.set("weight", parser.nextHexInt());
/* 104 */     position.set("status", parser.nextHexLong());
/*     */     
/* 106 */     position.setFixTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));
/* 107 */     position.setValid((parser.nextInt().intValue() > 0));
/* 108 */     position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_DEG_HEM));
/* 109 */     position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_DEG_HEM));
/* 110 */     position.setSpeed(parser.nextInt().intValue());
/* 111 */     position.setCourse(parser.nextInt().intValue());
/*     */     
/* 113 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\EnvotechProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */