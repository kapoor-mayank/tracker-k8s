package org.traccar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

import org.traccar.database.QueryExtended;
import org.traccar.database.QueryIgnore;
import org.traccar.helper.Hashing;


public class User
        extends ExtendedModel {
    private String name;
    private String login;
    private String email;
    private String phone;
    private boolean readonly;
    private boolean administrator;
    private String map;
    private double latitude;
    private double longitude;
    private int zoom;
    private boolean twelveHourFormat;

    public String getName() {
        return this.name;
    }

    private String coordinateFormat;
    private boolean disabled;
    private Date expirationTime;
    private int deviceLimit;
    private int userLimit;
    private boolean deviceReadonly;
    private String token;
    private boolean limitCommands;
    private String poiLayer;
    private String hashedPassword;
    private String salt;

    public void setName(String name) {
        this.name = name;
    }


    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }


    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }


    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public boolean getReadonly() {
        return this.readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }


    public boolean getAdministrator() {
        return this.administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }


    public String getMap() {
        return this.map;
    }

    public void setMap(String map) {
        this.map = map;
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


    public String getCoordinateFormat() {
        return this.coordinateFormat;
    }

    public void setCoordinateFormat(String coordinateFormat) {
        this.coordinateFormat = coordinateFormat;
    }


    public boolean getDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }


    public Date getExpirationTime() {
        return this.expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }


    public int getDeviceLimit() {
        return this.deviceLimit;
    }

    public void setDeviceLimit(int deviceLimit) {
        this.deviceLimit = deviceLimit;
    }


    public int getUserLimit() {
        return this.userLimit;
    }

    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }


    public boolean getDeviceReadonly() {
        return this.deviceReadonly;
    }

    public void setDeviceReadonly(boolean deviceReadonly) {
        this.deviceReadonly = deviceReadonly;
    }


    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        if (token != null && !token.isEmpty()) {
            if (!token.matches("^[a-zA-Z0-9-]{16,}$")) {
                throw new IllegalArgumentException("Illegal token");
            }
            this.token = token;
        } else {
            this.token = null;
        }
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

    @QueryIgnore
    public String getPassword() {
        return null;
    }

    public void setPassword(String password) {
        if (password != null && !password.isEmpty()) {
            Hashing.HashingResult hashingResult = Hashing.createHash(password);
            this.hashedPassword = hashingResult.getHash();
            this.salt = hashingResult.getSalt();
        }
    }


    @JsonIgnore
    @QueryExtended
    public String getHashedPassword() {
        return this.hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }


    @JsonIgnore
    @QueryExtended
    public String getSalt() {
        return this.salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public boolean isPasswordValid(String password) {
        return Hashing.validatePassword(password, this.hashedPassword, this.salt);
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\User.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */