/*     */ package org.traccar;
/*     */ 
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.handler.codec.string.StringEncoder;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collection;
/*     */ import java.util.HashSet;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ import org.traccar.database.ActiveDevice;
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
/*     */ public abstract class BaseProtocol
/*     */   implements Protocol
/*     */ {
/*     */   private final String name;
/*  34 */   private final Set<String> supportedDataCommands = new HashSet<>();
/*  35 */   private final Set<String> supportedTextCommands = new HashSet<>();
/*  36 */   private final List<TrackerConnector> connectorList = new LinkedList<>();
/*     */   
/*  38 */   private StringProtocolEncoder textCommandEncoder = null;
/*     */   
/*     */   public static String nameFromClass(Class<?> clazz) {
/*  41 */     String className = clazz.getSimpleName();
/*  42 */     return className.substring(0, className.length() - 8).toLowerCase();
/*     */   }
/*     */   
/*     */   public BaseProtocol() {
/*  46 */     this.name = nameFromClass(getClass());
/*     */   }
/*     */ 
/*     */   
/*     */   public String getName() {
/*  51 */     return this.name;
/*     */   }
/*     */   
/*     */   protected void addServer(TrackerServer server) {
/*  55 */     this.connectorList.add(server);
/*     */   }
/*     */   
/*     */   protected void addClient(TrackerClient client) {
/*  59 */     this.connectorList.add(client);
/*     */   }
/*     */ 
/*     */   
/*     */   public Collection<TrackerConnector> getConnectorList() {
/*  64 */     return this.connectorList;
/*     */   }
/*     */   
/*     */   public void setSupportedDataCommands(String... commands) {
/*  68 */     this.supportedDataCommands.addAll(Arrays.asList(commands));
/*     */   }
/*     */   
/*     */   public void setSupportedTextCommands(String... commands) {
/*  72 */     this.supportedTextCommands.addAll(Arrays.asList(commands));
/*     */   }
/*     */   
/*     */   public void setSupportedCommands(String... commands) {
/*  76 */     this.supportedDataCommands.addAll(Arrays.asList(commands));
/*  77 */     this.supportedTextCommands.addAll(Arrays.asList(commands));
/*     */   }
/*     */ 
/*     */   
/*     */   public Collection<String> getSupportedDataCommands() {
/*  82 */     Set<String> commands = new HashSet<>(this.supportedDataCommands);
/*  83 */     commands.add("custom");
/*  84 */     return commands;
/*     */   }
/*     */ 
/*     */   
/*     */   public Collection<String> getSupportedTextCommands() {
/*  89 */     Set<String> commands = new HashSet<>(this.supportedTextCommands);
/*  90 */     commands.add("custom");
/*  91 */     return commands;
/*     */   }
/*     */ 
/*     */   
/*     */   public void sendDataCommand(ActiveDevice activeDevice, Command command) {
/*  96 */     boolean ignoreCustomCommand = Context.getConfig().getBoolean("server.ignoreCustomCommand");
/*  97 */     if (this.supportedDataCommands.contains(command.getType()) && (
/*  98 */       !command.getType().equals("custom") || !ignoreCustomCommand)) {
/*  99 */       activeDevice.write(command);
/* 100 */     } else if (command.getType().equals("custom")) {
/* 101 */       String data = command.getString("data");
/* 102 */       if (BasePipelineFactory.getHandler(activeDevice.getChannel().pipeline(), StringEncoder.class) != null) {
/* 103 */         activeDevice.write(data);
/*     */       } else {
/* 105 */         activeDevice.write(Unpooled.wrappedBuffer(DataConverter.parseHex(data)));
/*     */       } 
/*     */     } else {
/* 108 */       throw new RuntimeException("Command " + command.getType() + " is not supported in protocol " + getName());
/*     */     } 
/*     */   }
/*     */   
/*     */   public void setTextCommandEncoder(StringProtocolEncoder textCommandEncoder) {
/* 113 */     this.textCommandEncoder = textCommandEncoder;
/*     */   }
/*     */ 
/*     */   
/*     */   public void sendTextCommand(String destAddress, Command command) throws Exception {
/* 118 */     if (Context.getSmsManager() != null) {
/* 119 */       if (command.getType().equals("custom")) {
/* 120 */         Context.getSmsManager().sendMessageSync(destAddress, command.getString("data"), true);
/* 121 */       } else if (this.supportedTextCommands.contains(command.getType()) && this.textCommandEncoder != null) {
/* 122 */         String encodedCommand = (String)this.textCommandEncoder.encodeCommand(command);
/* 123 */         if (encodedCommand != null) {
/* 124 */           Context.getSmsManager().sendMessageSync(destAddress, encodedCommand, true);
/*     */         } else {
/* 126 */           throw new RuntimeException("Failed to encode command");
/*     */         } 
/*     */       } else {
/* 129 */         throw new RuntimeException("Command " + command
/* 130 */             .getType() + " is not supported in protocol " + getName());
/*     */       } 
/*     */     } else {
/* 133 */       throw new RuntimeException("SMS is not enabled");
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\BaseProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */