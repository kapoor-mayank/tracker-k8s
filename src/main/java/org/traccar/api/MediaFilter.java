package org.traccar.api;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.traccar.Context;
import org.traccar.Main;
import org.traccar.database.StatisticsManager;
import org.traccar.helper.Log;
import org.traccar.model.Device;


public class MediaFilter
        implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {
            HttpSession session = ((HttpServletRequest) request).getSession(false);
            Long userId = null;
            if (session != null) {
                userId = (Long) session.getAttribute("userId");
                if (userId != null) {
                    Context.getPermissionsManager().checkUserEnabled(userId.longValue());
                    ((StatisticsManager) Main.getInjector().getInstance(StatisticsManager.class)).registerRequest(userId.longValue());
                }
            }
            if (userId == null) {
                httpResponse.sendError(401);

                return;
            }
            String path = ((HttpServletRequest) request).getPathInfo();
            String[] parts = path.split("/");
            if (parts.length < 2 || (parts.length == 2 && !path.endsWith("/"))) {
                Context.getPermissionsManager().checkAdmin(userId.longValue());
            } else {
                Device device = Context.getDeviceManager().getByUniqueId(parts[1]);
                if (device != null) {
                    Context.getPermissionsManager().checkDevice(userId.longValue(), device.getId());
                } else {
                    httpResponse.sendError(404);

                    return;
                }
            }
            chain.doFilter(request, response);
        } catch (SecurityException e) {
            httpResponse.setStatus(403);
            httpResponse.getWriter().println(Log.exceptionStack(e));
        } catch (SQLException e) {
            httpResponse.setStatus(400);
            httpResponse.getWriter().println(Log.exceptionStack(e));
        }
    }

    public void destroy() {
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\MediaFilter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */