package org.traccar.helper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


public class BitBuffer {
    private final ByteBuf buffer;
    private int writeByte;
    private int writeCount;
    private int readByte;
    private int readCount;

    public BitBuffer() {
        this.buffer = Unpooled.buffer();
    }

    public BitBuffer(ByteBuf buffer) {
        this.buffer = buffer;
    }

    public void writeEncoded(byte[] bytes) {
        for (byte b : bytes) {
            b = (byte) (b - 48);
            if (b > 40) {
                b = (byte) (b - 8);
            }
            write(b);
        }
    }

    public void write(int b) {
        if (this.writeCount == 0) {
            this.writeByte |= b;
            this.writeCount = 6;
        } else {
            int remaining = 8 - this.writeCount;
            this.writeByte <<= remaining;
            this.writeByte |= b >> 6 - remaining;
            this.buffer.writeByte(this.writeByte);
            this.writeByte = b & (1 << 6 - remaining) - 1;
            this.writeCount = 6 - remaining;
        }
    }

    public int readUnsigned(int length) {
        int result = 0;

        while (length > 0) {
            if (this.readCount == 0) {
                this.readByte = this.buffer.readUnsignedByte();
                this.readCount = 8;
            }
            if (this.readCount >= length) {
                result <<= length;
                result |= this.readByte >> this.readCount - length;
                this.readByte &= (1 << this.readCount - length) - 1;
                this.readCount -= length;
                length = 0;
                continue;
            }
            result <<= this.readCount;
            result |= this.readByte;
            length -= this.readCount;
            this.readByte = 0;
            this.readCount = 0;
        }


        return result;
    }

    public int readSigned(int length) {
        int result = readUnsigned(length);
        int signBit = 1 << length - 1;
        if ((result & signBit) == 0) {
            return result;
        }
        result &= signBit - 1;
        result += signBit - 1 ^ 0xFFFFFFFF;
        return result;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\BitBuffer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */