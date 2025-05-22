/*    */ package org.traccar.notification;
/*    */ 
/*    */ import org.traccar.config.Config;
/*    */ import org.traccar.model.ExtendedModel;
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
/*    */ 
/*    */ public class PropertiesProvider
/*    */ {
/*    */   private Config config;
/*    */   private ExtendedModel extendedModel;
/*    */   
/*    */   public PropertiesProvider(Config config) {
/* 28 */     this.config = config;
/*    */   }
/*    */   
/*    */   public PropertiesProvider(ExtendedModel extendedModel) {
/* 32 */     this.extendedModel = extendedModel;
/*    */   }
/*    */   
/*    */   public String getString(String key) {
/* 36 */     if (this.config != null) {
/* 37 */       return this.config.getString(key);
/*    */     }
/* 39 */     return this.extendedModel.getString(key);
/*    */   }
/*    */ 
/*    */   
/*    */   public String getString(String key, String defaultValue) {
/* 44 */     String value = getString(key);
/* 45 */     if (value == null) {
/* 46 */       value = defaultValue;
/*    */     }
/* 48 */     return value;
/*    */   }
/*    */   
/*    */   public int getInteger(String key, int defaultValue) {
/* 52 */     if (this.config != null) {
/* 53 */       return this.config.getInteger(key, defaultValue);
/*    */     }
/* 55 */     Object result = this.extendedModel.getAttributes().get(key);
/* 56 */     if (result != null) {
/* 57 */       return (result instanceof String) ? Integer.parseInt((String)result) : ((Integer)result).intValue();
/*    */     }
/* 59 */     return defaultValue;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public Boolean getBoolean(String key) {
/* 65 */     if (this.config != null) {
/* 66 */       if (this.config.hasKey(key)) {
/* 67 */         return Boolean.valueOf(this.config.getBoolean(key));
/*    */       }
/* 69 */       return null;
/*    */     } 
/*    */     
/* 72 */     Object result = this.extendedModel.getAttributes().get(key);
/* 73 */     if (result != null) {
/* 74 */       return (result instanceof String) ? Boolean.valueOf((String)result) : (Boolean)result;
/*    */     }
/* 76 */     return null;
/*    */   }
/*    */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\notification\PropertiesProvider.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */