package org.traccar.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.traccar.config.Config;

public final class Log {
    private static final String STACK_PACKAGE = "org.traccar";

    private static final int STACK_LIMIT = 3;

    private static class RollingFileHandler extends Handler {
        private String name;
        private String suffix;
        private Writer writer;
        private boolean rotate;

        RollingFileHandler(String name, boolean rotate) {
            this.name = name;
            this.rotate = rotate;
            // Initialize the suffix with the current date to compare and generate new file based on Date change
            this.suffix = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
        }

        public synchronized void publish(LogRecord record) {
            if (isLoggable(record)) {
                try {
                    String currentSuffix = (new SimpleDateFormat("yyyyMMdd")).format(new Date(record.getMillis()));

                    // Check if the suffix (date) has changed for log rotation
                    if (this.rotate && !currentSuffix.equals(this.suffix)) {
                        if (this.writer != null) {
                            this.writer.close(); // Close the current writer
                            File oldFile = new File(this.name);
                            File newFile = new File(this.name + "." + this.suffix);

                            // Rename the current log file with the old suffix
                            if (!oldFile.renameTo(newFile)) {
                                throw new RuntimeException("Log file renaming failed");
                            }
                            this.writer = null; // Reset the writer to null for reinitialization
                        }
                        this.suffix = currentSuffix; // Update suffix with new date
                    }

                    // Initialize the writer if it's null (after rotation or first run)
                    if (this.writer == null) {
                        this.writer = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(this.name, true), StandardCharsets.UTF_8));
                    }

                    // Write the formatted log record to the writer
                    this.writer.write(getFormatter().format(record));
                    this.writer.flush();  // Ensure logs are flushed to the file

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public synchronized void flush() {
            if (this.writer != null) {
                try {
                    this.writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public synchronized void close() throws SecurityException {
            if (this.writer != null) {
                try {
                    this.writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class LogFormatter extends Formatter {
        private boolean fullStackTraces;

        LogFormatter(boolean fullStackTraces) {
            this.fullStackTraces = fullStackTraces;
        }

        private static String formatLevel(Level level) {
            switch (level.getName()) {
                case "FINEST":
                    return "TRACE";
                case "FINER":
                case "FINE":
                case "CONFIG":
                    return "DEBUG";
                case "INFO":
                    return "INFO";
                case "WARNING":
                    return "WARN";
            }
            return "ERROR";
        }

        public String format(LogRecord record) {
            StringBuilder message = new StringBuilder();
            if (record.getMessage() != null)
                message.append(record.getMessage());
            if (record.getThrown() != null) {
                if (message.length() > 0)
                    message.append(" - ");
                if (this.fullStackTraces) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    record.getThrown().printStackTrace(printWriter);
                    message.append(System.lineSeparator()).append(stringWriter.toString());
                } else {
                    message.append(Log.exceptionStack(record.getThrown()));
                }
            }
            return String.format("%1$tF %1$tT %2$5s: %3$s%n", new Object[]{new Date(record.getMillis()), formatLevel(record.getLevel()), message.toString()});
        }
    }

    public static void setupDefaultLogger() {
        String path = null;
        URL url = ClassLoader.getSystemClassLoader().getResource(".");
        if (url != null) {
            File jarPath = new File(url.getPath());
            File logsPath = new File(jarPath, "logs");
            if (!logsPath.exists() || !logsPath.isDirectory())
                logsPath = jarPath;
            path = (new File(logsPath, "tracker-server.log")).getPath();
        }
        setupLogger((path == null), path, Level.WARNING.getName(), false, true);
    }

    public static void setupLogger(Config config) {
        setupLogger(config.getBoolean("logger.console"), config.getString("logger.file"),
                config.getString("logger.level"), config.getBoolean("logger.fullStackTraces"),
                config.getBoolean("logger.rotate"));
    }

    private static void setupLogger(boolean console, String file, String levelString, boolean fullStackTraces, boolean rotate) {
        Handler handler;
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler1 : rootLogger.getHandlers())
            rootLogger.removeHandler(handler1);
        if (console) {
            handler = new ConsoleHandler();
        } else {
            handler = new RollingFileHandler(file, rotate);
        }
        handler.setFormatter(new LogFormatter(fullStackTraces));
        Level level = Level.parse(levelString.toUpperCase());
        rootLogger.setLevel(level);
        handler.setLevel(level);
        handler.setFilter(record -> (record != null && !record.getLoggerName().startsWith("sun")));
        rootLogger.addHandler(handler);
    }

    public static String exceptionStack(Throwable exception) {
        StringBuilder s = new StringBuilder();
        String exceptionMsg = exception.getMessage();
        if (exceptionMsg != null) {
            s.append(exceptionMsg).append(" - ");
        }
        s.append(exception.getClass().getSimpleName());
        StackTraceElement[] stack = exception.getStackTrace();
        if (stack.length > 0) {
            int count = STACK_LIMIT;
            boolean first = true;
            boolean skip = false;
            String file = "";
            s.append(" (");
            for (StackTraceElement element : stack) {
                if (count > 0 && element.getClassName().startsWith("org.traccar")) {
                    if (!first) {
                        s.append(" < ");
                    } else {
                        first = false;
                    }
                    if (skip) {
                        s.append("... < ");
                        skip = false;
                    }
                    if (file.equals(element.getFileName())) {
                        s.append("*");
                    } else {
                        file = element.getFileName();
                        s.append(file, 0, file.length() - 5);
                        count--;
                    }
                    s.append(":").append(element.getLineNumber());
                } else {
                    skip = true;
                }
            }
            if (skip) {
                if (!first)
                    s.append(" < ");
                s.append("...");
            }
            s.append(")");
        }
        return s.toString();
    }
}