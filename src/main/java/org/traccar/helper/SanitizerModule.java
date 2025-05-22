package org.traccar.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

import org.owasp.encoder.Encode;


public class SanitizerModule
        extends SimpleModule {
    public static class SanitizerSerializer
            extends StdSerializer<String> {
        protected SanitizerSerializer() {
            super(String.class);
        }


        public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(Encode.forHtml(value));
        }
    }


    public SanitizerModule() {
        addSerializer((JsonSerializer) new SanitizerSerializer());
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\SanitizerModule.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */