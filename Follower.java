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


public class Follower{
      
   public static void main(String[] argv){

      MQTTClient m_UMAClient = new MQTTClient();
      Scanner sc = new Scanner(System.in);

      BufferedInputStream dataZ = null;
      String tempZ = "";
      float valueZ=0;

      BufferedInputStream dataX = null;
      String tempX = "";
      float valueX=0;
      
      BufferedInputStream securityStream = null;
      String security = "";
      String tempSecurity = "Stop";


      try {
         long r = 0;
         while(true){

            r ++;
             
            dataZ =  new BufferedInputStream(
                     new FileInputStream(
                     new File("ZFile.txt")));

            dataX =  new BufferedInputStream(
                     new FileInputStream(
                     new File("XFile.txt")));

            securityStream =  new BufferedInputStream(
                     new FileInputStream(
                     new File("FileText.txt")));

            byte[] buf = new byte[20];
            

            int n = 0;
            
            if(r==10000){

               // Put the text from FileText.txt in security
               while ((n = securityStream.read(buf)) >= 0) {
                  
                  for (byte bit : buf) {
                     if(bit>40)
                     security += (char) bit;
                        
                  }
               }

               buf = new byte[20];
               n = 0;
               // Put the text from ZFile.txt in tempZ
               while ((n = dataZ.read(buf)) >= 0) {
                     
                  for (byte bit : buf) {
                     tempZ += (char) bit;
                        
                  }
               }

               // Convert it in float
               try {
                  valueZ = Float.parseFloat(tempZ);
               } catch (NumberFormatException e) {
                   System.out.println("Z is not a number");
               }
               tempZ = "";

               buf = new byte[20];
               n = 0;
               
               // Put the text from XFile.txt in tempZ
               while ((n = dataX.read(buf)) >= 0) {
                  
                  for (byte bit : buf) {
                     tempX += (char) bit;
                        
                  }
               }

               // Convert it in float
               try {
                  valueX = Float.parseFloat(tempX);
               } catch (NumberFormatException e) {
                   System.out.println("X is not a number");
               }
               tempX = "";

               
              // security = new String(security);
               r=0;

               // Convert the Kinect values in MQTT values

               if (valueZ == -1.0f){
                  security = "Stop";
               }

               else if (valueZ < 1.2f && valueZ >= 0.0f) {
                  valueZ = 0.0f;

                  if (valueX > 1.0f){
                     valueX = 0.8f;
                  }

                  else if (valueX < -1.0f) {
                     valueX = -0.8f;
                  }

                  else{
                     valueX = valueX;
                  }
               }

               else {
                  valueX = valueX/(valueZ+3.0f);
                  valueZ = (valueZ)/8.0f;
               }

               // Publishing ...
               out.println("\""+security+"\"");

               switch(security){
                  case "Failed":
                     security = tempSecurity;
                     out.println("\""+security+"\"");
                     break;
               }
               
               switch (security){
                  
                  case "follow":
                     m_UMAClient.publish("NavigationCommand", 2, "Motion " + Float.toString(valueZ) + " " + Float.toString(valueX) );
                     break;

                  case "Stop":
                     m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.0");
                     break;

                  default : 
                     m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.0");
                     break;
               }
               tempSecurity = security;
               security = "";
            }     
         }
      }











      catch (FileNotFoundException e) {
         e.printStackTrace();
      } 
      catch (IOException e) {
         e.printStackTrace();
      }

      finally {
         try {
               m_UMAClient.publish("NavigationCommand", 2, "Motion 0.0 0.0" );
            if (dataZ != null)
               dataZ.close();
            if (dataX != null)
               dataX.close();
            if (securityStream != null)
               securityStream.close();
         } 
         catch (IOException e) {
            e.printStackTrace();
         }

      }
   }
}
 

      
        