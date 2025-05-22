package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.ByteOrder;

import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;


public class AdmProtocol
        extends BaseProtocol {
    public AdmProtocol() {
        setSupportedDataCommands(new String[]{"getDeviceStatus", "custom"});


        addServer(new TrackerServer(false, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast((ChannelHandler) new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 1024, 2, 1, -3, 0, true));
                pipeline.addLast((ChannelHandler) new StringEncoder());
                pipeline.addLast((ChannelHandler) new AdmProtocolEncoder());
                pipeline.addLast((ChannelHandler) new AdmProtocolDecoder((Protocol) AdmProtocol.this));
            }
        });
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AdmProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */