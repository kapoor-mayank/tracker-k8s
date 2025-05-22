package org.traccar.geocoder;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.json.JsonObject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;


public abstract class JsonGeocoder
        implements Geocoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonGeocoder.class);

    private final String url;

    private final AddressFormat addressFormat;
    private Map<Map.Entry<Double, Double>, String> cache;

    public JsonGeocoder(String url, final int cacheSize, AddressFormat addressFormat) {
        this.url = url;
        this.addressFormat = addressFormat;
        if (cacheSize > 0) {
            this.cache = Collections.synchronizedMap(new LinkedHashMap<Map.Entry<Double, Double>, String>() {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return (size() > cacheSize);
                }
            });
        }
    }


    private String handleResponse(double latitude, double longitude, JsonObject json, Geocoder.ReverseGeocoderCallback callback) {
        Address address = parseAddress(json);
        if (address != null) {
            String formattedAddress = this.addressFormat.format(address);
            if (this.cache != null) {
                this.cache.put(new AbstractMap.SimpleImmutableEntry<>(Double.valueOf(latitude), Double.valueOf(longitude)), formattedAddress);
            }
            if (callback != null) {
                callback.onSuccess(formattedAddress);
            }
            return formattedAddress;
        }
        String msg = "Empty address. Error: " + parseError(json);
        if (callback != null) {
            callback.onFailure(new GeocoderException(msg));
        } else {
            LOGGER.warn(msg);
        }

        return null;
    }


    public String getAddress(final double latitude, final double longitude, final Geocoder.ReverseGeocoderCallback callback) {
        if (this.cache != null) {
            String cachedAddress = this.cache.get(new AbstractMap.SimpleImmutableEntry<>(Double.valueOf(latitude), Double.valueOf(longitude)));
            if (cachedAddress != null) {
                if (callback != null) {
                    callback.onSuccess(cachedAddress);
                }
                return cachedAddress;
            }
        }

        Invocation.Builder request = Context.getClient().target(String.format(this.url, new Object[]{Double.valueOf(latitude), Double.valueOf(longitude)})).request();

        if (callback != null) {
            request.async().get(new InvocationCallback<JsonObject>() {
                public void completed(JsonObject json) {
                    JsonGeocoder.this.handleResponse(latitude, longitude, json, callback);
                }


                public void failed(Throwable throwable) {
                    callback.onFailure(throwable);
                }
            });
        } else {
            try {
                return handleResponse(latitude, longitude, (JsonObject) request.get(JsonObject.class), null);
            } catch (ClientErrorException e) {
                LOGGER.warn("Geocoder network error", (Throwable) e);
            }
        }
        return null;
    }


    protected String parseError(JsonObject json) {
        return null;
    }

    public abstract Address parseAddress(JsonObject paramJsonObject);
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geocoder\JsonGeocoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */