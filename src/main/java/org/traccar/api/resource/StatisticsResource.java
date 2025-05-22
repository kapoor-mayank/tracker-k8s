package org.traccar.api.resource;

import java.sql.SQLException;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.traccar.Context;
import org.traccar.api.BaseResource;
import org.traccar.helper.DateUtil;
import org.traccar.model.Statistics;


@Path("statistics")
@Produces({"application/json"})
@Consumes({"application/json"})
public class StatisticsResource
        extends BaseResource {
    @GET
    public Collection<Statistics> get(@QueryParam("from") String from, @QueryParam("to") String to) throws SQLException {
        Context.getPermissionsManager().checkAdmin(getUserId());
        return Context.getDataManager().getStatistics(DateUtil.parseDate(from), DateUtil.parseDate(to));
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\resource\StatisticsResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */