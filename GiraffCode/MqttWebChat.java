import gui.SingletonMapViewportImpl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.giraff.pilot20.dockingassistant.DockingStationTracker;

import semiautonomy.SingletonCommonDataImpl;
import semiautonomy.SingletonSemiautonomyManagerImpl;
import semiautonomy.Utils;


public class MqttWebChat extends JApplet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @param args
	 */
	MyMqttCallBack myClient;
	JPanel fr;
	JLabel jl;
	Image schematicMapImage,geometricMapImage;
	ImageIcon imgIcon;
	JLabel img;
	
	////////////////
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
	////////////////
	@Override
	public void init(){
	
		System.out.println("-----------------------------------");
		System.out.println("-----------------------------------");
		System.out.println("-Start has been called:"+n+"times--");
		System.out.println("-----------------------------------");
		System.out.println("-----------------------------------");
		commonData= SingletonCommonDataImpl.getSingletonObject();
		schematicMapImage = getImage (getDocumentBase(), "schematicMap.png");
		geometricMapImage = getImage (getDocumentBase(), "geometricMap.png");
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
		rightInfoPanel.setLayout(new BorderLayout());
		rightInfoPanel.add(mapViewport,BorderLayout.CENTER);
		this.setSize(400,600);
		this.add(rightInfoPanel);
		
		
		
	/*	myClient=new MyMqttCallBack();
		imgIcon= new ImageIcon();
	   	fr=new JPanel();
    	fr.setLocation(0, 0);
    	fr.setSize(400,600);
    	fr.setVisible(true);
    	fr.setOpaque(true);
    	fr.setLayout(new BoxLayout(fr,BoxLayout.Y_AXIS));
    	fr.setBackground(Color.blue);
		jl= new JLabel("Waiting...");
		jl.setOpaque(true);
		jl.setVisible(true);
		jl.setBackground(Color.cyan);
		imagen = getImage (getDocumentBase(), "schematicMap.png");
		
		imgIcon.setImage(imagen);
		img= new JLabel("Waiting...");
		img.setOpaque(true);
		img.setVisible(true);
		img.setBackground(Color.red);
		img.setIcon(imgIcon);
    	this.setSize(400,600);
    	
    	this.add(fr);
    	fr.add(jl);
    	fr.add(img);

		
		Thread t0= new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String msg=myClient.message;
				long time= System.currentTimeMillis();
				while (!myClient.message.equals("exit")){
					if(!msg.equals(myClient.message)){
					jl.setText(myClient.message);
					msg=myClient.message;
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}},"thread");
		t0.start();*/
	}
	
	@Override
	public void stop(){
		/*try {
			myClient.client.disconnect();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			myClient.client.close();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	public void UP(){
		semiautonomyManager.up();
	}
	public void DOWN(){
		semiautonomyManager.down();
		
	}
	public void LEFT(){
		semiautonomyManager.left();
	}
	public void RIGHT(){
		semiautonomyManager.right();
	}
	public void MOTORS(int x,int y){
		semiautonomyManager.motion(x,y);
	}
	public void STOP(int x, int y){
		if (x>0 && x<800 && y>0 && y<500){
			semiautonomyManager.stopGiraff();
		}
	}
	public void stopMotion(){

		semiautonomyManager.stopMotion();
	
	}
}
