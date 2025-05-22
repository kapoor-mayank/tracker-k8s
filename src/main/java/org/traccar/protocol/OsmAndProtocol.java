package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;

public class OsmAndProtocol extends BaseProtocol {
    public OsmAndProtocol() {
        addServer(new TrackerServer(false, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast((ChannelHandler)new HttpResponseEncoder());
                pipeline.addLast((ChannelHandler)new HttpRequestDecoder());
                pipeline.addLast((ChannelHandler)new HttpObjectAggregator(16384));
                pipeline.addLast((ChannelHandler)new OsmAndProtocolDecoder((Protocol)OsmAndProtocol.this));
            }
        });
    }
}
