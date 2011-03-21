/**
 * 
 */
package org.jboss.bpm.console.client.history;

import java.util.List;

import org.gwt.mosaic.ui.client.ListBox;
import org.gwt.mosaic.ui.client.event.RowSelectionEvent;
import org.gwt.mosaic.ui.client.event.RowSelectionHandler;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.gwt.mosaic.ui.client.list.DefaultListModel;
import org.jboss.bpm.console.client.common.DataDriven;
import org.jboss.bpm.console.client.common.LoadingOverlay;
import org.jboss.bpm.console.client.model.HistoryProcessInstanceRef;
import org.jboss.bpm.console.client.util.SimpleDateFormat;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.framework.Registry;

import com.mvc4g.client.Controller;
import com.mvc4g.client.ViewInterface;

/**
 * @author Jeff Yu
 * @date Mar 18, 2011
 */
public class ProcessHistoryInstanceListView implements ViewInterface, WidgetProvider, DataDriven {
	
	public static final String ID = ProcessHistoryInstanceListView.class.getName();
	
	private Controller controller;
	
	private MosaicPanel panel;
	
	private MosaicPanel instanceList;
	
	private ListBox<HistoryProcessInstanceRef> listbox;
	
	 private SimpleDateFormat dateFormat = new SimpleDateFormat();
		
	@Override
	public void provideWidget(ProvisioningCallback callback) {
		
		controller = Registry.get(Controller.class);
		controller.addView(ID, this);
		
		panel = new MosaicPanel();
		panel.setPadding(0);
		panel.setWidgetSpacing(5);
		
		instanceList = new MosaicPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
		instanceList.setPadding(0);
		instanceList.setWidgetSpacing(5);
		
		listbox = new org.gwt.mosaic.ui.client.ListBox<HistoryProcessInstanceRef>(new String[]{"Instance Id", "Correlation Key", "Status", "Start Time", "Finish Time"});
		listbox.setCellRenderer(new org.gwt.mosaic.ui.client.ListBox.CellRenderer<HistoryProcessInstanceRef>(){

			@Override
			public void renderCell(org.gwt.mosaic.ui.client.ListBox<HistoryProcessInstanceRef> listBox, int row, int column, HistoryProcessInstanceRef item) {
				switch(column) {
				case 0:
					listBox.setText(row, column, item.getProcessInstanceId());
					break;
				case 1:
					listBox.setText(row, column, item.getKey());
					break;
				case 2:
					listBox.setText(row, column, item.getState());
					break;
				case 3:
					listBox.setText(row, column, dateFormat.format(item.getStartTime()));
					break;
				case 4:
					listBox.setText(row, column, dateFormat.format(item.getEndTime()));
					break;
				default:
					throw new RuntimeException("Should not happen!");
				}				
			}
			
		});
		
		listbox.addRowSelectionHandler(new RowSelectionHandler(){

			@Override
			public void onRowSelection(RowSelectionEvent event) {
				int index = listbox.getSelectedIndex();
				if (index != -1) {
					HistoryProcessInstanceRef historyInstance = listbox.getItem(index);
					//TODO: might need to use a pop-up window for the whole process events.
				}
			}
			
		});
				
        instanceList.add(listbox, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
        
        panel.add(instanceList);
		callback.onSuccess(panel);

	}	

	
	@Override
	public void setController(Controller controller) {
		this.controller = controller;
	}




	@Override
	public void reset() {
		
	}


	@Override
	public void update(Object... data) {
		List<HistoryProcessInstanceRef> result = (List<HistoryProcessInstanceRef>)data[0];
		DefaultListModel<HistoryProcessInstanceRef> model = (DefaultListModel<HistoryProcessInstanceRef>)listbox.getModel();
		model.clear();
		
		for (HistoryProcessInstanceRef ref : result) {
			model.add(ref);
		}
		panel.invalidate();		
		
	}

	@Override
	public void setLoading(boolean isLoading) {
		LoadingOverlay.on(instanceList, isLoading);
	}

}
