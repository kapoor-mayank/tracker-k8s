package org.traccar.sms.smpp;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.SmppSessionHandler;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.Context;
import org.traccar.notification.MessageException;
import org.traccar.sms.SmsManager;


public class SmppClient
        implements SmsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmppClient.class);

    private SmppSessionConfiguration sessionConfig = new SmppSessionConfiguration();
    private SmppSession smppSession;
    private DefaultSmppSessionHandler sessionHandler = new ClientSmppSessionHandler(this);
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private DefaultSmppClient clientBootstrap = new DefaultSmppClient();

    private ScheduledExecutorService enquireLinkExecutor;

    private ScheduledFuture<?> enquireLinkTask;

    private Integer enquireLinkPeriod;

    private Integer enquireLinkTimeout;

    private ScheduledExecutorService reconnectionExecutor;
    private ScheduledFuture<?> reconnectionTask;
    private Integer reconnectionDelay;
    private String sourceAddress;
    private String commandSourceAddress;
    private int submitTimeout;
    private boolean requestDlr;
    private boolean detectDlrByOpts;
    private String notificationsCharsetName;
    private byte notificationsDataCoding;
    private String commandsCharsetName;
    private byte commandsDataCoding;
    private byte sourceTon;
    private byte sourceNpi;
    private byte commandSourceTon;
    private byte commandSourceNpi;
    private byte destTon;
    private byte destNpi;

    public SmppClient() {
        this.sessionConfig.setName("Traccar.smppSession");
        this.sessionConfig.setInterfaceVersion(
                (byte) Context.getConfig().getInteger("sms.smpp.version", 52));
        this.sessionConfig.setType(SmppBindType.TRANSCEIVER);
        this.sessionConfig.setHost(Context.getConfig().getString("sms.smpp.host", "localhost"));
        this.sessionConfig.setPort(Context.getConfig().getInteger("sms.smpp.port", 2775));
        this.sessionConfig.setSystemId(Context.getConfig().getString("sms.smpp.username", "user"));
        this.sessionConfig.setSystemType(Context.getConfig().getString("sms.smpp.systemType", null));
        this.sessionConfig.setPassword(Context.getConfig().getString("sms.smpp.password", "password"));
        this.sessionConfig.getLoggingOptions().setLogBytes(false);
        this.sessionConfig.getLoggingOptions().setLogPdu(Context.getConfig().getBoolean("sms.smpp.logPdu"));

        this.sourceAddress = Context.getConfig().getString("sms.smpp.sourceAddress", "");
        this.commandSourceAddress = Context.getConfig().getString("sms.smpp.commandSourceAddress", this.sourceAddress);
        this.submitTimeout = Context.getConfig().getInteger("sms.smpp.submitTimeout", 10000);

        this.requestDlr = Context.getConfig().getBoolean("sms.smpp.requestDlr");
        this.detectDlrByOpts = Context.getConfig().getBoolean("sms.smpp.detectDlrByOpts");

        this.notificationsCharsetName = Context.getConfig().getString("sms.smpp.notificationsCharset", "UCS-2");

        this.notificationsDataCoding = (byte) Context.getConfig().getInteger("sms.smpp.notificationsDataCoding", 8);

        this.commandsCharsetName = Context.getConfig().getString("sms.smpp.commandsCharset", "GSM");

        this.commandsDataCoding = (byte) Context.getConfig().getInteger("sms.smpp.commandsDataCoding", 0);


        this.sourceTon = (byte) Context.getConfig().getInteger("sms.smpp.sourceTon", 5);
        this.commandSourceTon = (byte) Context.getConfig().getInteger("sms.smpp.commandSourceTon", this.sourceTon);
        this.sourceNpi = (byte) Context.getConfig().getInteger("sms.smpp.sourceNpi", 0);
        this.commandSourceNpi = (byte) Context.getConfig().getInteger("sms.smpp.commandSourceNpi", this.sourceNpi);

        this.destTon = (byte) Context.getConfig().getInteger("sms.smpp.destTon", 1);
        this.destNpi = (byte) Context.getConfig().getInteger("sms.smpp.destNpi", 1);

        this.enquireLinkPeriod = Integer.valueOf(Context.getConfig().getInteger("sms.smpp.enquireLinkPeriod", 60000));
        this.enquireLinkTimeout = Integer.valueOf(Context.getConfig().getInteger("sms.smpp.enquireLinkTimeout", 10000));
        this.enquireLinkExecutor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                String name = SmppClient.this.sessionConfig.getName();
                thread.setName("EnquireLink-" + name);
                return thread;
            }
        });

        this.reconnectionDelay = Integer.valueOf(Context.getConfig().getInteger("sms.smpp.reconnectionDelay", 10000));
        this.reconnectionExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                String name = SmppClient.this.sessionConfig.getName();
                thread.setName("Reconnection-" + name);
                return thread;
            }
        });

        scheduleReconnect();
    }

    public synchronized SmppSession getSession() {
        return this.smppSession;
    }

    public String mapDataCodingToCharset(byte dataCoding) {
        switch (dataCoding) {
            case 3:
                return "ISO-8859-1";
            case 8:
                return "UCS-2";
        }
        return "GSM";
    }


    public boolean getDetectDlrByOpts() {
        return this.detectDlrByOpts;
    }

    protected synchronized void reconnect() {
        try {
            disconnect();
            this.smppSession = this.clientBootstrap.bind(this.sessionConfig, (SmppSessionHandler) this.sessionHandler);
            stopReconnectionkTask();
            runEnquireLinkTask();
            LOGGER.info("SMPP session connected");
        } catch (SmppTimeoutException | SmppChannelException | com.cloudhopper.smpp.type.UnrecoverablePduException |
                 InterruptedException error) {

            LOGGER.warn("Unable to connect to SMPP server: ", error);
        }
    }

    public void scheduleReconnect() {
        if (this.reconnectionTask == null || this.reconnectionTask.isDone()) {
            this.reconnectionTask = this.reconnectionExecutor.scheduleWithFixedDelay(new ReconnectionTask(this), this.reconnectionDelay

                    .intValue(), this.reconnectionDelay.intValue(), TimeUnit.MILLISECONDS);
        }
    }

    private void stopReconnectionkTask() {
        if (this.reconnectionTask != null) {
            this.reconnectionTask.cancel(false);
        }
    }

    private void disconnect() {
        stopEnquireLinkTask();
        destroySession();
    }

    private void runEnquireLinkTask() {
        this.enquireLinkTask = this.enquireLinkExecutor.scheduleWithFixedDelay(new EnquireLinkTask(this, this.enquireLinkTimeout), this.enquireLinkPeriod

                .intValue(), this.enquireLinkPeriod.intValue(), TimeUnit.MILLISECONDS);
    }

    private void stopEnquireLinkTask() {
        if (this.enquireLinkTask != null) {
            this.enquireLinkTask.cancel(true);
        }
    }

    private void destroySession() {
        if (this.smppSession != null) {
            LOGGER.info("Cleaning up SMPP session... ");
            this.smppSession.destroy();
            this.smppSession = null;
        }
    }


    public synchronized void sendMessageSync(String destAddress, String message, boolean command) throws MessageException, InterruptedException, IllegalStateException {
        if (getSession() != null && getSession().isBound()) {
            try {
                SubmitSm submit = new SubmitSm();

                byte[] textBytes = CharsetUtil.encode(message, command ? this.commandsCharsetName : this.notificationsCharsetName);
                submit.setDataCoding(command ? this.commandsDataCoding : this.notificationsDataCoding);
                if (this.requestDlr) {
                    submit.setRegisteredDelivery((byte) 1);
                }

                if (textBytes != null && textBytes.length > 255) {
                    submit.addOptionalParameter(new Tlv((short) 1060, textBytes, "message_payload"));
                } else {

                    submit.setShortMessage(textBytes);
                }

                submit.setSourceAddress(command ? new Address(this.commandSourceTon, this.commandSourceNpi, this.commandSourceAddress) : new Address(this.sourceTon, this.sourceNpi, this.sourceAddress));

                submit.setDestAddress(new Address(this.destTon, this.destNpi, destAddress));
                SubmitSmResp submitResponce = getSession().submit(submit, this.submitTimeout);
                if (submitResponce.getCommandStatus() == 0) {
                    LOGGER.info("SMS submitted, message id: " + submitResponce.getMessageId());
                } else {
                    throw new IllegalStateException(submitResponce.getResultMessage());
                }
            } catch (SmppChannelException | com.cloudhopper.smpp.type.RecoverablePduException | SmppTimeoutException |
                     com.cloudhopper.smpp.type.UnrecoverablePduException error) {

                throw new MessageException(error);
            }
        } else {
            throw new MessageException(new SmppChannelException("SMPP session is not connected"));
        }
    }


    public void sendMessageAsync(final String destAddress, final String message, final boolean command) {
        this.executorService.execute(new Runnable() {
            public void run() {
                try {
                    SmppClient.this.sendMessageSync(destAddress, message, command);
                } catch (MessageException | InterruptedException | IllegalStateException error) {
                    SmppClient.LOGGER.warn("SMS sending error", error);
                }
            }
        });
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\sms\smpp\SmppClient.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */