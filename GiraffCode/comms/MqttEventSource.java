package comms;

import java.util.EventObject;

public class MqttEventSource extends EventObject{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String m_topic="empty";
	private String m_message="empty";
	public MqttEventSource(Object source, String Topic, String Message) {
		super(source);
		// TODO Auto-generated constructor stub
		this.m_topic=Topic;
		this.m_message=Message;
	}
	public String getTopic(){
		return m_topic;
	}
	public String getMessage(){
		return m_message;
	}
	


}
