package semiautonomy;

import java.awt.Point;





interface SingletonCommonData {
	/*void readConfigFile();
	void importTopology();
	boolean getACK();
	void setACK();
    PluginState getPluginState();
    void setPluginState(PluginState newState);
    void setPluginMode(String mode);
	void setTopology(String topologyGraph);
	void saveLabels();
	void relocalizeAt(double x,double y);
	boolean getFindRobot();
    void goToNode(String node);
    void setFindRobot(boolean value);*/
    
	///////////
	// Getters
	///////////
	public Point getImageMousePosition();
    public String getNodeSelected();
    public String getNavigationMode();
    ///////////
    // Setters 
    ///////////
    public void setLocalization(double x, double y, double phi);
    public void setTopology(String topologyGraph);
    public void setNavigationMode(String modeMessage);
    public void setImageMousePosition(int x, int y);
    public void setNodeSelected(boolean aNodeisSelected, String nodeName);
}
