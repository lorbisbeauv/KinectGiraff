package gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import semiautonomy.SingletonCommonDataImpl;
import semiautonomy.SingletonSemiautonomyManagerImpl;


//------------------------------------------------------------------
//
//		NODE BUTTON
//
//------------------------------------------------------------------
	/**
	 * @author Francisco Melendez
	 * @email fco.melendez@uma.es
	 * 
	 * A NodeButton represents a node from the world model.
	 * By clicking on it, a user is able to edit its parameters
	 * 
	 * 
	 */
	public class MapNodeButtonImpl implements MapNodeButton{
	    
		String node;
		public int sideSize=6;
		public boolean isNavigationTarget=false;
		public JLabel m_buttonLabel=new JLabel();
		private SingletonCommonDataImpl m_commonData;
		private SingletonSemiautonomyManagerImpl m_semiautonomyManager;
		
		
	    public MapNodeButtonImpl(String nodeName, Point nodeLocation){
	    	m_commonData= SingletonCommonDataImpl.getSingletonObject();
	    	m_semiautonomyManager= SingletonSemiautonomyManagerImpl.getSingletonObject();
	        node=nodeName;
	        m_buttonLabel.setName(nodeName);
		    if(nodeName.contains("NODE")||nodeName.startsWith("n"))
		    {
		    	m_buttonLabel.setOpaque(true);  
		    	m_buttonLabel.setBackground(Color.BLUE);
		    	m_buttonLabel.setToolTipText(nodeName);
		    	m_buttonLabel.setSize(sideSize,sideSize);
			    Point p=new Point(nodeLocation);
			    m_buttonLabel.setLocation(p.x-sideSize/2,p.y-sideSize/2);
			    m_buttonLabel.setVisible(false);
		    }
		    else
		    {
		    	isNavigationTarget=true;
		    	m_buttonLabel.setHorizontalAlignment(SwingConstants.CENTER);
		    	m_buttonLabel.setVerticalAlignment(SwingConstants.CENTER);
		    	m_buttonLabel.setSize(60,20);
			    Point p=new Point(nodeLocation);
			    m_buttonLabel.setLocation(p.x-30,p.y-10);
			    m_buttonLabel.setText(nodeName);
			    m_buttonLabel.setToolTipText(nodeName);
			    m_buttonLabel.setVisible(true);  
			    this.changeColor(false);
		    }
		    m_buttonLabel.addMouseListener(new NodeMouseListener());
	        startNodeVisibilityThread();
	    }
	    ///PUBLIC METHODS    
	    @Override
	    public void move(){ 
			Thread movingThread=new Thread(new Runnable(){
				@Override
				public void run() {
					m_commonData.setNodeSelected(true,node);
					// TODO Auto-generated method stub
					Point p= new Point(m_commonData.getImageMousePosition());
					while (!m_commonData.getNodeSelected().equals(new String("none")) && m_commonData.pluginSTOP==false){
						if (p!=m_commonData.getImageMousePosition()){
							p= m_commonData.getImageMousePosition();
							if(isNavigationTarget==true){
								p.setLocation(p.x-30,p.y-10);
							}
							else{
								p.setLocation(p.x-3,p.y-3);
							}
							
							moveButton(p);
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					if(p!=new Point(0,0)){
						m_semiautonomyManager.moveNode(node, p);
					}
				}},"moving node");
			movingThread.start();	
	    }
	    public void moveButton(Point mouseLoc){		
	    	m_buttonLabel.setLocation(mouseLoc);		  	
	    }
		@Override
		public void rename() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void delete() {
			// TODO Auto-generated method stub
			m_buttonLabel.getParent().remove(m_buttonLabel);		
		}
		
	    ///PRIVATE METHODS///
		private void showMenu(){
			m_commonData.setPluginState("IDLE");
			changeColor(true);
			JMenuItem navigateTo,delete,move,rename,createArc;
			JPopupMenu tasks=new JPopupMenu("Options:");
		    createArc = new JMenuItem("Create Arc");
		    createArc.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					createArc();
				}		    	    	
		    });
		    tasks.add(createArc);
		    delete = new JMenuItem("Delete");
		    delete.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					m_semiautonomyManager.deleteNode(node);
				}		    	    	
		    });
		    tasks.add(delete);
		    move = new JMenuItem("Move");
		    move.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					move();
				}		    	    	
		    });
		    tasks.add(move);
			navigateTo = new JMenuItem("Navigate");
			navigateTo.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
	        		navigate();
				}		    	    	
		    });
		    tasks.add(navigateTo);
		    rename = new JMenuItem("Rename");
		    rename.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					m_semiautonomyManager.renameNode(node);
				}		    	    	
		    });
		    tasks.add(rename);
		    tasks.show(m_buttonLabel, 6, 0);
		}
		private void createArc(){
			/* 
			Thread createArcThread=new Thread(new Runnable(){
				@Override
				public void run() {
	        		UMA_GiraffPlugin.m_CommonData.nodeselected=true;
	        		UMA_GiraffPlugin.m_CommonData.lastClickedNode=node;
					// TODO Auto-generated method stub
					while (UMA_GiraffPlugin.m_CommonData.nodeselected==true){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					UMA_GiraffPlugin.m_CommonData.lastClickedNode="none";
				}},"creating arc");
			createArcThread.start(); 
			*/		
			m_commonData.setNodeSelected(true, node);
		}
		private void navigate(){
			m_semiautonomyManager.goTo(node,true);
		}	
		
	    private void setVisibility(boolean value){
	    	if(isNavigationTarget==false){
	    		m_buttonLabel.setVisible(value);	
	    		m_buttonLabel.setOpaque(value);
	    	}
	    }
	    private void changeColor(boolean color){
	    	if(isNavigationTarget==true){
	            if (color==true){
	            	m_buttonLabel.setFont(new Font("Arial",Font.BOLD, 16));
	            	m_buttonLabel.setOpaque(false);
	            	m_buttonLabel.setBackground(new Color(255,255,255,255));
	            	m_buttonLabel.setForeground(Color.CYAN);
	            }
	            else{
	            	m_buttonLabel.setFont(new Font("Arial",Font.PLAIN, 14));
	            	m_buttonLabel.setOpaque(false);  
	            	m_buttonLabel.setBackground(new Color(255,255,255,255));
	            	m_buttonLabel.setForeground(Color.BLUE);
	            }
	    	}
	    	else{
	            if (color==true){
	            	m_buttonLabel.setOpaque(true);
	            	m_buttonLabel.setSize(sideSize+3,sideSize+3);
	            	m_buttonLabel.setBackground(Color.CYAN);
	            }
	            else{
	            	m_buttonLabel.setOpaque(true);  
	            	m_buttonLabel.setSize(sideSize,sideSize);
	            	m_buttonLabel.setBackground(Color.BLUE);
	            }
	    	}
	    }
	    
	    private void setNodeCursorType(String str){
	    	if (str.equals("cross")){
	    		m_buttonLabel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	    	}
	    	else if (str.equals("custom")){
	    		m_buttonLabel.setCursor(new Cursor(Cursor.CUSTOM_CURSOR));
	    	}
	    	else if (str.equals("hand")){
	    		m_buttonLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    	}
	    	else{
	    		m_buttonLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    	}
	    }
	    private void startNodeVisibilityThread(){
	        Thread display_t=new Thread(new Runnable(){
	        	boolean viewNodes=false;
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(m_commonData.pluginSTOP==false){
						if(m_commonData.getPluginMode().equals("USER") && viewNodes==true){
							setVisibility(false);
							viewNodes=false;
						}
						else if(m_commonData.getPluginMode().equals("ENGINEER") && viewNodes==false){
							setVisibility(true);
							viewNodes=true;						
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}},"displayOrNot");
	       display_t.start();	    	
	    }
	    ///PRIVATE CLASSES
	    private class NodeMouseListener implements MouseListener{

	    	public NodeMouseListener(){
	    		
	    	}
	        @Override
	        public void mouseClicked(MouseEvent e) {
				System.out.println("MOUSE CLICKED");
	        	if(m_commonData.getPluginState().equals("RELOCALIZE_AT")){
	        		Point p=new Point(m_commonData.topology.nodesMap.get(node));
	        		m_semiautonomyManager.relocalize(p);
	        		m_commonData.setPluginState("IDLE");
	        		System.out.println("Relocalizing...");
	        	}
	        	else if (m_commonData.getPluginMode().equals("USER")){
	        		m_semiautonomyManager.goTo(node,true);
	                System.out.println("Going To..."+node);
	        	}
	        	else if (m_commonData.getNodeSelected().equals(new String("none"))){
	        		showMenu();
	        		System.out.println("Please, select a Task");
	        	}
	        	else if (!m_commonData.getNodeSelected().equals("none")){
	        		System.out.println("Creating Arc:"+m_commonData.getNodeSelected()+"-"+node);
	        		if(!m_commonData.getNodeSelected().equals(node)){
	        			m_semiautonomyManager.addArc(node,m_commonData.getNodeSelected());
					System.out.println("Arc Created!");
	        		}
	        		else{
	        			System.out.println("Please, select two different nodes to create an arc");	
	        		}
					m_commonData.setNodeSelected(false, "none");
	        	}
	        	else{
	        		System.out.println("Cancelling NODE Task");
	        	}
	        }

	        @Override
	        public void mousePressed(MouseEvent e) {           	
	        }

	        @Override
	        public void mouseReleased(MouseEvent e) {
	        }

	        @Override
	        public void mouseEntered(MouseEvent e) {
	             changeColor(true);
	             setNodeCursorType("hand");
	        }

	        @Override
	        public void mouseExited(MouseEvent e) {
	             changeColor(false);
	             setNodeCursorType("default");
	        }
	    	
	    }
	    
	}
