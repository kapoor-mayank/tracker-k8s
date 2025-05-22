package org.traccar.geocoder;

import javax.json.JsonObject;


public class NominatimGeocoder
        extends JsonGeocoder {
    private static String formatUrl(String url, String key, String language) {
        if (url == null) {
            url = "https://nominatim.openstreetmap.org/reverse";
        }
        url = url + "?format=json&lat=%f&lon=%f&zoom=18&addressdetails=1";
        if (key != null) {
            url = url + "&key=" + key;
        }
        if (language != null) {
            url = url + "&accept-language=" + language;
        }
        return url;
    }

    public NominatimGeocoder(String url, String key, String language, int cacheSize, AddressFormat addressFormat) {
        super(formatUrl(url, key, language), cacheSize, addressFormat);
    }


    public Address parseAddress(JsonObject json) {
        JsonObject result = json.getJsonObject("address");

        if (result != null) {
            Address address = new Address();

            if (json.containsKey("display_name")) {
                address.setFormattedAddress(json.getString("display_name"));
            }

            if (result.containsKey("house_number")) {
                address.setHouse(result.getString("house_number"));
            }
            if (result.containsKey("road")) {
                address.setStreet(result.getString("road"));
            }
            if (result.containsKey("suburb")) {
                address.setSuburb(result.getString("suburb"));
            }

            if (result.containsKey("village")) {
                address.setSettlement(result.getString("village"));
            } else if (result.containsKey("town")) {
                address.setSettlement(result.getString("town"));
            } else if (result.containsKey("city")) {
                address.setSettlement(result.getString("city"));
            }

            if (result.containsKey("state_district")) {
                address.setDistrict(result.getString("state_district"));
            } else if (result.containsKey("region")) {
                address.setDistrict(result.getString("region"));
            }

            if (result.containsKey("state")) {
                address.setState(result.getString("state"));
            }
            if (result.containsKey("country_code")) {
                address.setCountry(result.getString("country_code").toUpperCase());
            }
            if (result.containsKey("postcode")) {
                address.setPostcode(result.getString("postcode"));
            }

            return address;
        }

        return null;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geocoder\NominatimGeocoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */