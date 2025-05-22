package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.string.StringDecoder;
import org.traccar.BaseProtocol;
import org.traccar.CharacterDelimiterFrameDecoder;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;


public class OkoProtocol
        extends BaseProtocol {
    public OkoProtocol() {
        addServer(new TrackerServer(false, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast((ChannelHandler) new CharacterDelimiterFrameDecoder(1024, '}'));
                pipeline.addLast((ChannelHandler) new StringDecoder());
                pipeline.addLast((ChannelHandler) new OkoProtocolDecoder((Protocol) OkoProtocol.this));
            }
        });
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\OkoProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */