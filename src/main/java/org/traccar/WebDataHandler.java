/*     */ package org.traccar;
/*     */ 
/*     */ import com.fasterxml.jackson.core.JsonProcessingException;
/*     */ import com.fasterxml.jackson.databind.ObjectMapper;
/*     */ import io.netty.channel.ChannelHandler.Sharable;
/*     */ import java.io.IOException;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.net.URLEncoder;
/*     */ import java.nio.charset.StandardCharsets;
/*     */ import java.util.Calendar;
/*     */ import java.util.Formatter;
/*     */ import java.util.HashMap;
/*     */ import java.util.Locale;
/*     */ import java.util.Map;
/*     */ import java.util.TimeZone;
/*     */ import javax.inject.Inject;
/*     */ import javax.ws.rs.client.Client;
/*     */ import javax.ws.rs.client.Entity;
/*     */ import javax.ws.rs.client.Invocation;
/*     */ import org.traccar.config.Config;
/*     */ import org.traccar.config.Keys;
/*     */ import org.traccar.database.IdentityManager;
/*     */ import org.traccar.helper.Checksum;
/*     */ import org.traccar.model.Device;
/*     */ import org.traccar.model.Group;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ @Sharable
/*     */ public class WebDataHandler
/*     */   extends BaseDataHandler
/*     */ {
/*     */   private static final String KEY_POSITION = "position";
/*     */   private static final String KEY_DEVICE = "device";
/*     */   private final IdentityManager identityManager;
/*     */   private final ObjectMapper objectMapper;
/*     */   private final Client client;
/*     */   private final String url;
/*     */   private final String header;
/*     */   private final boolean json;
/*     */   
/*     */   @Inject
/*     */   public WebDataHandler(Config config, IdentityManager identityManager, ObjectMapper objectMapper, Client client) {
/*  63 */     this.identityManager = identityManager;
/*  64 */     this.objectMapper = objectMapper;
/*  65 */     this.client = client;
/*  66 */     this.url = config.getString(Keys.FORWARD_URL);
/*  67 */     this.header = config.getString(Keys.FORWARD_HEADER);
/*  68 */     this.json = config.getBoolean(Keys.FORWARD_JSON);
/*     */   }
/*     */ 
/*     */   
/*     */   private static String formatSentence(Position position) {
/*  73 */     StringBuilder s = new StringBuilder("$GPRMC,");
/*     */     
/*  75 */     try (Formatter f = new Formatter(s, Locale.ENGLISH)) {
/*     */       
/*  77 */       Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
/*  78 */       calendar.setTimeInMillis(position.getFixTime().getTime());
/*     */       
/*  80 */       f.format("%1$tH%1$tM%1$tS.%1$tL,A,", new Object[] { calendar });
/*     */       
/*  82 */       double lat = position.getLatitude();
/*  83 */       double lon = position.getLongitude();
/*     */       
/*  85 */       f.format("%02d%07.4f,%c,", new Object[] { Integer.valueOf((int)Math.abs(lat)), Double.valueOf(Math.abs(lat) % 1.0D * 60.0D), Character.valueOf((char) ((lat < 0.0D) ? 83 : 78)) });
/*  86 */       f.format("%03d%07.4f,%c,", new Object[] { Integer.valueOf((int)Math.abs(lon)), Double.valueOf(Math.abs(lon) % 1.0D * 60.0D), Character.valueOf((char) ((lon < 0.0D) ? 87 : 69)) });
/*     */       
/*  88 */       f.format("%.2f,%.2f,", new Object[] { Double.valueOf(position.getSpeed()), Double.valueOf(position.getCourse()) });
/*  89 */       f.format("%1$td%1$tm%1$ty,,", new Object[] { calendar });
/*     */     } 
/*     */     
/*  92 */     s.append(Checksum.nmea(s.toString()));
/*     */     
/*  94 */     return s.toString();
/*     */   }
/*     */   
/*     */   private String calculateStatus(Position position) {
/*  98 */     if (position.getAttributes().containsKey("alarm"))
/*  99 */       return "0xF841"; 
/* 100 */     if (position.getSpeed() < 1.0D) {
/* 101 */       return "0xF020";
/*     */     }
/* 103 */     return "0xF11C";
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public String formatRequest(Position position) throws UnsupportedEncodingException, JsonProcessingException {
/* 109 */     Device device = this.identityManager.getById(position.getDeviceId());
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
/*     */     
/* 127 */     String request = this.url.replace("{name}", URLEncoder.encode(device.getName(), StandardCharsets.UTF_8.name())).replace("{uniqueId}", device.getUniqueId()).replace("{status}", device.getStatus()).replace("{deviceId}", String.valueOf(position.getDeviceId())).replace("{protocol}", String.valueOf(position.getProtocol())).replace("{deviceTime}", String.valueOf(position.getDeviceTime().getTime())).replace("{fixTime}", String.valueOf(position.getFixTime().getTime())).replace("{valid}", String.valueOf(position.getValid())).replace("{latitude}", String.valueOf(position.getLatitude())).replace("{longitude}", String.valueOf(position.getLongitude())).replace("{altitude}", String.valueOf(position.getAltitude())).replace("{speed}", String.valueOf(position.getSpeed())).replace("{course}", String.valueOf(position.getCourse())).replace("{accuracy}", String.valueOf(position.getAccuracy())).replace("{statusCode}", calculateStatus(position)).replace("{packetType}", (position.getPacketType() != null) ? String.valueOf(position.getPacketType()) : "");
/*     */     
/* 129 */     if (position.getAddress() != null) {
/* 130 */       request = request.replace("{address}", 
/* 131 */           URLEncoder.encode(position.getAddress(), StandardCharsets.UTF_8.name()));
/*     */     }
/*     */     
/* 134 */     if (request.contains("{attributes}")) {
/* 135 */       String attributes = this.objectMapper.writeValueAsString(position.getAttributes());
/* 136 */       request = request.replace("{attributes}", 
/* 137 */           URLEncoder.encode(attributes, StandardCharsets.UTF_8.name()));
/*     */     } 
/*     */     
/* 140 */     if (request.contains("{gprmc}")) {
/* 141 */       request = request.replace("{gprmc}", formatSentence(position));
/*     */     }
/*     */     
/* 144 */     if (request.contains("{group}")) {
/* 145 */       String deviceGroupName = "";
/* 146 */       if (device.getGroupId() != 0L) {
/* 147 */         Group group = (Group)Context.getGroupsManager().getById(device.getGroupId());
/* 148 */         if (group != null) {
/* 149 */           deviceGroupName = group.getName();
/*     */         }
/*     */       } 
/*     */       
/* 153 */       request = request.replace("{group}", URLEncoder.encode(deviceGroupName, StandardCharsets.UTF_8.name()));
/*     */     } 
/*     */     
/* 156 */     return request;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   protected Position handlePosition(Position position) {
/*     */     String url;
/* 163 */     if (this.json) {
/* 164 */       url = this.url;
/*     */     } else {
/*     */       try {
/* 167 */         url = formatRequest(position);
/* 168 */       } catch (UnsupportedEncodingException|JsonProcessingException e) {
/* 169 */         throw new RuntimeException("Forwarding formatting error", e);
/*     */       } 
/*     */     } 
/*     */     
/* 173 */     Invocation.Builder requestBuilder = this.client.target(url).request();
/*     */     
/* 175 */     if (this.header != null && !this.header.isEmpty()) {
/* 176 */       for (String line : this.header.split("\\r?\\n")) {
/* 177 */         String[] values = line.split(":", 2);
/* 178 */         requestBuilder.header(values[0].trim(), values[1].trim());
/*     */       } 
/*     */     }
/*     */     
/* 182 */     if (this.json) {
/* 183 */       requestBuilder.async().post(Entity.json(prepareJsonPayload(position)));
/*     */     } else {
/* 185 */       requestBuilder.async().get();
/*     */     } 
/*     */     
/* 188 */     return position;
/*     */   }
/*     */ 
/*     */   
/*     */   private Map<String, Object> prepareJsonPayload(Position position) {
/* 193 */     Map<String, Object> data = new HashMap<>();
/* 194 */     Device device = this.identityManager.getById(position.getDeviceId());
/*     */     
/* 196 */     data.put("position", position);
/*     */     
/* 198 */     if (device != null) {
/* 199 */       data.put("device", device);
/*     */     }
/*     */     
/* 202 */     return data;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\WebDataHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */