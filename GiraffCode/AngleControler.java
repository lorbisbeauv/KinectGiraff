import gui.SingletonMapViewportImpl;

import java.io.*; 
import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import comms.MQTTClient;
import comms.MqttEventListener;
import comms.MqttEventSource;

import static java.lang.System.out;
import java.util.Scanner;


public class AngleControler{
		
	public static void main(String[] argv){

		MQTTClient m_UMAClient = new MQTTClient();
		int choice = 0;
		Scanner sc = new Scanner(System.in);
		
		while(true){

			out.println("Type the angle");
			choice = sc.nextInt();
			sc.nextLine();

			choice = choice / 30;

			out.println("turning");

			m_UMAClient.publish("NavigationCommand", 2, "Motion 0.2 -0.5");

			try{
				Thread.currentThread().sleep(choice*1000);
			}
			catch(InterruptedException ie){
			}

			m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.0");

			out.println("done");
		}
   	}
}

