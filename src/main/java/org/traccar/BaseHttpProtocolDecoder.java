package org.traccar;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;


public abstract class BaseHttpProtocolDecoder
        extends BaseProtocolDecoder {
    public BaseHttpProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    public void sendResponse(Channel channel, HttpResponseStatus status) {
        if (channel != null) {
            DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
            defaultFullHttpResponse.headers().add((CharSequence) HttpHeaderNames.CONTENT_LENGTH, Integer.valueOf(0));
            channel.writeAndFlush(new NetworkMessage(defaultFullHttpResponse, channel.remoteAddress()));
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\BaseHttpProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */