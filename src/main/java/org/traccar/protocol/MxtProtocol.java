package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;


public class MxtProtocol
        extends BaseProtocol {
    public MxtProtocol() {
        addServer(new TrackerServer(false, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast((ChannelHandler) new MxtFrameDecoder());
                pipeline.addLast((ChannelHandler) new MxtProtocolDecoder((Protocol) MxtProtocol.this));
            }
        });
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\MxtProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */