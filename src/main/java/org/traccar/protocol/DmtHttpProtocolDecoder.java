package org.traccar.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.StringReader;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.*;
import org.traccar.helper.BitUtil;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Position;


public class DmtHttpProtocolDecoder
        extends BaseHttpProtocolDecoder {

    private final ExecutorService executorService = Executors.newFixedThreadPool(5); // Thread pool for async tasks
    private static final HexDataStorageHandler hexDataStorage = new HexDataStorageHandler(); // Initialize storage helper

    private static final Logger LOGGER = LoggerFactory.getLogger(DmtHttpProtocolDecoder.class);
    public DmtHttpProtocolDecoder(Protocol protocol) {
        super(protocol);
    }


    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
        Position result;
        FullHttpRequest request = (FullHttpRequest) msg;

        JsonObject root = Json.createReader(new StringReader(request.content().toString(StandardCharsets.US_ASCII))).readObject();
        LOGGER.info("Received data: " + root.toString());

        if (root.containsKey("device")) {
            result = decodeEdge(channel, remoteAddress, root);
        } else {
            result = (Position)decodeTraditional(channel, remoteAddress, root);
        }

        Integer sequenceNo = (Integer) result.getAttributes().get("index");
        String uniqueId = Context.getIdentityManager().getById(result.getDeviceId()).getUniqueId();
        String decodedData = result.toString();
        // Asynchronously store the data
        Long finalIndex = Long.valueOf(sequenceNo);
        String finalUniqueId = uniqueId;
        executorService.submit(() -> {
            try {
                hexDataStorage.storeHexAndDecodedData(root.toString(), decodedData, finalIndex, finalUniqueId);
            } catch (Exception e) {
                // Log any errors during the database operation
                LOGGER.error("Error storing data", e);
            }
        });
        sendResponse(channel, (result != null) ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST);
        return result;
    }


    private Collection<Position> decodeTraditional(Channel channel, SocketAddress remoteAddress, JsonObject root) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[]{root.getString("IMEI")});
        if (deviceSession == null) {
            return null;
        }

        List<Position> positions = new LinkedList<>();

        JsonArray records = root.getJsonArray("Records");

        for (int i = 0; i < records.size(); i++) {
            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            JsonObject record = records.getJsonObject(i);

            position.set("index", Integer.valueOf(record.getInt("SeqNo")));
            position.set("event", Integer.valueOf(record.getInt("Reason")));

            position.setDeviceTime(dateFormat.parse(record.getString("DateUTC")));

            JsonArray fields = record.getJsonArray("Fields");

            for (int j = 0; j < fields.size(); j++) {
                int input, output;
                JsonObject adc, field = fields.getJsonObject(j);
                switch (field.getInt("FType")) {
                    case 0:
                        position.setFixTime(dateFormat.parse(field.getString("GpsUTC")));
                        position.setLatitude(field.getJsonNumber("Lat").doubleValue());
                        position.setLongitude(field.getJsonNumber("Long").doubleValue());
                        position.setAltitude(field.getInt("Alt"));
                        position.setSpeed(UnitsConverter.knotsFromCps(field.getInt("Spd")));
                        position.setCourse(field.getInt("Head"));
                        position.setAccuracy(field.getInt("PosAcc"));
                        position.setValid((field.getInt("GpsStat") > 0));
                        break;
                    case 2:
                        input = field.getInt("DIn");
                        output = field.getInt("DOut");

                        position.set("ignition", Boolean.valueOf(BitUtil.check(input, 0)));

                        position.set("input", Integer.valueOf(input));
                        position.set("output", Integer.valueOf(output));
                        position.set("status", Integer.valueOf(field.getInt("DevStat")));
                        break;
                    case 6:
                        adc = field.getJsonObject("AnalogueData");
                        if (adc.containsKey("1")) {
                            position.set("battery", Double.valueOf(adc.getInt("1") * 0.001D));
                        }
                        if (adc.containsKey("2")) {
                            position.set("power", Double.valueOf(adc.getInt("2") * 0.01D));
                        }
                        if (adc.containsKey("3")) {
                            position.set("deviceTemp", Double.valueOf(adc.getInt("3") * 0.01D));
                        }
                        if (adc.containsKey("4")) {
                            position.set("rssi", Integer.valueOf(adc.getInt("4")));
                        }
                        if (adc.containsKey("5")) {
                            position.set("solarPower", Double.valueOf(adc.getInt("5") * 0.001D));
                        }
                        break;
                }


            }
            positions.add(position);
        }

        return positions;
    }


    private Position decodeEdge(Channel channel, SocketAddress remoteAddress, JsonObject root) {
        JsonObject device = root.getJsonObject("device");

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[]{device.getString("imei")});
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position(getProtocolName());
//        String imei = device.getString("imei"); // Extract IMEI directly
//        position.setDeviceId(Long.parseLong(imei)); // Convert IMEI to long and set deviceId
        position.setDeviceId(deviceSession.getDeviceId());
        LOGGER.info("IMEI from DeviceSession: " + deviceSession.getDeviceId());
        LOGGER.info("IMEI from JsonObject: " + device.getString("imei"));

        Date time = new Date(OffsetDateTime.parse(root.getString("date")).toInstant().toEpochMilli());

        if (root.containsKey("lat") && root.containsKey("lng")) {
            position.setValid(true);
            position.setTime(time);
            position.setLatitude(root.getJsonNumber("lat").doubleValue());
            position.setLongitude(root.getJsonNumber("lng").doubleValue());
            position.setAccuracy(root.getJsonNumber("posAcc").doubleValue());
        } else {
            getLastLocation(position, time);
        }

        position.set("index", Integer.valueOf(root.getInt("sqn")));
        position.set("event", Integer.valueOf(root.getInt("reason")));

        if (root.containsKey("analogues")) {
            JsonArray analogues = root.getJsonArray("analogues");
            for (int i = 0; i < analogues.size(); i++) {
                JsonObject adc = analogues.getJsonObject(i);
                position.set("adc" + adc.getInt("id"), Integer.valueOf(adc.getInt("val")));
            }
        }

        if (root.containsKey("inputs")) {
            int input = root.getInt("inputs");
            position.set("ignition", Boolean.valueOf(BitUtil.check(input, 0)));
            position.set("input", Integer.valueOf(input));
        }
        if (root.containsKey("outputs")) {
            position.set("output", Integer.valueOf(root.getInt("outputs")));
        }
        if (root.containsKey("status")) {
            position.set("status", Integer.valueOf(root.getInt("status")));
        }

        if (root.containsKey("counters")) {
            JsonArray counters = root.getJsonArray("counters");
            for (int i = 0; i < counters.size(); i++) {
                JsonObject counter = counters.getJsonObject(i);
                switch (counter.getInt("id")) {
                    case 0:
                        position.set("battery", Double.valueOf(counter.getInt("val") * 0.001D));
                        break;
                    case 1:
                        position.set("batteryLevel", Double.valueOf(counter.getInt("val") * 0.01D));
                        break;
                    default:
                        position.set("counter" + counter.getInt("id"), Integer.valueOf(counter.getInt("val")));
                        break;
                }


            }
        }
        return position;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\DmtHttpProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */