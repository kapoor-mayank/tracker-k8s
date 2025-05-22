/*     */ package org.traccar;
/*     */ 
/*     */ import com.sun.jna.Pointer;
/*     */ import com.sun.jna.platform.win32.Advapi32;
/*     */ import com.sun.jna.platform.win32.Winsvc;
/*     */ import java.io.File;
/*     */ import java.net.URISyntaxException;
/*     */ import jnr.posix.POSIXFactory;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public abstract class WindowsService
/*     */ {
/*  37 */   private static final Advapi32 ADVAPI_32 = Advapi32.INSTANCE;
/*     */   
/*  39 */   private final Object waitObject = new Object();
/*     */   
/*     */   private String serviceName;
/*     */   private ServiceMain serviceMain;
/*     */   private ServiceControl serviceControl;
/*     */   private Winsvc.SERVICE_STATUS_HANDLE serviceStatusHandle;
/*     */   
/*     */   public WindowsService(String serviceName) {
/*  47 */     this.serviceName = serviceName;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean install(String displayName, String description, String[] dependencies, String account, String password, String config) throws URISyntaxException {
/*  54 */     String javaHome = System.getProperty("java.home");
/*  55 */     String javaBinary = javaHome + "\\bin\\java.exe";
/*     */     
/*  57 */     File jar = new File(WindowsService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
/*     */ 
/*     */     
/*  60 */     String command = javaBinary + " -Duser.dir=\"" + jar.getParentFile().getAbsolutePath() + "\" -jar \"" + jar.getAbsolutePath() + "\" --service \"" + config + "\"";
/*     */ 
/*     */     
/*  63 */     boolean success = false;
/*  64 */     StringBuilder dep = new StringBuilder();
/*     */     
/*  66 */     if (dependencies != null) {
/*  67 */       for (String s : dependencies) {
/*  68 */         dep.append(s);
/*  69 */         dep.append("\000");
/*     */       } 
/*     */     }
/*  72 */     dep.append("\000");
/*     */     
/*  74 */     Winsvc.SERVICE_DESCRIPTION desc = new Winsvc.SERVICE_DESCRIPTION();
/*  75 */     desc.lpDescription = description;
/*     */     
/*  77 */     Winsvc.SC_HANDLE serviceManager = openServiceControlManager(null, 983103);
/*     */     
/*  79 */     if (serviceManager != null) {
/*  80 */       Winsvc.SC_HANDLE service = ADVAPI_32.CreateService(serviceManager, this.serviceName, displayName, 983551, 16, 2, 1, command, null, null, dep
/*     */ 
/*     */ 
/*     */           
/*  84 */           .toString(), account, password);
/*     */       
/*  86 */       if (service != null) {
/*  87 */         success = ADVAPI_32.ChangeServiceConfig2(service, 1, (Winsvc.ChangeServiceConfig2Info)desc);
/*  88 */         ADVAPI_32.CloseServiceHandle(service);
/*     */       } 
/*  90 */       ADVAPI_32.CloseServiceHandle(serviceManager);
/*     */     } 
/*  92 */     return success;
/*     */   }
/*     */   
/*     */   public boolean uninstall() {
/*  96 */     boolean success = false;
/*     */     
/*  98 */     Winsvc.SC_HANDLE serviceManager = openServiceControlManager(null, 983103);
/*     */     
/* 100 */     if (serviceManager != null) {
/* 101 */       Winsvc.SC_HANDLE service = ADVAPI_32.OpenService(serviceManager, this.serviceName, 983551);
/*     */       
/* 103 */       if (service != null) {
/* 104 */         success = ADVAPI_32.DeleteService(service);
/* 105 */         ADVAPI_32.CloseServiceHandle(service);
/*     */       } 
/* 107 */       ADVAPI_32.CloseServiceHandle(serviceManager);
/*     */     } 
/* 109 */     return success;
/*     */   }
/*     */   
/*     */   public boolean start() {
/* 113 */     boolean success = false;
/*     */     
/* 115 */     Winsvc.SC_HANDLE serviceManager = openServiceControlManager(null, 536870912);
/*     */     
/* 117 */     if (serviceManager != null) {
/* 118 */       Winsvc.SC_HANDLE service = ADVAPI_32.OpenService(serviceManager, this.serviceName, 536870912);
/*     */       
/* 120 */       if (service != null) {
/* 121 */         success = ADVAPI_32.StartService(service, 0, null);
/* 122 */         ADVAPI_32.CloseServiceHandle(service);
/*     */       } 
/* 124 */       ADVAPI_32.CloseServiceHandle(serviceManager);
/*     */     } 
/*     */     
/* 127 */     return success;
/*     */   }
/*     */   
/*     */   public boolean stop() {
/* 131 */     boolean success = false;
/*     */     
/* 133 */     Winsvc.SC_HANDLE serviceManager = openServiceControlManager(null, 536870912);
/*     */     
/* 135 */     if (serviceManager != null) {
/* 136 */       Winsvc.SC_HANDLE service = Advapi32.INSTANCE.OpenService(serviceManager, this.serviceName, 536870912);
/*     */       
/* 138 */       if (service != null) {
/* 139 */         Winsvc.SERVICE_STATUS serviceStatus = new Winsvc.SERVICE_STATUS();
/* 140 */         success = Advapi32.INSTANCE.ControlService(service, 1, serviceStatus);
/* 141 */         Advapi32.INSTANCE.CloseServiceHandle(service);
/*     */       } 
/* 143 */       Advapi32.INSTANCE.CloseServiceHandle(serviceManager);
/*     */     } 
/*     */     
/* 146 */     return success;
/*     */   }
/*     */ 
/*     */   
/*     */   public void init() throws URISyntaxException {
/* 151 */     String path = (new File(WindowsService.class.getProtectionDomain().getCodeSource().getLocation().toURI())).getParent();
/*     */     
/* 153 */     POSIXFactory.getPOSIX().chdir(path);
/*     */     
/* 155 */     this.serviceMain = new ServiceMain();
/* 156 */     Winsvc.SERVICE_TABLE_ENTRY entry = new Winsvc.SERVICE_TABLE_ENTRY();
/* 157 */     entry.lpServiceName = this.serviceName;
/* 158 */     entry.lpServiceProc = this.serviceMain;
/*     */     
/* 160 */     Advapi32.INSTANCE.StartServiceCtrlDispatcher((Winsvc.SERVICE_TABLE_ENTRY[])entry.toArray(2));
/*     */   }
/*     */   
/*     */   private Winsvc.SC_HANDLE openServiceControlManager(String machine, int access) {
/* 164 */     return ADVAPI_32.OpenSCManager(machine, null, access);
/*     */   }
/*     */   
/*     */   private void reportStatus(int status, int win32ExitCode, int waitHint) {
/* 168 */     Winsvc.SERVICE_STATUS serviceStatus = new Winsvc.SERVICE_STATUS();
/* 169 */     serviceStatus.dwServiceType = 16;
/* 170 */     serviceStatus.dwControlsAccepted = 5;
/* 171 */     serviceStatus.dwWin32ExitCode = win32ExitCode;
/* 172 */     serviceStatus.dwWaitHint = waitHint;
/* 173 */     serviceStatus.dwCurrentState = status;
/*     */     
/* 175 */     ADVAPI_32.SetServiceStatus(this.serviceStatusHandle, serviceStatus);
/*     */   }
/*     */   
/*     */   public abstract void run();
/*     */   
/*     */   private class ServiceMain
/*     */     implements Winsvc.SERVICE_MAIN_FUNCTION {
/*     */     public void callback(int dwArgc, Pointer lpszArgv) {
/* 183 */       WindowsService.this.serviceControl = new WindowsService.ServiceControl();
/* 184 */       WindowsService.this.serviceStatusHandle = WindowsService.ADVAPI_32.RegisterServiceCtrlHandlerEx(WindowsService.this.serviceName, WindowsService.this.serviceControl, null);
/*     */       
/* 186 */       WindowsService.this.reportStatus(2, 0, 3000);
/* 187 */       WindowsService.this.reportStatus(4, 0, 0);
/*     */       
/* 189 */       Thread.currentThread().setContextClassLoader(WindowsService.class.getClassLoader());
/*     */       
/* 191 */       WindowsService.this.run();
/*     */       
/*     */       try {
/* 194 */         synchronized (WindowsService.this.waitObject) {
/* 195 */           WindowsService.this.waitObject.wait();
/*     */         } 
/* 197 */       } catch (InterruptedException interruptedException) {}
/*     */ 
/*     */       
/* 200 */       WindowsService.this.reportStatus(1, 0, 0);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 207 */       System.exit(0);
/*     */     }
/*     */     
/*     */     private ServiceMain() {} }
/*     */   
/*     */   private class ServiceControl implements Winsvc.HandlerEx { private ServiceControl() {}
/*     */     
/*     */     public int callback(int dwControl, int dwEventType, Pointer lpEventData, Pointer lpContext) {
/* 215 */       switch (dwControl) {
/*     */         case 1:
/*     */         case 5:
/* 218 */           WindowsService.this.reportStatus(3, 0, 5000);
/* 219 */           synchronized (WindowsService.this.waitObject) {
/* 220 */             WindowsService.this.waitObject.notifyAll();
/*     */           } 
/*     */           break;
/*     */       } 
/*     */ 
/*     */       
/* 226 */       return 0;
/*     */     } }
/*     */ 
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\WindowsService.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */