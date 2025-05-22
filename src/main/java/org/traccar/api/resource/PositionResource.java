package org.traccar.api.resource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.traccar.Context;
import org.traccar.api.BaseResource;
import org.traccar.helper.DateUtil;
import org.traccar.model.Position;
import org.traccar.web.CsvBuilder;
import org.traccar.web.GpxBuilder;


@Path("positions")
@Produces({"application/json"})
@Consumes({"application/json"})
public class PositionResource
        extends BaseResource {
    public static final String TEXT_CSV = "text/csv";
    public static final String CONTENT_DISPOSITION_VALUE_CSV = "attachment; filename=positions.csv";
    public static final String GPX = "application/gpx+xml";
    public static final String CONTENT_DISPOSITION_VALUE_GPX = "attachment; filename=positions.gpx";

    @GET
    public Collection<Position> getJson(@QueryParam("deviceId") long deviceId, @QueryParam("id") List<Long> positionIds, @QueryParam("from") String from, @QueryParam("to") String to) throws SQLException {
        if (!positionIds.isEmpty()) {
            ArrayList<Position> positions = new ArrayList<>();
            for (Long positionId : positionIds) {
                Position position = (Position) Context.getDataManager().getObject(Position.class, positionId.longValue());
                Context.getPermissionsManager().checkDevice(getUserId(), position.getDeviceId());
                positions.add(position);
            }
            return positions;
        }
        if (deviceId == 0L) {
            return Context.getDeviceManager().getInitialState(getUserId());
        }
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        return Context.getDataManager().getPositions(deviceId,
                DateUtil.parseDate(from), DateUtil.parseDate(to));
    }


    @GET
    @Produces({"text/csv"})
    public Response getCsv(@QueryParam("deviceId") long deviceId, @QueryParam("from") String from, @QueryParam("to") String to) throws SQLException {
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        CsvBuilder csv = new CsvBuilder();
        csv.addHeaderLine(new Position());
        csv.addArray(Context.getDataManager().getPositions(deviceId,
                DateUtil.parseDate(from), DateUtil.parseDate(to)));
        return Response.ok(csv.build()).header("Content-Disposition", "attachment; filename=positions.csv").build();
    }


    @GET
    @Produces({"application/gpx+xml"})
    public Response getGpx(@QueryParam("deviceId") long deviceId, @QueryParam("from") String from, @QueryParam("to") String to) throws SQLException {
        Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
        GpxBuilder gpx = new GpxBuilder(Context.getIdentityManager().getById(deviceId).getName());
        gpx.addPositions(Context.getDataManager().getPositions(deviceId,
                DateUtil.parseDate(from), DateUtil.parseDate(to)));
        return Response.ok(gpx.build()).header("Content-Disposition", "attachment; filename=positions.gpx").build();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\resource\PositionResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */