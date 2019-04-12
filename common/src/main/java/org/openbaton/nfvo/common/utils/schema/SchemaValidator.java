/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.common.utils.schema;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.util.Set;
import org.openbaton.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaValidator {

  private static Logger log = LoggerFactory.getLogger(SchemaValidator.class);

  private static JsonNode getJsonNodeFromStringContent(String content)
      throws BadRequestException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node;
    try {
      node = mapper.readTree(content);
    } catch (JsonParseException e) {
      log.error(e.getMessage());
      throw new BadRequestException(e);
    }
    return node;
  }

  private static JsonSchema getJsonSchemaFromStringContent(String schemaContent) {
    JsonSchemaFactory factory = new JsonSchemaFactory();
    return factory.getSchema(schemaContent);
  }

  public static Set<ValidationMessage> validateSchema(Class jsonClassSchema, String jsonInstance)
      throws BadRequestException, IOException {

    JsonSchema finalSchema =
        getJsonSchemaFromStringContent(getJsonSchemaFromClass(jsonClassSchema));
    JsonNode node = getJsonNodeFromStringContent(jsonInstance);

    return finalSchema.validate(node);
  }

  @SuppressWarnings("unchecked")
  private static String getJsonSchemaFromClass(Class javaClass) throws IOException {

    ObjectMapper mapper = new ObjectMapper();
    JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
    JsonNode jsonSchema = schemaGen.generateJsonSchema(javaClass);
    String jsonSchemaAsString = mapper.writeValueAsString(jsonSchema);
    log.trace("The schema is: " + jsonSchemaAsString);
    return jsonSchemaAsString;
  }
}
