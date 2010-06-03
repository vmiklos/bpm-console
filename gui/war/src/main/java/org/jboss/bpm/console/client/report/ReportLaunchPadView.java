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
package org.jboss.bpm.console.client.report;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.mvc4g.client.Controller;
import com.mvc4g.client.Event;
import com.mvc4g.client.ViewInterface;
import org.gwt.mosaic.ui.client.CaptionLayoutPanel;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.bpm.console.client.URLBuilder;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.common.LoadingOverlay;
import org.jboss.bpm.report.model.ReportReference;
import org.jboss.errai.workspaces.client.framework.Registry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * List the available reports and allows to redner them.<br>
 * Driven by a list of {@link org.jboss.bpm.report.model.ReportReference}s
 * that need to be retrieved from the server module.
 *
 * @see org.jboss.bpm.console.client.report.UpdateReportConfigAction
 *
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
class ReportLaunchPadView extends MosaicPanel implements ViewInterface, DataDriven
{
  public final static String ID = ReportLaunchPadView.class.getName();

  private Controller controller;

  private MosaicPanel inputPanel;

  private com.google.gwt.user.client.ui.ListBox dropBox;

  List<ReportReference> reportTemplates;
  private HTML description;
  private ReportFrame reportFrame;

  private Map<String, ReportParameterForm> forms = new HashMap<String, ReportParameterForm>();

  public ReportLaunchPadView()
  {
    super(new BoxLayout(BoxLayout.Orientation.VERTICAL));
    this.setPadding(5);

    CaptionLayoutPanel header = new CaptionLayoutPanel("Report configuration");
    header.setStyleName("bpm-detail-panel");
    
    header.setLayout(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));    

    MosaicPanel templatePanel = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));    
    description = new HTML();

    dropBox = new com.google.gwt.user.client.ui.ListBox(false);  
    dropBox.addChangeHandler(new ChangeHandler() {

      public void onChange(ChangeEvent changeEvent)
      {
        String reportTitle = dropBox.getItemText(dropBox.getSelectedIndex());
        selectForm(reportTitle);                
      }
    });

    templatePanel.add(dropBox, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    templatePanel.add(description, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
    // ----

    inputPanel = new MosaicPanel();
    
    // ---
    
    header.add(templatePanel, new BoxLayoutData("250 px", "100 px"));
    header.add(inputPanel, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

    // --

    reportFrame = new ReportFrame();

    this.add(header, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    this.add(reportFrame, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

  }

  private ReportParameterForm createInput(final ReportReference reportRef)
  {
    final ReportParameterForm form =
        new ReportParameterForm(reportRef,
            new ReportParamCallback()
            {
              public void onSumbit(Map<String, String> paramValues)
              {
                boolean valid = true;
                for(String key : paramValues.keySet())
                {
                  String s = paramValues.get(key);
                  if(s == null || s.equals(""))
                    valid = false;
                }

                if(valid)
                {
                  String url = URLBuilder.getInstance().getReportURL(reportRef.getReportFileName());
                  RenderDispatchEvent eventPayload = new RenderDispatchEvent(reportRef.getTitle(), url);
                  eventPayload.setParameters(fieldValues2PostParams(paramValues));
                  controller.handleEvent( new Event(RenderReportAction.ID, eventPayload));
                }
                else
                {
                  MessageBox.alert("Report Parameters", "Please provide the required input parameters");
                }
              }
            });

    return form;
  }

  private ReportReference getCurrentSelection()
  {
    String template = dropBox.getItemText(dropBox.getSelectedIndex());
    for(ReportReference r : reportTemplates)
    {
      if(r.getTitle().equals(template))
        return r;
    }
    
    return null;
  }

  public void update(List<ReportReference> reports)
  {
    reportTemplates = reports;
    forms.clear();

    for(ReportReference report : reports)
    {
      dropBox.addItem(report.getTitle());

      forms.put(report.getTitle(), createInput(report));
    }

    dropBox.setSelectedIndex(0);

    // default form
    selectForm(dropBox.getItemText(0));
    
    Registry.get(ApplicationContext.class).refreshView();
  }

  private void selectForm(String reportTitle)
  {
    inputPanel.clear();
    ReportReference current = getCurrentSelection();
    description.setText(current.getDescription());
    inputPanel.add(forms.get(reportTitle));
    ReportLaunchPadView.this.layout();
  }

  public void displayReport(String title, String dispatchUrl)
  {    
    reportFrame.setFrameUrl(dispatchUrl);    
  }

  public void reset()
  {
    String url = GWT.getModuleBaseURL() + "blank.html";
    System.out.println("** Blank URL: " + url);
    displayReport("", url);
  }

  public void update(Object... data)
  {
    String title = (String)data[0];
    String url = (String)data[1];
    displayReport(title, url);
  }

  public void setLoading(boolean isLoading)
  {
    LoadingOverlay.on(reportFrame, isLoading);
  }

  private static String fieldValues2PostParams(Map<String,String> values)
  {
    StringBuffer sb = new StringBuffer();
    Iterator<String> keys = values.keySet().iterator();
    while(keys.hasNext())
    {
      String key = keys.next();
      sb.append(key).append("=").append(values.get(key));
      sb.append(";");
    }

    System.out.println(sb.toString());
    return sb.toString();
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

}
