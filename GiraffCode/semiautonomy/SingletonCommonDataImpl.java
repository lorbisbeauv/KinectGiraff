package semiautonomy;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;

import comms.MqttEventListener;
import comms.MqttEventSource;

import configuration.AppConfigurationImpl;



public class SingletonCommonDataImpl implements SingletonCommonData{
	public enum PluginModes {USER, ENGINEER,DEBUG};
	public PluginModes pluginMode;
	public enum PluginState {CREATE_LABEL,GOTO_POINT,GOTO_LABEL,RELOCALIZE_AT,ADD_ARC,
			IMPORT_TOPOLOGY,IDLE, WORKING,ADD_NODE,MOVING_NODE,DELETING_NODE,RENAMING_NODE};
	

	public String geometricMapPath="empty", schematicMapPath="empty";
	
	public double robotXplace=0,robotYplace=0;;
	public double  robotOrientation=0;

	public String laserInfo="empty";
	/// temporal
 
    public boolean randomNav=false;
    public boolean updatingTopology=false;
    public boolean busy=false;
    public boolean editTopology=false;
    public boolean cancelNavigation=false;
    ////////////////////////
    AppConfigurationImpl configurationData;
    ImageIcon mapSchematicIcon,mapGeometricIcon; //It creates a map Icon in order to know the map Dimensions 
    public String m_UserMode="user";
    public int mapWidth=0,mapHeight=0;
    public String specifiedPlace="unknown";
    public boolean pluginSTOP=false,viewportFollowsRobot=true;
    private static SingletonCommonDataImpl singletonObject;
    private Set<CommonDataEventListener> m_listeners;
    public TopologyImp topology;
    private Utils m_utils;
    private Point mousePoint;
	private PluginState pluginState=PluginState.IDLE;
	   private boolean openMORAcontrols=false;
		private String lastClickedNode="none";
		private boolean nodeIsSelected=false;
		private String m_navigationMode="Manual";
		public boolean collaborativeControlEnabled=false;
		public Point collaborativeTarget;
		public String testApiCommand= "Waiting";
		public Image imgSchematic,imgGeometric;
    /** A private Constructor prevents any other class from instantiating. */
	private SingletonCommonDataImpl() {
		topology= new TopologyImp();
		m_utils= new Utils();
		configurationData= new AppConfigurationImpl();
		m_listeners = new HashSet<CommonDataEventListener>(); 
		mousePoint= new Point(0,0);
		collaborativeTarget= new Point(0,0);
		//m_UserMode="engineer";
		m_UserMode=configurationData.m_UserMode;
	}
	public void start(){
		if(m_UserMode.equals("engineer")){
			pluginMode=PluginModes.ENGINEER;
		}
		else{
			pluginMode=PluginModes.USER;  	
		}
        setPluginState("IDLE");
        if (configurationData.isApplet==false){
        	geometricMapPath="./plugins/geometricMap.png";
        	schematicMapPath="./plugins/schematicMap.png";
        	mapSchematicIcon= new ImageIcon(schematicMapPath);
        }
        else{
        	mapSchematicIcon=new ImageIcon();
        	mapSchematicIcon.setImage(imgSchematic);
        	mapGeometricIcon=new ImageIcon();
        	mapGeometricIcon.setImage(imgGeometric);
        }
		
		mapWidth=mapSchematicIcon.getIconWidth();
		mapHeight=mapSchematicIcon.getIconHeight();
		System.out.println("COMMON DATA->(W,H) "+mapWidth+","+mapHeight);		
	}
	public static synchronized SingletonCommonDataImpl getSingletonObject() {
		if (singletonObject == null) {
			singletonObject = new SingletonCommonDataImpl();
		}
		return singletonObject;
	}
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	public void destroySingleton(){
		singletonObject=null;
	}
	/////////////////////////////////////
	///////////TEST//////////////////////
	
	
	
	////////////////////////////////////
	////////////////////////////////////
	
	
	 //////////////////////////
	 // PLUGIN STATE Controls
	 /////////////////////////
	    public String getPluginState()
	    {
	    	String state;
	    	if (pluginState==PluginState.ADD_ARC)
	    		state="ADD_ARC";
	    	else if (pluginState==PluginState.ADD_NODE)
	    		state="ADD_NODE";
	    	else if (pluginState==PluginState.CREATE_LABEL)
	    		state="CREATE_LABEL";
	    	else if (pluginState==PluginState.DELETING_NODE)
	    		state="DELETING_NODE";
	    	else if (pluginState==PluginState.GOTO_LABEL)
	    		state="GOTO_LABEL";
	    	else if (pluginState==PluginState.GOTO_POINT)
	    		state="GOTO_POINT";
	    	else if (pluginState==PluginState.IMPORT_TOPOLOGY)
	    		state="IMPORT_TOPOLOGY";
	    	else if (pluginState==PluginState.MOVING_NODE)
	    		state="MOVING_NODE";
	    	else if (pluginState==PluginState.RELOCALIZE_AT)
	    		state="RELOCALIZE_AT";
	    	else if (pluginState==PluginState.RENAMING_NODE)
	    		state="RENAMING_NODE";
	    	else if (pluginState==PluginState.WORKING)
	    		state="WORKING";
	    	else
	    		state="IDLE";

	        return state;
	    }
	    public void setPluginState(String newState){
	    	if (newState.equals("ADD_ARC"))
	    		pluginState=PluginState.ADD_ARC;
	    	else if (newState.equals("ADD_NODE"))
	    		pluginState=PluginState.ADD_NODE;
	    	else if (newState.equals("CREATE_LABEL"))
	    		pluginState=PluginState.CREATE_LABEL;
	    	else if (newState.equals("DELETING_NODE"))
	    		pluginState=PluginState.DELETING_NODE;
	    	else if (newState.equals("GOTO_LABEL"))
	    		pluginState=PluginState.GOTO_LABEL;
	    	else if (newState.equals("GOTO_POINT"))
	    		pluginState=PluginState.GOTO_POINT;
	    	else if (newState.equals("IMPORT_TOPOLOGY"))
	    		pluginState=PluginState.IMPORT_TOPOLOGY;
	    	else if (newState.equals("MOVING_NODE"))
	    		pluginState=PluginState.MOVING_NODE;
	    	else if (newState.equals("RELOCALIZE_AT"))
	    		pluginState=PluginState.RELOCALIZE_AT;
	    	else if (newState.equals("RENAMING_NODE"))
	    		pluginState=PluginState.RENAMING_NODE;
	    	else if (newState.equals("WORKING"))
	    		pluginState=PluginState.RENAMING_NODE;
	    	else
	    		pluginState=PluginState.IDLE;
	        System.out.println("Plugin state:"+pluginState.toString());
	    }
	    public String getPluginMode(){
	    	String mode;
	    	if (pluginMode==PluginModes.USER)
	    		mode="USER";
	    	else
	    		mode="ENGINEER";
	    	return mode;
	    }
	    public void setPluginMode(String mode)
	    {
	    	if(mode.equals("user")||mode.equals("USER")){
	    		pluginMode= PluginModes.USER;
	    	}
	    	else{
	    		pluginMode= PluginModes.ENGINEER;
	    	}
	    }
	 ///////////////////////
		@Override
		public void setLocalization(double x, double y, double phi) {
			// TODO Auto-generated method stub
			this.robotXplace=x;
			this.robotYplace=y;
			this.robotOrientation=phi;
			notifyListeners("viewport","repaintMap");
    		//System.out.println("SEMIAUTONOMY LISTENER MESSAGE-->Localization, event thrown to Repaint Map");
		}
		@Override
		public void setTopology(String topologyGraph) {
			// TODO Auto-generated method stub
				topology.updatingTopology=true;
				StringTokenizer separateNodesArcs;        
		         separateNodesArcs= new StringTokenizer(topologyGraph,"&");
		         topology.sNodeList=separateNodesArcs.nextToken("&");
		         topology.sArcList=separateNodesArcs.nextToken("&");
		         StringTokenizer separateNodes,separateArcs;
		         separateNodes= new StringTokenizer(topology.sNodeList,"#"); 
		         separateArcs= new StringTokenizer(topology.sArcList,"#"); 
		         String NodeInfo="";
		         String ArcInfo="";
		         topology.nodesMap.clear();
		         topology.nodesNameMap.clear();
		         topology.arcsMap.mapClear();
		         int i=0;
		         while (separateNodes.hasMoreElements()){
	             	Point p=new Point(0,0);
	                 StringTokenizer separateNodeInfo;
	                 NodeInfo=separateNodes.nextToken("#");
	                 separateNodeInfo=new StringTokenizer(NodeInfo," ");
	                 String name=separateNodeInfo.nextToken(" ");
	                 String xPos=separateNodeInfo.nextToken(" ");
	                 String yPos=separateNodeInfo.nextToken(" ");
	                 double d_xPos=Double.parseDouble(xPos);
	                 double d_yPos=Double.parseDouble(yPos);
	                 if(!name.contains("Robot")){
	     	            Point ori= new Point(m_utils.transformToSchematicMapPoint(d_xPos,d_yPos,mapWidth/2,mapHeight/2 ));
	                 	p.setLocation(ori);
	                 	if(topology.nodesMap.containsKey(name)){
	                 		System.out.println("This Node is in the map already:"+name);	                 		
	                 	}
	                 	System.out.println("ADDING NODE TO MAP:"+name);
	                 	topology.nodesMap.put(name,p.getLocation());
	                 	topology.nodesNameMap.put(i,name);
	                 	i++;
	                 }
		         }
		         while (separateArcs.hasMoreElements()){
		                 StringTokenizer separateArcsInfo;
		                 ArcInfo=separateArcs.nextToken("#");
		                 separateArcsInfo=new StringTokenizer(ArcInfo," ");
		                 String nodeOrig=separateArcsInfo.nextToken(" ");
		                 String nodeDest=separateArcsInfo.nextToken(" ");
		                 if(!nodeOrig.contains("Robot") && !nodeDest.contains("Robot")){
		                     if(topology.arcsMap.mapContainsKey(nodeOrig)){
		                         String name=nodeOrig;
		                         while(topology.arcsMap.mapContainsKey(name)){
		                             name=name+":rep";
		                         }
		                         topology.arcsMap.mapPut(name,nodeDest);
		                     }
		                     else{
		                         topology.arcsMap.mapPut(nodeOrig,nodeDest);
		                     }
		                 }                  
		         }
		         topology.updatingTopology=false;
		         notifyListeners("viewport","update");
		}
		@Override
		public void setNavigationMode(String modeMessage) {
			// TODO Auto-generated method stub
			StringTokenizer tk=new StringTokenizer(modeMessage,"|");
    		m_navigationMode=modeMessage;
    		System.out.println("Navigation Mode message reveived:"+modeMessage);
	    	if(modeMessage.contains("Manual")||modeMessage.contains("manual")){
	    		if(collaborativeControlEnabled==false){
	    		System.out.println("Manual");
	    		}
	    	}
	    	else{
	    		String mode=tk.nextToken();
	    		String place=modeMessage.replaceFirst(mode+"|","");
	    		System.out.println(mode);
	    		specifiedPlace=place;
	    	}
		}
		@Override
		public void setImageMousePosition(int x, int y) {
			// TODO Auto-generated method stub
            this.mousePoint.x=x;
            this.mousePoint.y=y;
		}
		@Override
		public Point getImageMousePosition() {
			// TODO Auto-generated method stub
			return new Point(this.mousePoint.x,this.mousePoint.y);
		}
		////////////////////////////////////////////////////////////////
		//CommonDAta events Manager
		//
	    public void addCommonDataEventListener(CommonDataEventListener listener) {
	        this.m_listeners.add(listener);
	    }
	 
	    public void removeCommonDataEventListener(CommonDataEventListener listener) {
	        this.m_listeners.remove(listener);
	    }
	    private void notifyListeners(String Modules,String Command) {
	        for (CommonDataEventListener commonDataEventListener: m_listeners) {
	        	commonDataEventListener.eventRead(new CommonDataChangesEventSource(this, Modules, Command));
	        }
	    }
		@Override
		public String getNodeSelected() {
			// TODO Auto-generated method stub
			return lastClickedNode;
		}
		@Override
		public void setNodeSelected(boolean aNodeisSelected, String nodeName) {
			// TODO Auto-generated method stub
			nodeIsSelected=aNodeisSelected;
			if (aNodeisSelected==true){
				lastClickedNode=nodeName;
			}
			else{
				lastClickedNode="none";
			}
		}
		@Override
		public String getNavigationMode() {
			// TODO Auto-generated method stub
			return m_navigationMode;
		}

	    //
}
