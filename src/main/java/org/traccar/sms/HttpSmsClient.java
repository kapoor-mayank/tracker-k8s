package org.traccar.sms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.helper.DataConverter;
import org.traccar.notification.MessageException;


public class HttpSmsClient
        implements SmsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpSmsClient.class);


    private String url = Context.getConfig().getString("sms.http.url");
    private String authorizationHeader = Context.getConfig().getString("sms.http.authorizationHeader", "Authorization");

    private String authorization = Context.getConfig().getString("sms.http.authorization");
    private String template;

    public HttpSmsClient() {
        if (this.authorization == null) {
            String user = Context.getConfig().getString("sms.http.user");
            String password = Context.getConfig().getString("sms.http.password");
            this
                    .authorization = "Basic " + DataConverter.printBase64((user + ":" + password).getBytes(StandardCharsets.UTF_8));
        }
        this.template = Context.getConfig().getString("sms.http.template").trim();
        if (this.template.charAt(0) == '{' || this.template.charAt(0) == '[') {
            this.encode = false;
            this.mediaType = MediaType.APPLICATION_JSON_TYPE;
        } else {
            this.encode = true;
            this.mediaType = MediaType.APPLICATION_FORM_URLENCODED_TYPE;
        }
    }

    private boolean encode;
    private MediaType mediaType;

    private String prepareValue(String value) throws UnsupportedEncodingException {
        return this.encode ? URLEncoder.encode(value, StandardCharsets.UTF_8.name()) : value;
    }

    private String preparePayload(String destAddress, String message) {
        try {
            return this.template
                    .replace("{phone}", prepareValue(destAddress))
                    .replace("{message}", prepareValue(message.trim()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private Invocation.Builder getRequestBuilder() {
        return Context.getClient().target(this.url).request()
                .header(this.authorizationHeader, this.authorization);
    }


    public void sendMessageSync(String destAddress, String message, boolean command) throws MessageException {
        Response response = getRequestBuilder().post(Entity.entity(preparePayload(destAddress, message), this.mediaType));
        if (response.getStatus() / 100 != 2) {
            throw new MessageException((String) response.readEntity(String.class));
        }
    }


    public void sendMessageAsync(String destAddress, String message, boolean command) {
        getRequestBuilder().async().post(
                Entity.entity(preparePayload(destAddress, message), this.mediaType), new InvocationCallback<String>() {
                    public void completed(String s) {
                    }


                    public void failed(Throwable throwable) {
                        HttpSmsClient.LOGGER.warn("SMS send failed", throwable);
                    }
                });
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\sms\HttpSmsClient.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */