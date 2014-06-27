package comms;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import semiautonomy.SingletonCommonDataImpl;
import configuration.AppConfigurationImpl;






public class MQTTClient implements MqttCallback{

    public MqttClient client;
    String clientId="UMAClient";    //String GiraffPlusBroker="false";
    public MqttConnectOptions options;
    boolean connecting=false;
    int numMessages=0,numTopologies=0;
    String id="empty";
    SingletonCommonDataImpl m_CommonData;
    AppConfigurationImpl configurationData;
    private Set<MqttEventListener> m_listeners;
    MemoryPersistence memPersistence; 
    static MQTTClient singletonObject;
	public MQTTClient() {
		memPersistence=new MemoryPersistence();
		configurationData= new AppConfigurationImpl();
		m_listeners = new HashSet<MqttEventListener>();
		m_CommonData= SingletonCommonDataImpl.getSingletonObject();
	    id=configurationData.m_identity+"/";
		if(configurationData.m_GiraffPlusBroker.equals("true"))
		{
			System.out.println("MQTT Connecting to GiraffPlus Broker...");
			try {
				options = new MqttConnectOptions();
				client = new MqttClient("ssl://giraffplus.xlab.si:8883","Pilot"+id,memPersistence);
				options.setSocketFactory(getAuthSSLContext().getSocketFactory());
				while(!client.isConnected()){
					System.out.println("Connecting...");
				client.connect(options);
				}
			} 
			catch (MqttException e) {
				// TODO Auto-generated catch block
				System.out.println("1-MqttException:"+e.toString());
			}	
			
		}
		else
		{
			System.out.println("MQTT Connecting to:"+configurationData.m_IPBroker+"...");
			try {
				client = new MqttClient("tcp://"+configurationData.m_IPBroker+":"+configurationData.m_PortBroker,id,memPersistence);
				client.connect();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				System.out.println("4-MqttException:"+e.toString());
			}
		}
		client.setCallback(this);
		try {
           // client.subscribe(id+"LaserScan",2);
            client.subscribe(id+"Topology",2);
            //client.subscribe(id+"Localization",0);
            client.subscribe(id+"Status",0);
            client.subscribe(id+"NavigationMode",2);
            //client.subscribe(id+"ErrorMsg",2);
            System.out.println("subscribed: "+id+"ErrorMsg");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*public MQTTClient() {
		configurationData= new AppConfigurationImpl();
		m_listeners = new HashSet<MqttEventListener>();
		m_CommonData= SingletonCommonDataImpl.getSingletonObject();
	    id=configurationData.m_identity+"/";
		try {
			if(configurationData.m_GiraffPlusBroker.equals("true"))
			{
				System.out.println("MQTT Connecting to GiraffPlus Broker...");
				try {
					options = new MqttConnectOptions();
					client = new MqttClient("ssl://giraffplus.xlab.si:8883","Pilot"+id);
					options.setSocketFactory(getAuthSSLContext().getSocketFactory());
					while(!client.isConnected()){
						System.out.println("Connecting...");
					client.connect(options);
					}
				} 
				catch (MqttException e) {
					// TODO Auto-generated catch block
					System.out.println("1-MqttException:"+e.toString());
				}	
				
			}
			else
			{
				System.out.println("MQTT Connecting to:"+configurationData.m_IPBroker+"...");
				try {
					client = new MqttClient("tcp://"+configurationData.m_IPBroker+":"+configurationData.m_PortBroker,id);
					client.connect();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					System.out.println("4-MqttException:"+e.toString());
				}
			}
			client.setCallback(new MqttCallback(){
			    @Override
				public void connectionLost(Throwable arg0) {
					// TODO Auto-generated method stub
			    	connecting=true;
			    	System.out.println("connection lost");
			    	
			    	while(connecting==true){
			    		if(configurationData.m_GiraffPlusBroker.equals("true"))
			    		{
			    			try{
							client.connect(options);
							connecting=false;
							} catch (MqttSecurityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (MqttException e) {
									connecting=true;
							}
			    		}
			    		else{
					    	try {
								client.connect();
								connecting=false;
							} catch (MqttSecurityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (MqttException e) {
									connecting=true;
							}
			    		}
			    	}
		            try {
						//client.subscribe(id+"LaserScan",2);
			            client.subscribe(id+"Topology",2);
			            client.subscribe(id+"Localization",0);
			            client.subscribe(id+"NavigationMode",2);
			            //client.subscribe(id+"ErrorMsg",2);
			            System.out.println("subscribed: "+id+"ErrorMsg");
					} catch (MqttSecurityException e) {
						System.out.println("MQTT Subscribe MqttSecurityException"+e.toString());
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						System.out.println("MQTT Subscribe MqttException"+e.toString());
					}
				}


				@Override
				public void deliveryComplete(MqttDeliveryToken arg0) {
					// TODO Auto-generated method stub
					
				}


				@Override
				public void messageArrived(MqttTopic topicArrived, MqttMessage messageArrived) {
					
					Thread t_msgManager= new Thread(new MessageManager(topicArrived,messageArrived),new String("Thread num:"+numMessages));
					t_msgManager.start();
					
				}
				
						
			});
		} catch (MqttException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		try {
           // client.subscribe(id+"LaserScan",2);
            client.subscribe(id+"Topology",2);
            client.subscribe(id+"Localization",0);
            client.subscribe(id+"NavigationMode",2);
            //client.subscribe(id+"ErrorMsg",2);
            System.out.println("subscribed: "+id+"ErrorMsg");
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
	public void publish(String topic,int qos,String msg){
	//	Thread t0= new Thread(new MessagePublisher(topic,msg,qos),"publisher");
		//t0.start();

		// TODO Auto-generated method stub
		if(connecting==false && m_CommonData.pluginSTOP==false && client!=null)
		{
	        MqttMessage message = new MqttMessage(msg.getBytes());
	        message.setQos(qos);
	        MqttDeliveryToken token = null;
	        try {
				token = client.getTopic(id+topic).publish(message);
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				System.out.println("Sending:"+msg+" topic:"+id+topic);
				token.waitForCompletion();
				} catch (MqttSecurityException e) {
					// TODO Auto-generated catch block
					System.out.println("Error while waiting for completion"+"Security Exception");
					e.printStackTrace();
				} catch (MqttException e) {
					System.out.println("Error while waiting for completion");
				}
	        System.out.println("Message sent:"+msg+" topic:"+id+topic);
		}
		else{
			System.out.println("Message Can´t be published... ");
			if(connecting==true)
				System.out.println("connecting");
			if( m_CommonData.pluginSTOP)
				System.out.println("pluginStop");
			if(client==null)
				System.out.println("client==null");
		}
	
		
	}
	public boolean isConnected(){
		boolean connected=false;
		if(client!=null){
			connected=client.isConnected();
		}
		return connected;
	}
	public void disconnect(){
		if(client!=null){
		try {
			client.disconnect();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception throwed by -disconnect()-");
		}
		System.out.println("Client set to NULL");
		client=null;
		}
		else{
			System.out.println("DISCONNECT->Client was NULL");
		}
	}
	
	private SSLContext getAuthSSLContext(){
  
        	String truststorePassword="GiraffPlus123";
        	String keystorePassword="GiraffPlus123";
        	
    			KeyStore trustStore = null;
    			{
    				InputStream tfis;
					try {
						tfis = new FileInputStream("./plugins/trust.jks");
	    				trustStore = KeyStore.getInstance("JKS");
	    				trustStore.load(tfis, truststorePassword.toCharArray());	    				
	    				tfis.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						System.out.println("1-FileNotFoundException:"+e.toString());
					} catch (KeyStoreException e) {
						// TODO Auto-generated catch block
						System.out.println("2-KeyStoreException:"+e.toString());
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						System.out.println("3-NoSuchAlgorithmException:"+e.toString());
					} catch (CertificateException e) {
						// TODO Auto-generated catch block
						System.out.println("4-CertificateException:"+e.toString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("5-IOException:"+e.toString());
					}

    			}
    			TrustManagerFactory trustManagerFactory;
				try {
					trustManagerFactory = TrustManagerFactory
							.getInstance("PKIX", "SunJSSE");
					trustManagerFactory.init(trustStore);
					X509TrustManager x509TrustManager = null;
	    			for (TrustManager trustManager : trustManagerFactory
	    					.getTrustManagers()) {
	    				if (trustManager instanceof X509TrustManager) {
	    					x509TrustManager = (X509TrustManager) trustManager;
	    					break;
	    				}
	    			}
	    			if (x509TrustManager == null) {
	    				System.out.println("Null Trust Manager");
	    				//throw new NullPointerException();
	    			}
	    			KeyStore keyStore = null;
	    			{
	    				InputStream fis;
						try {
							fis = new FileInputStream("./plugins/client.jks");
		    				keyStore = KeyStore.getInstance("JKS");
		    				keyStore.load(fis, keystorePassword.toCharArray());
		    				fis.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							System.out.println("6-FileNotFoundException:"+e.toString());
						} catch (CertificateException e) {
							// TODO Auto-generated catch block
							System.out.println("7-CertificateException:"+e.toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							System.out.println("8-IOException:"+e.toString());
						}

	    			}
	    			
	    			
	    			KeyManagerFactory keyManagerFactory = KeyManagerFactory
	    					.getInstance("SunX509", "SunJSSE");
	    			try {
						keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
					} catch (UnrecoverableKeyException e) {
						// TODO Auto-generated catch block
						System.out.println("9-UnrecoverableKeyException:"+e.toString());
					}
	    			X509KeyManager x509KeyManager = null;
	    			for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
	    				if (keyManager instanceof X509KeyManager) {
	    					x509KeyManager = (X509KeyManager) keyManager;
	    					break;
	    				}
	    			}
	    			if (x509KeyManager == null) {
	    				System.out.println("Null Key Manager!");
	    				//throw new NullPointerException();
	    			}
	    			
	    			
	    			SSLContext sslContext = SSLContext.getInstance("TLS");
	    			// the final null means use the default secure random source
	    			sslContext.init(new KeyManager[] { x509KeyManager },
	    			new TrustManager[] { x509TrustManager }, null);
	    			return sslContext; 
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					System.out.println("6-NoSuchAlgorithmException:"+e.toString());
					return null;
				} catch (NoSuchProviderException e) {
					// TODO Auto-generated catch block
					System.out.println("7-NoSuchProviderException:"+e.toString());
					return null;
				}
				catch (KeyStoreException e) {
					// TODO Auto-generated catch block
					System.out.println("8-KeyStoreException:"+e.toString());
					return null;
				} catch (KeyManagementException e) {
					// TODO Auto-generated catch block
					System.out.println("9-KeyManagementException:"+e.toString());
					return null;
				}
    			
    			
    			

    			
    			 
	}	
	////////////////////////////////////////////////////////////////
	//Mqtt events Manager
	//
    public void addMqttEventListener(MqttEventListener listener) {
        this.m_listeners.add(listener);
    }
 
    public void removeMqttEventListener(MqttEventListener listener) {
        this.m_listeners.remove(listener);
    }
    private void notifyListeners(String Topic,String Message) {
        for (MqttEventListener mqttEventListener: m_listeners) {
        	mqttEventListener.eventRead(new MqttEventSource(this, Topic, Message));
        }
    }
    //
    
    
    /////////////////////////////////////////////////////////////////
	private class MessageManager implements Runnable
	{
		String msg;
		String Topic_arrived;
		int qos;
		public MessageManager(String t_topicArrived, MqttMessage t_messageArrived){
			msg = new String(t_messageArrived.getPayload());
	        Topic_arrived=t_topicArrived;//new String(t_topicArrived.getName());
	        qos=t_messageArrived.getQos();
	       //// System.out.println("message received on topic:"+ Topic_arrived +" QoS:"+ qos);
		}

		@Override
		public void run() {		
			notifyListeners(Topic_arrived,msg);
			////System.out.println("message received, listeners notified");
		}
		
	}
	private class MessagePublisher implements Runnable
	{
		String msg;
		String topic;
		int qos;
		public MessagePublisher(String t_topicArrived, String Message, int Qos){
			msg = Message;
	        topic=t_topicArrived;
	        qos=Qos;
	       //// System.out.println("message received on topic:"+ Topic_arrived +" QoS:"+ qos);
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(connecting==false && m_CommonData.pluginSTOP){
		        MqttMessage message = new MqttMessage(msg.getBytes());
		        message.setQos(qos);
		        MqttDeliveryToken token = null;
		        try {
					token = client.getTopic(id+topic).publish(message);
				} catch (MqttPersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					System.out.println("Sending:"+msg+" topic:"+id+topic);
					token.waitForCompletion();
					} catch (MqttSecurityException e) {
						// TODO Auto-generated catch block
						System.out.println("Error while waiting for completion"+"Security Exception");
						e.printStackTrace();
					} catch (MqttException e) {
						System.out.println("Error while waiting for completion");
					}
		        System.out.println("Message sent:"+msg+" topic:"+id+topic);
				}
				else{
					System.out.println("Message Can´t be published... Client is connecting");
				}
		}
		
	}
	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
    	connecting=true;
    	System.out.println("connection lost");
    	while(connecting==true){
    		if(configurationData.m_GiraffPlusBroker.equals("true"))
    		{
    			try{
    			client = new MqttClient("ssl://giraffplus.xlab.si:8883","Pilot"+id,memPersistence);
				client.connect(options);
				connecting=false;
				} catch (MqttSecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
						connecting=true;
				}
    		}
    		else{
		    	try {
		    		client = new MqttClient("tcp://"+configurationData.m_IPBroker+":"+configurationData.m_PortBroker,id,memPersistence);
					client.connect();
					connecting=false;
				} catch (MqttSecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
						connecting=true;
				}
    		}
    	}
        try {
			//client.subscribe(id+"LaserScan",2);
            client.subscribe(id+"Topology",2);
            //client.subscribe(id+"Localization",0);
            client.subscribe(id+"Status",0);
            client.subscribe(id+"NavigationMode",2);
            //client.subscribe(id+"ErrorMsg",2);
            System.out.println("subscribed: "+id+"ErrorMsg");
		} catch (MqttSecurityException e) {
			System.out.println("MQTT Subscribe MqttSecurityException"+e.toString());
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			System.out.println("MQTT Subscribe MqttException"+e.toString());
		}
	}



	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		Thread t_msgManager= new Thread(new MessageManager(arg0,arg1),new String("Thread num:"+numMessages));
		t_msgManager.start();
	}
}

