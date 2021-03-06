package won.owner.protocol.message;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Optional;

import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import won.protocol.exception.WonMessageProcessingException;
import won.protocol.message.WonMessage;
import won.protocol.message.WonMessageType;
import won.protocol.message.processor.WonMessageProcessor;
import won.protocol.util.AtomModelWrapper;
import won.protocol.util.linkeddata.CachingLinkedDataSource;
import won.protocol.util.linkeddata.WonLinkedDataUtils;

/**
 * Removes elements from the linked data cache when certain messages are seen.
 * User: ypanchenko Date: 27.10.2015
 */
public class LinkedDataCacheInvalidator implements WonMessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void setLinkedDataSource(final CachingLinkedDataSource linkedDataSource) {
        this.linkedDataSource = linkedDataSource;
    }

    public void setLinkedDataSourceOnBehalfOfAtom(CachingLinkedDataSource linkedDataSourceOnBehalfOfAtom) {
        this.linkedDataSourceOnBehalfOfAtom = linkedDataSourceOnBehalfOfAtom;
    }

    private CachingLinkedDataSource linkedDataSource;
    private CachingLinkedDataSource linkedDataSourceOnBehalfOfAtom;

    @Override
    public WonMessage process(final WonMessage message) throws WonMessageProcessingException {
        WonMessageType type = message.getMessageType();
        if (type == WonMessageType.SUCCESS_RESPONSE) {
            type = message.getRespondingToMessageType();
        }
        URI webId = message.getRecipientAtomURI();
        if (type.isConnectionSpecificMessage()) {
            Optional<URI> connectionURI = WonLinkedDataUtils.getConnectionURIForIncomingMessage(message,
                            linkedDataSource);
            if (connectionURI.isPresent()) {
                // the cached list of events of the receiver atom for the involved connection
                // should be invalidated, since one more
                // message was created
                logger.debug("invalidating events list for atom " + message.getRecipientAtomURI() + " for connection "
                                + connectionURI.get());
                URI messageContainerUri = WonLinkedDataUtils
                                .getMessageContainerURIforConnectionURI(connectionURI.get(), linkedDataSource);
                invalidate(messageContainerUri, webId);
                if (type.causesConnectionStateChange()) {
                    invalidate(connectionURI.get(), webId);
                }
            }
        }
        if (type.causesNewConnection()) {
            // the list of connections of the receiver atom should be invalidated, since
            // these type
            // of messages mean that the new connection has been created recently
            logger.debug("invalidating connections list for atom " + message.getRecipientAtomURI());
            Dataset atom = linkedDataSource.getDataForResource(message.getRecipientAtomURI());
            AtomModelWrapper wrapper = new AtomModelWrapper(atom);
            URI connectionsListUri = URI.create(wrapper.getConnectionContainerUri());
            invalidate(connectionsListUri, webId);
        }
        if (type.causesAtomStateChange()) {
            invalidate(message.getRecipientAtomURI(), webId);
        }
        return message;
    }

    private void invalidate(URI uri, URI webId) {
        if (uri == null)
            return;
        linkedDataSource.invalidate(uri);
        linkedDataSource.invalidate(uri, webId);
        if (linkedDataSourceOnBehalfOfAtom != linkedDataSource) {
            linkedDataSourceOnBehalfOfAtom.invalidate(uri);
            linkedDataSourceOnBehalfOfAtom.invalidate(uri, webId);
        }
    }
}
