package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.traccar.BaseProtocol;
import org.traccar.CharacterDelimiterFrameDecoder;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;


public class GnxProtocol
        extends BaseProtocol {
    public GnxProtocol() {
        addServer(new TrackerServer(false, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast((ChannelHandler) new CharacterDelimiterFrameDecoder(1024, "\n\r"));
                pipeline.addLast((ChannelHandler) new StringDecoder());
                pipeline.addLast((ChannelHandler) new StringEncoder());
                pipeline.addLast((ChannelHandler) new GnxProtocolDecoder((Protocol) GnxProtocol.this));
            }
        });
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GnxProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */