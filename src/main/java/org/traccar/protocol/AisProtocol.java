package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.string.StringDecoder;
import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;


public class AisProtocol
        extends BaseProtocol {
    public AisProtocol() {
        addServer(new TrackerServer(false, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast((ChannelHandler) new StringDecoder());
                pipeline.addLast((ChannelHandler) new AisProtocolDecoder((Protocol) AisProtocol.this));
            }
        });
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\AisProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */