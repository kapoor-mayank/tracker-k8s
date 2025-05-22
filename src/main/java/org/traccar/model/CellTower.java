package org.traccar.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.traccar.Context;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class CellTower {
    private String radioType;
    private Long cellId;
    private Integer locationAreaCode;
    private Integer mobileCountryCode;
    private Integer mobileNetworkCode;
    private Integer signalStrength;

    public static CellTower from(int mcc, int mnc, int lac, long cid) {
        CellTower cellTower = new CellTower();
        cellTower.setMobileCountryCode(Integer.valueOf(mcc));
        cellTower.setMobileNetworkCode(Integer.valueOf(mnc));
        cellTower.setLocationAreaCode(Integer.valueOf(lac));
        cellTower.setCellId(Long.valueOf(cid));
        return cellTower;
    }

    public static CellTower from(int mcc, int mnc, int lac, long cid, int rssi) {
        CellTower cellTower = from(mcc, mnc, lac, cid);
        cellTower.setSignalStrength(Integer.valueOf(rssi));
        return cellTower;
    }

    public static CellTower fromLacCid(int lac, long cid) {
        return from(
                Context.getConfig().getInteger("geolocation.mcc"),
                Context.getConfig().getInteger("geolocation.mnc"), lac, cid);
    }

    public static CellTower fromCidLac(long cid, int lac) {
        return fromLacCid(lac, cid);
    }


    public String getRadioType() {
        return this.radioType;
    }

    public void setRadioType(String radioType) {
        this.radioType = radioType;
    }


    public Long getCellId() {
        return this.cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
    }


    public Integer getLocationAreaCode() {
        return this.locationAreaCode;
    }

    public void setLocationAreaCode(Integer locationAreaCode) {
        this.locationAreaCode = locationAreaCode;
    }


    public Integer getMobileCountryCode() {
        return this.mobileCountryCode;
    }

    public void setMobileCountryCode(Integer mobileCountryCode) {
        this.mobileCountryCode = mobileCountryCode;
    }


    public Integer getMobileNetworkCode() {
        return this.mobileNetworkCode;
    }

    public void setMobileNetworkCode(Integer mobileNetworkCode) {
        this.mobileNetworkCode = mobileNetworkCode;
    }


    public Integer getSignalStrength() {
        return this.signalStrength;
    }

    public void setSignalStrength(Integer signalStrength) {
        this.signalStrength = signalStrength;
    }

    public void setOperator(long operator) {
        String operatorString = String.valueOf(operator);
        this.mobileCountryCode = Integer.valueOf(Integer.parseInt(operatorString.substring(0, 3)));
        this.mobileNetworkCode = Integer.valueOf(Integer.parseInt(operatorString.substring(3)));
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\CellTower.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */