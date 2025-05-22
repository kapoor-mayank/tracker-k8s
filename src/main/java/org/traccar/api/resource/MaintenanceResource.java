package org.traccar.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.traccar.api.ExtendedObjectResource;
import org.traccar.model.Maintenance;


@Path("maintenance")
@Produces({"application/json"})
@Consumes({"application/json"})
public class MaintenanceResource
        extends ExtendedObjectResource<Maintenance> {
    public MaintenanceResource() {
        super(Maintenance.class);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\resource\MaintenanceResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */