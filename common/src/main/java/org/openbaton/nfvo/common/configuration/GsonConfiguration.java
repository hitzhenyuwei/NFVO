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
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/** Created by lto on 10/11/15. */
@Configuration
public class GsonConfiguration {

  @Bean
  @Scope("prototype")
  Gson vnfmGson() {
    return new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(NFVMessage.class, new VnfmGsonDeserializerNFVMessage())
        .registerTypeAdapter(BaseVimInstance.class, new NfvoGsonDeserializerVimInstance())
        .registerTypeAdapter(BaseVimInstance.class, new NfvoGsonSerializerVimInstance())
        .create();
  }
}
