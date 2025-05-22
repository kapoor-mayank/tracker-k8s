package org.traccar.helper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;


public final class BufferUtil {
    public static int readSignedMagnitudeInt(ByteBuf buffer) {
        long value = buffer.readUnsignedInt();
        int result = (int) BitUtil.to(value, 31);
        return BitUtil.check(value, 31) ? -result : result;
    }

    public static int indexOf(ByteBuf buffer, int fromIndex, int toIndex, byte value, int count) {
        int startIndex = fromIndex;
        for (int i = 0; i < count; i++) {
            int result = buffer.indexOf(startIndex, toIndex, value);
            if (result < 0 || i == count - 1) {
                return result;
            }
            startIndex = result + 1;
        }
        return -1;
    }

    public static int indexOf(String needle, ByteBuf haystack) {
        return indexOf(needle, haystack, haystack.readerIndex(), haystack.writerIndex());
    }

    public static int indexOf(String needle, ByteBuf haystack, int startIndex, int endIndex) {
        ByteBuf wrappedNeedle = Unpooled.wrappedBuffer(needle.getBytes(StandardCharsets.US_ASCII));
        try {
            return indexOf(wrappedNeedle, haystack, startIndex, endIndex);
        } finally {
            wrappedNeedle.release();
        }
    }

    public static int indexOf(ByteBuf needle, ByteBuf haystack, int startIndex, int endIndex) {
        ByteBuf wrappedHaystack;
        if (startIndex == haystack.readerIndex() && endIndex == haystack.writerIndex()) {
            wrappedHaystack = haystack;
        } else {
            wrappedHaystack = Unpooled.wrappedBuffer(haystack);
            wrappedHaystack.readerIndex(startIndex - haystack.readerIndex());
            wrappedHaystack.writerIndex(endIndex - haystack.readerIndex());
        }
        int result = ByteBufUtil.indexOf(needle, wrappedHaystack);
        return (result < 0) ? result : (startIndex + result);
    }

    public static String readString(ByteBuf buf, int length) {
        return buf.readCharSequence(length, StandardCharsets.US_ASCII).toString();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\BufferUtil.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */