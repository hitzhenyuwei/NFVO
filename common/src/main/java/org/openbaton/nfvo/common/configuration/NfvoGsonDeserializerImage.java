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

package org.openbaton.nfvo.common.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.images.DockerImage;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NfvoGsonDeserializerImage implements JsonDeserializer<BaseNfvImage> {

  private Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public BaseNfvImage deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    if (json.getAsJsonObject().has("tags")) {
      return gson.fromJson(json, DockerImage.class);
    } else if (json.getAsJsonObject().has("diskFormat")) {
      return gson.fromJson(json, NFVImage.class);
    } else return gson.fromJson(json, BaseNfvImage.class);
  }
}
