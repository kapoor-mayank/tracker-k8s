package org.traccar.helper;


public final class StringUtil {
    public static boolean containsHex(String value) {
        for (char c : value.toCharArray()) {
            if ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                return true;
            }
        }
        return false;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\StringUtil.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */