package server.pir;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The singleton ServerConfigFile class
 * @author Tommaso
 *
 */
public class ServerConfigFile {
	
	/** The instance. */
	static ServerConfigFile instance ;
	/** The socket port */
	private int socketPort;
	
	/**
	 * The class constructor that would be call just once
	 * 
	 * @param conf_file the path of the configuration file
	 */
	private ServerConfigFile(String conf_file){
		
		try {
			
	    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        Document doc = docBuilder.parse (new File(conf_file));
	        
	        NamedNodeMap nnmAttributes;
            
	        nnmAttributes = doc.getElementsByTagName("server").item(0).getAttributes();			
	        socketPort	= Integer.valueOf(nnmAttributes.getNamedItem("socketport").getNodeValue());
          
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
	public static ServerConfigFile getInstance(String conf_file) {
		
		if (instance == null)
			instance = new ServerConfigFile(conf_file) ;

		return instance ;
	}
	
	/**
	 * Sets the default value for the socket portif the file is not found
	 */
	private void setDefaultValue(){
		socketPort = 1222;
	}
	
	/**
	 * Returns the socket port
	 * 
	 * @return the socket port
	 */
	public int getSocketPort(){
		return socketPort;
	}
	
}


