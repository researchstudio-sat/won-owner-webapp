package won.node.camel.processor.fixed;

import java.lang.invoke.MethodHandles;
import java.net.URI;

import javax.persistence.EntityManager;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import won.node.camel.processor.AbstractCamelProcessor;
import won.node.camel.processor.annotation.FixedMessageProcessor;
import won.protocol.exception.IllegalMessageForConnectionStateException;
import won.protocol.message.WonMessage;
import won.protocol.message.processor.camel.WonCamelConstants;
import won.protocol.model.Connection;
import won.protocol.model.ConnectionState;
import won.protocol.util.Prefixer;
import won.protocol.util.RdfUtils;
import won.protocol.vocabulary.WONMSG;

/**
 * User: syim Date: 02.03.2015
 */
@Component
@FixedMessageProcessor(direction = WONMSG.FromExternalString, messageType = WONMSG.ConnectionMessageString)
public class SendMessageFromNodeProcessor extends AbstractCamelProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    @Autowired
    EntityManager entityManager;

    public void process(final Exchange exchange) throws Exception {
        Message message = exchange.getIn();
        WonMessage wonMessage = (WonMessage) message.getHeader(WonCamelConstants.MESSAGE_HEADER);
        URI senderSocket = wonMessage.getSenderSocketURIRequired();
        URI recipientSocket = wonMessage.getRecipientSocketURIRequired();
        Connection con = connectionRepository
                        .findOneBySocketURIAndTargetSocketURIForUpdate(recipientSocket, senderSocket).get();
        entityManager.refresh(con);
        if (con.getState() != ConnectionState.CONNECTED) {
            throw new IllegalMessageForConnectionStateException(con.getConnectionURI(), "CONNECTION_MESSAGE",
                            con.getState());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("received this ConnectionMessage FromExternal:\n{}",
                            RdfUtils.toString(Prefixer.setPrefixes(wonMessage.getCompleteDataset())));
            if (!wonMessage.getForwardedMessageURIs().isEmpty()) {
                logger.debug("This message contains the forwarded message(s) {}", wonMessage.getForwardedMessageURIs());
            }
        }
    }
}
