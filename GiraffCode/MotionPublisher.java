import gui.SingletonMapViewportImpl;

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


public class MotionPublisher{
		
	public static void main(String[] argv){

		MQTTClient m_UMAClient = new MQTTClient();
		String choice = new String("");
		Scanner sc = new Scanner(System.in);

       	while(true){

       		switch (choice){

       			case "SPIN" :

       				out.println("Entered in rotate mod");
       				while(choice !=  new String("MOVE")){
       					
						out.println("To move forward, type \"MOVE\"");
						out.println("To change the rotation speed, type a number between 0 & 10");
						out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
						choice = sc.nextLine();

						switch (choice){

							case "0" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.0");
								break;
							case "1" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.1");
								break; 
							case "2" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.2");
								break;
							case "3" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.3");
								break;
							case "4" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.4");
								break;
							case "5" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.5");
								break;
							case "6" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.6");
								break;
							case "7" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.7");
								break;
							case "8" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.8");
								break;
							case "9" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.9");
								break;
							case "10" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 1.0");
								break;
							case "SPIN" :
								out.println("Already in rotate mod");
								break;
							case "MOVE" :
								out.println("Going to move forward mod");
								break;
							default : 
								out.println("Wrong entry, try again");
								break;
						}
       				}

       				break;


       			case "MOVE" : 

       				out.println("Entered in move forward mod");
       				while(choice != new String("SPIN")){

       					out.println("To rotate, type \"SPIN\"");
						out.println("To change the speed, type a number between 0 & 10");
						out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
						choice = sc.nextLine();

						switch (choice){

							case "0" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.0");
								break;
							case "1" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.1 0.0");
								break; 
							case "2" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.2 0.0");
								break;
							case "3" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.3 0.0");
								break;
							case "4" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.4 0.0");
								break;
							case "5" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.5 0.0");
								break;
							case "6" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.6 0.0");
								break;
							case "7" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.7 0.0");
								break;
							case "8" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.8 0.0");
								break;
							case "9" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 0.9 0.0");
								break;
							case "10" :
								m_UMAClient.publish("NavigationCommand", 2, "Motion 1.0 0.0");
								break;
							case "SPIN" :
								out.println("Going to rotate mod");
								break;
							case "MOVE" :
								out.println("Already in move forward mod");
								break;
							default : 
								out.println("Wrong entry, try again");
								break;
						}
       				}

       				break;

       			default :
					out.println("\nTo rotate, type \"SPIN\"");
					out.println("To move forward, type \"MOVE\"");
					out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
					choice = sc.nextLine();
					out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
					break;
       		}
		}
   	}
}

