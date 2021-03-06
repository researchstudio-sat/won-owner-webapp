/*
 * Copyright 2017 Research Studios Austria Forschungsges.m.b.H. Licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package won.bot.framework.eventbot.action.impl.factory;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import won.bot.framework.bot.context.FactoryBotContextWrapper;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.factory.FactoryHintEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.HintFromMatcherEvent;
import won.bot.framework.eventbot.listener.EventListener;

/**
 * Checks if the received hint is for a factoryURI
 */
public class FactoryHintCheckAction extends BaseEventBotAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public FactoryHintCheckAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        if (!(getEventListenerContext().getBotContextWrapper() instanceof FactoryBotContextWrapper)) {
            logger.error("FactoryHintCheckAction can only work for FactoryBotContextWrappers");
        } else if (!(event instanceof HintFromMatcherEvent)) {
            logger.error("FactoryHintCheckAction can only handle HintFromMatcherEvent");
            return;
        }
        FactoryBotContextWrapper botContextWrapper = (FactoryBotContextWrapper) getEventListenerContext()
                        .getBotContextWrapper();
        Optional<URI> ownUri = Optional.of(((HintFromMatcherEvent) event).getRecipientAtom());
        Optional<URI> requesterUri = Optional.of(((HintFromMatcherEvent) event).getHintTargetAtom());
        if (ownUri.isPresent() && requesterUri.isPresent() && botContextWrapper.isFactoryAtom(ownUri.get())) {
            logger.debug("FactoryHint for factoryURI: " + ownUri.get() + " from the requesterUri: "
                            + requesterUri.get());
            EventBus bus = getEventListenerContext().getEventBus();
            bus.publish(new FactoryHintEvent(requesterUri.get(), ownUri.get()));
        } else {
            logger.warn("NON FactoryHint for URI: " + ownUri + " from the requesterUri: " + requesterUri
                            + " ignore the hint");
        }
    }
}
