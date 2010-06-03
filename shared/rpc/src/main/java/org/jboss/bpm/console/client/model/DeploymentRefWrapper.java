/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.bpm.console.client.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
@XmlRootElement(name = "wrapper")
public class DeploymentRefWrapper
{
  List<DeploymentRef> deployments = new ArrayList<DeploymentRef>();


  public DeploymentRefWrapper()
  {
  }

  public DeploymentRefWrapper(List<DeploymentRef> deployments)
  {
    this.deployments = deployments;
  }

  public List<DeploymentRef> getDeployments()
  {
    return deployments;
  }

  public void setDeployments(List<DeploymentRef> deployments)
  {
    this.deployments = deployments;
  }

  public int getTotalCount()
  {
    return deployments.size();
  }
}
