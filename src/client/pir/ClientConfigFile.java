package client.pir;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The singleton ClientConfigFile class
 * @author Tommaso
 *
 */
public class ClientConfigFile {
	
	/** The instance  */
	static ClientConfigFile instance ;
	/** The socket port */
	private int socketPort;
	/** The server address */
	private String serverAddress;
	
	/**
	 * The class constructor that would be call just once
	 * 
	 * @param conf_file the path of the configuration file
	 */
	private ClientConfigFile(String conf_file){
		
		try {
			
	    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        Document doc = docBuilder.parse (new File(conf_file));
	        
	        NamedNodeMap nnmAttributes;
            
	        nnmAttributes = doc.getElementsByTagName("client").item(0).getAttributes();			
	        socketPort	= Integer.valueOf(nnmAttributes.getNamedItem("socketport").getNodeValue());
	        serverAddress = nnmAttributes.getNamedItem("serveraddress").getNodeValue();
          
		}catch (java.io.FileNotFoundException fnfe) {
			
			setDefaultValue();
			
        }catch (SAXParseException err) {
        	
	        System.err.println("** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
	        System.err.println(" " + err.getMessage ());

        }catch (SAXException e) {
        	
	        Exception x = e.getException ();
	        ((x == null) ? e : x).printStackTrace ();

        }catch (Throwable t) {
        t.printStackTrace ();
        }
	}
	
	/**
	 * Gets the single instance of ServerConfigFile.
	 * 
	 * @return single instance of ServerConfigFile
	 */
	public static ClientConfigFile getInstance(String conf_file) {
		
		if (instance == null)
			instance = new ClientConfigFile(conf_file) ;

		return instance ;
	}
	
	/**
	 * Sets the default value for the socket port and the server address if the file is not found
	 */
	private void setDefaultValue(){
		socketPort = 1222;
		serverAddress = "127.0.0.1";
	}
	
	/**
	 * Returns the socket port
	 * 
	 * @return the socket port
	 */
	public int getSocketPort(){
		return socketPort;
	}
	
	/**
	 * Returns the server address
	 * 
	 * @return the server address
	 */
	public String getServerAddress(){
		return serverAddress;
	}
	
}


