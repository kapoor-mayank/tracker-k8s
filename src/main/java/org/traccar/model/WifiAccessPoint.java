package org.traccar.model;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class WifiAccessPoint {
    private String macAddress;
    private Integer signalStrength;
    private Integer channel;

    public static WifiAccessPoint from(String macAddress, int signalStrength) {
        WifiAccessPoint wifiAccessPoint = new WifiAccessPoint();
        wifiAccessPoint.setMacAddress(macAddress);
        wifiAccessPoint.setSignalStrength(Integer.valueOf(signalStrength));
        return wifiAccessPoint;
    }

    public static WifiAccessPoint from(String macAddress, int signalStrength, int channel) {
        WifiAccessPoint wifiAccessPoint = from(macAddress, signalStrength);
        wifiAccessPoint.setChannel(Integer.valueOf(channel));
        return wifiAccessPoint;
    }


    public String getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }


    public Integer getSignalStrength() {
        return this.signalStrength;
    }

    public void setSignalStrength(Integer signalStrength) {
        this.signalStrength = signalStrength;
    }


    public Integer getChannel() {
        return this.channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\WifiAccessPoint.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */