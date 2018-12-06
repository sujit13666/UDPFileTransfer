import java.io.*;
import java.net.*;
import java.util.*;
/**
 * This is the Client for UDP based chatting system. 
 * 
 *
 * @author      Sujit Ghosh 102233329
 * @version     1.0
 * @email       sujit.bit.0329@gmail.com
 */
public class UDPClient
{
    public static void main (String args[]) throws Exception 
    {
        System.out.println("Author: Sujit Ghosh, ID: 102233329");
        System.out.println("-------------------------------------");
        System.out.println("Instructions: ");
        System.out.println("1. Type filename to fetch server file in client directory");
        System.out.println("2. Type '\\q' to quit the program");
        System.out.println("-------------------------------------");
        
        Checksum checksum = new Checksum(); // Checksum class to check the validity of data
        String userInput;  // User input
        String reply; // reply from bridge
        
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); // Buffer Reader to read from user input
        
        DatagramSocket clientSocket = new DatagramSocket();  // Creating socket
        
        boolean requestedFile = false; // check if client is receiving file
        int receivedFrameNumber = 0; // last received frame number
        do{
            
            // Initializing send and receive data message variables
            byte[] sendData = new byte[128];
            byte[] receiveData = new byte[128];
            // Client Scoket Creation
            InetAddress IPAddress = InetAddress.getByName("localhost");
            
            // Getting User Input
            userInput = inFromUser.readLine();
            System.out.println("Sending Command: "+userInput);
            System.out.println("-------------------------------------");
            
            if(!userInput.equals("\\q")){
                
                // Sending byte data to Bridge
                sendData = (checksum.getChecksumString(userInput)).getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4000);
                clientSocket.send(sendPacket);
                
                
                while(true)
                {
                    try{
                        // Getting reply from server via bridge
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        clientSocket.setSoTimeout(1000);
                        clientSocket.receive(receivePacket);
                        reply = new String(receivePacket.getData()).trim();
                        
                        // Check if its a data frame or response frame
                        int replyChecksum = 0;
                        String[] replyData = reply.split("###");
                        
                        
                        if(replyData.length==2) // 2 length reply data is normal reply
                        {
                            if(!checksum.validChecksumValueWithoutDataFrame(reply))
                            {
                                // If damaged response frame just ignore, later on the request will be sent
                                System.out.println("Response : "+replyData[0]);
                                System.out.println("Response Ignored: Damaged response received");
                                System.out.println("-------------------------------------");
                            }else if(replyData[0].trim().equals("END"))
                            {
                                // If its end of file transfer then break; user can input again
                                receivedFrameNumber = 0;
                                requestedFile = false;
                                System.out.println("Response : End");
                                System.out.println("--------------End of File------------");
                                break;
                            }else if(replyData[0].trim().equals("doesn't exist"))
                            {
                                // If the file doesn't exist, then break; user can input again
                                System.out.println("****** This file doesn't exist *****"); 
                                System.out.println("-------------------------------------");
                                break;
                            }
                        }else if(replyData.length==3) // 3 length reply data is data with frame reply
                        {
                            // receiving file started
                            requestedFile = true;
                            
                            // getting frame number
                            receivedFrameNumber = Integer.parseInt(replyData[0].trim());
                            
                            // If the value is correct send for next packet/ chunk
                            if(checksum.validChecksumValueWithDataFrame(reply))
                            {
                                // sending request for next packet
                                int sendingFrameNumber = (receivedFrameNumber==1)? 0:1;
                                sendPacket.setData((checksum.getChecksumString("ACK"+sendingFrameNumber)).getBytes());
                                clientSocket.send(sendPacket);
                                System.out.println("Response Frame: "+replyData[0]);
                                System.out.println("Response Data : ["+replyData[1]+"]");
                                System.out.println("Sending for frame : "+sendingFrameNumber);
                                System.out.println("-------------------------------------");
                            }else{
                                // If damaged data frame just ignore the data and the request will be sent again
                                System.out.println("Response Frame: "+replyData[0]);
                                System.out.println("Response : "+replyData[1]);
                                System.out.println("Response Ignored: Damaged response received");
                                System.out.println("-------------------------------------");
                            }

                            
                        }
                        
                    }catch(java.net.SocketTimeoutException e){
                        // If timeout happens then Sending same ack again
                        if(requestedFile){
                            // Sending the same request with same ACK number again
                            int sendingFrameNumber = (receivedFrameNumber==1)? 0:1;
                            sendPacket.setData((checksum.getChecksumString("ACK"+sendingFrameNumber)).getBytes());
                            clientSocket.send(sendPacket);
                            System.out.println("Timeout : Data lost or command damaged");
                            System.out.println("Sending for frame again: "+sendingFrameNumber);
                            System.out.println("-------------------------------------");
                        }else{
                            // Sending the same command request again
                            System.out.println("Timeout : Data lost or command damaged");
                            System.out.println("Sending command again : "+userInput);
                            System.out.println("-------------------------------------");
                            clientSocket.send(sendPacket);
                        }
                        
                    }
                }
            }else
            {
                System.out.println("Program Terminated"); // program terminated
            }
            
        }while(!userInput.equals("\\q"));
        clientSocket.close();
    }
}
