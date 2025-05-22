package org.traccar.helper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;


public final class Checksum {
    public static class Algorithm {
        private int poly;
        private int init;
        private boolean refIn;
        private boolean refOut;
        private int xorOut;
        private int[] table;

        public Algorithm(int bits, int poly, int init, boolean refIn, boolean refOut, int xorOut) {
            this.poly = poly;
            this.init = init;
            this.refIn = refIn;
            this.refOut = refOut;
            this.xorOut = xorOut;
            this.table = (bits == 8) ? initTable8() : initTable16();
        }

        private int[] initTable8() {
            int[] table = new int[256];

            for (int i = 0; i < 256; i++) {
                int crc = i;
                for (int j = 0; j < 8; j++) {
                    boolean bit = ((crc & 0x80) != 0);
                    crc <<= 1;
                    if (bit) {
                        crc ^= this.poly;
                    }
                }
                table[i] = crc & 0xFF;
            }
            return table;
        }

        private int[] initTable16() {
            int[] table = new int[256];

            for (int i = 0; i < 256; i++) {
                int crc = i << 8;
                for (int j = 0; j < 8; j++) {
                    boolean bit = ((crc & 0x8000) != 0);
                    crc <<= 1;
                    if (bit) {
                        crc ^= this.poly;
                    }
                }
                table[i] = crc & 0xFFFF;
            }
            return table;
        }
    }


    private static int reverse(int value, int bits) {
        int result = 0;
        for (int i = 0; i < bits; i++) {
            result = result << 1 | value & 0x1;
            value >>= 1;
        }
        return result;
    }

    public static int crc8(Algorithm algorithm, ByteBuffer buf) {
        int crc = algorithm.init;
        while (buf.hasRemaining()) {
            int b = buf.get() & 0xFF;
            if (algorithm.refIn) {
                b = reverse(b, 8);
            }
            crc = algorithm.table[crc & 0xFF ^ b];
        }
        if (algorithm.refOut) {
            crc = reverse(crc, 8);
        }
        return (crc ^ algorithm.xorOut) & 0xFF;
    }

    public static int crc16(Algorithm algorithm, ByteBuffer buf) {
        int crc = algorithm.init;
        while (buf.hasRemaining()) {
            int b = buf.get() & 0xFF;
            if (algorithm.refIn) {
                b = reverse(b, 8);
            }
            crc = crc << 8 ^ algorithm.table[crc >> 8 & 0xFF ^ b];
        }
        if (algorithm.refOut) {
            crc = reverse(crc, 16);
        }
        return (crc ^ algorithm.xorOut) & 0xFFFF;
    }

    public static final Algorithm CRC8_EGTS = new Algorithm(8, 49, 255, false, false, 0);
    public static final Algorithm CRC8_ROHC = new Algorithm(8, 7, 255, true, true, 0);

    public static final Algorithm CRC16_IBM = new Algorithm(16, 32773, 0, true, true, 0);
    public static final Algorithm CRC16_X25 = new Algorithm(16, 4129, 65535, true, true, 65535);
    public static final Algorithm CRC16_MODBUS = new Algorithm(16, 32773, 65535, true, true, 0);
    public static final Algorithm CRC16_CCITT_FALSE = new Algorithm(16, 4129, 65535, false, false, 0);
    public static final Algorithm CRC16_KERMIT = new Algorithm(16, 4129, 0, true, true, 0);
    public static final Algorithm CRC16_XMODEM = new Algorithm(16, 4129, 0, false, false, 0);

    public static int crc32(ByteBuffer buf) {
        CRC32 checksum = new CRC32();
        while (buf.hasRemaining()) {
            checksum.update(buf.get());
        }
        return (int) checksum.getValue();
    }

    public static int xor(ByteBuffer buf) {
        int checksum = 0;
        while (buf.hasRemaining()) {
            checksum ^= buf.get();
        }
        return checksum;
    }

    public static int xor(String string) {
        byte checksum = 0;
        for (byte b : string.getBytes(StandardCharsets.US_ASCII)) {
            checksum = (byte) (checksum ^ b);
        }
        return checksum;
    }

    public static String nmea(String msg) {
        int checksum = 0;
        byte[] bytes = msg.getBytes(StandardCharsets.US_ASCII);
        for (int i = 1; i < bytes.length; i++) {
            checksum ^= bytes[i];
        }
        return String.format("*%02x", new Object[]{Integer.valueOf(checksum)}).toUpperCase();
    }

    public static int sum(ByteBuffer buf) {
        byte checksum = 0;
        while (buf.hasRemaining()) {
            checksum = (byte) (checksum + buf.get());
        }
        return checksum;
    }

    public static int modulo256(ByteBuffer buf) {
        int checksum = 0;
        while (buf.hasRemaining()) {
            checksum = checksum + buf.get() & 0xFF;
        }
        return checksum;
    }

    public static String sum(String msg) {
        byte checksum = 0;
        for (byte b : msg.getBytes(StandardCharsets.US_ASCII)) {
            checksum = (byte) (checksum + b);
        }
        return String.format("%02X", new Object[]{Byte.valueOf(checksum)}).toUpperCase();
    }

    public static long luhn(long imei) {
        long checksum = 0L;
        long remain = imei;

        for (int i = 0; remain != 0L; i++) {
            long digit = remain % 10L;

            if (i % 2 == 0) {
                digit *= 2L;
                if (digit >= 10L) {
                    digit = 1L + digit % 10L;
                }
            }

            checksum += digit;
            remain /= 10L;
        }

        return (10L - checksum % 10L) % 10L;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\Checksum.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */