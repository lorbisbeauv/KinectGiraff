import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MyMqttCallBack implements MqttCallback {

	public MqttClient client;
	public String message= "empty";
	MemoryPersistence memPersistence; 
	/**
	 * @param args
	 * @throws MqttException 
	 */
	public MyMqttCallBack(){
		memPersistence= new MemoryPersistence();
		try {
			client= new MqttClient("tcp://150.214.109.134:22","testing", memPersistence);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.setCallback(this);
		try {
			client.connect();
		} catch (MqttSecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client.subscribe("SemiautonomyTest");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		try {
			client.connect();
		} catch (MqttSecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client.subscribe("SemiautonomyTest");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void messageArrived(String arg0, MqttMessage arg1){
		// TODO Auto-generated method stub
		message=new String(arg1.getPayload());
		try {
			memPersistence.clear();
		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
