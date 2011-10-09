/**
 * Copyright 2011 Miklos Vajna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.bpm.console.client.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "wrapper")
public class NodeInstanceRefWrapper
{

   List<NodeInstanceRef> instances;

   public NodeInstanceRefWrapper()
   {
   }

   public NodeInstanceRefWrapper(List<NodeInstanceRef> instances)
   {
      this.instances = instances;
   }

   @XmlElement
   public List<NodeInstanceRef> getInstances()
   {
      return instances;
   }

   public void setInstances(List<NodeInstanceRef> instances)
   {
      this.instances = instances;
   }
}
