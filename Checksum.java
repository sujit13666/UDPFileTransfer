
/**
 * This class is responsible for checking the checksum and creating strings with checksum
 *
 * @author      Sujit Ghosh 102233329
 * @version     1.0
 * @email       sujit.bit.0329@gmail.com
 */
public class Checksum
{
    // Create checksum value string
    public String getChecksumString(String data){
        int checksum = 0;
        for(int i=0;i<data.trim().length(); i++)
        {
            checksum+=(int)data.trim().charAt(i);
        }
        return data+"###"+checksum;
    }
    
    // Check checksum value of normal reply
    public boolean validChecksumValueWithoutDataFrame(String reply){
        String[] replyData = reply.split("###");
        int checksum = 0;
        for(int i=0;i<replyData[0].trim().length(); i++)
        {
            checksum+=(int)replyData[0].trim().charAt(i);
        }
        
        return (checksum!=Integer.parseInt(replyData[1]))?false:true;
    }
    
    // Check checksum value of reply with data frame
    public boolean validChecksumValueWithDataFrame(String reply){
        String[] replyData = reply.split("###");
        int checksum = 0;
        if(replyData.length==3){
            for(int i=0;i<replyData[1].trim().length(); i++)
            {
                checksum+=(int)replyData[1].trim().charAt(i);
            }
        
            return (checksum!=Integer.parseInt(replyData[2]))?false:true;
        }else{
            return false;
        }
        
    }
}
