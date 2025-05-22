package org.traccar.api;

import java.security.Principal;
import javax.ws.rs.core.SecurityContext;


public class UserSecurityContext
        implements SecurityContext {
    private UserPrincipal principal;

    public UserSecurityContext(UserPrincipal principal) {
        this.principal = principal;
    }


    public Principal getUserPrincipal() {
        return this.principal;
    }


    public boolean isUserInRole(String role) {
        return true;
    }


    public boolean isSecure() {
        return false;
    }


    public String getAuthenticationScheme() {
        return "BASIC";
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\UserSecurityContext.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */