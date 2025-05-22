package org.traccar.model;

import org.traccar.database.QueryIgnore;

public class Server
        extends ExtendedModel {
    private boolean registration;
    private boolean readonly;
    private boolean deviceReadonly;
    private String map;
    private String bingKey;
    private String mapUrl;
    private double latitude;
    private double longitude;
    private int zoom;
    private boolean twelveHourFormat;
    private boolean forceSettings;
    private String coordinateFormat;
    private boolean limitCommands;
    private String poiLayer;

    @QueryIgnore
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }


    public void setVersion(String version) {
    }


    public boolean getRegistration() {
        return this.registration;
    }

    public void setRegistration(boolean registration) {
        this.registration = registration;
    }


    public boolean getReadonly() {
        return this.readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }


    public boolean getDeviceReadonly() {
        return this.deviceReadonly;
    }

    public void setDeviceReadonly(boolean deviceReadonly) {
        this.deviceReadonly = deviceReadonly;
    }


    public String getMap() {
        return this.map;
    }

    public void setMap(String map) {
        this.map = map;
    }


    public String getBingKey() {
        return this.bingKey;
    }

    public void setBingKey(String bingKey) {
        this.bingKey = bingKey;
    }


    public String getMapUrl() {
        return this.mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }


    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    public int getZoom() {
        return this.zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }


    public boolean getTwelveHourFormat() {
        return this.twelveHourFormat;
    }

    public void setTwelveHourFormat(boolean twelveHourFormat) {
        this.twelveHourFormat = twelveHourFormat;
    }


    public boolean getForceSettings() {
        return this.forceSettings;
    }

    public void setForceSettings(boolean forceSettings) {
        this.forceSettings = forceSettings;
    }


    public String getCoordinateFormat() {
        return this.coordinateFormat;
    }

    public void setCoordinateFormat(String coordinateFormat) {
        this.coordinateFormat = coordinateFormat;
    }


    public boolean getLimitCommands() {
        return this.limitCommands;
    }

    public void setLimitCommands(boolean limitCommands) {
        this.limitCommands = limitCommands;
    }


    public String getPoiLayer() {
        return this.poiLayer;
    }

    public void setPoiLayer(String poiLayer) {
        this.poiLayer = poiLayer;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\Server.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */