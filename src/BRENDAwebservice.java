import java.util.ArrayList;
import java.util.Scanner;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import javax.xml.namespace.QName;

public class BRENDAwebservice {
	
	// Note that this service now requires email/password authentication so
	// we switched to just using a local EC-to-GO lookup table
	// when updating the Reaction Finder's knowledge base.
	public static ArrayList<String> getGOxrefsFromID(String eznum){
		ArrayList<String> GOxrefs = new ArrayList<String>();
		ArrayList<String> recnames = getRecommendedNameAndGOxrefFromID(eznum);
		for(String recname : recnames){
			GOxrefs.add(recname.substring(recname.lastIndexOf("*")+1,recname.lastIndexOf("#")));
		}
		return GOxrefs;
	}
	
	
	public static ArrayList<String> getRecommendedNamesFromID(String eznum){
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> recnames = getRecommendedNameAndGOxrefFromID(eznum);
		for(String recname : recnames){
			recname = recname.substring(0,recname.lastIndexOf("#goNumber"));
			names.add(recname.substring(recname.lastIndexOf("*"),recname.length()));
		}
		return names;
	}
	
	
       
	public static ArrayList<String> getRecommendedNameAndGOxrefFromID(String eznum) 
		   {
			ArrayList<String> result = new ArrayList<String>();
			try{
			      Service service = new Service();
			      Call call = (Call) service.createCall();
			      String endpoint = "https://www.brenda-enzymes.org/soap/brenda_server.php";
			      call.setTargetEndpointAddress( new java.net.URL(endpoint) );
			      call.setOperationName(new QName("http://soapinterop.org/", "getRecommendedName"));
			      String resultstring = (String) call.invoke( new Object[] {"ecNumber*" + eznum} );
			      System.out.println(resultstring);
	
			      Scanner scanner = new Scanner(resultstring);
			      scanner.useDelimiter("!");
			      while(scanner.hasNext()){
			    	  result.add(scanner.next());
			      }
			      scanner.close();
		      }
			catch(Exception ex){ex.printStackTrace();}
			return result;
		   }
}