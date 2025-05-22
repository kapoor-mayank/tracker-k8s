/*    */ package org.traccar.protocol;
/*    */ 
/*    */ import io.netty.channel.Channel;
/*    */ import java.net.SocketAddress;
/*    */ import java.text.SimpleDateFormat;
/*    */ import org.traccar.BaseProtocolDecoder;
/*    */ import org.traccar.Context;
/*    */ import org.traccar.DeviceSession;
/*    */ import org.traccar.Protocol;
/*    */ import org.traccar.helper.UnitsConverter;
/*    */ import org.traccar.model.Position;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class GenxProtocolDecoder
/*    */   extends BaseProtocolDecoder
/*    */ {
/*    */   private int[] reportColumns;
/*    */   
/*    */   public GenxProtocolDecoder(Protocol protocol) {
/* 34 */     super(protocol);
/* 35 */     setReportColumns(Context.getConfig().getString(getProtocolName() + ".reportColumns", "1,2,3,4"));
/*    */   }
/*    */   
/*    */   public void setReportColumns(String format) {
/* 39 */     String[] columns = format.split(",");
/* 40 */     this.reportColumns = new int[columns.length];
/* 41 */     for (int i = 0; i < columns.length; i++) {
/* 42 */       this.reportColumns[i] = Integer.parseInt(columns[i]);
/*    */     }
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 50 */     String[] values = ((String)msg).split(",");
/*    */     
/* 52 */     Position position = new Position(getProtocolName());
/* 53 */     position.setValid(true);
/*    */     
/* 55 */     for (int i = 0; i < Math.min(values.length, this.reportColumns.length); i++) {
/* 56 */       DeviceSession deviceSession; switch (this.reportColumns[i]) {
/*    */         case 1:
/*    */         case 28:
/* 59 */           deviceSession = getDeviceSession(channel, remoteAddress, new String[] { values[i] });
/* 60 */           if (deviceSession != null) {
/* 61 */             position.setDeviceId(deviceSession.getDeviceId());
/*    */           }
/*    */           break;
/*    */         case 2:
/* 65 */           position.setTime((new SimpleDateFormat("MM/dd/yy HH:mm:ss")).parse(values[i]));
/*    */           break;
/*    */         case 3:
/* 68 */           position.setLatitude(Double.parseDouble(values[i]));
/*    */           break;
/*    */         case 4:
/* 71 */           position.setLongitude(Double.parseDouble(values[i]));
/*    */           break;
/*    */         case 11:
/* 74 */           position.set("ignition", Boolean.valueOf(values[i].equals("ON")));
/*    */           break;
/*    */         case 13:
/* 77 */           position.setSpeed(UnitsConverter.knotsFromKph(Integer.parseInt(values[i])));
/*    */           break;
/*    */         case 17:
/* 80 */           position.setCourse(Integer.parseInt(values[i]));
/*    */           break;
/*    */         case 23:
/* 83 */           position.set("odometer", Double.valueOf(Double.parseDouble(values[i]) * 1000.0D));
/*    */           break;
/*    */         case 27:
/* 86 */           position.setAltitude(UnitsConverter.metersFromFeet(Integer.parseInt(values[i])));
/*    */           break;
/*    */         case 46:
/* 89 */           position.set("sat", Integer.valueOf(Integer.parseInt(values[i])));
/*    */           break;
/*    */       } 
/*    */ 
/*    */ 
/*    */     
/*    */     } 
/* 96 */     return (position.getDeviceId() != 0L) ? position : null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GenxProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */