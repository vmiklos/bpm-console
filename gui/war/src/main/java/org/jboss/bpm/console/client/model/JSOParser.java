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

import com.google.gwt.core.client.JsArray;
import org.jboss.bpm.report.model.ReportParameter;
import org.jboss.bpm.report.model.ReportReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class JSOParser
{
  public static List<ProcessDefinitionRef> parseProcessDefinitions(String json)
  {
    List<ProcessDefinitionRef> results = new ArrayList<ProcessDefinitionRef>();
    JSOModel rootModel = JSOModel.fromJson(json);

    JsArray<JSOModel> definitions = rootModel.getArray("definitions");
    for(int i=0; i<definitions.length(); i++)
    {
      JSOModel def = definitions.get(i);
      results.add(parseProcessDefinition(def));
    }

    return results;
  }

  public static ProcessDefinitionRef parseProcessDefinition(JSOModel rootModel)
  {
    ProcessDefinitionRef def = new ProcessDefinitionRef();

    def.setId(rootModel.get("id"));
    def.setName(rootModel.get("name"));
    def.setVersion(rootModel.getLong("version"));
    def.setKey(rootModel.get("key"));
    def.setDeploymentId(rootModel.get("deploymentId"));
    def.setSuspended(rootModel.getBoolean("suspended"));

    def.setPackageName(rootModel.get("packageName", ""));
    def.setFormUrl(rootModel.get("formUrl", null));
    def.setDiagramUrl(rootModel.get("diagramUrl", null));
    
    return def;
  }

  public static List<ProcessInstanceRef> parseProcessInstances(String json)
  {
    System.out.println(json);
    List<ProcessInstanceRef> results = new ArrayList<ProcessInstanceRef>();
    JSOModel rootModel = JSOModel.fromJson(json);

    JsArray<JSOModel> instances = rootModel.getArray("instances");
    for(int i=0; i<instances.length(); i++)
    {
      results.add(parseProcessInstance(instances.get(i)));
    }
    return results;
  }

  public static ProcessInstanceRef parseProcessInstance(JSOModel root)
  {
    String id = root.get("id");
    String definitionId = root.get("definitionId");
    Date start = root.getDate("startDate");
    Date end = root.getDate("endDate", null);
    boolean suspended = root.getBoolean("suspended");

    ProcessInstanceRef processInstance = new ProcessInstanceRef(
        id, definitionId,
        start, end,
        suspended
    );
    
    JSOModel rootTokenJson = root.getObject("rootToken");
    
    if (rootTokenJson != null) {
      TokenReference rootToken = parseTokenReference(rootTokenJson);
      processInstance.setRootToken(rootToken);
    }
    // tokens
    /*JSONWalk.JSONWrapper rootTokenJSON = JSONWalk.on(root).next("rootToken");
    if (rootTokenJSON != null) {
        JSONObject tokJso = rootTokenJSON.asObject();

        TokenReference rootToken = parseTokenReference(tokJso);
        processInstance.setRootToken(rootToken);
    } */

    return processInstance;
  }


  private static TokenReference parseTokenReference(JSOModel rootToken) {
    TokenReference tokenReference = new TokenReference();
    tokenReference.setId(rootToken.get("id"));
    tokenReference.setCurrentNodeName(rootToken.get("currentNodeName"));
    tokenReference.setCanBeSignaled(rootToken.getBoolean("canBeSignaled"));
    
    if (rootToken.hasKey("name")) {
      tokenReference.setName(rootToken.get("name"));
    }
    
    JsArray<JSOModel> tokenChildrenJson = rootToken.getArray("children");
    // parse children elements
    if (tokenChildrenJson != null) {
      List<TokenReference> children = new ArrayList<TokenReference>();
      
      for (int i = 0; i < tokenChildrenJson.length(); i++) {
        children.add(parseTokenReference(tokenChildrenJson.get(i)));
      }
      
      tokenReference.setChildren(children);
    }
    
    JsArray<JSOModel> tokenSignalsJson = rootToken.getArray("availableSignals");
    // parse availableSignals elements
    if (tokenSignalsJson != null) {
      // this will be probably always an empty array - as described in ModelAdaptor in adoptExecution method
      List<String> availableSignals = new ArrayList<String>();
      for (int i = 0; i < tokenSignalsJson.length(); i++) {
        availableSignals.add(tokenSignalsJson.get(i).toString());
      }
      
      tokenReference.setAvailableSignals(availableSignals);
    }
    
    return tokenReference;
  }

  public static List<ReportReference> parseReportConfig(String json)
  {
    List<ReportReference> results = new ArrayList<ReportReference>();
    JsArray<JSOModel> rootModel = JSOModel.arrayFromJson(json);

    for(int i=0; i<rootModel.length(); i++)
    {

      JSOModel reportCfg = rootModel.get(i);
      String title = reportCfg.get("title");
      String description = reportCfg.get("description", "");
      String reportFile = reportCfg.get("reportFileName");

      ReportReference reportRef = new ReportReference(reportFile);
      reportRef.setTitle(title);
      reportRef.setDescription(description);

      // parameter
      JsArray<JSOModel> params = reportCfg.getArray("parameterMetaData");
      for (int x=0; x < params.length(); x++)
      {
        JSOModel p = params.get(x);
        String name = p.get("name");
        String type = p.get("type");
        String dataType = p.get("dataType");

        ReportParameter pmd = new ReportParameter(name, ReportParameter.Type.valueOf(type));
        pmd.setDataType(ReportParameter.DataType.valueOf(dataType));

        // optional values
        pmd.setHelptext(p.get("helptext", ""));
        pmd.setPromptText(p.get("promptText", ""));

        reportRef.getParameterMetaData().add(pmd);
      }

      results.add(reportRef);
    }
    return results;
  }

  public static ServerStatus parseStatus(String json)
  {
    ServerStatus status = new ServerStatus();
    JSOModel rootModel = JSOModel.fromJson(json);

    JsArray<JSOModel> plugins = rootModel.getArray("plugins");
    for(int i=0; i<plugins.length(); i++)
    {
      JSOModel item = plugins.get(i);
      String type = item.get("type");
      boolean avail = item.getBoolean("available");
      status.getPlugins().add( new PluginInfo(type, avail) );  
    }

    return status;
  }
  
  public static List<HistoryActivityInstanceRef> parseProcessInstanceHistory(String json)
  {
   
    List<HistoryActivityInstanceRef> results = new ArrayList<HistoryActivityInstanceRef>();
    JSOModel rootModel = JSOModel.fromJson(json);
    
    JsArray<JSOModel> historyEntires = rootModel.getArray("historyEntires");
    
    for(int i=0; i<historyEntires.length(); i++)
    {
      JSOModel entry = historyEntires.get(i);
      results.add(parseProcessInstanceHistory(entry));
    }

    return results;
  }
  
  public static HistoryActivityInstanceRef parseProcessInstanceHistory(JSOModel rootModel)
  {
    HistoryActivityInstanceRef def = new HistoryActivityInstanceRef();

    def.setActivityName(rootModel.get("activityName"));
    def.setStartTime(rootModel.getDate("startTime"));
    def.setEndTime(rootModel.getDate("endTime"));
    def.setExecutionId(rootModel.get("executionId"));
    def.setDuration(rootModel.getLong("duration"));

    
    return def;
  }
  
  public static List<HistoryProcessInstanceRef> parseProcessDefinitionHistory(String json)
  {
   
    List<HistoryProcessInstanceRef> results = new ArrayList<HistoryProcessInstanceRef>();
    JSOModel rootModel = JSOModel.fromJson(json);
    
    JsArray<JSOModel> historyEntires = rootModel.getArray("historyEntires");
    
    for(int i=0; i<historyEntires.length(); i++)
    {
      JSOModel entry = historyEntires.get(i);
      results.add(parseProcessDefinitionHistory(entry));
    }

    return results;
  }
  
  public static HistoryProcessInstanceRef parseProcessDefinitionHistory(JSOModel rootModel)
  {
    HistoryProcessInstanceRef def = new HistoryProcessInstanceRef();

    def.setProcessDefinitionId(rootModel.get("processDefinitionId"));
    def.setProcessInstanceId(rootModel.get("processInstanceId"));
    def.setStartTime(rootModel.getDate("startTime"));
    def.setEndTime(rootModel.getDate("endTime"));
    def.setEndActivityName(rootModel.get("endActivityName"));
    def.setState(rootModel.get("state"));
    def.setKey(rootModel.get("key"));
    def.setDuration(rootModel.getLong("duration"));

    
    return def;
  }
}


