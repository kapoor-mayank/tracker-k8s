package org.traccar.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.traccar.Context;


@Provider
public class ObjectMapperProvider
        implements ContextResolver<ObjectMapper> {
    public ObjectMapper getContext(Class<?> type) {
        return Context.getObjectMapper();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\api\ObjectMapperProvider.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */