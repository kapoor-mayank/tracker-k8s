/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.channel.Channel;
/*     */ import java.net.SocketAddress;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Arrays;
/*     */ import java.util.Date;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.stream.Collectors;
/*     */ import org.traccar.BaseProtocolDecoder;
/*     */ import org.traccar.Context;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.Protocol;
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
/*     */ public class T622IridiumProtocolDecoder
/*     */   extends BaseProtocolDecoder
/*     */ {
/*     */   private String format;
/*     */   
/*     */   public T622IridiumProtocolDecoder(Protocol protocol) {
/*  39 */     super(protocol);
/*  40 */     this.format = Context.getConfig().getString(getProtocolName() + ".format");
/*     */   }
/*     */   
/*     */   public List<Integer> getParameters(long deviceId) {
/*  44 */     return (List<Integer>)Arrays.<String>stream(this.format.split(","))
/*  45 */       .map(s -> Integer.valueOf(Integer.parseInt(s, 16)))
/*  46 */       .collect(Collectors.toList());
/*     */   }
/*     */   
/*     */   public void setFormat(String format) {
/*  50 */     this.format = format;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/*  57 */     ByteBuf buf = (ByteBuf)msg;
/*     */     
/*  59 */     buf.readUnsignedByte();
/*  60 */     buf.readUnsignedShort();
/*  61 */     buf.readUnsignedByte();
/*  62 */     buf.readUnsignedShort();
/*  63 */     buf.readUnsignedInt();
/*     */     
/*  65 */     String imei = buf.readCharSequence(15, StandardCharsets.US_ASCII).toString();
/*  66 */     DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { imei });
/*  67 */     if (deviceSession == null) {
/*  68 */       return null;
/*     */     }
/*     */     
/*  71 */     buf.readUnsignedByte();
/*  72 */     buf.readUnsignedShort();
/*  73 */     buf.readUnsignedShort();
/*  74 */     buf.readUnsignedInt();
/*  75 */     buf.readUnsignedByte();
/*  76 */     buf.readUnsignedShort();
/*     */     
/*  78 */     Position position = new Position(getProtocolName());
/*  79 */     position.setDeviceId(deviceSession.getDeviceId());
/*     */     
/*  81 */     List<Integer> parameters = getParameters(deviceSession.getDeviceId());
/*     */     
/*  83 */     for (Iterator<Integer> iterator = parameters.iterator(); iterator.hasNext(); ) { int parameter = ((Integer)iterator.next()).intValue();
/*  84 */       switch (parameter) {
/*     */         case 1:
/*  86 */           position.set("event", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/*     */         case 2:
/*  89 */           position.setLatitude(buf.readIntLE() / 1000000.0D);
/*     */         
/*     */         case 3:
/*  92 */           position.setLongitude(buf.readIntLE() / 1000000.0D);
/*     */         
/*     */         case 4:
/*  95 */           position.setTime(new Date((buf.readUnsignedIntLE() + 946684800L) * 1000L));
/*     */         
/*     */         case 5:
/*  98 */           position.setValid((buf.readUnsignedByte() > 0));
/*     */         
/*     */         case 6:
/* 101 */           position.set("sat", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/*     */         case 7:
/* 104 */           position.set("rssi", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/*     */         case 8:
/* 107 */           position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShortLE()));
/*     */         
/*     */         case 9:
/* 110 */           position.setCourse(buf.readUnsignedShortLE());
/*     */         
/*     */         case 10:
/* 113 */           position.set("hdop", Double.valueOf(buf.readUnsignedByte() * 0.1D));
/*     */         
/*     */         case 11:
/* 116 */           position.setAltitude(buf.readShortLE());
/*     */         
/*     */         case 12:
/* 119 */           position.set("odometer", Long.valueOf(buf.readUnsignedIntLE()));
/*     */         
/*     */         case 13:
/* 122 */           position.set("hours", Long.valueOf(buf.readUnsignedIntLE() * 1000L));
/*     */         
/*     */         case 20:
/* 125 */           position.set("output", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/*     */         case 21:
/* 128 */           position.set("input", Short.valueOf(buf.readUnsignedByte()));
/*     */         
/*     */         case 25:
/* 131 */           position.set("battery", Double.valueOf(buf.readUnsignedShortLE() * 0.01D));
/*     */         
/*     */         case 26:
/* 134 */           position.set("power", Double.valueOf(buf.readUnsignedShortLE() * 0.01D));
/*     */         
/*     */         case 27:
/* 137 */           buf.readUnsignedByte();
/*     */       } 
/*     */ 
/*     */ 
/*     */       
/*     */        }
/*     */     
/* 144 */     return position;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\T622IridiumProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */