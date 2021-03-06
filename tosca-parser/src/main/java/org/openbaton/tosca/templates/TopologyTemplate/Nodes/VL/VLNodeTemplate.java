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

package org.openbaton.tosca.templates.TopologyTemplate.Nodes.VL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openbaton.tosca.templates.TopologyTemplate.Nodes.NodeTemplate;

@SuppressWarnings({"unsafe", "unchecked"})
public class VLNodeTemplate {

  private String type = "";
  private String name = "";
  private String vendor = "";
  private Set<String> qos;

  public VLNodeTemplate(NodeTemplate vl, String name) {

    this.name = name;

    this.type = vl.getType();

    if (vl.getProperties() != null) {

      Map<String, Object> propertiesMap = (Map<String, Object>) vl.getProperties();

      if (propertiesMap.containsKey("vendor")) {

        this.vendor = (String) propertiesMap.get("vendor");
      }
      if (propertiesMap.containsKey("qos")) {
        qos = new HashSet<String>();
        qos.addAll((ArrayList<String>) propertiesMap.get("qos"));
      }
    }
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public String toString() {

    return "VL: "
        + "\n"
        + "type: "
        + type
        + "\n"
        + "name: "
        + name
        + "\n"
        + "vendor: "
        + vendor
        + "\n";
  }

  public Set<String> getQos() {
    return qos;
  }

  public void setQos(Set<String> qos) {
    this.qos = qos;
  }
}
