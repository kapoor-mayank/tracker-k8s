package org.traccar.geocoder;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;


public class GoogleGeocoder
        extends JsonGeocoder {
    private static String formatUrl(String key, String language) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f";
        if (key != null) {
            url = url + "&key=" + key;
        }
        if (language != null) {
            url = url + "&language=" + language;
        }
        return url;
    }

    public GoogleGeocoder(String key, String language, int cacheSize, AddressFormat addressFormat) {
        super(formatUrl(key, language), cacheSize, addressFormat);
    }


    public Address parseAddress(JsonObject json) {
        JsonArray results = json.getJsonArray("results");

        if (!results.isEmpty()) {
            Address address = new Address();

            JsonObject result = (JsonObject) results.get(0);
            JsonArray components = result.getJsonArray("address_components");

            if (result.containsKey("formatted_address")) {
                address.setFormattedAddress(result.getString("formatted_address"));
            }

            for (JsonObject component : components.getValuesAs(JsonObject.class)) {

                String value = component.getString("short_name");

                for (JsonString type : component.getJsonArray("types").getValuesAs(JsonString.class)) {

                    switch (type.getString()) {
                        case "street_number":
                            address.setHouse(value);
                            break;
                        case "route":
                            address.setStreet(value);
                            break;
                        case "locality":
                            address.setSettlement(value);
                            break;
                        case "administrative_area_level_2":
                            address.setDistrict(value);
                            break;
                        case "administrative_area_level_1":
                            address.setState(value);
                            break;
                        case "country":
                            address.setCountry(value);
                            break;
                        case "postal_code":
                            address.setPostcode(value);
                    }


                }
            }
            return address;
        }

        return null;
    }


    protected String parseError(JsonObject json) {
        return json.getString("error_message");
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geocoder\GoogleGeocoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */