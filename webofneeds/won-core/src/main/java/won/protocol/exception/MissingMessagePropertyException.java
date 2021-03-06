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
package won.protocol.exception;

import java.net.URI;

import org.apache.jena.rdf.model.Property;

/**
 * Exception indicating a missing message property.
 */
public class MissingMessagePropertyException extends WonMessageNotWellFormedException {
    private URI missingProperty;

    private static String createExceptionMessage(URI missingProperty) {
        return String.format("Missing message property: %s", missingProperty);
    }

    public MissingMessagePropertyException(String missingProperty) {
        this(URI.create(missingProperty));
    }

    public MissingMessagePropertyException(Property property) {
        this(property.toString());
    }

    public MissingMessagePropertyException(URI missingProperty) {
        super(createExceptionMessage(missingProperty));
        this.missingProperty = missingProperty;
    }

    public MissingMessagePropertyException(Throwable cause, URI missingProperty) {
        super(cause);
        this.missingProperty = missingProperty;
    }

    public URI getMissingProperty() {
        return missingProperty;
    }
}
