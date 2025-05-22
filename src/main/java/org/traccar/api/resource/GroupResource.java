package org.traccar.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.traccar.api.SimpleObjectResource;
import org.traccar.model.Group;


@Path("groups")
@Produces({"application/json"})
@Consumes({"application/json"})
public class GroupResource
        extends SimpleObjectResource<Group> {
    public GroupResource() {
        super(Group.class);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\resource\GroupResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */