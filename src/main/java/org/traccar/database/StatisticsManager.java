package org.traccar.database;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.config.Config;
import org.traccar.config.Keys;
import org.traccar.helper.DateUtil;
import org.traccar.model.BaseModel;
import org.traccar.model.Statistics;


public class StatisticsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsManager.class);

    private static final int SPLIT_MODE = 5;

    private final Config config;

    private final DataManager dataManager;
    private final Client client;
    private AtomicInteger lastUpdate = new AtomicInteger(Calendar.getInstance().get(5));

    private Set<Long> users = new HashSet<>();
    private Set<Long> devices = new HashSet<>();

    private int requests;
    private int messagesReceived;
    private int messagesStored;
    private int mailSent;
    private int smsSent;
    private int geocoderRequests;
    private int geolocationRequests;

    @Inject
    public StatisticsManager(Config config, DataManager dataManager, Client client) {
        this.config = config;
        this.dataManager = dataManager;
        this.client = client;
    }

    private void checkSplit() {
        int currentUpdate = Calendar.getInstance().get(5);
        if (this.lastUpdate.getAndSet(currentUpdate) != currentUpdate) {
            Statistics statistics = new Statistics();
            statistics.setCaptureTime(new Date());
            statistics.setActiveUsers(this.users.size());
            statistics.setActiveDevices(this.devices.size());
            statistics.setRequests(this.requests);
            statistics.setMessagesReceived(this.messagesReceived);
            statistics.setMessagesStored(this.messagesStored);
            statistics.setMailSent(this.mailSent);
            statistics.setSmsSent(this.smsSent);
            statistics.setGeocoderRequests(this.geocoderRequests);
            statistics.setGeolocationRequests(this.geolocationRequests);

            try {
                this.dataManager.addObject((BaseModel) statistics);
            } catch (SQLException e) {
                LOGGER.warn("Error saving statistics", e);
            }

            String url = this.config.getString(Keys.SERVER_STATISTICS);
            if (url != null) {
                String time = DateUtil.formatDate(statistics.getCaptureTime());

                Form form = new Form();
                form.param("version", getClass().getPackage().getImplementationVersion());
                form.param("captureTime", time);
                form.param("activeUsers", String.valueOf(statistics.getActiveUsers()));
                form.param("activeDevices", String.valueOf(statistics.getActiveDevices()));
                form.param("requests", String.valueOf(statistics.getRequests()));
                form.param("messagesReceived", String.valueOf(statistics.getMessagesReceived()));
                form.param("messagesStored", String.valueOf(statistics.getMessagesStored()));
                form.param("mailSent", String.valueOf(statistics.getMailSent()));
                form.param("smsSent", String.valueOf(statistics.getSmsSent()));
                form.param("geocoderRequests", String.valueOf(statistics.getGeocoderRequests()));
                form.param("geolocationRequests", String.valueOf(statistics.getGeolocationRequests()));

                this.client.target(url).request().async().post(Entity.form(form));
            }

            this.users.clear();
            this.devices.clear();
            this.requests = 0;
            this.messagesReceived = 0;
            this.messagesStored = 0;
            this.mailSent = 0;
            this.smsSent = 0;
            this.geocoderRequests = 0;
            this.geolocationRequests = 0;
        }
    }

    public synchronized void registerRequest(long userId) {
        checkSplit();
        this.requests++;
        if (userId != 0L) {
            this.users.add(Long.valueOf(userId));
        }
    }

    public synchronized void registerMessageReceived() {
        checkSplit();
        this.messagesReceived++;
    }

    public synchronized void registerMessageStored(long deviceId) {
        checkSplit();
        this.messagesStored++;
        if (deviceId != 0L) {
            this.devices.add(Long.valueOf(deviceId));
        }
    }

    public synchronized void registerMail() {
        checkSplit();
        this.mailSent++;
    }

    public synchronized void registerSms() {
        checkSplit();
        this.smsSent++;
    }

    public synchronized void registerGeocoderRequest() {
        checkSplit();
        this.geocoderRequests++;
    }

    public synchronized void registerGeolocationRequest() {
        checkSplit();
        this.geolocationRequests++;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\StatisticsManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */