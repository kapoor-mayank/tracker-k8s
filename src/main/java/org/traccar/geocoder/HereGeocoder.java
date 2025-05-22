package org.traccar.geocoder;

import javax.json.JsonObject;


public class HereGeocoder
        extends JsonGeocoder {
    private static String formatUrl(String id, String key, String language) {
        String url = "https://reverse.geocoder.api.here.com/6.2/reversegeocode.json";
        url = url + "?mode=retrieveAddresses&maxresults=1";
        url = url + "&prox=%f,%f,0";
        url = url + "&app_id=" + id;
        url = url + "&app_code=" + key;
        if (language != null) {
            url = url + "&language=" + language;
        }
        return url;
    }

    public HereGeocoder(String id, String key, String language, int cacheSize, AddressFormat addressFormat) {
        super(formatUrl(id, key, language), cacheSize, addressFormat);
    }


    public Address parseAddress(JsonObject json) {
        JsonObject result = json.getJsonObject("Response").getJsonArray("View").getJsonObject(0).getJsonArray("Result").getJsonObject(0).getJsonObject("Location").getJsonObject("Address");

        if (result != null) {
            Address address = new Address();

            if (json.containsKey("Label")) {
                address.setFormattedAddress(json.getString("Label"));
            }

            if (result.containsKey("HouseNumber")) {
                address.setHouse(result.getString("HouseNumber"));
            }
            if (result.containsKey("Street")) {
                address.setStreet(result.getString("Street"));
            }
            if (result.containsKey("City")) {
                address.setSettlement(result.getString("City"));
            }
            if (result.containsKey("District")) {
                address.setDistrict(result.getString("District"));
            }
            if (result.containsKey("State")) {
                address.setState(result.getString("State"));
            }
            if (result.containsKey("Country")) {
                address.setCountry(result.getString("Country").toUpperCase());
            }
            if (result.containsKey("PostalCode")) {
                address.setPostcode(result.getString("PostalCode"));
            }

            return address;
        }

        return null;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geocoder\HereGeocoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */