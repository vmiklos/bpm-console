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
package org.jboss.bpm.console.client.process;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.mvc4g.client.Controller;
import com.mvc4g.client.ViewInterface;
import org.gwt.mosaic.ui.client.ListBox;
import org.gwt.mosaic.ui.client.layout.MosaicPanel;
import org.gwt.mosaic.ui.client.list.DefaultListModel;
import org.gwt.mosaic.ui.client.table.AbstractScrollTable;
import org.jboss.bpm.console.client.ApplicationContext;
import org.jboss.bpm.console.client.LazyPanel;
import org.jboss.bpm.console.client.util.ConsoleLog;
import org.jboss.bpm.console.client.util.DOMUtil;
import org.jboss.errai.workspaces.client.framework.Registry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class InstanceDataView extends MosaicPanel implements ViewInterface, LazyPanel
{
  public final static String ID = InstanceDataView.class.getName();

  private Controller controller;

  private ListBox listBox;

  private String instanceId;

  private boolean isInitialized;

  boolean isRiftsawInstance = false;

  public InstanceDataView()
  {
    super();
    this.setPadding(5);
    ApplicationContext appContext = Registry.get(ApplicationContext.class);
    isRiftsawInstance = appContext.getConfig().getProfileName().equals("BPEL Console");    
  }

  public void initialize()
  {
    if(!isInitialized)
    {
      listBox =
          new ListBox<Node>(
              new String[] {
                  "Key", "XSD Type", "Java Type", "Value"}
          );

      listBox.setColumnResizePolicy(AbstractScrollTable.ColumnResizePolicy.MULTI_CELL);
      
      listBox.setCellRenderer(new ListBox.CellRenderer<DataEntry>() {
        public void renderCell(ListBox<DataEntry> listBox, int row, int column,
                               DataEntry item) {
          switch (column) {
            case 0:
              listBox.setText(row, column, item.key);
              break;
            case 1:
              listBox.setText(row, column, item.xsd);
              break;
            case 2:
              listBox.setText(row,column, item.java);
              break;
            case 3:
              if(isRiftsawInstance)
              {
                JSONTree tree = new JSONTree(item.value);
                listBox.setWidget(row,column, tree);
              }
              else
              {
                listBox.setText(row,column, item.value);
              }
              break;
            default:
              throw new RuntimeException("Unexpected column size");
          }
        }
      });

      this.add(listBox);

      this.isInitialized = true;
    }
  }

  public boolean isInitialized()
  {
    return isInitialized;
  }

  public void setController(Controller controller)
  {
    this.controller = controller;
  }

  public void update(String instanceId, Document xml)
  {
    this.instanceId = instanceId;
    parseMessage(xml);
  }

  private void parseMessage(Document messageDom)
  {
    try
    {
      // parse the XML document into a DOM
      //Document messageDom = XMLParser.parse(messageXml);

      Node dataSetNode = messageDom.getElementsByTagName("dataset").item(0);
      List<Node> dataSetNodeChildren = DOMUtil.getChildElements(dataSetNode.getChildNodes());
      List<DataEntry> results = new ArrayList<DataEntry>();

      for(Node dataNode : dataSetNodeChildren)
      {
        DataEntry dataEntry = new DataEntry();
        NamedNodeMap dataNodeAttributes = dataNode.getAttributes();

        Node valueNode = DOMUtil.getChildElements(dataNode.getChildNodes()).get(0); // expected to have just one child‚
        NamedNodeMap valueNodeAttributes = valueNode.getAttributes();

        dataEntry.key = dataNodeAttributes.getNamedItem("key").getNodeValue();
        dataEntry.java = dataNodeAttributes.getNamedItem("javaType").getNodeValue();
        dataEntry.xsd = valueNodeAttributes.getNamedItem("xsi:type").getNodeValue();

        List<Node> valueChildElements = DOMUtil.getChildElements(valueNode.getChildNodes());

        if(valueChildElements.isEmpty()
            && valueNode.hasChildNodes()
            && Node.TEXT_NODE == valueNode.getChildNodes().item(0).getNodeType())
        {
          dataEntry.value = valueNode.getFirstChild().getNodeValue();          
        }
        else
        {
          // complex types or empty elements
          dataEntry.value = "n/a";
        }

        results.add(dataEntry);
      }

      bindData(results);
    }
    catch (Throwable e)
    {
      ConsoleLog.error("Failed to parse XML document", e);
    }

  }

  private void bindData(List<DataEntry> data)
  {
    initialize();

    final DefaultListModel<DataEntry> model =
        (DefaultListModel<DataEntry>) listBox.getModel();
    model.clear();

    for(DataEntry d : data)
    {
      model.add(d);
    }

    // layout again
    this.layout();
  }

  private class DataEntry
  {
    String key;
    String xsd;
    String java;
    String value;
  }
}
