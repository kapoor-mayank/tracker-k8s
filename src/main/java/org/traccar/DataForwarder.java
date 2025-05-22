package org.traccar;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataForwarder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataForwarder.class);

    private static class Info {
        private InetAddress address;
        private int port;
        private boolean datagram;
        private DatagramSocket udpSocket;
        private Socket tcpSocket;
        private OutputStream tcpSteam;

        private Info() {
        }
    }

    private Map<String, List<Info>> infoMap = new HashMap<>();
    private Map<String, Integer> fails = new HashMap<>();

    public DataForwarder() {
        String config = Context.getConfig().getString("forwarder.config", "");
        for (String line : config.split("\r?\n")) {
            try {
                String[] params = line.trim().split(" +");
                if (params.length >= 4) {
                    Info info = new Info();
                    info.address = InetAddress.getByName(params[1]);
                    info.port = Integer.parseInt(params[2]);
                    info.datagram = params[3].equalsIgnoreCase("UDP");

                    List<Info> infoList = this.infoMap.getOrDefault(params[0], new ArrayList<>());
                    infoList.add(info);
                    this.infoMap.put(params[0], infoList);
                }
            } catch (IOException e) {
                LOGGER.warn("DataForwarder init error", e);
            }
        }
    }

    public void forward(String uniqueId, byte[] data) {
        if (((Integer) this.fails.getOrDefault(uniqueId, Integer.valueOf(0))).intValue() >= 5)
            return;
        try {
            List<Info> infoList = this.infoMap.get(uniqueId);
            if (infoList != null) {
                for (Info info : infoList) {
                    if (info.datagram) {
                        if (info.udpSocket == null) {
                            info.udpSocket = new DatagramSocket();
                        }
                        info.udpSocket.send(new DatagramPacket(data, data.length, info.address, info.port));
                        continue;
                    }
                    if (info.tcpSocket == null || info.tcpSocket.isClosed()) {
                        info.tcpSocket = new Socket(info.address, info.port);
                        info.tcpSteam = info.tcpSocket.getOutputStream();
                    }
                    info.tcpSteam.write(data);
                }

            }
        } catch (IOException e) {
            this.fails.put(uniqueId, Integer.valueOf(((Integer) this.fails.getOrDefault(uniqueId, Integer.valueOf(0))).intValue() + 1));
            LOGGER.warn("DataForwarder forward error", e);
        }
    }
}