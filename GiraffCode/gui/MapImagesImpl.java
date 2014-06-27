package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import comms.MqttEventListener;

import semiautonomy.CommonDataChangesEventSource;
import semiautonomy.CommonDataEventListener;
import semiautonomy.SingletonCommonDataImpl;
import semiautonomy.SingletonSemiautonomyManagerImpl;
import semiautonomy.Utils;



//------------------------------------------------------------------
//
//	MAP IMAGES CLASS
//
//------------------------------------------------------------------

	
	
	/**
	 * @author Francisco Melendez
	 * @email fco.melendez@uma.es
	 * 
	 * This class is responsible for showing a geometric or schematic Map depending on the Plugin Mode (ENGINEER or USER)
	 * Robot localization is displayed for both modes
	 * Topology is displayed for ENGINEER mode 
	 */
	public class MapImagesImpl implements MapImages{
		public ImageIcon schematicMapIcon,geometricMapIcon;
		private SingletonCommonDataImpl m_commonData; 
		private SingletonSemiautonomyManagerImpl m_semiautonomyManager; 
		
		public MouseListener imageMouseListener;
	    public Utils m_utils;
	    Point robotLocation= new Point(0,0);
	    public double[] distances= new double[683];
	    public Map<String,String> arcsMap= new HashMap<String,String>();
	    public Map<String,String> searchArcsMap= new HashMap<String,String>();
	    public Map<Integer,Point> navigationPathPoints= new HashMap<Integer,Point>();
	    public Map<Integer,Point> drawPathPoints= new HashMap<Integer,Point>();
	    public JLabel m_mapImages;

	    private int incX=0,incY=0,numberOfNavigationPathPoints=0,numberOfdrawPathPoints=0,antX=0,antY=0;
	    boolean navigating=false;
	    
		public MapImagesImpl()
		{
			m_commonData= SingletonCommonDataImpl.getSingletonObject();
			m_semiautonomyManager= SingletonSemiautonomyManagerImpl.getSingletonObject(); 

			schematicMapIcon =new ImageIcon();//new ImageIcon(m_commonData.schematicMapPath);
			schematicMapIcon.setImage(m_commonData.imgSchematic);
			geometricMapIcon = new ImageIcon();
			geometricMapIcon.setImage(m_commonData.imgGeometric);
			m_utils= new Utils();
			arcsMap=m_commonData.topology.arcsMap.getMap();
			searchArcsMap=m_commonData.topology.arcsMap.getMap();
			imageMouseListener=new MouseListener() {
				boolean isAlreadyOneClick=false;
	            @Override
	            public void mouseClicked(MouseEvent e) {
	            	System.out.println("mouse Clicked");
	                int mouseX=e.getX();
	                int mouseY=e.getY();
	            	if(m_commonData.getPluginState().equals("RELOCALIZE_AT")){
	            		m_semiautonomyManager.relocalize(e.getPoint());
	            		m_commonData.setPluginState("PluginState.IDLE");
	            	}
	            	else if(!m_commonData.getNodeSelected().equals("none")){
	            		m_commonData.setNodeSelected(false,"none");
	                } 
	            	else if(m_commonData.getPluginMode().equals("ENGINEER")){
	            		int xpos1=0,xpos2=0,ypos1=0,ypos2=0;
	            	    String keyNode=" ";
	            	    String value=" ";
	            	    boolean found=false;
	        	        for (Map.Entry<String, String> entry : searchArcsMap.entrySet())
	        	        {   
	        	        	if(found==false){
		        	        	keyNode=entry.getKey();
		        	        	value=entry.getValue();
		        	            System.out.println("KEY:"+keyNode+",VALUE"+value);
		        	            if(keyNode.contains(":rep"))
		        	            {
		        		            String[] str=keyNode.split(":");
		        		            keyNode=str[0];
		        	            }
		        	            if(m_commonData.topology.nodesMap.containsKey(keyNode)){
		        	            xpos1=m_commonData.topology.nodesMap.get(keyNode).x;
		        	            ypos1=m_commonData.topology.nodesMap.get(keyNode).y;
		        	            xpos2=m_commonData.topology.nodesMap.get(entry.getValue()).x;
		        	            ypos2=m_commonData.topology.nodesMap.get(entry.getValue()).y;
		        	            }
		        	            if(m_mapImages.getGraphics().getClipBounds(new Rectangle(mouseX-2,mouseY-2,4,4)).intersectsLine(xpos1, ypos1, xpos2, ypos2)){
			                            UIManager.put("OptionPane.noButtonText", "No");
			                            UIManager.put("OptionPane.yesButtonText", "Yes");	                            
			                            if (showDeleteArcMsg()==JOptionPane.YES_OPTION){
			                            	deleteArc(keyNode,entry.getValue());
			                            }
			                            found=true;
		        	            }
	        	        	}
	        	        }
	                	if(isAlreadyOneClick){  
	                		m_semiautonomyManager.addNode(e.getPoint());
	            	        isAlreadyOneClick = false;
		            	}
		    	        else {
			    	        isAlreadyOneClick = true;
			    	        System.out.println("clicked");
			    	        Timer t = new Timer("doubleclickTimer", false);
			    	        t.schedule(new TimerTask() {
			    	            public void run() {
			    	            isAlreadyOneClick = false;
			    	            }
			    	        }, 500);
		    	        }
	        	    }

	            }

	            @Override
	            public void mousePressed(MouseEvent e) {	    
					//////// GO TO POINT CLICKING ON MAP //////////////
	            	
	            	/*double x=m_utils.transformToGeometricXCoord(e.getPoint(), m_commonData.mapWidth/2, m_commonData.mapHeight/2);
					double y=m_utils.transformToGeometricYCoord(e.getPoint(), m_commonData.mapWidth/2, m_commonData.mapHeight/2);
					String command="GoToPoint "+x+" "+y;
	                m_semiautonomyManager.goToPoint(command);*/
	            	
	            	
	            	/////////// PATH//////////////////
	            /*	if(navigating==true){
	            		m_semiautonomyManager.stopGiraff();
	            	}*/
	            }

	            @Override
	            public void mouseReleased(MouseEvent e) {
	            	/////////PATH////////////////
	            	/*System.out.println("mouse Released");
	            	startNavigation();*/

	            }

	            @Override
	            public void mouseEntered(MouseEvent e) {
	            }

	            @Override
	            public void mouseExited(MouseEvent e) {
	            }
	        };	
	        m_mapImages= new JLabel(){
	        	/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
	    	    protected void paintComponent(Graphics g2){
	    		    Graphics2D g=(Graphics2D) g2;
					super.paintComponent(g);
	    			int xpos1,xpos2,ypos1,ypos2;
	    		    String keyNode=" ";
	    		    Map<Integer,Point> m_drawPathPoints= drawPathPoints;
	    	        robotLocation=new Point(m_utils.transformToSchematicMapPoint(m_commonData.robotXplace,
	    	        		m_commonData.robotYplace,
	    					schematicMapIcon.getIconWidth()/2, 
	    					schematicMapIcon.getIconHeight()/2));	
	    	        //System.out.println("Robot at:"+robotLocation.getX()+","+robotLocation.getY());
	    		    if(m_commonData.getPluginMode().equals("USER") && this.getIcon()==geometricMapIcon){
	    		    	this.setIcon(schematicMapIcon);
	    		    	g.drawImage(schematicMapIcon.getImage(),0,0, null);  
	    		    }
	    		    else if(m_commonData.getPluginMode().equals("ENGINEER") && this.getIcon()==schematicMapIcon){
	    		    	this.setIcon(geometricMapIcon);
	    		    	g.drawImage(geometricMapIcon.getImage(),0,0, null);  
	    		    }
	    		    double angle= -(Math.PI*2/360)*(m_commonData.robotOrientation);        
	    		    int numpoints = 4;
	    		    
	    		    ///////////////////////
	    		    // Painting Laser Scans
	    		    ///
	    		    
	    			    int xLaser[]= new int[217];
	    			    int yLaser[]= new int[217];
	    			    int xFreeSpace[]= new int[217];
	    			    int yFreeSpace[]= new int[217];
	    			    int xUnreachableSpace[]= new int[217];
	    			    int yUnreachableSpace[]= new int[217];
	    			    int num=0;
	    			    for (num=0;num<=216;num++){
	    			    	double inc=distances[num]*100/3;
	    			    	if(inc>20){
	    			    		inc=20;
	    			    		xFreeSpace[num]=(int)(robotLocation.x + inc*Math.cos( angle + (2*Math.PI/3) - (4*Math.PI/3)*num/216 ));
	    				    	yFreeSpace[num]=(int)(robotLocation.y + inc*Math.sin( angle + (2*Math.PI/3) - (4*Math.PI/3)*num/216 ));
	    				    	xLaser[num]=robotLocation.x;
	    				    	yLaser[num]=robotLocation.y;
	    				    	xUnreachableSpace[num]=robotLocation.x;
	    				    	yUnreachableSpace[num]=robotLocation.y;
	    			    	}
	    			    	else
	    			    	{
	    			    		
	    				    	xFreeSpace[num]=robotLocation.x;
	    				    	yFreeSpace[num]=robotLocation.y;	
	    				    	xLaser[num]=(int)(robotLocation.x + inc*Math.cos( angle + (2*Math.PI/3) - (4*Math.PI/3)*num/216 ));
	    				    	yLaser[num]=(int)(robotLocation.y + inc*Math.sin( angle + (2*Math.PI/3) - (4*Math.PI/3)*num/216 ));
	    				    	inc=20;
	    				    	xUnreachableSpace[num]=(int)(robotLocation.x + inc*Math.cos( angle + (2*Math.PI/3) - (4*Math.PI/3)*num/216 ));;
	    				    	yUnreachableSpace[num]=(int)(robotLocation.y + inc*Math.sin( angle + (2*Math.PI/3) - (4*Math.PI/3)*num/216 ));;
	    			    	}

	    			    }
	    			    xLaser[0]=robotLocation.x;
	    			    yLaser[0]=robotLocation.y;
	    			    xLaser[216]=robotLocation.x;
	    			    yLaser[216]=robotLocation.y;
	    			    xFreeSpace[0]=robotLocation.x;
	    			    yFreeSpace[0]=robotLocation.y;
	    			    xFreeSpace[216]=robotLocation.x;
	    			    yFreeSpace[216]=robotLocation.y;
	    			   // UMA_GiraffPlugin.UMAClient.publish("ClientACK", 2, "alive");
	    		    	/*xLaser[137]=robotLocation.x;
	    		    	yLaser[137]=robotLocation.y;*/
	    			    g.setColor(new Color(255,175,175,150));
	    			    g.fillPolygon(xUnreachableSpace, yUnreachableSpace, 216);
	    			    g.setColor(new Color(255,100,100,200));
	    			    g.fillPolygon(xLaser, yLaser, 216);
	    			    g.setColor(new Color(255,0,0,255));
	    			    g.drawPolygon(xLaser, yLaser, 216);
	    			    g.setColor(new Color(175,175,255,100));
	    			    g.drawPolygon(xFreeSpace, yFreeSpace, 216);
	    		    
	    		    //
	    		    ///////////////////////
	    		    
	    		    
	    		    //g.setColor(new Color(175,175,255,125));
	    		    //g.fillOval((int)getRobotPose().getX()-13, (int)getRobotPose().getY()-13,26,26);
	    		    int xpointsarrayOut[] = {(int)(robotLocation.x+13*Math.cos(angle)), 
	    		                        (int)(robotLocation.x+13*Math.cos(angle-(4*Math.PI/6))),
	    		                         (int)(robotLocation.x),
	    		                            (int)(robotLocation.x+13*Math.cos(angle+(4*Math.PI/6)))};
	    		    int ypointsarrayOut[] = {(int)(13*Math.sin(angle)+robotLocation.y), 
	    		                        (int)(13*Math.sin(angle-(4*Math.PI/6))+robotLocation.y),
	    		                        (int)((int)getRobotPose().getY()),
	    		                            (int)(13*Math.sin(angle+(4*Math.PI/6))+robotLocation.y)};
	    		    g.setColor(new Color(200,200,255,100));
	    		    g.fillPolygon(xFreeSpace, yFreeSpace, 216);
	    		    //g.drawPolygon(xFreeSpace, yFreeSpace, 216);
	    		    
	    		    g.setColor(Color.CYAN);
	    		    g.fillPolygon(xpointsarrayOut, ypointsarrayOut, numpoints);
	    		    g.setColor(Color.BLACK); 
	    		    g.drawPolygon(xpointsarrayOut, ypointsarrayOut, numpoints);
	    		    g.drawOval((int)(robotLocation.x-5),(int)(robotLocation.y-5),10,10);
	    		    g.setColor(Color.BLUE);
	    		    g.fillOval((int)(robotLocation.x-5),(int)(robotLocation.y-5),10,10);
	    		    		    
	    		    if(m_commonData.getPluginMode().equals("ENGINEER") && !arcsMap.isEmpty()
	    		    		&& m_commonData.updatingTopology==false){
	    		    	for (Map.Entry<String, String> entry : arcsMap.entrySet())//java.util.ConcurrentModificationException-thrown
	    		        {   
	    		        	
	    		            keyNode=entry.getKey();
	    		            String valueNode=entry.getValue();
	    		            //System.out.println(keyNode);
	    		            if(keyNode.contains(":rep"))
	    		            {
	    			            String[] str=keyNode.split(":");
	    			            keyNode=str[0];
	    			        }
	    		            if(valueNode.contains(":rep"))
	    		            {
	    			            String[] str=valueNode.split(":");
	    			            valueNode=str[0];
	    			        }
	    		            if(m_commonData.topology.nodesMap.containsKey(keyNode) && m_commonData.topology.nodesMap.containsKey(valueNode))
	    		            {		            	
	    			            xpos1=m_commonData.topology.nodesMap.get(keyNode).x;
	    			            ypos1=m_commonData.topology.nodesMap.get(keyNode).y;
	    			            xpos2=m_commonData.topology.nodesMap.get(valueNode).x;
	    			            ypos2=m_commonData.topology.nodesMap.get(valueNode).y;
	    			            g.setColor(Color.ORANGE);
	    			            g.drawLine(xpos1, ypos1, xpos2, ypos2);
	    		            }
	    		            else{
	    		            	System.out.println("Error:"+keyNode);
	    		            }
	    		        }
	    		    }
	    		    if(!m_drawPathPoints.isEmpty()){
	    		    	int [] xpoints= new int[m_drawPathPoints.size()];
	    		    	int [] ypoints= new int[m_drawPathPoints.size()];
	    		    	for (Map.Entry<Integer, Point> entry : m_drawPathPoints.entrySet())//java.util.ConcurrentModificationException-thrown
	    		        { 
	    		    		xpoints[entry.getKey()]=entry.getValue().x;
	    		    		ypoints[entry.getKey()]=entry.getValue().y;
	    		        }
	    		    	
	    		    	g.setColor(Color.DARK_GRAY);
	    		    	
	    		    	float[] dash1 = { 2f, 0f, 2f };
	    		    	BasicStroke bs1 = new BasicStroke(2, BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND, 1.0f, dash1, 2f );
	    		    	g.setStroke(bs1);
	    		    	g.drawPolyline(xpoints, ypoints,m_drawPathPoints.size());
	    		    	g.fillOval(xpoints[0]-5, ypoints[0]-5, 10, 10);
	    		    	if(m_drawPathPoints.size()>1)
	    		    	{
	    		    	g.fillOval(xpoints[m_drawPathPoints.size()-1]-7, ypoints[m_drawPathPoints.size()-1]-7, 14, 14);
	    		    	g.setColor(Color.GREEN);
	    		    	g.fillOval(xpoints[m_drawPathPoints.size()-1]-5, ypoints[m_drawPathPoints.size()-1]-5, 10, 10);
	    		    	}
	    		    }
	    		    g.setColor(Color.GREEN);
	    		    g.fillOval(m_commonData.collaborativeTarget.x-4,m_commonData.collaborativeTarget.y-4, 8, 8);
	    	    }
	        };
	        m_mapImages.addMouseListener(imageMouseListener);
	        m_mapImages.addMouseMotionListener(new MouseMotionListener(){

	    		@Override
	    		public void mouseDragged(MouseEvent e) {
	    			// TODO Auto-generated method stub
	    			
	    			
	    			//////PATH/////////
	    		/*	if(navigating==false){
	    			storeNavigationPath(e.getX(),e.getY());
	    			}	*/
	    			
	    		}

	    		@Override
	    		public void mouseMoved(MouseEvent e) {
	    			// TODO Auto-generated method stub
	    			m_commonData.setImageMousePosition(e.getX(),e.getY());
	    		}
	        	
	        });
	        m_mapImages.setBorder(new BevelBorder(BevelBorder.RAISED));
	        m_mapImages.setLayout(null);
	        m_mapImages.setSize(schematicMapIcon.getIconWidth(),schematicMapIcon.getIconHeight()); 
	        m_mapImages.setIcon(schematicMapIcon); 
		}
		
		
		
		public int showDeleteArcMsg(){
	       int reply=JOptionPane.showConfirmDialog(m_mapImages,"Do you want to DELETE THIS ARC?");       
	       return reply;
		}
		
		
		public void deleteArc(String nodeA, String nodeB){
			m_semiautonomyManager.deleteArc(nodeA, nodeB);
		}

		public void addNamedNode(String name, double locationX, double locationY,boolean geometricPoint){
			MapNodeButtonImpl button;
		    double xPos=locationX,yPos=locationY;
		    Point p=new Point(m_utils.transformToSchematicMapPoint(xPos,yPos,schematicMapIcon.getIconWidth()/2,schematicMapIcon.getIconHeight()/2));
		    Point location=new Point(0,0);
		    if(geometricPoint){
		        location.setLocation(p.x,p.y);
		    }
		    else{
		        location.setLocation((int)(xPos),(int)(yPos));
		    }
		    button= new MapNodeButtonImpl(name,location);
		    m_mapImages.add(button.m_buttonLabel);
		    //this.setComponentZOrder(button,0);
		}
	    public void setCursorType(String str){
	    	if (str.equals("cross")){
	    		m_mapImages.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	    	}
	    	else if (str.equals("custom")){
	    		m_mapImages.setCursor(new Cursor(Cursor.CUSTOM_CURSOR));
	    	}
	    	else{
	    		m_mapImages.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    	}
	    }

	    public Point getRobotPose(){
	    	return robotLocation;
	    }
	    //////////////////////////////
	    // TEMPORAL
	    ///////////////////////////////
	    public void updateLaserInfo(String laser){
	    	//System.out.println(laser);  
	    	 laser=laser.replaceAll("Aperture ","");
	         laser=laser.replaceAll("Nsamples ","");
	         laser=laser.replaceAll("Values ","");
	         
			StringTokenizer laserData;        
	         laserData= new StringTokenizer(laser,"#");
//	         String aperture=laserData.nextToken("#");
	         //double d_aperture=Double.parseDouble(aperture);
	//         String samples=new String(laserData.nextToken("#"));
	         //int nsamples=Integer.parseInt(samples);
	         //samples=samples.replaceFirst(" ","");
	         String values=laserData.nextToken("#");
	         //values=values.replaceFirst(" ","");
	         //System.out.println(values);
	         StringTokenizer laserPoints;
	         laserPoints= new StringTokenizer(values," ");
	         int i=0;
	         double ant=0.00;
	         while (laserPoints.hasMoreTokens())
	         {
	        	if(i<682)
	        	{ 
	        	//System.out.println(distances[i]);
	        	distances[i]=Double.parseDouble(laserPoints.nextToken());
		        	
	        		if (distances[i]==0.00)
		        	{
		        		distances[i]=ant;
		        	}
	        		else{
	        		ant=distances[i];
	        		}
	        	}
	        	i++;
	         }
	         
	 }
	    
	    

	private void storeNavigationPath(int x, int y)
	{
		m_commonData.cancelNavigation=true;
		if(antX==0 && antY==0)
		{
			antX=x;
			antY=y;
		}
		else
		{			
			incX=incX+Math.abs(antX-x);
			incY=incY+Math.abs(antY-y);
			antX=x;
			antY=y;
		}
		double module= Math.sqrt(Math.pow(incX,2)+Math.pow(incY,2));
		if(module>10){
			incX=0;
			incY=0;
			navigationPathPoints.put(numberOfNavigationPathPoints,new Point (x,y));
			numberOfNavigationPathPoints++;
			
		}
		
		drawPathPoints.put(numberOfdrawPathPoints, new Point (x,y));
		numberOfdrawPathPoints++;
		

	}
	public void startNavigation()
	{
		Thread t0= new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int map=0;
				m_commonData.cancelNavigation=false;
				System.out.println("THREAD---->>>navigation Points received");
				while (map<navigationPathPoints.size() && m_commonData.cancelNavigation==false && m_commonData.pluginSTOP==false)
				{
					navigating=true;
					
					double x=m_utils.transformToGeometricXCoord(navigationPathPoints.get(map), m_commonData.mapWidth/2, m_commonData.mapHeight/2);
					double y=m_utils.transformToGeometricYCoord(navigationPathPoints.get(map), m_commonData.mapWidth/2, m_commonData.mapHeight/2);
					String command="GoToPoint "+x+" "+y;
					System.out.println("THREAD---->>>"+command);
	                m_semiautonomyManager.goToPoint(command);
	                Point p= navigationPathPoints.get(map);
					map++;
					System.out.println("THREAD---->>>"+"next point is Point number:"+map);
					//navigationPathPoints.remove(map);
					
					double dist=p.distance(robotLocation);
					while(dist>40 && m_commonData.cancelNavigation==false && m_commonData.pluginSTOP==false){
						dist=p.distance(robotLocation);
						try {
							Thread.sleep(100);
							System.out.println("THREAD---->>>"+"Navigating to:"+(map-1)+", path length is ->"+navigationPathPoints.size());
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
				///
            	navigationPathPoints.clear();
            	drawPathPoints.clear();
            	numberOfNavigationPathPoints=0;
            	numberOfdrawPathPoints=0;
            	antX=0;
            	antY=0;
            	incX=0;
            	incY=0;
            	navigating=false;
            	m_semiautonomyManager.stopGiraff();
            	System.out.println("THREAD---->>>"+"Path finished");
			}
			
		}, "Follow Path");
		t0.start();

	}
	    
}

