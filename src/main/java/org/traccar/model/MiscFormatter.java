package org.traccar.model;

import java.text.DecimalFormat;
import java.util.Map;


public final class MiscFormatter {
    private static final String XML_ROOT_NODE = "info";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    private static String format(Object value) {
        if (value instanceof Double || value instanceof Float) {
            return DECIMAL_FORMAT.format(value);
        }
        return value.toString();
    }


    public static String toXmlString(Map<String, Object> attributes) {
        StringBuilder result = new StringBuilder();

        result.append("<").append("info").append(">");

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {

            result.append("<").append(entry.getKey()).append(">");
            result.append(format(entry.getValue()));
            result.append("</").append(entry.getKey()).append(">");
        }

        result.append("</").append("info").append(">");

        return result.toString();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\MiscFormatter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */