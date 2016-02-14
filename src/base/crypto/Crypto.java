package base.crypto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * The crypto class
 * @author Tommaso
 *
 */
public class Crypto {

	/** The cryptographic algorithm used*/
	private final static String CRIPTOGRAPHIC_ALGORITHM = "AES/CTR/PKCS5Padding";
	/** The cryptographic algorithm used to generate the key */
	private final static String KEYGENERATOR_CRIPTOGRAPHIC_ALGORITHM = "AES";	
	/** Path of the file where the key used to encrypt the nodes is stored */
	private final static String KEY_FILE_PATH = "conf" + File.separatorChar + "key";
	/** The byte vector containing the key*/
	private static byte[] key = null;
	
	/**
	 * Encrypts the byte vector containing the node information using the given key
	 * 
	 * @param key
	 * @param node
	 * 
	 * @return the byte vector containing the encrypted node information
	 */
	public static byte[] encryptBytes(byte[] key, byte[] bytes) {	
		
		byte[] encrypted = null;
		
		try {		
			SecretKeySpec skeySpec = new SecretKeySpec(key, KEYGENERATOR_CRIPTOGRAPHIC_ALGORITHM);
			Cipher cipher = Cipher.getInstance(CRIPTOGRAPHIC_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec,  new IvParameterSpec(new byte[]{99,88,77,66,55,44,33,22,11,0,99,88,77,66,55,44}));		
			//Encrypts the node byte vector
			encrypted = cipher.doFinal(bytes);			
		}  catch(Exception e) {
			
			e.printStackTrace();
		}
		
		return encrypted;
		
	}
	
	/**
	 * Decrypts the byte vector containing the node information using the given key
	 * 
	 * @param key
	 * @param encrypted
	 * 
	 * @return the byte vector containing the decrypted node information
	 */
	public static byte[] decryptBytes(byte[] key, byte[] encrypted) {
		
		byte[] node = null;
		
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key, KEYGENERATOR_CRIPTOGRAPHIC_ALGORITHM);
			Cipher cipher = Cipher.getInstance(CRIPTOGRAPHIC_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[]{99,88,77,66,55,44,33,22,11,0,99,88,77,66,55,44}));
			node = cipher.doFinal(encrypted);
		} catch (Exception e) {
			
			System.out.println(e.toString());
		}
		
		return node;
		
	}
	
	/**
	 * Creates a 128 bit AES key and save it on the disk
	 */
	public static void createAndSaveKey() {
		
		byte[] key = null;
		
		try {
			
			KeyGenerator kgen = KeyGenerator.getInstance(KEYGENERATOR_CRIPTOGRAPHIC_ALGORITHM);
			kgen.init(128);
			SecretKey skey = kgen.generateKey();
			key = skey.getEncoded();
			
			// Save the secret key in file system
			FileOutputStream fout;
			DataOutputStream dos;
			fout = new FileOutputStream(KEY_FILE_PATH);
			dos = new DataOutputStream(fout);
			dos.write(key);
			fout.close();
			
		} catch (Exception e) {
			System.out.println(e.toString() + " during the key generation and save");
		}
		
	}
	
	/**
	 * Read from the file the key
	 * 
	 * @return the byte vector containing the key 
	 */
	public static byte[] loadKey(){
		
		if(key == null) {
		
			try {
				
//				FileInputStream fin;
//				DataInputStream dis;
//				fin = new FileInputStream(KEY_FILE_PATH);
//				dis = new DataInputStream(fin);
//				key = new byte[16];
//				dis.read(key);
//				fin.close();
				
				key = new byte[16];
				DataInputStream dis = new DataInputStream(new FileInputStream(KEY_FILE_PATH));  
	            dis.readFully(key);  
	            dis.close(); 
				return key;
				
			} catch (Exception e) {
				System.out.println(e.toString() + " during the key loading");
			}
			
			return null;
			
		} else {
			return key;
		}
		
	}
	
}
