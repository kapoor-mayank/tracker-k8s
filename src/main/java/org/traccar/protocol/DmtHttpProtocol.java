package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;


public class DmtHttpProtocol
        extends BaseProtocol {
    public DmtHttpProtocol() {
        addServer(new TrackerServer(false, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast((ChannelHandler) new HttpResponseEncoder());
                pipeline.addLast((ChannelHandler) new HttpRequestDecoder());
                pipeline.addLast((ChannelHandler) new HttpObjectAggregator(65535));
                pipeline.addLast((ChannelHandler) new DmtHttpProtocolDecoder((Protocol) DmtHttpProtocol.this));
            }
        });
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\DmtHttpProtocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */