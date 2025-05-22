package org.traccar.api;

import java.security.Principal;


public class UserPrincipal
        implements Principal {
    private long userId;

    public UserPrincipal(long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return Long.valueOf(this.userId);
    }


    public String getName() {
        return null;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\UserPrincipal.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */