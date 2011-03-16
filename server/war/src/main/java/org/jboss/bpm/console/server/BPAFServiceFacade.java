/**
 * 
 */
package org.jboss.bpm.console.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.bpm.console.server.gson.GsonFactory;
import org.jboss.bpm.monitor.client.model.HistoryInstance;
import org.jboss.bpm.monitor.client.model.HistoryInstanceRef;
import org.jboss.bpm.monitor.model.BPAFDataSource;
import org.jboss.bpm.monitor.model.DefaultBPAFDataSource;
import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.bpm.monitor.model.bpaf.State;
import org.jboss.bpm.monitor.model.bpaf.Tuple;
import org.jboss.bpm.monitor.model.metric.Timespan;

import com.google.gson.Gson;

/**
 * 
 * @author Jeff Yu
 * @date Mar 13, 2011
 */
@Path("/bpaf")
public class BPAFServiceFacade {
	
    public static final String ENTITY_MANAGER_FACTORY = "bpel/EntityManagerFactory";
    
    private BPAFDataSource datasource;
    
    private BPAFDataSource createDataSource()
    {
        BPAFDataSource ds = null;
        try {
            InitialContext ctx = new InitialContext();
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            EntityManagerFactory emf = (EntityManagerFactory)ctx.lookup(ENTITY_MANAGER_FACTORY);
            System.out.println("RESTEASY>>>>>>......" + emf);
            if(null==emf)
                throw new IllegalStateException("EntityManagerFactory not bound: "+ENTITY_MANAGER_FACTORY);

            ds = new DefaultBPAFDataSource(emf);

        } catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Failed to create service" + e.getMessage());
        }

        return ds;
    }
	
	public BPAFServiceFacade() {
		this.datasource = createDataSource();
	}
	
	@GET
	@Produces("applications/json")
	@Path("search")
	public Response getHistoryInstance(@QueryParam(value="definitionkey") String definitionKey, 
				@QueryParam(value="status")String status, @QueryParam(value="startTime")long startTime, 
				@QueryParam(value="endTime")long endTime, @QueryParam(value="correlationkey")String correlationKey) {
		
		assertDataSource();
		
		List<Event> events = datasource.getInstanceEvents(definitionKey, 
				new Timespan(startTime, endTime, "Custom"), getStatus(status));
		List<String> instanceIds = null;
		if (correlationKey != null ) {
			instanceIds = datasource.getProcessInstances(definitionKey, "correlation-key", correlationKey);
		}
		
		Map<String, HistoryInstance> hisInstances = new HashMap<String, HistoryInstance>();
		
		for(Event e : events)
	    {
			if(instanceIds == null || instanceIds.contains(e.getProcessInstanceID())) {
				if (e.getEventDetails().getCurrentState().equals(getStatus(status))) {
					HistoryInstance hi = createHistoryInstance(status, hisInstances, e);					
					hisInstances.put(e.getProcessInstanceID(), hi);
				}
			}
	    }
		
		HistoryInstanceRef ref = new HistoryInstanceRef();
		ref.setInstances(hisInstances.values());
	
		return createJsonResponse(ref);
	}
	
	@GET
	@Produces("applications/json")
	@Path("test")
	public Response test() {
		HistoryInstance hi = new HistoryInstance();
		hi.setEndTime(new Date().getTime());
		hi.setStartTime(new Date().getTime());
		hi.setInstanceId("instanceId");
		hi.setKey("This is correlationkey");
		hi.setStatus("Open");
		
		List<HistoryInstance> his = new ArrayList<HistoryInstance>();
		his.add(hi);
		
		HistoryInstanceRef ref = new HistoryInstanceRef();
		ref.setInstances(his);
	
		return createJsonResponse(ref);
	}

	private HistoryInstance createHistoryInstance(String status, Map<String, HistoryInstance> hisInstances, Event e) {
		HistoryInstance hi = null;
		if (!hisInstances.containsKey(e.getProcessInstanceID())) {
			hi = new HistoryInstance();						
		} else {
			hi = hisInstances.get(e.getProcessInstanceID());
		}
		hi.setInstanceId(e.getProcessInstanceID());
		hi.setStatus(status);
		for (Tuple tuple : e.getDataElement()) {
			if ("correlation-key".equals(tuple.getName())) {
				hi.setKey(tuple.getValue());
			}
			if ("process-start-time".equals(tuple.getName())) {
				hi.setStartTime(new Long(tuple.getValue()));
			}
			if ("process-end-time".equals(tuple.getName())) {
				hi.setEndTime(new Long(tuple.getValue()));
			}
		}
		return hi;
	}
	
	private State getStatus(String status) {
		if ("COMPLETED".equalsIgnoreCase(status)) {
			return State.Closed_Completed;
		}
		if ("FAILED".equalsIgnoreCase(status)) {
			return State.Closed_Completed_Failed;
		}
		if ("TERMINATED".equalsIgnoreCase(status)) {
			return State.Closed_Cancelled_Terminated;
		}
		return null;
	}
	
	private Response createJsonResponse(Object wrapper) {
	    Gson gson = GsonFactory.createInstance();
	    String json = gson.toJson(wrapper);
	    return Response.ok(json).type("application/json").build();
	}
	
    private void assertDataSource() {
        if(null==this.datasource)
            throw new IllegalStateException("BPAFDataSource not initialized");

    }
	
}
