package org.traccar.api;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.traccar.Context;


public class AsyncSocketServlet
        extends WebSocketServlet {
    private static final long ASYNC_TIMEOUT = 600000L;

    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(Context.getConfig().getLong("web.timeout", 600000L));
        factory.setCreator(new WebSocketCreator() {
            public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
                if (req.getSession() != null) {
                    long userId = ((Long) req.getSession().getAttribute("userId")).longValue();
                    return new AsyncSocket(userId);
                }
                return null;
            }
        });
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\AsyncSocketServlet.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */