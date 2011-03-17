/**
 * 
 */
package org.jboss.bpm.console.server.plugin;

import java.util.List;

import org.jboss.bpm.console.client.model.HistoryProcessInstanceRef;

/**
 * @author Jeff Yu
 * @date Mar 17, 2011
 */
public interface ProcessHistoryPlugin {
	
	List<HistoryProcessInstanceRef> getHistoryProcessInstances(String definitionkey, String status,
																long starttime, long endtime, String correlationkey);
	
}
