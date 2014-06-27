package semiautonomy;

import java.awt.Point;

interface SingletonSemiautonomyManager {
	
	////////////////////////
	// ROBOT EVENTS change the status variables
	////////////////////////
	public void setLocalization(double x, double y, double phi); // OpenMORA GUI coordinates
	public void setTopology(String topology);
	public void setNavigationMode(String modeMessage); //modes: "auto","manual","collaborative"
	public void setLaserScan(String laserReads);
	
	//////////////////////////////////////
	// GUI UPDATE EVENTS update the GUI after a change in a status variable 
	//////////////////////////////////////
	
	
	////////////////////////
	// USER EVENTS
	////////////////////////
    // Topology Management
    public void getTopology();
    public void loadTopology();
    public void saveTopology();
    
    // Graph Edit Commands
    public void addNode(Point nodeLocation);
    public void deleteNode(String name);
    public void moveNode(String name, Point newPosition);
    public void renameNode(String oldName);
    public void addArc(String nodeA, String nodeB);
    public void deleteArc(String nodeA, String nodeB);
    
    // Navigation Commands
	public void stopGiraff();
	public void goTo (String target, boolean requestConfirmation);
	public void goTo (Point p);
	public void relocalize(Point p);
	public void motors(String command);

}
