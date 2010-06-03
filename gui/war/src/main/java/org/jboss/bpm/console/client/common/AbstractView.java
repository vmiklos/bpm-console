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
package org.jboss.bpm.console.client.common;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.mvc4g.client.ViewInterface;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.LazyPanel;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public abstract class AbstractView extends MosaicPanel
    implements ViewInterface, LazyPanel
{
  private AbstractImagePrototype icon;

  protected boolean initialized;
  
  protected AbstractView()
  {
  }

  protected AbstractImagePrototype getIcon()
  {
    return icon;
  }

  protected void setIcon(AbstractImagePrototype icon)
  {
    this.icon = icon;
  }

  public String getIconTitle()
  {
    HTML html = new HTML(
        (icon != null ? (icon.getHTML() + "&nbsp;")
            : (""))
            + this.getTitle());

    return html.toString();
  }
}
