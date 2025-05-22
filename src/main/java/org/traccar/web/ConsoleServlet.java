package org.traccar.web;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.h2.server.web.ConnectionInfo;
import org.h2.server.web.WebServer;
import org.h2.server.web.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;


public class ConsoleServlet
        extends WebServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleServlet.class);


    public void init() {
        super.init();

        try {
            Field field = WebServlet.class.getDeclaredField("server");
            field.setAccessible(true);
            WebServer server = (WebServer) field.get(this);


            ConnectionInfo connectionInfo = new ConnectionInfo("Traccar|" + Context.getConfig().getString("database.driver") + "|" + Context.getConfig().getString("database.url") + "|" + Context.getConfig().getString("database.user"));


            Method method = WebServer.class.getDeclaredMethod("updateSetting", new Class[]{ConnectionInfo.class});
            method.setAccessible(true);
            method.invoke(server, new Object[]{connectionInfo});

            method = WebServer.class.getDeclaredMethod("setAllowOthers", new Class[]{boolean.class});
            method.setAccessible(true);
            method.invoke(server, new Object[]{Boolean.valueOf(true)});
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException |
                 java.lang.reflect.InvocationTargetException e) {
            LOGGER.warn("Console reflection error", e);
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\web\ConsoleServlet.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */