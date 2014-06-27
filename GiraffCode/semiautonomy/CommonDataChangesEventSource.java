package semiautonomy;

import java.util.EventObject;

public class CommonDataChangesEventSource extends EventObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String m_modules="empty";
	private String m_command="empty";
	public CommonDataChangesEventSource(Object source, String Modules, String Command) {
		super(source);
		// TODO Auto-generated constructor stub
		this.m_modules=Modules;
		this.m_command=Command;
	}
	public String getModules(){
		return m_modules;
	}
	public String getCommand(){
		return m_command;
	}
	


}