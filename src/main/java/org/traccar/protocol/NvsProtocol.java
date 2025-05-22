package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;


public class NvsProtocol
        extends BaseProtocol {
    public NvsProtocol() {
        addServer(new TrackerServer(false, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast((ChannelHandler) new NvsFrameDecoder());
                pipeline.addLast((ChannelHandler) new NvsProtocolDecoder((Protocol) NvsProtocol.this));
            }
        });
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\NvsProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */