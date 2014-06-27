package gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import comms.MqttEventListener;
import comms.MqttEventSource;

import semiautonomy.CommonDataChangesEventSource;
import semiautonomy.CommonDataEventListener;
import semiautonomy.SingletonCommonDataImpl;
import semiautonomy.SingletonSemiautonomyManagerImpl;
import semiautonomy.Utils;



public class SingletonMapViewportImpl extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public MapImagesImpl m_mapLabel;
	JScrollPane m_mapScroll;
	private JPanel southPanel;
	public JComboBox targetList;
	JToggleButton relocalizeButton;
	public JButton saveTopology,loadTopology;
	public JCheckBox findRobotButton;
	public JCheckBox userModeButton;
	public JCheckBox collaborativeControlButton;
	Utils m_utils;
	private boolean updatingTargetList=false;  // Is the targetList being updated?  
    int numerOfGotoLabelEvents=0;		 // when an item from the targetList is selected, the event is thrown twice
    						 // "gotolabel" counts the number of events and discards the second one
    
   
    private SingletonCommonDataImpl m_commonData; 
    private SingletonSemiautonomyManagerImpl m_semiautonomyManager; 
 	private static SingletonMapViewportImpl singletonObject;
 	private MyItemListener m_itemListener;
 	public ViewportCommonDataEventListener m_listener;
 	public JCheckBox randomNavigationDemo;
 	/** A private Constructor prevents any other class from instantiating. */
 	private SingletonMapViewportImpl() {
 		m_listener= new ViewportCommonDataEventListener();
 		m_commonData= SingletonCommonDataImpl.getSingletonObject();
 		m_semiautonomyManager= SingletonSemiautonomyManagerImpl.getSingletonObject();
		m_utils=new Utils();
		initializePluginPanelComponents();
		initializeSouthPanelComponents(); 
 		m_commonData.addCommonDataEventListener(m_listener);
		Thread t0=new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(m_commonData.pluginSTOP==false)
				{
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					repaintMap();
				}
			}},"Graphics updater");
		t0.start();
 	}
 	
 	public static synchronized SingletonMapViewportImpl getSingletonObject() {
 		if (singletonObject == null) {
 			singletonObject = new SingletonMapViewportImpl();
 		}
 		return singletonObject;
 	}
 	
 	public Object clone() throws CloneNotSupportedException {
 		throw new CloneNotSupportedException();
 	} 

	public void destroySingleton(){
		singletonObject=null;
	}
	public void repaintMap(){
		updatePosition();
		m_mapLabel.m_mapImages.repaint();
		m_mapLabel.m_mapImages.validate();
		m_mapScroll.validate();
	}
	
	public void updateViewport(){
	    targetList.removeAllItems();
	    targetList.insertItemAt("Navigate To:",0);
	    targetList.setSelectedItem("Navigate To:");
	    updatingTargetList=true;
		while (m_commonData.topology.sNodeList.equals("empty")||m_commonData.topology.sArcList.equals("empty"))
		{
			
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}	
		System.out.println("Adding Topology");
	    StringTokenizer separateNodes;
	    separateNodes= new StringTokenizer(m_commonData.topology.sNodeList,"#"); 
	    String NodeInfo="";
	    m_mapLabel.m_mapImages.removeAll();
	    while (separateNodes.hasMoreElements())
	    {
	    	
	            StringTokenizer separateNodeInfo;
	            NodeInfo=separateNodes.nextToken("#");
	            separateNodeInfo=new StringTokenizer(NodeInfo," ");
	            String name=separateNodeInfo.nextToken(" ");
	            String xPos=separateNodeInfo.nextToken(" ");
	            String yPos=separateNodeInfo.nextToken(" ");
	            System.out.println(name+" "+xPos+" "+yPos);
	            double d_xPos=Double.parseDouble(xPos);
	            double d_yPos=Double.parseDouble(yPos);
	            if(!name.contains("Robot"))
	            {
	            m_mapLabel.addNamedNode(name,d_xPos,d_yPos,true);
		            if(!name.contains("NODE")){
		            	targetList.addItem(name);
		            	//System.out.println("ITEM ADDED TO TARGET LIST:"+name);
		            }
	            }	
	    }
	    updatingTargetList=false;
	}
	
/////////////////////////////////////////////////////
// PRIVATE METHODS	
/////////////////////////////////////////////////////	
	private void initializePluginPanelComponents(){
        
		targetList = new JComboBox();

		targetList.setVisible(true);
		targetList.setFont(new Font("Arial",Font.BOLD,12));
		targetList.insertItemAt("Navigate To:",0);
		targetList.setSelectedItem("Navigate To:");
		m_itemListener= new MyItemListener();
			
		targetList.addItemListener(m_itemListener);
		m_mapLabel= new MapImagesImpl(); 
		targetList.setMinimumSize(new Dimension(m_mapLabel.schematicMapIcon.getIconWidth(),30));
		targetList.setPreferredSize(new Dimension(m_mapLabel.schematicMapIcon.getIconWidth(),50));
		targetList.setMaximumSize(new Dimension(m_mapLabel.schematicMapIcon.getIconWidth(),100));
	    m_mapScroll= new JScrollPane();
        m_mapScroll.setLayout(new ScrollPaneLayout());
        //m_mapScroll.setSize(m_mapLabel.schematicMapIcon.getImage().getWidth(m_mapLabel),m_mapLabel.schematicMapIcon.getImage().getHeight(m_mapLabel));
        m_mapScroll.setMinimumSize(new Dimension(m_mapLabel.schematicMapIcon.getIconWidth(),300));
        m_mapScroll.setPreferredSize(new Dimension(m_mapLabel.schematicMapIcon.getIconWidth(),400));
        m_mapScroll.setMaximumSize(new Dimension(m_mapLabel.schematicMapIcon.getIconWidth(),700));
        m_mapScroll.getViewport().add(m_mapLabel.m_mapImages,ScrollPaneLayout.UPPER_LEFT_CORNER);
        m_mapScroll.getHorizontalScrollBar().addMouseListener(new MouseListener(){

    		@Override
    		public void mouseClicked(MouseEvent e) {
    			// TODO Auto-generated method stub
    			findRobotButton.setSelected(false);			
    		}

    		@Override
    		public void mousePressed(MouseEvent e) {
    			// TODO Auto-generated method stub
    			findRobotButton.setSelected(false);	
    			
    		}

    		@Override
    		public void mouseReleased(MouseEvent e) {
    			// TODO Auto-generated method stub
    			
    		}

    		@Override
    		public void mouseEntered(MouseEvent e) {
    			// TODO Auto-generated method stub
    			
    		}

    		@Override
    		public void mouseExited(MouseEvent e) {
    			// TODO Auto-generated method stub
    			
    		}});
        m_mapScroll.getVerticalScrollBar().addMouseListener(new MouseListener(){

    		@Override
    		public void mouseClicked(MouseEvent e) {
    			// TODO Auto-generated method stub
    			findRobotButton.setSelected(false);
    		}

    		@Override
    		public void mousePressed(MouseEvent e) {
    			// TODO Auto-generated method stub
    			findRobotButton.setSelected(false);	 			
    		}

    		@Override
    		public void mouseReleased(MouseEvent e) {
    			// TODO Auto-generated method stub
    			
    		}

    		@Override
    		public void mouseEntered(MouseEvent e) {
    			// TODO Auto-generated method stub
    			
    		}

    		@Override
    		public void mouseExited(MouseEvent e) {
    			// TODO Auto-generated method stub
    			
    		}});

        
	
		
        southPanel= new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.setMinimumSize(new Dimension(m_commonData.mapWidth,100));
        southPanel.setMaximumSize(new Dimension(m_commonData.mapWidth,500));
        southPanel.setPreferredSize(new Dimension(m_commonData.mapWidth,300));
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); 
        this.setMinimumSize(new Dimension(m_commonData.mapWidth,600));
        this.setMaximumSize(new Dimension(m_commonData.mapWidth,700));
        this.setPreferredSize(new Dimension(m_commonData.mapWidth,800));       
        this.add(targetList);
        targetList.setAlignmentX(SwingConstants.CENTER);
        this.add(m_mapScroll);
        m_mapScroll.setAlignmentX(SwingConstants.CENTER);
        this.add(southPanel);
        southPanel.setAlignmentX(SwingConstants.CENTER);
	}
	private void initializeSouthPanelComponents()
	{
		relocalizeButton= new JToggleButton("Relocalize");
		relocalizeButton.setMinimumSize(new Dimension((int)m_commonData.mapWidth-10,20));
		relocalizeButton.setPreferredSize(new Dimension((int)m_commonData.mapWidth-8,25));
		relocalizeButton.setMaximumSize(new Dimension((int)m_commonData.mapWidth,30));
	    relocalizeButton.addActionListener(new ActionListener() { 
	        public void actionPerformed(ActionEvent e)
	        {
	        	if(relocalizeButton.isSelected()){
	            Thread relocalize= new Thread(new Runnable(){
	                public void run(){	  
	                    m_mapLabel.setCursorType("cross");
	                    m_commonData.setPluginState("RELOCALIZE_AT");
	                    while (m_commonData.getPluginState().equals("RELOCALIZE_AT")){
	                        try {
	                            Thread.sleep(100);
	                        } catch (InterruptedException ex) {
	                            
	                        }
	                    }
	                    m_mapLabel.setCursorType("default");
	                    relocalizeButton.setSelected(false);
	                }
	            },"relocalize");
	            relocalize.start();	            
	        	}
	        	else{
	        		m_commonData.setPluginState("IDLE");
	        	}
	        }
	    });
	    
	    saveTopology= new JButton("Save Topology");
	    saveTopology.setHorizontalAlignment(SwingConstants.LEFT);
	    saveTopology.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				m_semiautonomyManager.saveTopology();
			}
	    	
	    });
	    
	    loadTopology= new JButton("Load Topology");
	    loadTopology.setHorizontalAlignment(SwingConstants.LEFT);
	    loadTopology.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				m_semiautonomyManager.loadTopology();
			}
	    	
	    });
	    findRobotButton= new JCheckBox("Center Map on ROBOT");
	    findRobotButton.setHorizontalAlignment(SwingConstants.LEFT);
	    findRobotButton.setSelected(true);		
	    findRobotButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (findRobotButton.isSelected()){
					setFindRobot(true);
				}
				else{
					setFindRobot(false);
				}
				
			} 
	
	    });
	    collaborativeControlButton= new JCheckBox("COLLABORATIVE CONTROL");
	    collaborativeControlButton.setSelected(false);		
	    collaborativeControlButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (collaborativeControlButton.isSelected()){
					m_commonData.collaborativeControlEnabled=true;				
				}
				else
				{
					m_commonData.collaborativeControlEnabled=false;
					m_commonData.setNavigationMode("Manual");
				}
				
			} 

        });
        randomNavigationDemo= new JCheckBox("Random Navigation DEMO");
        randomNavigationDemo.setHorizontalAlignment(SwingConstants.LEFT);
        randomNavigationDemo.setSelected(false);		
        randomNavigationDemo.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (randomNavigationDemo.isSelected()){
					m_commonData.randomNav=true;
				}
				else{
					m_commonData.randomNav=false;
					//m_semiautonomyManager.stopGiraff(); 
				}
				
			} 
        });
	    if(m_commonData.m_UserMode.equals("engineer")){
	        userModeButton= new JCheckBox("USER VIEW");
	        userModeButton.setSelected(false);		
	        userModeButton.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (userModeButton.isSelected()){
						if(m_commonData.getPluginMode().equals("ENGINEER"))
						{
						m_commonData.setPluginMode("USER");
				    	southPanel.remove(randomNavigationDemo);
						southPanel.remove(saveTopology);
						southPanel.remove(loadTopology);
						UpdateViewportUI();
						}						
					}
					else{
						if(m_commonData.getPluginMode().equals("USER"))
						{
						m_commonData.setPluginMode("ENGINEER");
				    	southPanel.add(randomNavigationDemo);
						southPanel.add(saveTopology);
						southPanel.add(loadTopology);
						UpdateViewportUI();
					
						}
					}
					
				} 
	
	        });
		    southPanel.add(relocalizeButton);
		    southPanel.add(findRobotButton);
		    southPanel.add(collaborativeControlButton);
	        southPanel.add(userModeButton);
		    southPanel.add(saveTopology);
		    southPanel.add(loadTopology);
		    southPanel.add(randomNavigationDemo);
	    }    
	    else{
	    	southPanel.add(relocalizeButton);
	    	southPanel.add(findRobotButton);
	    	southPanel.add(collaborativeControlButton);
	    }
	}
	private void UpdateViewportUI(){
		southPanel.validate();
		this.validate();
		this.repaint();
	}
	public void resetList(){
		targetList.setSelectedItem(new String("Navigate To:"));
		targetList.hidePopup();
	}
    public boolean getFindRobot()
    {
    	return m_commonData.viewportFollowsRobot;
    }
     public void setFindRobot(boolean value){
         m_commonData.viewportFollowsRobot=value;
    }
	public void updatePosition(){
        double right_limit_distance=m_commonData.mapWidth-m_mapLabel.getRobotPose().getX();
        double lower_limit_distance=m_commonData.mapHeight-m_mapLabel.getRobotPose().getY();
        double left_limit_distance=m_mapLabel.getRobotPose().getX();
        double upper_limit_distance=m_mapLabel.getRobotPose().getY();
        int xPoint=m_mapLabel.getRobotPose().x,yPoint=m_mapLabel.getRobotPose().y;
		if (getFindRobot()==true)
		{
			if(right_limit_distance<m_mapScroll.getViewport().getWidth()/2)
			{
				xPoint=m_commonData.mapWidth-m_mapScroll.getViewport().getWidth();
			}
			else if(left_limit_distance<m_mapScroll.getViewport().getWidth()/2)
			{
				xPoint=0;
			}
			else{
				xPoint=xPoint-m_mapScroll.getViewport().getWidth()/2;
			}
			if(lower_limit_distance<m_mapScroll.getViewport().getHeight()/2)
			{ 
				yPoint=m_commonData.mapHeight-m_mapScroll.getViewport().getHeight();    
			}
			else if(upper_limit_distance<m_mapScroll.getViewport().getHeight()/2)
			{
				yPoint=0;
			}
			else{
				yPoint=yPoint-m_mapScroll.getViewport().getHeight()/2;
			}
			
			m_mapScroll.getViewport().setViewPosition(new Point(xPoint, yPoint));  
		}
	}	
	private class MyItemListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			numerOfGotoLabelEvents++;
			// TODO Auto-generated method stub
			String item=e.getItem().toString();
			if(!item.equals(new String("Navigate To:"))&&(numerOfGotoLabelEvents==1))
			{
				System.out.println("Label selected:"+item);
				if(updatingTargetList==false)
				{
					m_semiautonomyManager.goTo(item,true);
					targetList.hidePopup();
					targetList.setSelectedItem("Navigate To:");
				}
			}	
			numerOfGotoLabelEvents--;
		}
		
	}
	private class ViewportCommonDataEventListener implements CommonDataEventListener {
		String m_modules="empty";
		String m_command="empty";

		@Override
		public void eventRead(CommonDataChangesEventSource Source) {
			// TODO Auto-generated method stub
			m_modules= Source.getModules();
			m_command= Source.getCommand();
			if (m_modules.contains("viewport"))
			{
				
				if (m_command.contains("repaint"))
				{
					repaintMap();
				}
				else if (m_command.contains("update"))
				{
					updateViewport();
				}
			
			}
			
		}

	}

}
