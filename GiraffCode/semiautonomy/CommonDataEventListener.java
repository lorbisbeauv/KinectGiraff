package semiautonomy;

import java.util.EventListener;


public interface CommonDataEventListener extends EventListener {
	/**
	 * @param args
	 */
	public void eventRead(CommonDataChangesEventSource Source);

}

