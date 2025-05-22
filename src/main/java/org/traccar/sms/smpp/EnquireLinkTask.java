package org.traccar.sms.smpp;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EnquireLinkTask
        implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnquireLinkTask.class);

    private SmppClient smppClient;
    private Integer enquireLinkTimeout;

    public EnquireLinkTask(SmppClient smppClient, Integer enquireLinkTimeout) {
        this.smppClient = smppClient;
        this.enquireLinkTimeout = enquireLinkTimeout;
    }


    public void run() {
        SmppSession smppSession = this.smppClient.getSession();
        if (smppSession != null && smppSession.isBound()) {
            try {
                smppSession.enquireLink(new EnquireLink(), this.enquireLinkTimeout.intValue());
            } catch (SmppTimeoutException | com.cloudhopper.smpp.type.SmppChannelException |
                     com.cloudhopper.smpp.type.RecoverablePduException |
                     com.cloudhopper.smpp.type.UnrecoverablePduException error) {

                LOGGER.warn("Enquire link failed, executing reconnect: ", error);
                this.smppClient.scheduleReconnect();
            } catch (InterruptedException error) {
                LOGGER.info("Enquire link interrupted, probably killed by reconnecting");
            }
        } else {
            LOGGER.warn("Enquire link running while session is not connected");
        }
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\sms\smpp\EnquireLinkTask.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */