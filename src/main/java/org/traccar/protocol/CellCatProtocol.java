package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;

public class CellCatProtocol extends BaseProtocol {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellCatProtocol.class);
    public CellCatProtocol() {
        LOGGER.info("CellCatProtocol Initialized");
        addServer(new TrackerServer(false, getName()) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                // Each message is fixed length of 30 bytes
                pipeline.addLast((ChannelHandler) new FixedLengthFrameDecoder(30));
                pipeline.addLast((ChannelHandler) new CellCatProtocolDecoder(CellCatProtocol.this));
            }
        });
    }
}
