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
package won.node.camel.processor.fixed;

import static won.node.camel.service.WonCamelHelper.*;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import won.node.camel.processor.AbstractCamelProcessor;
import won.node.camel.processor.annotation.FixedMessageProcessor;
import won.protocol.exception.IllegalMessageForConnectionStateException;
import won.protocol.model.Connection;
import won.protocol.model.ConnectionState;
import won.protocol.vocabulary.WONMSG;

@Component
@FixedMessageProcessor(direction = WONMSG.FromSystemString, messageType = WONMSG.ConnectionMessageString)
public class SendMessageFromSystemProcessor extends AbstractCamelProcessor {
    public void process(final Exchange exchange) throws Exception {
        Connection con = connectionService.getConnectionForMessageRequired(getMessageRequired(exchange),
                        getDirectionRequired(exchange));
        if (con.getState() != ConnectionState.CONNECTED) {
            throw new IllegalMessageForConnectionStateException(con.getConnectionURI(), "CONNECTION_MESSAGE",
                            con.getState());
        }
    }
}
