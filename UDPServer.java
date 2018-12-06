import java.io.*;
import java.net.*;
import java.util.ArrayList;
/**
 * This is the server of UDP packet transferring
 * 
 *
 * @author      Sujit Ghosh  102233329
 * @version     1.0
 * @email:      sujit.bit.0329@gmail.com
 */
public class UDPServer
{
    public static void main (String args[]) throws Exception 
    {
        System.out.println("Author: Sujit Ghsoh, ID: 102233329");
        System.out.println("-------------------------------------");
        
        Checksum checksum = new Checksum();
        
        DatagramSocket serverSocket = new DatagramSocket(5000); // Creation of Datagram Socket using 5000 port
        ArrayList<String> dataList = new ArrayList<String>(); // Initializing the empty arraylist for data packets
                
        int frameNumber = 0; // Frame can be 1 or 0 to check if the same ack is received
        int chunkNumber = 0; // Data chunk is arryList index which is going to be sent
        while(true)
        {
            // Initializing send and receive data message variables
            byte[] receiveData = new byte[128];
            byte[] sendData = new byte[128];
            
            // UDPServer receiveing mesage from Bridge
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String message = new String( receivePacket.getData());
            String command = message.trim();
            
            String[] commandData = command.split("###");
            System.out.println("Command Received: "+commandData[0]);
            
            // Checking Checksum
            if(!checksum.validChecksumValueWithoutDataFrame(command)){
                System.out.println("Command Ignored: Damaged data received");
                System.out.println("-------------------------------------"); 
                continue;
            }   
            
            // Extracting Bridge ipaddress and port
            InetAddress IPAddress = receivePacket.getAddress();
            int bridgePort = receivePacket.getPort();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, bridgePort);
            
            // Check if the command is for file or ack
            if(commandData[0].equals(("alice.txt")))
            {
                frameNumber = 0; // Initialize frame as 0
                chunkNumber = 0; // initialize chunk as 0
                dataList = new ArrayList<String>(); // Initialize empty arraylist
                
                // Reading File
                File file = new File("alice.txt"); 
                BufferedReader br = new BufferedReader(new FileReader(file));
                String totalString = "";
                String st; 

                // Making Whole File into single string
                while ((st = br.readLine()) != null)
                {
                    totalString+= (""+st);
                }
                
                // parting whole string into 100 character / 100 byte chunk and put into arraylist
                int counter = 0;
                for(int i=0; (i+100)<totalString.length(); i+=100)
                {
                    dataList.add(totalString.substring(i,i+100));
                    counter+=100;                    
                }
                dataList.add(totalString.substring(counter,totalString.length()));
                
            }else if(commandData[0].trim().toLowerCase().contains("ack"))
            {
                int receivedAckNumber = Integer.parseInt(commandData[0].trim().substring(3,commandData[0].trim().length())); // Getting ACK number
                chunkNumber = (receivedAckNumber!=frameNumber)?chunkNumber+1 :chunkNumber; // set chunk number
                frameNumber = receivedAckNumber; // set frame 
            }
            else
            {
                // Sending file doesn't exist sentence after checksum added
                sendData = (checksum.getChecksumString("doesn't exist")).getBytes();
                sendPacket.setData(sendData);
                serverSocket.send(sendPacket);
                System.out.println("Sending Response: doesn't exist");    
                System.out.println("-------------------------------------");    
                continue;
            }
            
            if(chunkNumber<dataList.size())
            {
                
                // get the specific data string from arraylist
                String sendStringWithChecksum = checksum.getChecksumString(dataList.get(chunkNumber));
                // Adding frame number with packet
                sendData = (frameNumber+"###"+sendStringWithChecksum).getBytes();
                // Sending data to Bridge
                sendPacket.setData(sendData);
                serverSocket.send(sendPacket);
                System.out.println("Sending Frame : "+frameNumber);    
                System.out.println("Sending Data Chunk : "+chunkNumber);    
                System.out.println("-------------------------------------");
            }else
            {
                // Send the end info to client after adding checksum into packet
                sendData = (checksum.getChecksumString("END")).getBytes();
                sendPacket.setData(sendData);
                serverSocket.send(sendPacket);
                System.out.println("Sending Response: END");    
                System.out.println("-------------------------------------");  
            }
        }
    }
}
