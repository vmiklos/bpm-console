/**
 * 
 */
package org.jboss.bpm.console.server;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.bpm.console.client.model.HistoryProcessInstanceRef;
import org.jboss.bpm.console.client.model.HistoryProcessInstanceRefWrapper;
import org.jboss.bpm.console.server.gson.GsonFactory;
import org.jboss.bpm.console.server.plugin.PluginMgr;
import org.jboss.bpm.console.server.plugin.ProcessHistoryPlugin;
import org.jboss.bpm.console.server.util.RsComment;

import com.google.gson.Gson;

/**
 * 
 * @author Jeff Yu
 * @date Mar 13, 2011
 */
@Path("/history")
@RsComment(
    title = "Process History",
    description = "Process History"
)
public class ProcessHistoryFacade {
	
	private static final Log log = LogFactory.getLog(ProcessHistoryFacade.class);
	
	private ProcessHistoryPlugin historyPlugin;
	
	public ProcessHistoryPlugin getProcessHistoryPlugin() {
		if (historyPlugin == null) {
			historyPlugin = PluginMgr.load(ProcessHistoryPlugin.class);
		}
		return historyPlugin;
	}
	
	@GET
	@Produces("applications/json")
	@Path("search")
	public Response findHisotryInstances(@Context UriInfo info) {
		String dkey = info.getQueryParameters().getFirst("definitionkey");
		String status = info.getQueryParameters().getFirst("status");
		String stime = info.getQueryParameters().getFirst("starttime");
		String etime = info.getQueryParameters().getFirst("endtime");
		String ckey = info.getQueryParameters().getFirst("correlationkey");
		
		List<HistoryProcessInstanceRef> refs = getProcessHistoryPlugin().getHistoryProcessInstances(dkey, status, new Long(stime), new Long(etime), ckey);
		HistoryProcessInstanceRefWrapper wrapper = new HistoryProcessInstanceRefWrapper(refs);
		
		return createJsonResponse(wrapper);
	}

	private Response createJsonResponse(Object wrapper) {
	    Gson gson = GsonFactory.createInstance();
	    String json = gson.toJson(wrapper);
	    return Response.ok(json).type("application/json").build();
	}
	
	
}
