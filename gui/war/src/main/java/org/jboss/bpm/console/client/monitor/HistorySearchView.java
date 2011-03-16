/**
 * 
 */
package org.jboss.bpm.console.client.monitor;

import java.util.Date;
import java.util.List;

import org.gwt.mosaic.ui.client.ScrollLayoutPanel;
import org.gwt.mosaic.ui.client.ToolBar;
import org.gwt.mosaic.ui.client.event.RowSelectionEvent;
import org.gwt.mosaic.ui.client.event.RowSelectionHandler;
import org.gwt.mosaic.ui.client.layout.BorderLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.gwt.mosaic.ui.client.list.DefaultListModel;
import org.jboss.bpm.monitor.gui.client.HistoryRecords;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * @author Jeff Yu
 * @date: Mar 02, 2011
 */
@LoadTool(name="History Query", group = "Processes", icon = "historySearchIcon", priority=1)
public class HistorySearchView implements WidgetProvider {
	
	private ListBox processStatusList;
	
	private ListBox definitionList;
	
	private TextBox correlationKey;
	
	private DateBox startTime;
	
	private DateBox endTime;
	
	private org.gwt.mosaic.ui.client.ListBox<String> instancesList;
	
	
	@Override
	public void provideWidget(ProvisioningCallback callback) {
		LayoutPanel panel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
		panel.setPadding(0);
		panel.setWidgetSpacing(5);
		
		final ToolBar toolbar = new ToolBar();
		panel.add(toolbar, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
		
		toolbar.add(new Button("Search", new ClickHandler() {

			@Override
			public void onClick(ClickEvent clickEvent) {
				String proDef = definitionList.getValue(definitionList.getSelectedIndex());
				String theStatus = processStatusList.getValue(processStatusList.getSelectedIndex());
				System.out.println(proDef);
				System.out.println(theStatus);
				Date theDate = startTime.getValue();
				if (theDate == null) {
					Date d = new Date(2003,1,1);
				}
				
				Date edate = endTime.getValue();
				if (edate == null) {
					edate = new Date();
				}
				String ckey = correlationKey.getValue();
				
				HistoryRecords rpcSvc = MessageBuilder.createCall(new RemoteCallback<List<String>>() {

					@Override
					public void callback(List<String> response) {
						DefaultListModel<String> value = (DefaultListModel<String>)instancesList.getModel();
						value.clear();
						for (String s : response) {
							value.add(s);
						}
						
					}
					
				}, HistoryRecords.class);
				System.out.println(rpcSvc);
				System.out.println(theDate.getTime());
				System.out.println(edate.getTime());
				rpcSvc.getInstances(proDef, theStatus, theDate.getTime(), edate.getTime(), "Session=[1]");
			}
			
		}) );
		
		final MosaicPanel formPanel = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
		panel.add(formPanel, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));

		BoxLayoutData bld1 = new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL);
        bld1.setPreferredWidth("120px");
        
        final MosaicPanel processDefBox = new MosaicPanel(new BoxLayout());
		processDefBox.add(new Label("Process Definition: "), bld1);
		
        formPanel.add(processDefBox);       
        formPanel.add(createProcessStatusListBox(bld1));		
        formPanel.add(createCorrelationKeyTextBox(bld1));		
        formPanel.add(createStartTimeDateBox(bld1));        
        formPanel.add(createEndTimeDateBox(bld1));

		populateProcessDefinitions(processDefBox);
				
		createProcessInstanceListBox(panel);
		
		callback.onSuccess(panel);
	}


	private void createProcessInstanceListBox(LayoutPanel panel) {
		final LayoutPanel resultWindow = new LayoutPanel(new BorderLayout());		
		panel.add(resultWindow, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
		
		final LayoutPanel resultLayout = new ScrollLayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
		resultWindow.add(resultLayout);
		
		instancesList = new org.gwt.mosaic.ui.client.ListBox<String>(new String[]{"Instance Id", "Correlation Key", "Status", "Start Time", "Finish Time"});
		instancesList.setCellRenderer(new org.gwt.mosaic.ui.client.ListBox.CellRenderer<String>(){

			@Override
			public void renderCell(org.gwt.mosaic.ui.client.ListBox<String> listBox, int row, int column, String item) {
				switch(column) {
				case 0:
					listBox.setWidget(row, column, new HTML(item));
					break;
				default:
					throw new RuntimeException("Should not happen!");
				}				
			}
			
		});
		
		instancesList.addRowSelectionHandler(new RowSelectionHandler(){

			@Override
			public void onRowSelection(RowSelectionEvent event) {
				int index = instancesList.getSelectedIndex();
				System.out.println("The row selection index is: " + index);
			}
			
		});
		
		resultLayout.add(instancesList, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
	}


	private void populateProcessDefinitions(final MosaicPanel processDefBox) {
		MessageBuilder.createCall(new RemoteCallback<List<String>>(){
			
			@Override
			public void callback(List<String> response) {
				definitionList = new ListBox();
				for (String s : response) {
					definitionList.addItem(s);
				}
				processDefBox.add(definitionList);
			}
			
		}, HistoryRecords.class).getProcessDefinitionKeys();
	}


	private MosaicPanel createEndTimeDateBox(BoxLayoutData bld1) {
		MosaicPanel box4 = new MosaicPanel(new BoxLayout());
		endTime = new DateBox();
		endTime.setWidth("550px");
		box4.add(new Label("End Time: "), bld1);
		box4.add(endTime);
		return box4;
	}


	private MosaicPanel createStartTimeDateBox(BoxLayoutData bld1) {
		MosaicPanel box3 = new MosaicPanel(new BoxLayout());
		startTime = new DateBox();
		startTime.setWidth("550px");
		box3.add(new Label("Start Time: "), bld1);
		box3.add(startTime);
		return box3;
	}


	private MosaicPanel createCorrelationKeyTextBox(BoxLayoutData bld1) {
		MosaicPanel box2 = new MosaicPanel(new BoxLayout());
		correlationKey = new TextBox();
		correlationKey.setWidth("550px");
		box2.add(new Label("Correlation Key: "), bld1);
		box2.add(correlationKey);
		return box2;
	}


	private MosaicPanel createProcessStatusListBox(BoxLayoutData bld1) {
		MosaicPanel box1 = new MosaicPanel(new BoxLayout());
		processStatusList = new ListBox();
		processStatusList.addItem("COMPLETED");
		processStatusList.addItem("FAILED");
		processStatusList.addItem("TERMINATED");		
		box1.add(new Label("Process Status: "), bld1);
		box1.add(processStatusList);
		return box1;
	}

}
