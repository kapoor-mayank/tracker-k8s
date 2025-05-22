package org.traccar.api.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.api.BaseResource;
import org.traccar.helper.DateUtil;
import org.traccar.model.Event;
import org.traccar.model.Position;
import org.traccar.reports.Events;
import org.traccar.reports.Route;
import org.traccar.reports.Stops;
import org.traccar.reports.Summary;
import org.traccar.reports.Trips;
import org.traccar.reports.model.StopReport;
import org.traccar.reports.model.SummaryReport;
import org.traccar.reports.model.TripReport;


@Path("reports")
@Produces({"application/json"})
@Consumes({"application/json"})
public class ReportResource
        extends BaseResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportResource.class);


    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";


    private static final String CONTENT_DISPOSITION_VALUE_XLSX = "attachment; filename=report.xlsx";


    private Response executeReport(long userId, boolean mail, ReportExecutor executor) throws SQLException, IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (mail) {
            (new Thread(() -> {
                try {
                    executor.execute(stream);


                    MimeBodyPart attachment = new MimeBodyPart();

                    attachment.setFileName("report.xlsx");

                    attachment.setDataHandler(new DataHandler((DataSource) new ByteArrayDataSource(stream.toByteArray(), "application/octet-stream")));

                    Context.getMailManager().sendMessage(userId, "Report", "The report is in the attachment.", attachment);
                } catch (SQLException | IOException | javax.mail.MessagingException e) {
                    LOGGER.warn("Report failed", e);
                }
            })).start();
            return Response.noContent().build();
        }
        executor.execute(stream);
        return Response.ok(stream.toByteArray())
                .header("Content-Disposition", "attachment; filename=report.xlsx").build();
    }


    @Path("route")
    @GET
    public Collection<Position> getRoute(@QueryParam("deviceId") List<Long> deviceIds, @QueryParam("groupId") List<Long> groupIds, @QueryParam("from") String from, @QueryParam("to") String to) throws SQLException {
        return Route.getObjects(getUserId(), deviceIds, groupIds,
                DateUtil.parseDate(from), DateUtil.parseDate(to));
    }


    @Path("route")
    @GET
    @Produces({"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
    public Response getRouteExcel(@QueryParam("deviceId") List<Long> deviceIds, @QueryParam("groupId") List<Long> groupIds, @QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("mail") boolean mail) throws SQLException, IOException {
        return executeReport(getUserId(), mail, stream -> Route.getExcel(stream, getUserId(), deviceIds, groupIds, DateUtil.parseDate(from), DateUtil.parseDate(to)));
    }


    @Path("events")
    @GET
    public Collection<Event> getEvents(@QueryParam("deviceId") List<Long> deviceIds, @QueryParam("groupId") List<Long> groupIds, @QueryParam("type") List<String> types, @QueryParam("from") String from, @QueryParam("to") String to) throws SQLException {
        return Events.getObjects(getUserId(), deviceIds, groupIds, types,
                DateUtil.parseDate(from), DateUtil.parseDate(to));
    }


    @Path("events")
    @GET
    @Produces({"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
    public Response getEventsExcel(@QueryParam("deviceId") List<Long> deviceIds, @QueryParam("groupId") List<Long> groupIds, @QueryParam("type") List<String> types, @QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("mail") boolean mail) throws SQLException, IOException {
        return executeReport(getUserId(), mail, stream -> Events.getExcel(stream, getUserId(), deviceIds, groupIds, types, DateUtil.parseDate(from), DateUtil.parseDate(to)));
    }


    @Path("summary")
    @GET
    public Collection<SummaryReport> getSummary(@QueryParam("deviceId") List<Long> deviceIds, @QueryParam("groupId") List<Long> groupIds, @QueryParam("from") String from, @QueryParam("to") String to) throws SQLException {
        return Summary.getObjects(getUserId(), deviceIds, groupIds,
                DateUtil.parseDate(from), DateUtil.parseDate(to));
    }


    @Path("summary")
    @GET
    @Produces({"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
    public Response getSummaryExcel(@QueryParam("deviceId") List<Long> deviceIds, @QueryParam("groupId") List<Long> groupIds, @QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("mail") boolean mail) throws SQLException, IOException {
        return executeReport(getUserId(), mail, stream -> Summary.getExcel(stream, getUserId(), deviceIds, groupIds, DateUtil.parseDate(from), DateUtil.parseDate(to)));
    }


    @Path("trips")
    @GET
    @Produces({"application/json"})
    public Collection<TripReport> getTrips(@QueryParam("deviceId") List<Long> deviceIds, @QueryParam("groupId") List<Long> groupIds, @QueryParam("from") String from, @QueryParam("to") String to) throws SQLException {
        return Trips.getObjects(getUserId(), deviceIds, groupIds,
                DateUtil.parseDate(from), DateUtil.parseDate(to));
    }


    @Path("trips")
    @GET
    @Produces({"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
    public Response getTripsExcel(@QueryParam("deviceId") List<Long> deviceIds, @QueryParam("groupId") List<Long> groupIds, @QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("mail") boolean mail) throws SQLException, IOException {
        return executeReport(getUserId(), mail, stream -> Trips.getExcel(stream, getUserId(), deviceIds, groupIds, DateUtil.parseDate(from), DateUtil.parseDate(to)));
    }


    @Path("stops")
    @GET
    @Produces({"application/json"})
    public Collection<StopReport> getStops(@QueryParam("deviceId") List<Long> deviceIds, @QueryParam("groupId") List<Long> groupIds, @QueryParam("from") String from, @QueryParam("to") String to) throws SQLException {
        return Stops.getObjects(getUserId(), deviceIds, groupIds,
                DateUtil.parseDate(from), DateUtil.parseDate(to));
    }


    @Path("stops")
    @GET
    @Produces({"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
    public Response getStopsExcel(@QueryParam("deviceId") List<Long> deviceIds, @QueryParam("groupId") List<Long> groupIds, @QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("mail") boolean mail) throws SQLException, IOException {
        return executeReport(getUserId(), mail, stream -> Stops.getExcel(stream, getUserId(), deviceIds, groupIds, DateUtil.parseDate(from), DateUtil.parseDate(to)));
    }

    private static interface ReportExecutor {
        void execute(ByteArrayOutputStream param1ByteArrayOutputStream) throws SQLException, IOException;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\resource\ReportResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */