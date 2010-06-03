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
package org.jboss.bpm.console.client.engine;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import org.gwt.mosaic.ui.client.*;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.common.PropertyGrid;
import org.jboss.bpm.console.client.model.DeploymentRef;

/**
 * Detail panel associated with a selected deployment.
 *
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class DeploymentDetailView extends CaptionLayoutPanel implements ViewInterface
{
  public final static String ID = DeploymentDetailView.class.getName();

  private Controller controller;

  private PropertyGrid grid;

  private DeploymentRef currentDeployment;

  private ResourcePanel resourcePanel;

  Button suspendBtn;
  Button resumeBtn;
  
  public DeploymentDetailView()
  {
    super("Deployment details");
    super.setStyleName("bpm-detail-panel");

    grid = new PropertyGrid(new String[] {"ID:", "Name:", "Processes:"});
    MosaicPanel propLayout = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
    propLayout.add(grid, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));


    suspendBtn =  new Button("Retire", new ClickHandler() {

      public void onClick(ClickEvent clickEvent)
      {

        DeploymentRef deploymentRef = getSelection();
        if(deploymentRef!=null)
        {
          MessageBox.confirm("Retire deployment",
              "Do you want to retire this deployment? Any associated process will be suspended.",
              new MessageBox.ConfirmationCallback() {
                public void onResult(boolean doIt)
                {
                  if(doIt)
                  {
                    controller.handleEvent(
                        new Event(
                            SuspendDeploymentAction.ID,
                            getSelection().getId()
                        )
                    );
                  }
                }
              });
        }
        else
        {
          MessageBox.alert("Missing selection", "Please select a deployment");
        }
      }
    }
    );

    resumeBtn =  new Button("Activate", new ClickHandler() {
      public void onClick(ClickEvent clickEvent)
      {        
        DeploymentRef deploymentRef = getSelection();
        if(deploymentRef!=null)
        {
          MessageBox.confirm("Activate deployment",
              "Do you want to resume this deployment?",
              new MessageBox.ConfirmationCallback() {
                public void onResult(boolean doIt)
                {
                  if(doIt)
                  {
                    controller.handleEvent(
                        new Event(
                            ResumeDeploymentAction.ID,
                            getSelection().getId()
                        )
                    );
                  }
                }
              });
        }
        else
        {
          MessageBox.alert("Missing selection", "Please select a deployment");
        }
      }
    }
    );

    MosaicPanel btnLayout = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    btnLayout.add(suspendBtn, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    btnLayout.add(resumeBtn, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    propLayout.add(btnLayout);
    
    // properties
    final DeckLayoutPanel deck = new DeckLayoutPanel();
    deck.add(propLayout);

    // resource info
    ScrollLayoutPanel scrollPanel = new ScrollLayoutPanel();
    resourcePanel = new ResourcePanel();
    scrollPanel.add(resourcePanel);
    deck.add(scrollPanel);

    // selection
    final com.google.gwt.user.client.ui.ListBox dropBox = new com.google.gwt.user.client.ui.ListBox(false);
    dropBox.setStyleName("bpm-operation-ui");
    dropBox.addItem("Properties");
    dropBox.addItem("Resources");
    dropBox.addChangeListener(new ChangeListener() {
      public void onChange(Widget sender) {
        deck.showWidget(dropBox.getSelectedIndex());
        deck.layout();
      }
    });

    this.getHeader().add(dropBox, Caption.CaptionRegion.RIGHT);
    this.add(deck, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    deck.showWidget(dropBox.getSelectedIndex());

    this.add(deck, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

  }

  private DeploymentRef getSelection()
  {
    return currentDeployment;
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
    resourcePanel.setController(controller);
  }

  public void update(DeploymentRef deployment)
  {
    this.currentDeployment= deployment;

    String[] values = new String[] {
        deployment.getId(),
        deployment.getName(),
        deployment.getDefinitions().toString()
    };

    resumeBtn.setEnabled(deployment.isSuspended());
    suspendBtn.setEnabled(!resumeBtn.isEnabled());    

    grid.update(values);
    resourcePanel.update(deployment);
  }

  public void clearView()
  {
    this.currentDeployment = null;
    grid.clear();
    resourcePanel.clearView();

    suspendBtn.setEnabled(false);
    resumeBtn.setEnabled(false);
  }
}
