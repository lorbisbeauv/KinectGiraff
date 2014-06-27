package comms;

import java.util.EventListener;

public interface MqttEventListener extends EventListener {

	/**
	 * @param args
	 */
	public void eventRead(MqttEventSource Source);

}
