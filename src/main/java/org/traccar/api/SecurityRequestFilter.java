package org.traccar.api;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
//import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.Main;
import org.traccar.database.StatisticsManager;
import org.traccar.helper.DataConverter;
import org.traccar.model.User;


public class SecurityRequestFilter
        implements ContainerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityRequestFilter.class);

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    public static final String BASIC_REALM = "Basic realm=\"api\"";

    public static String[] decodeBasicAuth(String auth) {
        auth = auth.replaceFirst("[B|b]asic ", "");
        byte[] decodedBytes = DataConverter.parseBase64(auth);
        if (decodedBytes != null && decodedBytes.length > 0) {
            return (new String(decodedBytes, StandardCharsets.US_ASCII)).split(":", 2);
        }
        return null;
    }


    public static final String X_REQUESTED_WITH = "X-Requested-With";
    public static final String XML_HTTP_REQUEST = "XMLHttpRequest";
    @javax.ws.rs.core.Context
    private HttpServletRequest request;
    @javax.ws.rs.core.Context
    private ResourceInfo resourceInfo;

    public void filter(ContainerRequestContext requestContext) {
        if (requestContext.getMethod().equals("OPTIONS")) {
            return;
        }

        SecurityContext securityContext = null;


        try {
            String authHeader = requestContext.getHeaderString("Authorization");
            if (authHeader != null) {

                try {
                    String[] auth = decodeBasicAuth(authHeader);
                    User user = Context.getPermissionsManager().login(auth[0], auth[1]);
                    if (user != null) {
                        ((StatisticsManager) Main.getInjector().getInstance(StatisticsManager.class)).registerRequest(user.getId());
                        securityContext = new UserSecurityContext(new UserPrincipal(user.getId()));
                    }
                } catch (SQLException e) {
                    throw new WebApplicationException(e);
                }

            } else if (this.request.getSession() != null) {

                Long userId = (Long) this.request.getSession().getAttribute("userId");
                if (userId != null) {
                    Context.getPermissionsManager().checkUserEnabled(userId.longValue());
                    ((StatisticsManager) Main.getInjector().getInstance(StatisticsManager.class)).registerRequest(userId.longValue());
                    securityContext = new UserSecurityContext(new UserPrincipal(userId.longValue()));
                }

            }

        } catch (SecurityException e) {
            LOGGER.warn("Authentication error", e);
        }

        if (securityContext != null) {
            requestContext.setSecurityContext(securityContext);
        } else {
            Method method = this.resourceInfo.getResourceMethod();
            if (!method.isAnnotationPresent((Class) PermitAll.class)) {
                Response.ResponseBuilder responseBuilder = Response.status(Response.Status.UNAUTHORIZED);
                if (!"XMLHttpRequest".equals(this.request.getHeader("X-Requested-With"))) {
                    responseBuilder.header("WWW-Authenticate", "Basic realm=\"api\"");
                }
                throw new WebApplicationException(responseBuilder.build());
            }
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\SecurityRequestFilter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */