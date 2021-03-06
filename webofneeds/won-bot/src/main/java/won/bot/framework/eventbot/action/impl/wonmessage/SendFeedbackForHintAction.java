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
package won.bot.framework.eventbot.action.impl.wonmessage;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.AtomHintFromMatcherEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.exception.WonMessageBuilderException;
import won.protocol.message.WonMessage;
import won.protocol.message.builder.WonMessageBuilder;
import won.protocol.util.linkeddata.WonLinkedDataUtils;

/**
 * User: fkleedorfer Date: 30.01.14
 */
public class SendFeedbackForHintAction extends BaseEventBotAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    // random number generator needed for random feedback value
    Random random = new Random(System.currentTimeMillis());

    public SendFeedbackForHintAction(final EventListenerContext context) {
        super(context);
    }

    @Override
    public void doRun(final Event event, EventListener executingListener) throws Exception {
        if (event instanceof AtomHintFromMatcherEvent) {
            // TODO: the hint with a match object is not really suitable here. Would be
            // better to
            // use connection object instead
            AtomHintFromMatcherEvent hintEvent = (AtomHintFromMatcherEvent) event;
            boolean feedbackValue = random.nextBoolean();
            WonMessage message = createFeedbackMessage(WonLinkedDataUtils.getConnectionURIForIncomingMessage(
                            hintEvent.getWonMessage(), getEventListenerContext().getLinkedDataSource())
                            .orElseThrow(() -> new IllegalStateException("Could not obtain connection URI for "
                                            + hintEvent.getWonMessage().toShortStringForDebug())),
                            feedbackValue);
            message = getEventListenerContext().getWonMessageSender().prepareMessage(message);
            logger.debug("sending {} feedback for hint {} in message {}",
                            new Object[] { (feedbackValue ? "positive" : "negative"), event, message.getMessageURI() });
            getEventListenerContext().getWonMessageSender().sendMessage(message);
        }
    }

    private WonMessage createFeedbackMessage(URI connectionURI, boolean booleanFeedbackValue)
                    throws WonMessageBuilderException {
        return WonMessageBuilder
                        .hintFeedbackMessage()
                        .connection(connectionURI)
                        .binaryFeedback(booleanFeedbackValue)
                        .build();
    }
}
