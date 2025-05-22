package org.traccar.api;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;


public class BaseResource {
    @Context
    private SecurityContext securityContext;

    protected long getUserId() {
        UserPrincipal principal = (UserPrincipal) this.securityContext.getUserPrincipal();
        if (principal != null) {
            return principal.getUserId().longValue();
        }
        return 0L;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\BaseResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */