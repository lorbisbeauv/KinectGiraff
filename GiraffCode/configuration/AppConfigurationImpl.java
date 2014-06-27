package configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

public class AppConfigurationImpl {
	public String m_IPBroker=null,m_PortBroker=null,m_UserMode="engineer", m_GiraffPlusBroker="false";
	public String m_identity="empty";
	public boolean isApplet=false;
	public AppConfigurationImpl(){
		if (isApplet==false){
		readConfigFile();
		}
		else{
			m_IPBroker="150.214.109.134";
			m_PortBroker="22";
			m_identity="AutonomyTest";
			m_UserMode="engineer";
			m_GiraffPlusBroker="false";
		}
	}
	
	
	 public void readConfigFile(){
			File archivo = null;
			FileReader fr = null;
			BufferedReader br = null;
			try {
			     // Apertura del fichero y creacion de BufferedReader para poder
				 // hacer una lectura comoda (disponer del metodo readLine()).
				archivo = new File ("./plugins/config.txt");
				fr = new FileReader (archivo);
				br = new BufferedReader(fr);
				
				// Lectura del fichero
				String linea="empty";
				while (linea!=null){
					linea=br.readLine();
					if (linea!=null){
					    StringTokenizer param1tokenizer= new StringTokenizer(linea," ");
						String param1=param1tokenizer.nextToken(" ");
						String param1_value=param1tokenizer.nextToken(" ");
						if (param1.contains(new String("IP"))){
							m_IPBroker=param1_value;
						}
						else if (param1.contains(new String("PORT"))){
					    	m_PortBroker=param1_value;
					    }
						else if (param1.contains(new String("USER_MODE"))){
					    	m_UserMode=param1_value;
					    }
						else if (param1.contains(new String("GP_BROKER"))){
							m_GiraffPlusBroker=param1_value;
						}
						else if (param1.contains(new String("GIRAFF_ID"))){
							m_identity=param1_value;
						}
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}finally{
			// En el finally cerramos el fichero, para asegurarnos
			// que se cierra tanto si todo va bien como si salta 
			// una excepcion.
			try{                    
				if( null != fr ){   
			    fr.close();     
			    }                  
			}catch (Exception e2){ 
			       e2.printStackTrace();
			    }
			}	
	    }

}
