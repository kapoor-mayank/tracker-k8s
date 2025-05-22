package org.traccar.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.traccar.BaseHttpProtocolDecoder;
import org.traccar.DeviceSession;
import org.traccar.Protocol;
import org.traccar.helper.DateUtil;
import org.traccar.model.CellTower;
import org.traccar.model.Network;
import org.traccar.model.Position;
import org.traccar.model.WifiAccessPoint;

public class OsmAndProtocolDecoder extends BaseHttpProtocolDecoder {
    public OsmAndProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest)msg;
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = decoder.parameters();
        if (params.isEmpty()) {
            decoder = new QueryStringDecoder(request.content().toString(StandardCharsets.US_ASCII), false);
            params = decoder.parameters();
        }
        Position position = new Position(getProtocolName());
        position.setValid(true);
        Network network = new Network();
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                DeviceSession deviceSession;
                String[] location;
                String[] cell;
                String[] wifi;
                switch ((String)entry.getKey()) {
                    case "protocol":
                        if (getProtocolName().equals("insert"))
                            position.setProtocol(value);
                        continue;
                    case "id":
                    case "deviceid":
                        deviceSession = getDeviceSession(channel, remoteAddress, new String[] { value });
                        if (deviceSession == null) {
                            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
                            return null;
                        }
                        position.setDeviceId(deviceSession.getDeviceId());
                        continue;
                    case "valid":
                        position.setValid((Boolean.parseBoolean(value) || "1".equals(value)));
                        continue;
                    case "timestamp":
                        try {
                            long timestamp = Long.parseLong(value);
                            if (timestamp < 2147483647L)
                                timestamp *= 1000L;
                            position.setTime(new Date(timestamp));
                        } catch (NumberFormatException error) {
                            if (value.contains("T")) {
                                position.setTime(DateUtil.parseDate(value));
                                continue;
                            }
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            position.setTime(dateFormat.parse(value));
                        }
                        continue;
                    case "lat":
                        position.setLatitude(Double.parseDouble(value));
                        continue;
                    case "lon":
                        position.setLongitude(Double.parseDouble(value));
                        continue;
                    case "location":
                        location = value.split(",");
                        position.setLatitude(Double.parseDouble(location[0]));
                        position.setLongitude(Double.parseDouble(location[1]));
                        continue;
                    case "cell":
                        cell = value.split(",");
                        if (cell.length > 4) {
                            network.addCellTower(CellTower.from(
                                    Integer.parseInt(cell[0]), Integer.parseInt(cell[1]),
                                    Integer.parseInt(cell[2]), Integer.parseInt(cell[3]), Integer.parseInt(cell[4])));
                            continue;
                        }
                        network.addCellTower(CellTower.from(
                                Integer.parseInt(cell[0]), Integer.parseInt(cell[1]),
                                Integer.parseInt(cell[2]), Integer.parseInt(cell[3])));
                        continue;
                    case "wifi":
                        wifi = value.split(",");
                        network.addWifiAccessPoint(WifiAccessPoint.from(wifi[0]
                                .replace('-', ':'), Integer.parseInt(wifi[1])));
                        continue;
                    case "speed":
                        position.setSpeed(convertSpeed(Double.parseDouble(value), "kn"));
                        continue;
                    case "bearing":
                    case "heading":
                        position.setCourse(Double.parseDouble(value));
                        continue;
                    case "altitude":
                        position.setAltitude(Double.parseDouble(value));
                        continue;
                    case "accuracy":
                        position.setAccuracy(Double.parseDouble(value));
                        continue;
                    case "hdop":
                        position.set("hdop", Double.valueOf(Double.parseDouble(value)));
                        continue;
                    case "batt":
                        position.set("batteryLevel", Double.valueOf(Double.parseDouble(value)));
                        continue;
                    case "driverUniqueId":
                        position.set("driverUniqueId", value);
                        continue;
                }
                try {
                    position.set(entry.getKey(), Double.valueOf(Double.parseDouble(value)));
                } catch (NumberFormatException e) {
                    switch (value) {
                        case "true":
                            position.set(entry.getKey(), Boolean.valueOf(true));
                            continue;
                        case "false":
                            position.set(entry.getKey(), Boolean.valueOf(false));
                            continue;
                    }
                    position.set(entry.getKey(), value);
                }
            }
        }
        if (position.getFixTime() == null)
            position.setTime(new Date());
        if (network.getCellTowers() != null || network.getWifiAccessPoints() != null)
            position.setNetwork(network);
        if (position.getLatitude() == 0.0D && position.getLongitude() == 0.0D)
            getLastLocation(position, position.getDeviceTime());
        if (position.getDeviceId() != 0L) {
            sendResponse(channel, HttpResponseStatus.OK);
            return position;
        }
        sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
        return null;
    }
}
