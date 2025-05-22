package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;


public class M2mProtocol
        extends BaseProtocol {
    public M2mProtocol() {
        addServer(new TrackerServer(false, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast((ChannelHandler) new FixedLengthFrameDecoder(23));
                pipeline.addLast((ChannelHandler) new M2mProtocolDecoder((Protocol) M2mProtocol.this));
            }
        });
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\M2mProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */