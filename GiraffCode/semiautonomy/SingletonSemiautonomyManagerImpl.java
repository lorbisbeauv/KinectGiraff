package semiautonomy;


import gui.SingletonMapViewportImpl;

import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import comms.MQTTClient;
import comms.MqttEventListener;
import comms.MqttEventSource;




public class SingletonSemiautonomyManagerImpl implements SingletonSemiautonomyManager{
	
	
	public MQTTClient m_UMAClient;
	private Utils m_utils;
	private SingletonCommonDataImpl m_commonData;
	public SemiautonomyMqttEventListener m_listener;
	private SingletonMapViewportImpl m_viewportPanel;
	private static SingletonSemiautonomyManagerImpl singletonObject;
	private Thread t;
	boolean first= true;
	public String currentTarget="none";
	
	/** A private Constructor prevents any other class from instantiating. */
	private SingletonSemiautonomyManagerImpl() {
		m_listener= new SemiautonomyMqttEventListener(); 
		m_commonData= SingletonCommonDataImpl.getSingletonObject();
		m_utils=new Utils();
	    m_UMAClient= new MQTTClient();
	    m_UMAClient.addMqttEventListener(m_listener);
	  /*  t= new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("MQTT Thread-> Wait for connection");
				while(m_commonData.pluginSTOP==false){
				System.out.println("MQTT Thread-> Request for Topology");
				getTopology();
				System.out.println("MQTT Thread-> waiting for Topology");
				System.out.println("MQTT Thread-> After pause for Topology");
				while(m_commonData.pluginSTOP==false){
					System.out.println("MQTT Thread-> Wait for connection 2 ");
					while(!threadClient.isConnected()){
						try {
							System.out.println("MQTT Thread-> Waiting for connection 2");
							Thread.sleep(50);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					System.out.println("MQTT Thread-> publishing ACK ");
					threadClient.publish("ClientACK", 2, "alive");
					System.out.println("MQTT Thread-> published ACK ");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			}
			}}, "Client ACK Sender");
		t.start();*/
	}
	public static synchronized SingletonSemiautonomyManagerImpl getSingletonObject() {
		if (singletonObject == null) {
			singletonObject = new SingletonSemiautonomyManagerImpl();
		}
		return singletonObject;
	}
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	public void destroySingleton(){
		singletonObject=null;
	}
	
	/////////////////////////////////////////////////////////////
	////
	//// USER EVENTS
	////
	/////////////////////////////////////////////////////////////

	
	/////////////////////////////////////
	// Topology Management Methods
	//
	
	@Override
	public void getTopology() {
		// TODO Auto-generated method stub
            String topic="TopologyCommand";
            String command="GetTopology";
            int qos=2;
            m_UMAClient.publish(topic,qos,command);
	
	}

	@Override
	public void loadTopology(){
		// TODO Auto-generated method stub
		String newName="NODE";
		newName=JOptionPane.showInputDialog("LOAD: Please, insert a TOPOLOGY NAME");
            String topic="TopologyCommand";
            String command="LOAD_GRAPH "+newName;
            int qos=2;
            m_UMAClient.publish(topic,qos,command);	
	}

	@Override
	public void saveTopology() {
		// TODO Auto-generated method stub
		String newName="NODE";
		newName=JOptionPane.showInputDialog("SAVE: Please, insert a TOPOLOGY NAME");
            String topic="TopologyCommand";
            String command="SAVE_GRAPH "+newName;
            int qos=2;
            m_UMAClient.publish(topic,qos,command);	
	}
	//
	//End of Topology Management Methods
	/////////////////////////////////////////
	
		
	////////////////////////////////////////
	// Graph Edit Methods
	//
	@Override
	public void addNode(Point nodeLocation) {
		// TODO Auto-generated method stub
		String type="Place";
		int i=1;
		boolean freeNameFound=false;
		 System.out.println("Add Node geometric Received:"+nodeLocation.getLocation().toString());
	 		double x=m_utils.transformToGeometricXCoord(nodeLocation, 
					m_commonData.mapWidth/2,
					m_commonData.mapHeight/2);
	 		double y=m_utils.transformToGeometricYCoord(nodeLocation, 
				    m_commonData.mapWidth/2,
					m_commonData.mapHeight/2);
		System.out.println("Add Node schematic obtained:"+x+","+y);
		while(freeNameFound==false)
        {   
        	String name="NODE"+i;
        	if(m_commonData.topology.nodesMap.containsKey(name)){
        		i++;
        	}
        	else{
        		freeNameFound=true;
                    String topic="TopologyCommand";
                    String command="ADD_NODE "+name+" "+type+" "+x+" "+y;
                    int qos=2;
                    m_UMAClient.publish(topic,qos,command);
        	}
        }
	}

	@Override
	public void deleteNode(String name) {
		// TODO Auto-generated method stub
		if(!name.equals("Docking")){
            String topic="TopologyCommand";
            String command="REMOVE_NODE "+name;
            int qos=2;
            m_UMAClient.publish(topic,qos,command);
		}
		else{
			System.out.println("DOCKING NODE can not be deleted");
		}
	}

	@Override
	public void moveNode(String name, Point newPosition) {
		// TODO Auto-generated method stub
 		double x=m_utils.transformToGeometricXCoord(newPosition, 
				m_commonData.mapWidth/2,
				m_commonData.mapHeight/2);
 		double y=m_utils.transformToGeometricYCoord(newPosition, 
			    m_commonData.mapWidth/2,
				m_commonData.mapHeight/2);
        	String topic="TopologyCommand";
            String command="MOVE_NODE "+name+" "+x+" "+y ;
            int qos=2;
            m_UMAClient.publish(topic,qos,command);
	}

	@Override
	public void renameNode(String oldName) {
		// TODO Auto-generated method stub
		if(!oldName.equals("Docking")){
			String newName="NODE";
			newName=JOptionPane.showInputDialog("Please, insert the NEW NAME");
			while(m_commonData.topology.nodesMap.containsKey(newName)){
				newName=JOptionPane.showInputDialog("This name already exists in the Topolgy. Please, insert a new name");
				System.out.println("NEWNAME:"+newName);
			}
			if (newName==null){
				newName=oldName;
			}
			else if (newName.contains("NODE")||newName.contains("node")){
				int i=1;
				newName="NODE"+i;
				while(m_commonData.topology.nodesMap.containsKey(newName)){
					i++;
					newName="NODE"+i;
				}
			}
	        String topic="TopologyCommand";
	        String command="CHANGE_NODE_LABEL "+oldName+" "+newName;
	        int qos=2;
	        m_UMAClient.publish(topic,qos,command);	
		}
		else{
			System.out.println("DOCKING NODE can not be deleted");
		}
	}

	@Override
	public void addArc(String nodeA, String nodeB) {
		// TODO Auto-generated method stub
            String topic="TopologyCommand";
            String command="ADD_ARC "+nodeA+" "+nodeB+" "+"Navegability";
            System.out.println(command);
            int qos=2;
            m_UMAClient.publish(topic,qos,command);
		
	}

	@Override
	public void deleteArc(String nodeA, String nodeB){
		// TODO Auto-generated method stub
            String topic="TopologyCommand";
            String command="REMOVE_ARC "+nodeA+" "+nodeB+" "+"Navegability";
            int qos=2;
            m_UMAClient.publish(topic,qos,command);		
	}
	//
	// End of Graph Edit Methods
	///////////////////////////////
		
	///////////////////////////////
	// Navigation Commands Methods
	//
	@Override
	public void stopGiraff() {
		// TODO Auto-generated method stub
		/*String firsttopic="NavigationCommand";
        String firstcommand="Motion 0.0 0.0";
        int firstqos=2;            
        m_UMAClient.publish(firsttopic,firstqos,firstcommand);*/
        
        String topic="NavigationCommand";
        String command="StopGiraff";
        int qos=2;    
        m_commonData.cancelNavigation=true;
        m_UMAClient.publish(topic,qos,command);
	}

	@Override
	public void goTo(String target, boolean requestConfirmation) {
		// TODO Auto-generated method stub
		String message="GoToNode "+target;
		String topic="NavigationCommand";
		int reply=555;
		if(requestConfirmation==true)
		{
	        
			UIManager.put("OptionPane.cancelButtonText", "Cancel");
	        UIManager.put("OptionPane.noButtonText", "No");
	        UIManager.put("OptionPane.okButtonText", "OK");
	        UIManager.put("OptionPane.yesButtonText", "Yes");
	        reply=JOptionPane.showConfirmDialog(null,"Do you want to START A NAVIGATION to this Point?");
	        
	        if (reply==JOptionPane.OK_OPTION)
	        {
	            m_commonData.setPluginState("GOTO_LABEL");
	            m_UMAClient.publish(topic,2,message);
	            m_commonData.setPluginState("IDLE");
	            m_commonData.viewportFollowsRobot=true;
	        }
	        else if( reply==JOptionPane.CANCEL_OPTION || reply==JOptionPane.NO_OPTION )
	        {
	        	System.out.println("Navigation cancelled by the user");
	        }
	        
		}
		else
		{
			
            m_commonData.setPluginState("GOTO_LABEL");
            m_UMAClient.publish(topic,2,message);
            m_commonData.setPluginState("IDLE");
            m_commonData.viewportFollowsRobot=true;
		
		}
	}
	
	@Override
	public void goTo(Point p) {
		// TODO Auto-generated method stub
	}
	@Override
	public void relocalize (Point p) {
		// TODO Auto-generated method stub
 		double x=m_utils.transformToGeometricXCoord(p,
 				m_commonData.mapWidth/2,
 				m_commonData.mapHeight/2);
 		double y=m_utils.transformToGeometricYCoord(p,
 		 				m_commonData.mapWidth,
 		 				m_commonData.mapHeight/2);
	         double x1=0.0,y1=0.0,x2=0.0,y2=0.00;
	         x1=x-0.25;
	         x2=x+0.25;
	         y1=y-0.25;
	         y2=y+0.25;
	         
	             String topic="NavigationCommand";
	             String command="Relocalize ["+x1+" "+x2+" "+y1+" "+y2+" "+50000+"]";
	             int qos=2;            
	             m_UMAClient.publish(topic,qos,command);
		
	}
	@Override
	public void motors(String command) {
		// TODO Auto-generated method stub
	}
	//	
	// End of Navigation Command Methods
	//////////////////////////////////////
	

	//////////////////////////////////////////////////////////////////////
	////
	////		ROBOT EVENTS
	////
	//////////////////////////////////////////////////////////////////////

	@Override
	public void setLocalization(double x, double y, double phi) {
		// TODO Auto-generated method stub
		m_commonData.setLocalization(x, y, phi);
	}
	@Override
	public void setTopology(String topology) {
		// TODO Auto-generated method stub
		m_commonData.setTopology(topology);
	}
	@Override
	public void setNavigationMode(String modeMessage) {
		// TODO Auto-generated method stub
		m_commonData.setNavigationMode(modeMessage);
	}
	@Override
	public void setLaserScan(String laserReads) {
		// TODO Auto-generated method stub
		m_viewportPanel.m_mapLabel.updateLaserInfo(laserReads);
	}
	
	//////////////////////////////////////
	//
	//	Class Mqtt Event Listener
	//
	/////////////////////////////////////
	
	private class SemiautonomyMqttEventListener implements MqttEventListener {
		String m_topic="empty";
		String m_message="empty";
		
		@Override
		public void eventRead(MqttEventSource eventSource) {
		// TODO Auto-generated method stub
			m_topic=eventSource.getTopic();
			m_message= eventSource.getMessage();
			StringTokenizer subelementos;
			long milis= System.currentTimeMillis();
	        String time= new java.sql.Timestamp(milis).toString();  
	        /*if (m_topic.contains("Localization")){
	               if (m_message.startsWith("New")){
	            	   // NAAS.debug_message="detectado";
	               }
	               else{
	            	  /// Eliminamos lugar topologico
	               String sub_m_message=m_message;
	               StringTokenizer  eliminar_location= new StringTokenizer(sub_m_message,"]");
	               sub_m_message=eliminar_location.nextToken();
	               
	               int l=sub_m_message.length();
	               sub_m_message =sub_m_message.substring(1,l-1);
	               subelementos= new StringTokenizer(sub_m_message," ");
	               double x=Double.parseDouble(subelementos.nextToken(" "));
	               double y=Double.parseDouble(subelementos.nextToken(" "));
	               double phi=Double.parseDouble(subelementos.nextToken(" "));
		        		setLocalization(x,y,phi);			        		
	               } 

	        }  */ 
	        if (m_topic.contains("Status")){
	               if (m_message.startsWith("New")){
	            	   // NAAS.debug_message="detectado";
	               }
	               else{
	            	  /// Eliminamos lugar topologico
		               String sub_m_message=m_message;
		               StringTokenizer  eliminar_location= new StringTokenizer(sub_m_message,"]");
		               sub_m_message=eliminar_location.nextToken();
		               
		               int l=sub_m_message.length();
		               sub_m_message =sub_m_message.substring(1,l-1);
		               subelementos= new StringTokenizer(sub_m_message," ");
		               double x=Double.parseDouble(subelementos.nextToken(" "));
		               double y=Double.parseDouble(subelementos.nextToken(" "));
		               double phi=Double.parseDouble(subelementos.nextToken(" "));
		               setLocalization(x,y,phi);
		               if (m_message.contains("Auto")){
		            	   m_commonData.setNavigationMode("Auto");
		               }
		               else{
		            	   m_commonData.setNavigationMode("Manual");
		            	   first=true;
		               }
		               if(m_commonData.randomNav==true){
		            	   if(first==true){
			            		first=false;
								int size=m_commonData.topology.nodesNameMap.size();
								while(size<=0){
									size=m_commonData.topology.nodesNameMap.size();
								}
								Random randomGenerator = new Random();
							    int randomInt = randomGenerator.nextInt(size);
								currentTarget=m_commonData.topology.nodesNameMap.get(randomInt);  
								System.out.println("TARGET:----->>>>>"+currentTarget+" "+randomInt);
		                        goTo(currentTarget,false); 
		            	   }
		            	   else if(m_message.contains(currentTarget)){
								int size=m_commonData.topology.nodesNameMap.size();
								while(size<=0){
									size=m_commonData.topology.nodesNameMap.size();
								}
								Random randomGenerator = new Random();
							    int randomInt = randomGenerator.nextInt(size);
								currentTarget=m_commonData.topology.nodesNameMap.get(randomInt);
		                    	m_viewportPanel.findRobotButton.setSelected(true);	                       
		                        goTo(currentTarget,false); 
		            	   }
			            }
		        		
	               } 

	        }   
	        else if (m_topic.contains("Topology")){           
	            
	        	if (m_message.startsWith("Graph")){
	                String graph=m_message.substring(5);
	                setTopology(graph);	                
	                ////System.out.println("SEMIAUTONOMY LISTENER MESSAGE-->Topology, event thown to Update Viewport");
	                ////m_viewportPanel.updateViewport();
	            }
	        	else{
	        		System.out.println("Topology data corrupted");
	        	}
	        }
	        else if (m_topic.contains("ErrorMsg")){
	        	System.out.println("SEMIAUTONOMY LISTENER MESSAGE--> Time:\t" +time +
	                    "  Topic:\t" + m_topic+ 
	                    "  Message:\t" + m_message);                                            
	        }
	        else if (m_topic.contains("NavigationMode")){
	        	/*leer Navigation Mode
	        	 * Manual-> Hago lo de antes
	        	 * auto-> despues de | destino final, target-> setopenMORAcontrols(true)
	        	 */
	        	String message=m_message;
	        	setNavigationMode(message);
	        }
	        else if (m_topic.contains("LaserScan")){    
	        	////System.out.println("SEMIAUTONOMY LISTENER MESSAGE-->LaserScan, event thown to Update Obstacles");
	        	////setLaserScan(m_message);
	        }	
			
		}
		
	}
	//////////////////////////
	//End of Class Mqtt Event Listener
	//////////////////////////
	
	public void goToPoint(String command){
		String topic="NavigationCommand";
		if(m_UMAClient.isConnected()){
		m_UMAClient.publish(topic, 0, command);
		}
	}
	public void up(){
		String topic="NavigationCommand";
        String command="Motion 0.1 0.0";
        int qos=0;            
        m_UMAClient.publish(topic,qos,command);
   	}
	public void down(){
		String topic="NavigationCommand";
        String command="Motion -0.05 0.0";
        int qos=0;            
        m_UMAClient.publish(topic,qos,command);
   	}
	public void left(){
		String topic="NavigationCommand";
        String command="Motion 0.0 0.15";
        int qos=0;            
        m_UMAClient.publish(topic,qos,command);
   	}
	public void right(){
		String topic="NavigationCommand";
        String command="Motion 0.0 -0.15";
        int qos=0;            
        m_UMAClient.publish(topic,qos,command);
   	}
	public void left(double module){
		String topic="NavigationCommand";
		Double d=0.80;
		d=d*module;
        String command="Motion 0.0 "+d;
        int qos=0;            
        m_UMAClient.publish(topic,qos,command);
   	}
	public void right(double module){
		String topic="NavigationCommand";
		Double d=-0.80;
		d=d*module;
		String command="Motion 0.0 "+d;
        int qos=0;            
        m_UMAClient.publish(topic,qos,command);
   	}
	public void stopMotion() {
		// TODO Auto-generated method stub
		String firsttopic="NavigationCommand";
        String firstcommand="Motion 0.0 0.0";
        int firstqos=2;            
        m_UMAClient.publish(firsttopic,firstqos,firstcommand);
	}
	public void motion(int x,int y) {
		// TODO Auto-generated method stub
		String w="0.0",v="0.0";
		double vlin=0.0;
		boolean skipManual=false;
		double ang=0.0;
		if(y<600 && y>450){
			v="-0.15";
		}
		else if(y<=450 && y>300){
			v="0.00";
		}
		else if(y<=300 && y>200){
			v="0.15";
		}
		else if(y<=200 && y>125){
			v="0.20";
		}
		else if(y<=125 && y>75){
			v="0.25";
		}
		else if(y<=75 && y>0){
			v="0.40";
		}
		
		if(x<800 && x>425){
			if(!v.equals(new String("-0.15"))){
				double wrad=0.80*(x-375)/425;
				w="-"+new Double(wrad).toString();
			}
			else{
				double wrad=0.80*(425-x)/425;
				w=new Double(wrad).toString();	
			}
			ang=-45.00;
			
		}
		else if(x<=425 && x>375){
			w="-0.0";
		}
		else if(x<=425 && x>0){
			if(!v.equals(new String("-0.15"))){
				double wrad=0.80*(425-x)/425;
				w=new Double(wrad).toString();
			}
			else{
				double wrad=0.80*(x-375)/425;
				w="-"+new Double(wrad).toString();
			}
			ang=45.00;
		}
		
        String topic="NavigationCommand";
        String command="Motion "+v+" "+w;
        int qos=0;    
		//System.out.println("module:"+new Double(module).toString()+" alpha:"+new Double(alpha).toString()+" vel:"+new Double(v).toString()+" w:"+new Double(w).toString());
                    
        m_UMAClient.publish(topic,qos,command);

		
	}

}
