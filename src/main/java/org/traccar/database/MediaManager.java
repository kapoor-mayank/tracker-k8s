package org.traccar.database;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MediaManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaManager.class);

    private String path;

    public MediaManager(String path) {
        this.path = path;
    }

    private File createFile(String uniqueId, String name) throws IOException {
        Path filePath = Paths.get(this.path, new String[]{uniqueId, name});
        Path directoryPath = filePath.getParent();
        if (directoryPath != null) {
            Files.createDirectories(directoryPath, (FileAttribute<?>[]) new FileAttribute[0]);
        }
        return filePath.toFile();
    }

    public String writeFile(String uniqueId, ByteBuf buf, String extension) {
        return writeFile(uniqueId, buf, null, extension);
    }

    public String writeFile(String uniqueId, ByteBuf buf, String prefix, String extension) {
        if (this.path != null) {
            int size = buf.readableBytes();
            String name = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date()) + "." + extension;
            if (prefix != null) {
                name = prefix + "-" + name;
            }
            try (FileOutputStream output = new FileOutputStream(createFile(uniqueId, name));
                 FileChannel fileChannel = output.getChannel()) {
                ByteBuffer byteBuffer = buf.nioBuffer();
                int written = 0;
                while (written < size) {
                    written += fileChannel.write(byteBuffer);
                }
                fileChannel.force(false);
                return name;
            } catch (IOException e) {
                LOGGER.warn("Save media file error", e);
            }
        }
        return null;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\MediaManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */