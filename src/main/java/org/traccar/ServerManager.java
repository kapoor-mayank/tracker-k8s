package org.traccar;

import java.io.File;
import java.net.BindException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerManager.class);

    private final List<TrackerConnector> connectorList = new LinkedList<>();
    private final Map<String, BaseProtocol> protocolList = new ConcurrentHashMap<>();


    public ServerManager() throws Exception {
        List<String> names = new LinkedList<>();
        String packageName = "org.traccar.protocol";
        String packagePath = packageName.replace('.', '/');
        URL packageUrl = getClass().getClassLoader().getResource(packagePath);

        if (packageUrl.getProtocol().equals("jar")) {
            String jarFileName = URLDecoder.decode(packageUrl.getFile(), StandardCharsets.UTF_8.name());
            try (JarFile jf = new JarFile(jarFileName.substring(5, jarFileName.indexOf("!")))) {
                Enumeration<JarEntry> jarEntries = jf.entries();
                while (jarEntries.hasMoreElements()) {
                    String entryName = ((JarEntry) jarEntries.nextElement()).getName();
                    if (entryName.startsWith(packagePath) && entryName.length() > packagePath.length() + 5) {
                        names.add(entryName.substring(packagePath.length() + 1, entryName.lastIndexOf('.')));
                    }
                }
            }
        } else {
            File folder = new File(new URI(packageUrl.toString()));
            File[] files = folder.listFiles();
            if (files != null) {
                for (File actual : files) {
                    String entryName = actual.getName();
                    names.add(entryName.substring(0, entryName.lastIndexOf('.')));
                }
            }
        }

        for (String name : names) {
            Class<?> protocolClass = Class.forName(packageName + '.' + name);
            if (BaseProtocol.class.isAssignableFrom(protocolClass) &&
                    Context.getConfig().hasKey(BaseProtocol.nameFromClass(protocolClass) + ".port")) {
                BaseProtocol protocol = (BaseProtocol) protocolClass.newInstance();
                this.connectorList.addAll(protocol.getConnectorList());
                this.protocolList.put(protocol.getName(), protocol);
            }
        }
    }

    public BaseProtocol getProtocol(String name) {
        return this.protocolList.get(name);
    }

    public void start() throws Exception {
//        LOGGER.info("ServerManager start()");
        for (TrackerConnector connector : this.connectorList) {
            try {
                connector.start();
            } catch (BindException e) {
                LOGGER.warn("Port disabled due to conflict", e);
            } catch (ConnectException e) {
                LOGGER.warn("Connection failed", e);
            }
        }
    }

    public void stop() {
        for (TrackerConnector connector : this.connectorList) {
            connector.stop();
        }
        GlobalTimer.release();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\ServerManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */