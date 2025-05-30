package org.traccar.geocoder;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;


public class AddressFormat
        extends Format {
    private final String format;

    public AddressFormat() {
        this("%h %r, %t, %s, %c");
    }

    public AddressFormat(String format) {
        this.format = format;
    }

    private static String replace(String s, String key, String value) {
        if (value != null) {
            s = s.replace(key, value);
        } else {
            s = s.replaceAll("[, ]*" + key, "");
        }
        return s;
    }


    public StringBuffer format(Object o, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        Address address = (Address) o;
        String result = this.format;

        result = replace(result, "%p", address.getPostcode());
        result = replace(result, "%c", address.getCountry());
        result = replace(result, "%s", address.getState());
        result = replace(result, "%d", address.getDistrict());
        result = replace(result, "%t", address.getSettlement());
        result = replace(result, "%u", address.getSuburb());
        result = replace(result, "%r", address.getStreet());
        result = replace(result, "%h", address.getHouse());
        result = replace(result, "%f", address.getFormattedAddress());

        result = result.replaceAll("^[, ]*", "");

        return stringBuffer.append(result);
    }


    public Address parseObject(String s, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\geocoder\AddressFormat.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */