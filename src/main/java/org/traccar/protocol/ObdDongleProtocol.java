package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;

public class ObdDongleProtocol extends BaseProtocol {
    public ObdDongleProtocol() {
        addServer(new TrackerServer(false, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(1099, 20, 2, 3, 0));
                pipeline.addLast((ChannelHandler)new ObdDongleProtocolDecoder((Protocol)ObdDongleProtocol.this));
            }
        });
    }
}
