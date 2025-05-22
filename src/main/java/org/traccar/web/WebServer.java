package org.traccar.web;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.proxy.AsyncProxyServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.api.AsyncSocketServlet;
import org.traccar.api.CorsResponseFilter;
import org.traccar.api.MediaFilter;
import org.traccar.api.ObjectMapperProvider;
import org.traccar.api.ResourceErrorHandler;
import org.traccar.api.SecurityRequestFilter;
import org.traccar.api.resource.ServerResource;
import org.traccar.config.Config;

public class WebServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);

    private Server server;

    private void initServer(Config config) {
        System.out.println("Port:" + config.getInteger("web.port", 8082));
        String address = config.getString("web.address");
        int port = config.getInteger("web.port", 8082);

        if (address == null) {
            this.server = new Server(port);
        } else {
            this.server = new Server(new InetSocketAddress(address, port));
        }
    }

    public WebServer(Config config) {
        initServer(config);

        ServletContextHandler servletHandler = new ServletContextHandler(1);

        int sessionTimeout = config.getInteger("web.sessionTimeout");
        if (sessionTimeout > 0) {
            servletHandler.getSessionHandler().setMaxInactiveInterval(sessionTimeout);
        }

        initApi(config, servletHandler);

        if (config.getBoolean("web.console")) {
            servletHandler.addServlet(new ServletHolder((Servlet) new ConsoleServlet()), "/console/*");
        }

        initWebApp(config, servletHandler);

        servletHandler.setErrorHandler(new ErrorHandler() {

            protected void handleErrorPage(HttpServletRequest request, Writer writer, int code, String message) throws IOException {
                writer.write("<!DOCTYPE<html><head><title>Error</title></head><html><body>" + code + " - " +
                        HttpStatus.getMessage(code) + "</body></html>");
            }

        });

        HandlerList handlers = new HandlerList();
        initClientProxy(config, handlers);
        handlers.addHandler((Handler) servletHandler);
        this.server.setHandler((Handler) handlers);
    }

    private void initClientProxy(Config config, HandlerList handlers) {
        int port = config.getInteger("osmand.port");
        if (port != 0) {
            ServletContextHandler servletHandler = new ServletContextHandler() {

                public void doScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                    if (target.equals("/") && request.getMethod().equals(HttpMethod.POST.asString())) {
                        super.doScope(target, baseRequest, request, response);
                    }
                }
            };
            ServletHolder servletHolder = new ServletHolder((Servlet) new AsyncProxyServlet.Transparent());
            servletHolder.setInitParameter("proxyTo", "http://localhost:" + port);
            servletHandler.addServlet(servletHolder, "/");
            handlers.addHandler((Handler) servletHandler);
        }
    }

    private void initWebApp(Config config, ServletContextHandler servletHandler) {
        ServletHolder servletHolder = new ServletHolder(DefaultServlet.class);
        servletHolder.setInitParameter("resourceBase", (new File(config.getString("web.path"))).getAbsolutePath());
        if (config.getBoolean("web.debug")) {
            servletHandler.setWelcomeFiles(new String[]{"debug.html", "index.html"});
        } else {
            String cache = config.getString("web.cacheControl");
            if (cache != null && !cache.isEmpty()) {
                servletHolder.setInitParameter("cacheControl", cache);
            }
            servletHandler.setWelcomeFiles(new String[]{"release.html", "index.html"});
        }
        servletHandler.addServlet(servletHolder, "/*");
    }

    private void initApi(Config config, ServletContextHandler servletHandler) {
        servletHandler.addServlet(new ServletHolder((Servlet) new AsyncSocketServlet()), "/api/socket");

        if (config.hasKey("media.path")) {
            ServletHolder servletHolder = new ServletHolder(DefaultServlet.class);
            servletHolder.setInitParameter("resourceBase", (new File(config.getString("media.path"))).getAbsolutePath());
            servletHolder.setInitParameter("dirAllowed", config.getString("media.dirAllowed", "false"));
            servletHolder.setInitParameter("pathInfoOnly", "true");
            servletHandler.addServlet(servletHolder, "/api/media/*");
            servletHandler.addFilter(MediaFilter.class, "/api/media/*", EnumSet.allOf(DispatcherType.class));
        }

        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.registerClasses(new Class[]{JacksonFeature.class, ObjectMapperProvider.class, ResourceErrorHandler.class});
        resourceConfig.registerClasses(new Class[]{SecurityRequestFilter.class, CorsResponseFilter.class});
        resourceConfig.packages(new String[]{ServerResource.class.getPackage().getName()});
        servletHandler.addServlet(new ServletHolder((Servlet) new ServletContainer(resourceConfig)), "/api/*");
    }

    public void start() {
        try {
//            LOGGER.info("WebServer start()");
            this.server.start();
        } catch (Exception error) {
            LOGGER.warn("Web server start failed", error);
        }
    }

    public void stop() {
        try {
            this.server.stop();
        } catch (Exception error) {
            LOGGER.warn("Web server stop failed", error);
        }
    }
}
