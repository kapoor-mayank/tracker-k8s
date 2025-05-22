package org.traccar.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Collection;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class Network {
    private Integer homeMobileCountryCode;
    private Integer homeMobileNetworkCode;

    public Network() {
    }

    public Network(CellTower cellTower) {
        addCellTower(cellTower);
    }


    public Integer getHomeMobileCountryCode() {
        return this.homeMobileCountryCode;
    }

    public void setHomeMobileCountryCode(Integer homeMobileCountryCode) {
        this.homeMobileCountryCode = homeMobileCountryCode;
    }


    public Integer getHomeMobileNetworkCode() {
        return this.homeMobileNetworkCode;
    }

    public void setHomeMobileNetworkCode(Integer homeMobileNetworkCode) {
        this.homeMobileNetworkCode = homeMobileNetworkCode;
    }

    private String radioType = "gsm";
    private String carrier;

    public String getRadioType() {
        return this.radioType;
    }

    public void setRadioType(String radioType) {
        this.radioType = radioType;
    }


    public String getCarrier() {
        return this.carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    private Boolean considerIp = Boolean.valueOf(false);
    private Collection<CellTower> cellTowers;

    public Boolean getConsiderIp() {
        return this.considerIp;
    }

    private Collection<WifiAccessPoint> wifiAccessPoints;

    public void setConsiderIp(Boolean considerIp) {
        this.considerIp = considerIp;
    }


    public Collection<CellTower> getCellTowers() {
        return this.cellTowers;
    }

    public void setCellTowers(Collection<CellTower> cellTowers) {
        this.cellTowers = cellTowers;
    }

    public void addCellTower(CellTower cellTower) {
        if (this.cellTowers == null) {
            this.cellTowers = new ArrayList<>();
        }
        this.cellTowers.add(cellTower);
    }


    public Collection<WifiAccessPoint> getWifiAccessPoints() {
        return this.wifiAccessPoints;
    }

    public void setWifiAccessPoints(Collection<WifiAccessPoint> wifiAccessPoints) {
        this.wifiAccessPoints = wifiAccessPoints;
    }

    public void addWifiAccessPoint(WifiAccessPoint wifiAccessPoint) {
        if (this.wifiAccessPoints == null) {
            this.wifiAccessPoints = new ArrayList<>();
        }
        this.wifiAccessPoints.add(wifiAccessPoint);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\Network.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */