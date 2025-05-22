package org.traccar;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.model.Position;


public abstract class BaseDataHandler
        extends ChannelInboundHandlerAdapter
        //extends ExtendedObjectDecoder
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDataHandler.class);

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Position) {
            Position position = handlePosition((Position) msg);
            if (position != null) {
                ctx.fireChannelRead(position);
            }
        } else {
//            LOGGER.info("BaseDataHandler channelRead(): {}", msg);
            super.channelRead(ctx, msg);
        }
    }

    protected abstract Position handlePosition(Position paramPosition);
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\BaseDataHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */