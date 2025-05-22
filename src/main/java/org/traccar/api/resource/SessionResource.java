package org.traccar.api.resource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import javax.annotation.security.PermitAll;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
//import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.traccar.Context;
import org.traccar.api.BaseResource;
import org.traccar.helper.DataConverter;
import org.traccar.helper.LogAction;
import org.traccar.model.User;


@Path("session")
@Produces({"application/json"})
@Consumes({"application/x-www-form-urlencoded"})
public class SessionResource
        extends BaseResource {
    public static final String USER_ID_KEY = "userId";
    public static final String USER_COOKIE_KEY = "user";
    public static final String PASS_COOKIE_KEY = "password";
    @javax.ws.rs.core.Context
    private HttpServletRequest request;

    @PermitAll
    @GET
    public User get(@QueryParam("token") String token) throws SQLException, UnsupportedEncodingException {
        Long userId = (Long) this.request.getSession().getAttribute("userId");
        if (userId == null) {
            Cookie[] cookies = this.request.getCookies();
            String email = null, password = null;
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("user")) {
                        byte[] emailBytes = DataConverter.parseBase64(
                                URLDecoder.decode(cookie.getValue(), StandardCharsets.US_ASCII.name()));
                        email = new String(emailBytes, StandardCharsets.UTF_8);
                    } else if (cookie.getName().equals("password")) {
                        byte[] passwordBytes = DataConverter.parseBase64(
                                URLDecoder.decode(cookie.getValue(), StandardCharsets.US_ASCII.name()));
                        password = new String(passwordBytes, StandardCharsets.UTF_8);
                    }
                }
            }
            if (email != null && password != null) {
                User user = Context.getPermissionsManager().login(email, password);
                if (user != null) {
                    userId = Long.valueOf(user.getId());
                    this.request.getSession().setAttribute("userId", userId);
                }
            } else if (token != null) {
                User user = Context.getUsersManager().getUserByToken(token);
                if (user != null) {
                    userId = Long.valueOf(user.getId());
                    this.request.getSession().setAttribute("userId", userId);
                }
            }
        }

        if (userId != null) {
            Context.getPermissionsManager().checkUserEnabled(userId.longValue());
            return Context.getPermissionsManager().getUser(userId.longValue());
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
    }


    @PermitAll
    @POST
    public User add(@FormParam("email") String email, @FormParam("password") String password) throws SQLException {
        User user = Context.getPermissionsManager().login(email, password);
        if (user != null) {
            this.request.getSession().setAttribute("userId", Long.valueOf(user.getId()));
            LogAction.login(user.getId());
            return user;
        }
        throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
    }


    @DELETE
    public Response remove() {
        LogAction.logout(getUserId());
        this.request.getSession().removeAttribute("userId");
        return Response.noContent().build();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\resource\SessionResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */