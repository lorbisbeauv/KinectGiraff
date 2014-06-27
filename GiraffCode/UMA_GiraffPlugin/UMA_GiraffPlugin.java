package UMA_GiraffPlugin; 

import gui.SingletonMapViewportImpl;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.giraff.pilot20.dockingassistant.DockingStationTracker;
import org.giraff.pilot20.drive.plugin.GiraffDriveAPI;
import org.giraff.pilot20.drive.plugin.GiraffDrivePlugin;

import comms.MQTTClient;

import semiautonomy.SingletonCommonDataImpl;
import semiautonomy.SingletonSemiautonomyManagerImpl;
import semiautonomy.Utils;




public class UMA_GiraffPlugin implements GiraffDrivePlugin{
 	// Time to wait from a mouse down to actually start driving.
	// Introduced to separate driving with mouse down from clicking.
	public static final long MOUSE_DOWN_TO_START_DRIVE_DELAY_MS = 400;
	
	// Resolution of the head tilt
	public static final int TILT_RESOLUTION = 15;
	
	private boolean m_doPaint = false;
	
	// Used to control the giraff.
	private volatile GiraffDriveAPI m_drive;
	
	// Current mouse location (-1 means not in frame)
	private int m_currentX = -1;
	private int m_currentY = -1;

	// Left button state. Timestamp used to filter out double clicks
	private boolean m_leftButtonDown = false;
	private long m_leftButtonDownTimeStamp = Long.MAX_VALUE;
	
	// Tilt state.
	// Tilt is represented by a number in the range [0, TILT_RESOLUTION] where TILT_RESOLUTION is looking 
	// maximum up.
	private int m_currentTilt = TILT_RESOLUTION / 2;
	///////////////////////////////////////////////////////////
	// ASSISTED DOCKING Variables
	///////////////////////////////////////////////////////////
    private static final int MAX_TARGET_DELAY_MS = 1000;    // Max time since last target before exiting ASSISTED_MODE
    
    
    private enum DriveMode {NORMAL, ASSISTED_DOCKING}
    
    private DriveMode m_mode = DriveMode.NORMAL;
	
	private DockingStationTracker m_tracker;

	// Determines if we are locked to the docking target or not.
	public boolean m_isTargetLocked = false;
	public Rectangle m_currentDockingTarget = null;            // Keeps the docking target in image coordinates
	public Rectangle m_currentDockingTargetMouseCoord = null;  // Keeps the docking target in window coordinates (same as mouse events)
	public long m_currentDockingTargetTimestamp = 0;

	// Keeps the size of the last remote image received.
	private Rectangle m_imageSize = new Rectangle(0, 0, 320, 240);
    private Rectangle m_lastStoredTrackerRect;
    
    
	///////////////////////////////////////////////////////////
	// UMA Variables
	///////////////////////////////////////////////////////////
    private SingletonCommonDataImpl commonData;
    private SingletonSemiautonomyManagerImpl semiautonomyManager;
    private SingletonMapViewportImpl mapViewport;
   // private MQTTClient UMAClient;
    static JPanel rightInfoPanel;
    static boolean MQTTClientBusy=false, disableButtons=false;
	boolean overScreenButtons=false; 
	static String topologicalPlace="unknown";
	private int viewportWidth=0,viewportHeight=0;
	private int lastX=0,lastY=0;
	int n=0;
	private Double m_currentModule=0.0, m_currentAng=0.0;
	private Utils m_utils;
	Image schematicMapImage,geometricMapImage;
	////////////////////////////////////////////////////////////
	// GiraffViewPlugin implementation
	////////////////////////////////////////////////////////////
	

	@Override
    public String getDisplayName() {
        return "OpenMORA plugin";
    }
    
	@Override
	public void start() {
	
		System.out.println("-----------------------------------");
		System.out.println("-----------------------------------");
		System.out.println("-Start has been called:"+n+"times--");
		System.out.println("-----------------------------------");
		System.out.println("-----------------------------------");
		commonData= SingletonCommonDataImpl.getSingletonObject();
		schematicMapImage = Toolkit.getDefaultToolkit().getImage("./plugins/schematicMap.png");
		geometricMapImage =  Toolkit.getDefaultToolkit().getImage("./plugins/geometricMap.png");
		commonData.imgSchematic=schematicMapImage;
		commonData.imgGeometric=geometricMapImage;
		commonData.start();
		m_utils= new Utils();
		//UMAClient= new MQTTClient();
        commonData.pluginSTOP=false;
        System.out.println("CommonDataImpl created");
        mapViewport= SingletonMapViewportImpl.getSingletonObject();
        System.out.println("Map Viewport created");
        semiautonomyManager= SingletonSemiautonomyManagerImpl.getSingletonObject();
        semiautonomyManager.getTopology();
        System.out.println("Semiautonomy Manager created");
        rightInfoPanel= new JPanel();  
        System.out.println("-rightInfoPanel created");
        
        
        m_doPaint = true;
		m_tracker = new DockingStationTracker();

        rightInfoPanel.setLayout(new BorderLayout());
		rightInfoPanel.add(mapViewport,BorderLayout.CENTER);

		
		
		
	}

	@Override
	public void stop() {
		// No threads running, so just make sure we stop driving the giraff and stop painting.
		



        System.out.println("-rightInfoPanel DESTROYED");
        semiautonomyManager.stopGiraff();
		semiautonomyManager.m_UMAClient.disconnect();
		semiautonomyManager.destroySingleton();
        semiautonomyManager= null;
        System.out.println("Semiautonomy DESTROYED"); 
        mapViewport.destroySingleton();
        mapViewport= null;
        System.out.println("Map Viewport DESTROYED");
		//UMAClient=null;
		//System.out.println("UMAClient DESTROYED");
        
		commonData.pluginSTOP=true;
	    commonData.destroySingleton();
		commonData= null;
        System.out.println("CommonDataImpl DESTROYED");

		System.out.println("///////////////////////////////////");
		System.out.println("///////////////////////////////////");
		System.out.println("//STOP has been called:"+n+"times//");
		System.out.println("///////////////////////////////////");
		System.out.println("///////////////////////////////////");
		n--;
		m_doPaint = false;
		m_drive = null;
		if (m_tracker != null){
			m_tracker.stop();
			m_tracker = null;
		}
        rightInfoPanel= null; 
	}
	/**
	 * 
	 * @param rect				Rect in image coordinates
	 * @param viewPort			Current view port (where image is displayed) in screen coordinates.
	 * @param imageScaleFactor  Rescale factor.
	 * 
	 * @return  Rectangle suitable for screen display.
	 */
	/*private Rectangle getScreenRect(Rectangle rect, Rectangle viewPort, float imageScaleFactor){
		return new Rectangle( 
				(int)(imageScaleFactor * rect.x + viewPort.x),
                (int)(imageScaleFactor * rect.y + viewPort.y),
                (int)(imageScaleFactor * rect.width), 
                (int)(imageScaleFactor * rect.height));
	}*/

	
	/////////////////////////////////////////////////////////////////////
	// Display constants and custom paint
	/////////////////////////////////////////////////////////////////////

	private static final Color COLOR_DRIVING = new Color(100, 250, 50, 150);
	private static final Color COLOR_IDLE = new Color(50, 150, 50, 150);
	@Override
	public void paintOverlay(Graphics2D g, Rectangle videoViewPort) {
		if (m_doPaint == true){
			viewportWidth=videoViewPort.getBounds().width;
			viewportHeight=videoViewPort.getBounds().height;
            Float level;
            level = (m_drive.getBatteryStatus()*100);
            int level_percent=level.intValue();
            int baseline=0;
			//Messages displayed on the ViewPort
            String messageNavigationMode="USER";
            String messageModeAutoSTOP="CLICK on the Screen to STOP";
            String messageModeCollaborativeSTOP="RELEASE Mouse Left Button to STOP";
            String messageBatteryLevel = "BATTERY: "+level_percent+"%";
            String messageUndock = "CLICK HERE to UNDOCK THE GIRAFF";
            String messageMoveBackwards = "MOVE BACKWARDS";
            String messageTurnAroundLeft= "<< TURN";
            String messageTurnAroundRight= "TURN >>";
            // Measure text output
			g.setFont(new Font("Arial",Font.PLAIN, 12));
			FontMetrics metrics = g.getFontMetrics();		
			g.setFont(new Font("Arial",Font.BOLD,16));
			FontMetrics centralMetrics = g.getFontMetrics();
			g.setFont(new Font("Arial",Font.BOLD, 16));
			FontMetrics buttonMetrics = g.getFontMetrics();
			g.setFont(new Font("Arial",Font.BOLD, 50));
			FontMetrics mainMetrics = g.getFontMetrics();

            // Make sure I don't paint outside the video frame
			Rectangle oldClip = g.getClipBounds();
			g.setClip(videoViewPort);
			

            if(!commonData.getNavigationMode().contains("Manual") && commonData.collaborativeControlEnabled==false){
            	messageNavigationMode="AUTO";
            	//System.out.println("PAINT METHOD:"+"message1-"+messageNavigationMode);
            }
            else if(commonData.collaborativeControlEnabled==true){
            	
            	String mod=m_currentModule.toString();
            	if(mod.length()>6)
            	mod=mod.substring(0, 6);
            	String ang=m_currentAng.toString();
            	if(ang.length()>6)
            	ang=ang.substring(0, 6);
            	messageNavigationMode="COLLABORATIVE Control";//+m_currentX+","+m_currentY+" m:"+mod+" a:"+ang;
            	//System.out.println("PAINT METHOD:"+"message1.2-"+messageNavigationMode);
            }
			
			paintBatteryLevel(g,videoViewPort,messageBatteryLevel, baseline, metrics);

			// Place and Write UNDOCK message
			if(m_drive.isCharging()){
				// REset Tilt Position after the docking procedure
	            m_currentTilt= (TILT_RESOLUTION / 2);
	            syncHeadTilt();
	            paintChargingScreen(g,videoViewPort, baseline, messageUndock,metrics,centralMetrics,buttonMetrics,mainMetrics);
	            //System.out.println("PAINT METHOD:"+"message2-"+"paintChargingScreen");
			}
			else{
				// Place and Write MODE AUTO STOP message
				if(messageNavigationMode.contains("AUTO")){
					paintAutoScreen(g,videoViewPort, baseline, messageModeAutoSTOP,metrics,centralMetrics,buttonMetrics,mainMetrics);	
					//System.out.println("PAINT METHOD:"+"message2.1.1-"+"paintAutoScreen");
				}
				else if(messageNavigationMode.contains("COLLABORATIVE")){
					paintCollaborativeScreen(g,videoViewPort, baseline,messageNavigationMode, messageModeCollaborativeSTOP,metrics,centralMetrics,buttonMetrics,mainMetrics);
					//System.out.println("PAINT METHOD:"+"message2.1.2-"+"paintCollaborativeScreen");
				}
				else{						
					if(disableButtons==false){	
						//System.out.println("PAINT METHOD:"+"message2.1.3a-"+"paintScreenButtons");
						paintScreenButtons(g, videoViewPort,baseline,messageMoveBackwards,messageTurnAroundLeft, messageTurnAroundRight, buttonMetrics);							
					}
					paintDriveLine(g,videoViewPort);					        
			        if (m_tracker != null){
			        	//System.out.println("PAINT METHOD:"+"message2.1.3b-"+"paintDockingInfo");
			        	paintDockingInfo(g,videoViewPort);
					}
				}
			}
			// Restore the clip bounds.	
			g.setClip(oldClip);
		}
			
	}

	@Override
	public JPanel getSettingsPanel(){
		// Ignore
		return null;
	}

	@Override
	public JPanel getRightInfoPanel() {
            // TODO Auto-generated method stub
            //////////////////////////////////////////////////////////////////////////////////// 
            rightInfoPanel.setMinimumSize(new Dimension(350,600));           
            rightInfoPanel.setMaximumSize(new Dimension(500,800));
			rightInfoPanel.setPreferredSize(new Dimension(400,700));
            return rightInfoPanel;
	}

	@Override
	public void setRemoteImage(BufferedImage image) {
        
		// Store the image size for later reference
		m_imageSize = new Rectangle(0, 0, image.getWidth(), image.getHeight());
		
		// Give image reference to the tracker
		if (m_tracker != null){
			m_tracker.setCurrentImage(image);
		}
	}
	
	////////////////////////////////////////////////////////////
	// Mouse event listener implementations
	////////////////////////////////////////////////////////////
	
	
	@Override
	public void mouseClicked(MouseEvent e) {
	    // Look for double clicks in current target to enter Mode.ASSISTED_DOCKING
	    if (e.getButton() ==  MouseEvent.BUTTON1) 
	    {
			if(m_drive.isCharging()){
				m_drive.undock();
			}
			else
			{
	        
				Point point = convertMouse(e.getX(), e.getY());
	            
	            if (point != null)
	            {
	                // Double click in target means enter ASSISTED_MODE
	                if (m_currentDockingTargetMouseCoord != null && e.getClickCount() == 2 && m_currentDockingTargetMouseCoord.contains(e.getX(), e.getY())){
	                    // Enter ASSISTED_MODE
	                    setMode(DriveMode.ASSISTED_DOCKING);
	                    
	                }	                
	                if (m_mode == DriveMode.ASSISTED_DOCKING){
	                    if (e.getClickCount() == 2){
	                        // Handled above
	                    } else{
	                        // Exit assisted docking?
	                        if (!m_currentDockingTargetMouseCoord.contains(e.getX(), e.getY())){
	                            setMode(DriveMode.NORMAL);
	                        }
	                    }
	                }
	                
	                // NOTE: New if statement as mode can have changed.
	                if (m_mode == DriveMode.NORMAL){
	        			// React to left button double clicks.
	            		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2){
	            			if (m_drive != null){
	            				m_drive.rotate(e.getX(), e.getY());
	            			}
	                                                      //  }
	            		}
	                }	            	
	            }
	            
			}
            
        }

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// Ignore
	}

	@Override
	public void mouseExited(MouseEvent e) {
		m_currentX = -1;
		m_currentY = -1;
		if(m_currentTilt>TILT_RESOLUTION / 2 && m_mode != DriveMode.ASSISTED_DOCKING){
			m_currentTilt=TILT_RESOLUTION / 2;
			syncHeadTilt();	
		}
		syncHeadTilt();
		resetLeftButtonStatus();
		/*
		if (m_drive != null){
			m_drive.stop();
		}*/
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// Check if this was the left button.
		if (e.getButton() == MouseEvent.BUTTON1){
			m_leftButtonDown  = true;
			m_leftButtonDownTimeStamp = System.currentTimeMillis();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// Check if this was the left button.
	
			if (commonData.collaborativeControlEnabled==true){
				commonData.collaborativeTarget.setLocation(0,0);
				semiautonomyManager.stopGiraff();
			}
			resetLeftButtonStatus();
				lastX=0;
				lastY=0;
			// Stop drive motion!				
			if (this.m_drive != null){
				if(this.m_drive.isCharging()==false){
				this.m_drive.stop();
				disableButtons=false;
				}
			}
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// Update last location
		m_currentX = e.getX();
		m_currentY = e.getY();
		
		//drive(e.getX(), e.getY());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// Update the last mouse location
		m_currentX = e.getX();
		m_currentY = e.getY();	
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (m_drive != null){
			m_currentTilt += e.getWheelRotation();
			
			// Keep in range [0, 1000]
			if (m_currentTilt < 0){
				m_currentTilt = 0;
			} else if (m_currentTilt > TILT_RESOLUTION){
				m_currentTilt = TILT_RESOLUTION;
			}
			
			syncHeadTilt();
		}
	}

	
	////////////////////////////////////////////////////////////
	// GiraffDrivePlugin implementation
	////////////////////////////////////////////////////////////
	
	@Override
	public void setLocalImage(BufferedImage image) {
		// Not used by this plugin.
	}

	public void setDriveAPI(GiraffDriveAPI api) {
		this.m_drive = api;
		if (api != null){
			// Get a known state of the head tilt.
			api.tiltHead(0);
		}
	}

	@Override
	public void pulse() {
		// NOTE: The giraff can only do one thing at the time. 
		// If mouse button is pressed keep drivin
		// NOTE: The giraff can only do one thing at the time. 
        // Check if still in assisted docking mode
			if (m_mode == DriveMode.ASSISTED_DOCKING){        
				// ASSISTED DOCKING
				Point p = getDockingPoint();
				// If mouse button is pressed keep driving
				if (p != null && m_leftButtonDown == true && !overScreenButtons){
				    drive(p.x, p.y);
				}                
				// Track in y-direction
				Rectangle rect = m_tracker.getBestTarget();
				
				if (rect != null){
				    if (rect != m_lastStoredTrackerRect){
				        m_lastStoredTrackerRect = rect;
				        
				        if (m_currentTilt <= 0){
				            // Track the target in the y-direction
				            if (rect.getCenterY() > m_imageSize.getHeight() / 2){
				                m_currentTilt -= 1;
				            } else{
				                m_currentTilt += 1;    
				            }
				            
				            // Keep in range [0, TILT_RESOLUTION]
				            if (m_currentTilt < 0){
				                m_currentTilt = 0;
				            } else if (m_currentTilt > TILT_RESOLUTION){
				                m_currentTilt = TILT_RESOLUTION;
				            }
				            
				            syncHeadTilt();
				        } 
				    }
				}
				
				// If docked exit assisted docking.
				if (m_drive!= null){
				    if (m_drive.isCharging() == true){
				        // Done docking!
				        setMode(DriveMode.NORMAL);
				    }
				}	
			}
			else if(!overScreenButtons){				
				if(m_leftButtonDown == true && m_currentX > -1 && m_currentY > -1){
					if(!commonData.getNavigationMode().contains("Manual") && commonData.collaborativeControlEnabled==false){
						semiautonomyManager.stopGiraff(); 
				    }
					else{
					disableButtons=true;

							drive(m_currentX, m_currentY);
					}
				} 
				else{						
					// Sync head tilt.
					syncHeadTilt();				
				}
			}
	}

	
	/////////////////////////////////////////////////////////////
	// Drive helpers
	/////////////////////////////////////////////////////////////
	
	/**
	 * Drives the giraff towards (x, y).
	 * 
	 * @param x		X in mouse coordinates
	 * @param y		Y in mouse coordinates
	 */
	public void drive(int x, int y){
		long now = System.currentTimeMillis();

			if(m_currentTilt>TILT_RESOLUTION / 2 && m_mode != DriveMode.ASSISTED_DOCKING){
				m_currentTilt=TILT_RESOLUTION / 2;
				syncHeadTilt();	
			}
			// Start moving
			if(commonData.collaborativeControlEnabled==true){
				double coordX=x-(viewportWidth/2);
				double coordY=(viewportHeight-y);
				double max=Math.sqrt(Math.pow(viewportWidth/2,2)+Math.pow(viewportHeight,2));
				double module= Math.sqrt(Math.pow(coordX,2)+Math.pow(coordY,2))/max;
				double ang= (180/Math.PI)*(Math.acos((coordX/max)/module)-(Math.PI/2));
				
				
				System.out.println("* Angle *---->"+ang);
				
				if(ang<-45.00){
					semiautonomyManager.right(module);
				}
				else if(-45.00 <= ang && ang<= 45.00){
				m_currentModule=module;
				m_currentAng=ang;
				Double targetX=commonData.robotXplace+3*(module)*Math.cos( (Math.PI/180)*(commonData.robotOrientation+ang) );
				Double targetY=commonData.robotYplace+3*(module)*Math.sin( (Math.PI/180)*(commonData.robotOrientation+ang) );
				double dist=new Point(x,y).distance(lastX,lastY);
				//if(dist>20){
				commonData.collaborativeTarget=m_utils.transformToSchematicMapPoint(targetX, targetY, commonData.mapWidth/2, commonData.mapHeight/2);
				String command="GoToPoint "+targetX.toString().substring(0,4)+" "+targetY.toString().substring(0,4);
				semiautonomyManager.goToPoint(command);
				lastX=x;
				lastY=y;
				//}
				}
				else if( ang > 45.00){
					semiautonomyManager.left(module);
				}

			}
			else if (now - m_leftButtonDownTimeStamp > MOUSE_DOWN_TO_START_DRIVE_DELAY_MS){
					if (m_drive != null){
						m_drive.moveTowards(x, y);
					}
			}
		
	}
	
	public void syncHeadTilt(){
			if (m_drive != null){
				// Translate to an angular value.
				java.lang.Float maxUp = m_drive.getMaxTiltUpAngle();
				java.lang.Float maxDown = m_drive.getMaxTiltDownAngle();
				
				if (maxUp != null && maxDown != null){
					float interval = maxUp - maxDown; // NOTE: maxDown is normally a negative number.
					
					float percentLookingUp = (float)m_currentTilt / (float)(TILT_RESOLUTION*1.25);
					float currentAngle = maxDown + interval * percentLookingUp;
					
			        m_drive.tiltHead(currentAngle);
				}
			}
	}	
	
	
	/**
	 * Resets left button variables to prevent accidental driving.
	 */
	private void resetLeftButtonStatus(){
		m_leftButtonDown = false;
		m_leftButtonDownTimeStamp = Long.MAX_VALUE;
	}

  
    public void setLocale(Locale locale) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
	////////////////////////////////////////////////
	// Internal helpers
	////////////////////////////////////////////////
	
	private void setMode(DriveMode mode){
	    m_mode = mode;	    
	    if (mode == DriveMode.ASSISTED_DOCKING){
            // Tell tracker to start tracking around this point.
            m_tracker.lockCurrentTarget(true);
	    } else{
	        m_tracker.lockCurrentTarget(false);
	    }
	}
	
    /**
     * Converts a mouse coordinate from m_videoFrame to float.
     * 
     * @param x     The mouse event coordinate.
     * 
     * @return  A Float2D.Float where both x and y are in the range [-1.0, 1.0] if successful. 
     * @return  null if operation failed.
     */
    private Point convertMouse(int x, int y){
        
        switch(m_mode){
        case NORMAL:
            // Return the current mouse position.
            return new Point(x, y);
        case ASSISTED_DOCKING:
            // Return the docking target
            return getDockingPoint();
        } 
        return null;
    }

    /**
     * Returns the docking point to drive to.
     * 
     * Note that we manipulate this value based to make the velocity during 
     * assisted docking a bit more stable when we close in.
     * 
     * @return
     */
    private Point getDockingPoint(){
        Point2D.Float point = null;
        
        long now = System.currentTimeMillis();
        if (m_currentDockingTargetMouseCoord == null || m_currentDockingTargetTimestamp + MAX_TARGET_DELAY_MS < now){
            // Target too old...
            setMode(DriveMode.NORMAL);
            m_currentDockingTarget = null;
            m_currentDockingTargetMouseCoord = null;
        } else{
            // Convert docking target to a float
            // NOTE: Drive to the central bottom point of target to make sure we don't stop prematurely.
            int x = m_currentDockingTargetMouseCoord.x + m_currentDockingTargetMouseCoord.width / 2;
            int y = m_currentDockingTargetMouseCoord.y + m_currentDockingTargetMouseCoord.height / 2;
            
            point = new Point2D.Float(x, y);
            
            /*
            // TODO: make this code work again. It was rather nice to move slower!
            // Cap speed at 50% of screen size for a safer and less wobbly experience.
            // Speed is determined by the distance from the bottom central pixel of the image. 
            // TODO: Get the real offset from the giraff? Or should this be handled by the frame work?
            float MAX_SPEED = m_imageSize.height / 2;
            float speedVectorX = point.x; 
            float speedVectorY = point.y;
            if ( Math.abs(speedVectorX) >  MAX_SPEED || speedVectorY >  MAX_SPEED){
                float devider = Math.max(Math.abs(speedVectorX), speedVectorY);
                
                speedVectorX = (speedVectorX / devider) * MAX_SPEED;
                speedVectorY = (speedVectorY / devider) * MAX_SPEED;
                
                point = new Point2D.Float( (speedVectorX) , (speedVectorY - 1.0f) );  // NOTE: y = -1.0 means speed 0, so speedVectorY makes a positive contribution.
            }
            
            // Avoid going too slow
            // NOTE: Speed is calculated from the bottom of the screen at (0, -1.0) and from the centre offset recorded in sentry
            // TODO: Replace hardcoded 0.04 with live value!
            float MIN_SPEED = 0.30f;
            speedVectorX = point.x; 
            speedVectorY = point.y + 1.0f;  // So 0 if y == -1.0, always positive
            if ( Math.abs(speedVectorX) < MIN_SPEED && speedVectorY < MIN_SPEED){
                float devider = Math.max(Math.abs(speedVectorX), speedVectorY);
                
                speedVectorX = (speedVectorX / devider) * MIN_SPEED;
                speedVectorY = (speedVectorY / devider) * MIN_SPEED;
                
                point = new Point2D.Float(speedVectorX, speedVectorY - 1.0f);  // NOTE: y = -1.0 means speed 0, so speedVectorY makes a positive contribution.
                
                //System.out.println("Driving to x = " + point.x + ", y = " + point.y);
            }
            */
        }
        
        if (point != null){
            return new Point((int) point.x, (int) point.y);
        } else{
            return null;
        }
    }
	@Override
	public void magnificationReset() {
		// TODO Auto-generated method stub
		
	}
  
    public static void main(String[] args)
    {
    	/*UMA_GiraffPlugin plugin = new UMA_GiraffPlugin();
    	JFrame fr;
    	plugin.start();
    	fr=new JFrame("Pilot");
    	fr.setSize(200,200);
    	fr.setVisible(true);
    	fr.add(plugin.getRightInfoPanel());*/
    	
    	/*SingletonCommonDataImpl Data= SingletonCommonDataImpl.getSingletonObject();
        System.out.println("CommonDataImpl created");
        SingletonMapViewportImpl Viewport= SingletonMapViewportImpl.getSingletonObject();
        System.out.println("Map Viewport created");
        SingletonSemiautonomyManagerImpl Manager= SingletonSemiautonomyManagerImpl.getSingletonObject();
        System.out.println("Semiautonomy Manager created");*/
    	
    	//MQTTClient client = new MQTTClient();
    	
    	
    	
    	
    	/*SingletonSemiautonomyManagerImpl Manager= SingletonSemiautonomyManagerImpl.getSingletonObject();
    	SingletonMapViewportImpl Viewport= SingletonMapViewportImpl.getSingletonObject();
        long time=System.currentTimeMillis();
        long dif=time;
        while (!Manager.UMAClient.isConnected()){
        	System.out.println("not connected yet");
        }*/
        
    	/*UMA_GiraffPlugin plugin = new UMA_GiraffPlugin();
    	JFrame fr;
    	plugin.start();
    	fr=new JFrame("Pilot");
    	fr.setSize(200,200);
    	fr.setVisible(true);
    	fr.add(plugin.getRightInfoPanel());*/
    }
    
    
    
    
    ////////////////////////////////////////////////////////////////////////
    ////
    //// VideoViewPort ScreenPainter Methods 
    ////
    ////////////////////////////////////////////////////////////////////////
    
    
    
    public void paintChargingScreen(Graphics2D g, Rectangle videoViewPort, int baseline, String messageUndock,FontMetrics metrics,FontMetrics centralMetrics,FontMetrics buttonMetrics,FontMetrics mainMetrics)
    {
    	Rectangle2D messageUndockBounds = centralMetrics.getStringBounds(messageUndock, g);
		Rectangle UndockRect = new Rectangle(videoViewPort.x + 10 +(int)(videoViewPort.getWidth()/2-(int) messageUndockBounds.getWidth()/2),
				(int)(videoViewPort.y + 10 +videoViewPort.getHeight()/2-messageUndockBounds.getHeight()/2),
				(int) messageUndockBounds.getWidth() + 2 * 5, (int) messageUndockBounds.getHeight() + 2 * 5);			
		g.setColor(new Color(255, 255, 255, 150)); // Transparent white
		g.fillRect(UndockRect.x, UndockRect.y, UndockRect.width, UndockRect.height);
		baseline = UndockRect.y + metrics.getAscent() + 5;
		g.setColor(Color.BLUE);
		g.setFont(new Font("Arial",Font.BOLD,16));
		baseline = UndockRect.y + centralMetrics.getAscent() + 5;
		g.drawString(messageUndock, UndockRect.x + 5, baseline);
		g.setColor(new Color(255, 255, 255, 100));
		g.fill3DRect(videoViewPort.getLocation().x,videoViewPort.getLocation().y,(int) videoViewPort.getWidth(),(int) videoViewPort.getHeight(), true);
		
		String messageCharging=new String("CHARGING");
		g.setFont(new Font("Arial",Font.BOLD, 50));
		Rectangle2D messageChargingBounds=mainMetrics.getStringBounds(messageCharging,g);
		Rectangle ChargingRect = new Rectangle(videoViewPort.x + 10 +(int)(videoViewPort.getWidth()/2-(int) messageChargingBounds.getWidth()/2),
		(int)(videoViewPort.y + 10 +videoViewPort.getHeight()/2-messageChargingBounds.getHeight()/2-70),
		(int) messageChargingBounds.getWidth() + 2 * 5, (int) messageChargingBounds.getHeight() + 2 * 5);
		g.fillRect(ChargingRect.x, ChargingRect.y, ChargingRect.width, ChargingRect.height);
		baseline = ChargingRect.y + mainMetrics.getAscent() + 5;
		g.setColor(Color.BLUE);
		g.drawString(messageCharging, ChargingRect.x + 5, baseline);
		if(UndockRect.contains(m_currentX,m_currentY)&& m_leftButtonDown == true){
		
		}
		else if(ChargingRect.contains(m_currentX,m_currentY) || UndockRect.contains(m_currentX,m_currentY)){
			overScreenButtons=true;
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(ChargingRect.x, ChargingRect.y, ChargingRect.width, ChargingRect.height);
			g.fillRect(UndockRect.x, UndockRect.y, UndockRect.width, UndockRect.height);
			g.setColor(Color.CYAN);
			g.drawRect(ChargingRect.x, ChargingRect.y, ChargingRect.width, ChargingRect.height);;
			g.drawRect(UndockRect.x, UndockRect.y, UndockRect.width, UndockRect.height);
			g.setFont(new Font("Arial",Font.BOLD,16));
			baseline = UndockRect.y + centralMetrics.getAscent() + 5;
			g.drawString(messageUndock, UndockRect.x + 5, baseline);
			g.setFont(new Font("Arial",Font.BOLD, 50));
			baseline = ChargingRect.y + mainMetrics.getAscent() + 5;
			g.drawString(messageCharging, ChargingRect.x + 5, baseline);
			if(m_leftButtonDown == true){
			g.setColor(Color.orange);
			g.drawRect(ChargingRect.x, ChargingRect.y, ChargingRect.width, ChargingRect.height);;
			g.drawRect(UndockRect.x, UndockRect.y, UndockRect.width, UndockRect.height);
			g.setFont(new Font("Arial",Font.BOLD,16));
			baseline = UndockRect.y + centralMetrics.getAscent() + 5;
			g.drawString(messageUndock, UndockRect.x + 5, baseline);
			g.setFont(new Font("Arial",Font.BOLD, 50));
			baseline = ChargingRect.y + mainMetrics.getAscent() + 5;
			g.drawString(messageCharging, ChargingRect.x + 5, baseline);
			}
		}
    }
    public void paintAutoScreen(Graphics2D g, Rectangle videoViewPort, int baseline, String messageModeAutoSTOP,FontMetrics metrics,FontMetrics centralMetrics,FontMetrics buttonMetrics,FontMetrics mainMetrics)
    {
    	String messageAUTO="empty";
    	Rectangle2D messageModeAutoSTOPBounds = centralMetrics.getStringBounds(messageModeAutoSTOP, g);
		Rectangle ModeAutoSTOPRect = new Rectangle(videoViewPort.x + 10 +(int)(videoViewPort.getWidth()/2-(int) messageModeAutoSTOPBounds.getWidth()/2),
													(int)(videoViewPort.y + 10 +videoViewPort.getHeight()/2-messageModeAutoSTOPBounds.getHeight()/2),
													(int) messageModeAutoSTOPBounds.getWidth() + 2 * 5, (int) messageModeAutoSTOPBounds.getHeight() + 2 * 5);	
		float thickness = 30.0f;
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(thickness));
		g.setColor(Color.BLUE);
		g.drawRoundRect(videoViewPort.getLocation().x,videoViewPort.getLocation().y,(int) videoViewPort.getWidth(),
						(int) videoViewPort.getHeight(),40,40);
		g.setStroke(oldStroke);			
		g.setColor(new Color(255, 255, 255, 150)); // Transparent white
		g.fillRect(ModeAutoSTOPRect.x, ModeAutoSTOPRect.y, ModeAutoSTOPRect.width, ModeAutoSTOPRect.height);
		baseline = ModeAutoSTOPRect.y + centralMetrics.getAscent() + 5;
		g.setColor(Color.BLUE);
		g.setFont(new Font("Arial",Font.BOLD,16));
		g.drawString(messageModeAutoSTOP, ModeAutoSTOPRect.x + 5, baseline);
		messageAUTO=new String("AUTO:"+"Semiautonomous Navigation");
		g.setFont(new Font("Arial",Font.BOLD, 50));
		Rectangle2D messageAUTOBounds=mainMetrics.getStringBounds(messageAUTO,g);
		Rectangle StopRect = new Rectangle(videoViewPort.x + 10 +(int)(videoViewPort.getWidth()/2-(int) messageAUTOBounds.getWidth()/2),
				(int)(videoViewPort.y + 10 +videoViewPort.getHeight()/2-messageAUTOBounds.getHeight()/2-70),
				(int) messageAUTOBounds.getWidth() + 2 * 5, (int) messageAUTOBounds.getHeight() + 2 * 5);
		g.setColor(new Color(255, 255, 255, 150)); // Transparent white
		g.fillRect(StopRect.x, StopRect.y, StopRect.width, StopRect.height);
		baseline = StopRect.y + mainMetrics.getAscent() + 5;
		g.setColor(Color.BLUE);
		g.drawString(messageAUTO, StopRect.x + 5, baseline);
		
		
	/*	if(ModeAutoSTOPRect.contains(m_currentX,m_currentY) || StopRect.contains(m_currentX,m_currentY)){
			g.setColor(new Color(255, 255, 255, 150)); // Transparent white
			g.fillRect(ModeAutoSTOPRect.x, ModeAutoSTOPRect.y, ModeAutoSTOPRect.width, ModeAutoSTOPRect.height);
			baseline = ModeAutoSTOPRect.y + centralMetrics.getAscent() + 5;
			g.setColor(Color.cyan);
			g.setFont(new Font("Arial",Font.BOLD,16));
			g.drawString(messageModeAutoSTOP, ModeAutoSTOPRect.x + 5, baseline);
			messageAUTO=new String("CANCEL"+"Navigation");
			g.setFont(new Font("Arial",Font.BOLD, 50));
			messageAUTOBounds=mainMetrics.getStringBounds(messageAUTO,g);
			StopRect = new Rectangle(videoViewPort.x + 10 +(int)(videoViewPort.getWidth()/2-(int) messageAUTOBounds.getWidth()/2),
					(int)(videoViewPort.y + 10 +videoViewPort.getHeight()/2-messageAUTOBounds.getHeight()/2-70),
					(int) messageAUTOBounds.getWidth() + 2 * 5, (int) messageAUTOBounds.getHeight() + 2 * 5);
			g.setColor(new Color(255, 255, 255, 150)); // Transparent white
			g.fillRect(StopRect.x, StopRect.y, StopRect.width, StopRect.height);
			baseline = StopRect.y + mainMetrics.getAscent() + 5;
			g.setColor(Color.cyan);
			g.drawString(messageAUTO, StopRect.x + 5, baseline);
			
		}*/
    }
    public void paintCollaborativeScreen(Graphics2D g, Rectangle videoViewPort, int baseline,String messageNavigationMode, String messageModeCollaborativeSTOP,FontMetrics metrics,FontMetrics centralMetrics,FontMetrics buttonMetrics,FontMetrics mainMetrics)
    {
    	String messageAUTO="empty";
    	Rectangle2D messageModeCollaborativeSTOPBounds = centralMetrics.getStringBounds(messageModeCollaborativeSTOP, g);
		Rectangle ModeCollaborativeSTOPRect = new Rectangle(videoViewPort.x + 10 +(int)(videoViewPort.getWidth()/2-(int) messageModeCollaborativeSTOPBounds.getWidth()/2),
													(int)(videoViewPort.y + 10 +videoViewPort.getHeight()/2-messageModeCollaborativeSTOPBounds.getHeight()/2),
													(int) messageModeCollaborativeSTOPBounds.getWidth() + 2 * 5, (int) messageModeCollaborativeSTOPBounds.getHeight() + 2 * 5);	
		float thickness = 30.0f;
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(thickness));
		g.setColor(Color.BLUE);
		g.drawRoundRect(videoViewPort.getLocation().x,videoViewPort.getLocation().y,(int) videoViewPort.getWidth(),
						(int) videoViewPort.getHeight(),40,40);
		g.setStroke(oldStroke);			
		g.setColor(new Color(255, 255, 255, 150)); // Transparent white
		g.fillRect(ModeCollaborativeSTOPRect.x, ModeCollaborativeSTOPRect.y, ModeCollaborativeSTOPRect.width, ModeCollaborativeSTOPRect.height);
		baseline = ModeCollaborativeSTOPRect.y + centralMetrics.getAscent() + 5;
		g.setColor(Color.BLUE);
		g.setFont(new Font("Arial",Font.BOLD,16));
		g.drawString(messageModeCollaborativeSTOP, ModeCollaborativeSTOPRect.x + 5, baseline);
		messageAUTO=messageNavigationMode;
		g.setFont(new Font("Arial",Font.BOLD, 50));
		Rectangle2D messageAUTOBounds=mainMetrics.getStringBounds(messageAUTO,g);
		Rectangle StopRect = new Rectangle(videoViewPort.x + 10 +(int)(videoViewPort.getWidth()/2-(int) messageAUTOBounds.getWidth()/2),
				(int)(videoViewPort.y + 10 +videoViewPort.getHeight()/2-messageAUTOBounds.getHeight()/2-70),
				(int) messageAUTOBounds.getWidth() + 2 * 5, (int) messageAUTOBounds.getHeight() + 2 * 5);
		g.setColor(new Color(255, 255, 255, 150)); // Transparent white
		g.fillRect(StopRect.x, StopRect.y, StopRect.width, StopRect.height);
		baseline = StopRect.y + mainMetrics.getAscent() + 5;
		g.setColor(Color.BLUE);
		g.drawString(messageAUTO, StopRect.x + 5, baseline);
		// Paint the drive line	
		if (m_currentX > -1 && m_currentY > -1
			&& videoViewPort.contains(new Point(m_currentX, m_currentY))
			&& !overScreenButtons){
				Path2D.Float path = m_drive.getMoveTowardsPath(m_currentX, m_currentY);
				float[] style=new float[] { 12.0f, 25.0f, 3.0f, 25.0f };
				if (path != null ){
					// Select color and shape depending of if we try to drive or not
					if (m_leftButtonDown == true ){
						g.setColor(Color.BLUE);
					}
					else{
						g.setColor(Color.cyan);
					}				
					float driveLineWidth = (float)videoViewPort.width / (float)60;
					Stroke stroke = new BasicStroke(driveLineWidth, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND,50.0f,style,0.0f);				
					g.setStroke(stroke);
					g.draw(path);
				}
		}
    }
    public void paintScreenButtons(Graphics2D g, Rectangle videoViewPort,int baseline, String messageMoveBackwards,String messageTurnAroundLeft,String messageTurnAroundRight, FontMetrics buttonMetrics){
		// Place and Write BACKWARDS and TURN Buttons
		Rectangle2D messageMoveBackwardsBounds = buttonMetrics.getStringBounds(messageMoveBackwards, g);
		Rectangle2D messageTurnAroundLeftBounds = buttonMetrics.getStringBounds(messageTurnAroundLeft, g);
		Rectangle2D messageTurnAroundRightBounds = buttonMetrics.getStringBounds(messageTurnAroundRight, g);
    	Rectangle MoveBackwardsRect =new Rectangle(videoViewPort.x + 20, videoViewPort.y-100 + (int) videoViewPort.getHeight() + (int)messageMoveBackwardsBounds.getHeight(),
				(int) messageMoveBackwardsBounds.getWidth() + 2 * 5,(int) messageMoveBackwardsBounds.getHeight() + 2 * 5); 	
		Rectangle TurnAroundLeftRect =new Rectangle(videoViewPort.x + 20, videoViewPort.y -70 + (int) videoViewPort.getHeight() + (int)messageTurnAroundLeftBounds.getHeight(),
				(int) messageTurnAroundLeftBounds.getWidth() + 2 * 5,(int) messageTurnAroundLeftBounds.getHeight() + 2 * 5); 
		Rectangle TurnAroundRightRect =new Rectangle(videoViewPort.x + 33+ (int)TurnAroundLeftRect.getWidth(), videoViewPort.y -70 + (int) videoViewPort.getHeight() + (int)messageTurnAroundRightBounds.getHeight(),
				(int) messageTurnAroundRightBounds.getWidth() + 2 * 5,(int) messageTurnAroundRightBounds.getHeight() + 2 * 5); 		
			g.setFont(new Font("Arial",Font.BOLD, 16));

    	if(MoveBackwardsRect.contains(m_currentX,m_currentY) && m_leftButtonDown == true){
			m_drive.backup();
			overScreenButtons=true;
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(MoveBackwardsRect.x, MoveBackwardsRect.y, MoveBackwardsRect.width, MoveBackwardsRect.height);
			g.setColor(Color.ORANGE);
			g.drawRect(MoveBackwardsRect.x, MoveBackwardsRect.y, MoveBackwardsRect.width, MoveBackwardsRect.height);
			baseline = MoveBackwardsRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageMoveBackwards, MoveBackwardsRect.x + 5, baseline);
			
		}
		else if(TurnAroundLeftRect.contains(m_currentX,m_currentY)&& m_leftButtonDown == true){
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(TurnAroundLeftRect.x, TurnAroundLeftRect.y, TurnAroundLeftRect.width, TurnAroundLeftRect.height);
			g.setColor(Color.orange);
			g.drawRect(TurnAroundLeftRect.x, TurnAroundLeftRect.y, TurnAroundLeftRect.width, TurnAroundLeftRect.height);
			m_drive.rotate(-10);
			overScreenButtons=true;
			baseline = TurnAroundLeftRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageTurnAroundLeft, TurnAroundLeftRect.x + 5, baseline);
		}
		else if(TurnAroundRightRect.contains(m_currentX,m_currentY)&& m_leftButtonDown == true){
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(TurnAroundRightRect.x, TurnAroundRightRect.y, TurnAroundRightRect.width, TurnAroundRightRect.height);
			g.setColor(Color.orange);
			g.drawRect(TurnAroundRightRect.x, TurnAroundRightRect.y, TurnAroundRightRect.width, TurnAroundRightRect.height);
			m_drive.rotate(10);
			overScreenButtons=true;
			baseline = TurnAroundRightRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageTurnAroundRight, TurnAroundRightRect.x + 5, baseline);
		}
		else if(MoveBackwardsRect.contains(m_currentX,m_currentY)){
			overScreenButtons=true;
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(MoveBackwardsRect.x, MoveBackwardsRect.y, MoveBackwardsRect.width, MoveBackwardsRect.height);
			g.setColor(Color.cyan);
			g.drawRect(MoveBackwardsRect.x, MoveBackwardsRect.y, MoveBackwardsRect.width, MoveBackwardsRect.height);
			baseline = MoveBackwardsRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageMoveBackwards, MoveBackwardsRect.x + 5, baseline);
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(TurnAroundLeftRect.x, TurnAroundLeftRect.y, TurnAroundLeftRect.width, TurnAroundLeftRect.height);
			g.setColor(Color.BLUE);
			g.drawRect(TurnAroundLeftRect.x, TurnAroundLeftRect.y, TurnAroundLeftRect.width, TurnAroundLeftRect.height);
			baseline = TurnAroundLeftRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageTurnAroundLeft, TurnAroundLeftRect.x + 5, baseline);
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(TurnAroundRightRect.x, TurnAroundRightRect.y, TurnAroundRightRect.width, TurnAroundRightRect.height);
			g.setColor(Color.BLUE);
			g.drawRect(TurnAroundRightRect.x, TurnAroundRightRect.y, TurnAroundRightRect.width, TurnAroundRightRect.height);
			baseline = TurnAroundRightRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageTurnAroundRight, TurnAroundRightRect.x + 5, baseline);
		}
		else if(TurnAroundLeftRect.contains(m_currentX,m_currentY)){
			overScreenButtons=true;
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(MoveBackwardsRect.x, MoveBackwardsRect.y, MoveBackwardsRect.width, MoveBackwardsRect.height);
			g.setColor(Color.BLUE);
			g.drawRect(MoveBackwardsRect.x, MoveBackwardsRect.y, MoveBackwardsRect.width, MoveBackwardsRect.height);
			baseline = MoveBackwardsRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageMoveBackwards, MoveBackwardsRect.x + 5, baseline);
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(TurnAroundLeftRect.x, TurnAroundLeftRect.y, TurnAroundLeftRect.width, TurnAroundLeftRect.height);
			g.setColor(Color.CYAN);
			g.drawRect(TurnAroundLeftRect.x, TurnAroundLeftRect.y, TurnAroundLeftRect.width, TurnAroundLeftRect.height);
			baseline = TurnAroundLeftRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageTurnAroundLeft, TurnAroundLeftRect.x + 5, baseline);
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(TurnAroundRightRect.x, TurnAroundRightRect.y, TurnAroundRightRect.width, TurnAroundRightRect.height);
			g.setColor(Color.BLUE);
			g.drawRect(TurnAroundRightRect.x, TurnAroundRightRect.y, TurnAroundRightRect.width, TurnAroundRightRect.height);
			baseline = TurnAroundRightRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageTurnAroundRight, TurnAroundRightRect.x + 5, baseline);
		}
		else if(TurnAroundRightRect.contains(m_currentX,m_currentY)){
			overScreenButtons=true;
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(MoveBackwardsRect.x, MoveBackwardsRect.y, MoveBackwardsRect.width, MoveBackwardsRect.height);
			g.setColor(Color.BLUE);
			g.drawRect(MoveBackwardsRect.x, MoveBackwardsRect.y, MoveBackwardsRect.width, MoveBackwardsRect.height);
			baseline = MoveBackwardsRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageMoveBackwards, MoveBackwardsRect.x + 5, baseline);
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(TurnAroundLeftRect.x, TurnAroundLeftRect.y, TurnAroundLeftRect.width, TurnAroundLeftRect.height);
			g.setColor(Color.BLUE);
			g.drawRect(TurnAroundLeftRect.x, TurnAroundLeftRect.y, TurnAroundLeftRect.width, TurnAroundLeftRect.height);
			baseline = TurnAroundLeftRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageTurnAroundLeft, TurnAroundLeftRect.x + 5, baseline);
			g.setColor(new Color(255, 255, 255, 255)); // Transparent white
			g.fillRect(TurnAroundRightRect.x, TurnAroundRightRect.y, TurnAroundRightRect.width, TurnAroundRightRect.height);
			g.setColor(Color.CYAN);
			g.drawRect(TurnAroundRightRect.x, TurnAroundRightRect.y, TurnAroundRightRect.width, TurnAroundRightRect.height);
			baseline = TurnAroundRightRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageTurnAroundRight, TurnAroundRightRect.x + 5, baseline);
		}
		else{
			overScreenButtons=false;
			g.setColor(new Color(255, 255, 255, 150)); // Transparent white
			g.fillRect(MoveBackwardsRect.x, MoveBackwardsRect.y, MoveBackwardsRect.width, MoveBackwardsRect.height);
			g.fillRect(TurnAroundLeftRect.x, TurnAroundLeftRect.y, TurnAroundLeftRect.width, TurnAroundLeftRect.height);
			g.fillRect(TurnAroundRightRect.x, TurnAroundRightRect.y, TurnAroundRightRect.width, TurnAroundRightRect.height);
			g.setColor(Color.BLUE);
			g.drawRect(MoveBackwardsRect.x, MoveBackwardsRect.y, MoveBackwardsRect.width, MoveBackwardsRect.height);
			g.drawRect(TurnAroundLeftRect.x, TurnAroundLeftRect.y, TurnAroundLeftRect.width, TurnAroundLeftRect.height);
			g.drawRect(TurnAroundRightRect.x, TurnAroundRightRect.y, TurnAroundRightRect.width, TurnAroundRightRect.height);
			g.setColor(Color.BLUE);
			baseline = TurnAroundRightRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageTurnAroundRight, TurnAroundRightRect.x + 5, baseline);
			baseline = MoveBackwardsRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageMoveBackwards, MoveBackwardsRect.x + 5, baseline);
			baseline = TurnAroundLeftRect.y + buttonMetrics.getAscent() + 5;
			g.drawString(messageTurnAroundLeft, TurnAroundLeftRect.x + 5, baseline);
		}	
    }
    public void paintDriveLine(Graphics2D g, Rectangle videoViewPort){
    	if (m_mode == DriveMode.NORMAL){
            // Paint the drive line	
			if (m_currentX > -1 && m_currentY > -1
				&& videoViewPort.contains(new Point(m_currentX, m_currentY))
				&& !overScreenButtons){
					Path2D.Float path = m_drive.getMoveTowardsPath(m_currentX, m_currentY);
					if (path != null ){
						// Select color and shape depending of if we try to drive or not
						if (m_leftButtonDown == true ){
							g.setColor(COLOR_DRIVING);
						}
						else{
							g.setColor(COLOR_IDLE);
						}				
						float driveLineWidth = (float)videoViewPort.width / (float)50;
						Stroke stroke = new BasicStroke(driveLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);				
						g.setStroke(stroke);
						g.draw(path);
					}
			}
        } 
        else{
            // We need to paint our own line
            // Paint the drive line
            Point p = getDockingPoint();
            
            // Change the HeadTilt. We need to maintain the target tracked when we are very close to the docking station 
            m_currentTilt=2;
            syncHeadTilt();
            
            if (p != null){
                if (p.x > -1 && p.y > -1 && videoViewPort.contains(p)){
                    Path2D.Float path = m_drive.getMoveTowardsPath(p.x, p.y);
    
                    if (path != null){
                        // Select color and shape depending of if we try to drive or not
                        if (m_leftButtonDown == true){
                            g.setColor(COLOR_DRIVING);
                        } else{
                            g.setColor(COLOR_IDLE);
                        }
                        
                        float driveLineWidth = (float)videoViewPort.width / (float)20;
                        Stroke stroke = new BasicStroke(driveLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                    
                        g.setStroke(stroke);
                        g.draw(path);
                    }
                }
            }
			
        }
    }
    public void paintDockingInfo(Graphics2D g, Rectangle videoViewPort){
    	// Determine the magnification
		// Using height as we crop image to 4:3 aspect ratio in 720p mode.
		float imageScaleFactor = (float) videoViewPort.height / (float) m_imageSize.height;
        
        /////////////////////////////////////////////
        // Paint the winner
        /////////////////////////////////////////////
        Rectangle currentTarget = m_tracker.getBestTarget();
        // A new target?
        if (currentTarget != m_currentDockingTarget){
            m_currentDockingTarget = currentTarget;
            
            if (currentTarget != null){
                m_currentDockingTargetTimestamp = System.currentTimeMillis();
                m_currentDockingTargetMouseCoord = new Rectangle( (int)(imageScaleFactor * currentTarget.x + videoViewPort.x),
                                                                  (int)(imageScaleFactor * currentTarget.y + videoViewPort.y),
                                                                  (int)(imageScaleFactor * currentTarget.width), 
                                                                  (int)(imageScaleFactor * currentTarget.height));
            } else{
                // No target found. 
                // Check time since last valid target
                long age = System.currentTimeMillis() - m_currentDockingTargetTimestamp;
                if (age > MAX_TARGET_DELAY_MS){
                    // Stored target too old.
                    m_currentDockingTargetMouseCoord = null;
                }
            }
        }
        
        // Paint the target
        if (currentTarget != null){
            g.setColor(new Color(80, 200, 0));
            
	        if (m_mode == DriveMode.ASSISTED_DOCKING){
                g.setStroke(new BasicStroke(4));  // set stroke wider to show that target is locked
            }else{
                g.setStroke(new BasicStroke(1));  // set stroke smaller to show that target is not locked. 
            }
	        
	        Rectangle screenTarget = new Rectangle( (int)(imageScaleFactor * currentTarget.x + videoViewPort.x),
                                                    (int)(imageScaleFactor * currentTarget.y + videoViewPort.y),
                                                    (int)(imageScaleFactor * currentTarget.width), 
                                                    (int)(imageScaleFactor * currentTarget.height));

	        g.drawRect(screenTarget.x, screenTarget.y, screenTarget.width, screenTarget.height);
        }
    }
    public void paintBatteryLevel(Graphics2D g, Rectangle videoViewPort, String messageBatteryLevel, int baseline, FontMetrics metrics){
		// Place & Write BATTERY LEVEL message in the upper left corner of the view port with a margin of 10 pixels to the edge of the frame
		// and a margin of 5 from the text to the edge of the background.
    	Rectangle2D messageBatteryLevelBounds = metrics.getStringBounds(messageBatteryLevel, g);
		Rectangle BatteryRect =new Rectangle(videoViewPort.x +20, videoViewPort.y+5 + (int)messageBatteryLevelBounds.getHeight(),
				(int) messageBatteryLevelBounds.getWidth() + 2 * 5,(int) messageBatteryLevelBounds.getHeight() + 2 * 5); 							
		g.setColor(new Color(255, 255, 255, 150)); // Transparent white
		g.fillRect(BatteryRect.x, BatteryRect.y, BatteryRect.width, BatteryRect.height);
		baseline = BatteryRect.y + metrics.getAscent() + 5;
		g.setColor(Color.BLUE);
		g.setFont(new Font("Arial",Font.PLAIN, 12));
		g.drawString(messageBatteryLevel, BatteryRect.x + 5, baseline);
    }
}

