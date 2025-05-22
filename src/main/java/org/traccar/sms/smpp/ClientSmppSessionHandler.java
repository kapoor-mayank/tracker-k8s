package org.traccar.sms.smpp;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.util.SmppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientSmppSessionHandler
        extends DefaultSmppSessionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSmppSessionHandler.class);

    private SmppClient smppClient;

    public ClientSmppSessionHandler(SmppClient smppClient) {
        this.smppClient = smppClient;
    }


    public void firePduRequestExpired(PduRequest pduRequest) {
        LOGGER.warn("PDU request expired: " + pduRequest);
    }


    public PduResponse firePduRequestReceived(PduRequest request) {
        PduResponse response;
        try {
            if (request instanceof DeliverSm) {
                boolean isDeliveryReceipt;
                String sourceAddress = ((DeliverSm) request).getSourceAddress().getAddress();
                String message = CharsetUtil.decode(((DeliverSm) request).getShortMessage(), this.smppClient
                        .mapDataCodingToCharset(((DeliverSm) request).getDataCoding()));
                LOGGER.info("SMS Message Received: " + message.trim() + ", Source Address: " + sourceAddress);


                if (this.smppClient.getDetectDlrByOpts()) {
                    isDeliveryReceipt = (request.getOptionalParameters() != null);
                } else {
                    isDeliveryReceipt = SmppUtil.isMessageTypeAnyDeliveryReceipt(((DeliverSm) request).getEsmClass());
                }

                if (!isDeliveryReceipt) {
                    TextMessageEventHandler.handleTextMessage(sourceAddress, message);
                }
            }
            response = request.createResponse();
        } catch (Exception error) {
            LOGGER.warn("SMS receiving error", error);
            response = request.createResponse();
            response.setResultMessage(error.getMessage());
            response.setCommandStatus(255);
        }
        return response;
    }


    public void fireChannelUnexpectedlyClosed() {
        LOGGER.warn("SMPP session channel unexpectedly closed");
        this.smppClient.scheduleReconnect();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\sms\smpp\ClientSmppSessionHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */