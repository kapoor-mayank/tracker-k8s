package org.traccar.database;

import io.netty.channel.Channel;

import java.net.SocketAddress;

import org.traccar.NetworkMessage;
import org.traccar.Protocol;
import org.traccar.model.Command;


public class ActiveDevice {
    private final long deviceId;
    private final Protocol protocol;
    private final Channel channel;
    private final SocketAddress remoteAddress;

    public ActiveDevice(long deviceId, Protocol protocol, Channel channel, SocketAddress remoteAddress) {
        this.deviceId = deviceId;
        this.protocol = protocol;
        this.channel = channel;
        this.remoteAddress = remoteAddress;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public long getDeviceId() {
        return this.deviceId;
    }

    public void sendCommand(Command command) {
        this.protocol.sendDataCommand(this, command);
    }

    public void write(Object message) {
        this.channel.writeAndFlush(new NetworkMessage(message, this.remoteAddress));
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\ActiveDevice.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */