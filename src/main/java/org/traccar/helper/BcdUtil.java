package org.traccar.helper;

import io.netty.buffer.ByteBuf;


public final class BcdUtil {
    public static int readInteger(ByteBuf buf, int digits) {
        int result = 0;

        for (int i = 0; i < digits / 2; i++) {
            int b = buf.readUnsignedByte();
            result *= 10;
            result += b >>> 4;
            result *= 10;
            result += b & 0xF;
        }

        if (digits % 2 != 0) {
            int b = buf.getUnsignedByte(buf.readerIndex());
            result *= 10;
            result += b >>> 4;
        }

        return result;
    }

    public static double readCoordinate(ByteBuf buf) {
        int b1 = buf.readUnsignedByte();
        int b2 = buf.readUnsignedByte();
        int b3 = buf.readUnsignedByte();
        int b4 = buf.readUnsignedByte();

        double value = ((b2 & 0xF) * 10 + (b3 >> 4));
        value += (((b3 & 0xF) * 10 + (b4 >> 4)) * 10 + (b4 & 0xF)) / 1000.0D;
        value /= 60.0D;
        value += (((b1 >> 4 & 0x7) * 10 + (b1 & 0xF)) * 10 + (b2 >> 4));

        if ((b1 & 0x80) != 0) {
            value = -value;
        }

        return value;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\BcdUtil.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */