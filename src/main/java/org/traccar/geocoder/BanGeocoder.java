package org.traccar.geocoder;

import javax.json.JsonArray;
import javax.json.JsonObject;


public class BanGeocoder
        extends JsonGeocoder {
    public BanGeocoder(int cacheSize, AddressFormat addressFormat) {
        super("https://api-adresse.data.gouv.fr/reverse/?lat=%f&lon=%f", cacheSize, addressFormat);
    }


    public Address parseAddress(JsonObject json) {
        JsonArray result = json.getJsonArray("features");

        if (result != null && !result.isEmpty()) {
            JsonObject location = result.getJsonObject(0).getJsonObject("properties");
            Address address = new Address();

            address.setCountry("FR");
            if (location.containsKey("postcode")) {
                address.setPostcode(location.getString("postcode"));
            }
            if (location.containsKey("context")) {
                address.setDistrict(location.getString("context"));
            }
            if (location.containsKey("name")) {
                address.setStreet(location.getString("name"));
            }
            if (location.containsKey("city")) {
                address.setSettlement(location.getString("city"));
            }
            if (location.containsKey("housenumber")) {
                address.setHouse(location.getString("housenumber"));
            }
            if (location.containsKey("label")) {
                address.setFormattedAddress(location.getString("label"));
            }

            return address;
        }

        return null;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geocoder\BanGeocoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */