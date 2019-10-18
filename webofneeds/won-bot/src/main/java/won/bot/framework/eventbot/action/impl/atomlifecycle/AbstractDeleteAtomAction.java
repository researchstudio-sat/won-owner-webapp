/*
 * Copyright 2012 Research Studios Austria Forschungsges.m.b.H. Licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package won.bot.framework.eventbot.action.impl.atomlifecycle;

import org.apache.jena.query.Dataset;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.protocol.message.WonMessage;
import won.protocol.message.WonMessageBuilder;
import won.protocol.util.WonRdfUtils;

import java.net.URI;

/**
 * Base class for actions that delete atoms.
 */
public abstract class AbstractDeleteAtomAction extends BaseEventBotAction {
    protected String uriListName;

    public AbstractDeleteAtomAction(EventListenerContext eventListenerContext) {
        this(eventListenerContext, eventListenerContext.getBotContextWrapper().getAtomCreateListName());
    }

    public AbstractDeleteAtomAction(EventListenerContext eventListenerContext, String uriListName) {
        super(eventListenerContext);
        this.uriListName = uriListName;
    }

    /**
     * Builds a delete message for the given atomURI
     * 
     * @param atomURI uri of the atom that should be deleted
     * @throws IllegalArgumentException if the atom could not be retrieved from the
     * node
     * @return delete WonMessage
     */
    protected final WonMessage buildWonMessage(URI atomURI) throws IllegalArgumentException {
        Dataset atomDataset = getEventListenerContext().getLinkedDataSource().getDataForResource(atomURI);
        if (atomDataset == null) {
            throw new IllegalStateException("Cannot delete atom " + atomURI + " : retrieved dataset is null");
        }
        URI wonNodeUri = WonRdfUtils.AtomUtils.getWonNodeURIFromAtom(atomDataset, atomURI);
        URI eventUri = getEventListenerContext().getWonNodeInformationService().generateEventURI(wonNodeUri);
        return WonMessageBuilder.setMessagePropertiesForDeleteFromOwner(eventUri, atomURI, wonNodeUri).build();
    }
}
