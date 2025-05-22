package org.traccar.geocoder;

import javax.json.JsonArray;
import javax.json.JsonObject;


public class MapQuestGeocoder
        extends JsonGeocoder {
    public MapQuestGeocoder(String url, String key, int cacheSize, AddressFormat addressFormat) {
        super(url + "?key=" + key + "&location=%f,%f", cacheSize, addressFormat);
    }


    public Address parseAddress(JsonObject json) {
        JsonArray result = json.getJsonArray("results");
        if (result != null) {
            JsonArray locations = result.getJsonObject(0).getJsonArray("locations");
            if (locations != null) {
                JsonObject location = locations.getJsonObject(0);

                Address address = new Address();

                if (location.containsKey("street")) {
                    address.setStreet(location.getString("street"));
                }
                if (location.containsKey("adminArea5")) {
                    address.setSettlement(location.getString("adminArea5"));
                }
                if (location.containsKey("adminArea4")) {
                    address.setDistrict(location.getString("adminArea4"));
                }
                if (location.containsKey("adminArea3")) {
                    address.setState(location.getString("adminArea3"));
                }
                if (location.containsKey("adminArea1")) {
                    address.setCountry(location.getString("adminArea1").toUpperCase());
                }
                if (location.containsKey("postalCode")) {
                    address.setPostcode(location.getString("postalCode"));
                }

                return address;
            }
        }
        return null;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geocoder\MapQuestGeocoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */