package org.traccar.model;

public class WiFiData {
    private String macAddress;
    private int signalStrength;
    private int channelNum;

    public WiFiData(String macAddress, int signalStrength, int channelNum) {
        this.macAddress = macAddress;
        this.signalStrength = signalStrength;
        this.channelNum = channelNum;
    }

    // Getters and toString method for logging/debugging
    public String getMacAddress() {
        return macAddress;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public int getChannelNum() {
        return channelNum;
    }

//    @Override
//    public String toString() {
//        return "{" +
//                "macAddress='" + macAddress + '\'' +
//                ", signalStrength=" + signalStrength +
//                ", channelNum=" + channelNum +
//                '}';
//    }
//    Added if needed to change WiFi information to proper JSON
    @Override
    public String toString() {
        return "{" +
                "\"macAddress\": \"" + macAddress + "\"," +
                "\"signalStrength\": " + signalStrength + "," +
                "\"channelNum\": " + channelNum +
                "}";
    }
}

