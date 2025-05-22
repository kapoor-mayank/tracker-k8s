package org.traccar.protocol;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.string.StringEncoder;
import org.traccar.BaseProtocol;
import org.traccar.Context;
import org.traccar.PipelineBuilder;
import org.traccar.Protocol;
import org.traccar.TrackerServer;


public class H02Protocol
        extends BaseProtocol {
    public H02Protocol() {
        setSupportedDataCommands(new String[]{"alarmArm", "alarmDisarm", "engineStop", "engineResume", "positionPeriodic"});


        addServer(new TrackerServer(false, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                int messageLength = Context.getConfig().getInteger(H02Protocol.this.getName() + ".messageLength");
                pipeline.addLast((ChannelHandler) new H02FrameDecoder(messageLength));
                pipeline.addLast((ChannelHandler) new StringEncoder());
                pipeline.addLast((ChannelHandler) new H02ProtocolEncoder());
                pipeline.addLast((ChannelHandler) new H02ProtocolDecoder((Protocol) H02Protocol.this));
            }
        });
        addServer(new TrackerServer(true, getName()) {
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                pipeline.addLast((ChannelHandler) new StringEncoder());
                pipeline.addLast((ChannelHandler) new H02ProtocolEncoder());
                pipeline.addLast((ChannelHandler) new H02ProtocolDecoder((Protocol) H02Protocol.this));
            }
        });
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\H02Protocol.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */