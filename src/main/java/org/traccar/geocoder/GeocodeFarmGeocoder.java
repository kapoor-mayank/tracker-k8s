package org.traccar.geocoder;

import javax.json.JsonObject;


public class GeocodeFarmGeocoder
        extends JsonGeocoder {
    private static String formatUrl(String key, String language) {
        String url = "https://www.geocode.farm/v3/json/reverse/";
        url = url + "?lat=%f&lon=%f&country=us&count=1";
        if (key != null) {
            url = url + "&key=" + key;
        }
        if (language != null) {
            url = url + "&lang=" + language;
        }
        return url;
    }

    public GeocodeFarmGeocoder(String key, String language, int cacheSize, AddressFormat addressFormat) {
        super(formatUrl(key, language), cacheSize, addressFormat);
    }


    public Address parseAddress(JsonObject json) {
        Address address = new Address();


        JsonObject result = json.getJsonObject("geocoding_results").getJsonArray("RESULTS").getJsonObject(0);

        JsonObject resultAddress = result.getJsonObject("ADDRESS");

        if (result.containsKey("formatted_address")) {
            address.setFormattedAddress(result.getString("formatted_address"));
        }
        if (resultAddress.containsKey("street_number")) {
            address.setStreet(resultAddress.getString("street_number"));
        }
        if (resultAddress.containsKey("street_name")) {
            address.setStreet(resultAddress.getString("street_name"));
        }
        if (resultAddress.containsKey("locality")) {
            address.setSettlement(resultAddress.getString("locality"));
        }
        if (resultAddress.containsKey("admin_1")) {
            address.setState(resultAddress.getString("admin_1"));
        }
        if (resultAddress.containsKey("country")) {
            address.setCountry(resultAddress.getString("country"));
        }

        return address;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geocoder\GeocodeFarmGeocoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */