package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;

public class DmtProtocol extends BaseProtocol {
    private static final Logger LOGGER = LoggerFactory.getLogger(DmtProtocol.class);
    public DmtProtocol() {
//        LOGGER.info("DmtProtocol Initialised");
        addServer(new TrackerServer(false, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast((ChannelHandler)new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 1024, 3, 2, 0, 0, true));
//                LOGGER.info("Dmt Protocol Added LengthFieldBasedFrameDecoder");
                pipeline.addLast((ChannelHandler)new DmtProtocolDecoder((Protocol)DmtProtocol.this));
//                LOGGER.info("Dmt Protocol Added decoder: {}", pipeline);
            }
        });
    }
}
