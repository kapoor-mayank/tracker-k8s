package org.traccar.api;

import io.netty.handler.codec.http.HttpHeaderNames;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.traccar.Context;


public class CorsResponseFilter
        implements ContainerResponseFilter {
    private static final String ORIGIN_ALL = "*";
    private static final String HEADERS_ALL = "origin, content-type, accept, authorization";
    private static final String METHODS_ALL = "GET, POST, PUT, DELETE, OPTIONS";

    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        if (!response.getHeaders().containsKey(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString())) {
            response.getHeaders().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(), "origin, content-type, accept, authorization");
        }

        if (!response.getHeaders().containsKey(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString())) {
            response.getHeaders().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), Boolean.valueOf(true));
        }

        if (!response.getHeaders().containsKey(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString())) {
            response.getHeaders().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(), "GET, POST, PUT, DELETE, OPTIONS");
        }

        if (!response.getHeaders().containsKey(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString())) {
            String origin = request.getHeaderString(HttpHeaderNames.ORIGIN.toString());
            String allowed = Context.getConfig().getString("web.origin");

            if (origin == null) {
                response.getHeaders().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), "*");
            } else if (allowed == null || allowed.equals("*") || allowed.contains(origin)) {
                response.getHeaders().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), origin);
            }
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\CorsResponseFilter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */