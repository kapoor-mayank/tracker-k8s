package org.traccar.helper;

import java.util.AbstractMap;
import java.util.Map;


public final class ObdDecoder {
    private static final int MODE_CURRENT = 1;
    private static final int MODE_FREEZE_FRAME = 2;
    private static final int MODE_CODES = 3;

    public static Map.Entry<String, Object> decode(int mode, String value) {
        switch (mode) {
            case 1:
            case 2:
                return decodeData(
                        Integer.parseInt(value.substring(0, 2), 16),
                        Integer.parseInt(value.substring(2), 16), true);
            case 3:
                return decodeCodes(value);
        }
        return null;
    }


    private static Map.Entry<String, Object> createEntry(String key, Object value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static Map.Entry<String, Object> decodeCodes(String value) {
        StringBuilder codes = new StringBuilder();
        for (int i = 0; i < value.length() / 4; i++) {
            int numValue = Integer.parseInt(value.substring(i * 4, (i + 1) * 4), 16);
            codes.append(' ');
            switch (numValue >> 14) {
                case 1:
                    codes.append('C');
                    break;
                case 2:
                    codes.append('B');
                    break;
                case 3:
                    codes.append('U');
                    break;
                default:
                    codes.append('P');
                    break;
            }
            codes.append(String.format("%04X", new Object[]{Integer.valueOf(numValue & 0x3FFF)}));
        }
        if (codes.length() > 0) {
            return createEntry("dtcs", codes.toString().trim());
        }
        return null;
    }


    public static Map.Entry<String, Object> decodeData(int pid, int value, boolean convert) {
        switch (pid) {
            case 4:
                return createEntry("engineLoad", Integer.valueOf(convert ? (value * 100 / 255) : value));
            case 5:
                return createEntry("coolantTemp", Integer.valueOf(convert ? (value - 40) : value));
            case 11:
                return createEntry("mapIntake", Integer.valueOf(value));
            case 12:
                return createEntry("rpm", Integer.valueOf(convert ? (value / 4) : value));
            case 13:
                return createEntry("obdSpeed", Integer.valueOf(value));
            case 15:
                return createEntry("intakeTemp", Integer.valueOf(convert ? (value - 40) : value));
            case 17:
                return createEntry("throttle", Integer.valueOf(convert ? (value * 100 / 255) : value));
            case 33:
                return createEntry("milDistance", Integer.valueOf(value));
            case 47:
                return createEntry("fuel", Integer.valueOf(convert ? (value * 100 / 255) : value));
            case 49:
                return createEntry("clearedDistance", Integer.valueOf(value));
        }
        return null;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\ObdDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */