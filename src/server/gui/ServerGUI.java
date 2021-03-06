package server.gui;


import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import base.disk.Disk;

import server.pir.Database;
import server.pir.Server;

public class ServerGUI extends java.awt.Frame {
        
	private Server server = null;
	private SwingWorker<Boolean,Void> worker;
	
    public javax.swing.JButton startjButton;
	public javax.swing.JButton stopjButton;
    public javax.swing.JButton closejButton;
    public javax.swing.JButton createDBjButton;   
    public javax.swing.JScrollPane logjScrollPane;
    public javax.swing.JTextArea logjTextArea;   
    public javax.swing.JLabel statusLabel;
    	
	/** Set look and feel */
	{
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}	
		
	private static final long serialVersionUID = 1L;	
	/** The class constructor, creates new form MainPanel */
    public ServerGUI() {    	
        initComponents();          
    }
    
    /**
     * Initialize all the GUI components
     */
    private void initComponents() {
    	
        logjScrollPane = new javax.swing.JScrollPane();
        logjTextArea = new javax.swing.JTextArea();
        startjButton = new javax.swing.JButton();
        stopjButton = new javax.swing.JButton();
        closejButton = new javax.swing.JButton();
        createDBjButton = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();

        setLayout(null);

        setResizable(false);
        setTitle("Server");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        logjTextArea.setColumns(20);
        logjTextArea.setRows(5);
        logjScrollPane.setViewportView(logjTextArea);

        add(logjScrollPane);
        logjScrollPane.setBounds(20, 42, 270, 587);

        startjButton.setText("Start");
        startjButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                startjButtonMouseClicked(evt);
            }
        });

        add(startjButton);
        startjButton.setBounds(310, 42, 73, 23);

        stopjButton.setText("Stop");
        stopjButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stopjButtonMouseClicked(evt);
            }
        });

        add(stopjButton);
        stopjButton.setBounds(310, 82, 73, 23);

        closejButton.setText("Chiudi");
        closejButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closejButtonMouseClicked(evt);
            }
        });
        add(closejButton);
        closejButton.setBounds(310, 122, 73, 23);
        
        createDBjButton.setText("Create DB");
        createDBjButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
            	createDBjButtonjButtonMouseClicked(evt);
            }
        });
        add(createDBjButton);
        createDBjButton.setBounds(305, 202, 83, 23);

        statusLabel.setFont(new java.awt.Font("Arial", 1, 12));
        statusLabel.setText("Stato");
        add(statusLabel);
        statusLabel.setBounds(330, 290, 30, 15);
        statusLabel.setVisible(false);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-408)/2, (screenSize.height-650)/2, 408, 650);
    }

    /**
     * Open a confirm dialog that advice about the close consequences and then close the GUI
     * 
     * @param evt mouse click
     */
    private void closejButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closejButtonMouseClicked   	
    	
    	int selection = javax.swing.JOptionPane.showConfirmDialog(null,
				"<html>Sei sicuro di voler chiudere il server?<br><br>Il server se attivo verra' terminato e si<br>potrebbero presentare inconsistenze<br>nei dati<br></html>","Conferma",JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		
		if(selection == JOptionPane.OK_OPTION) {
			if( server != null) {
				server.saveCache();
				server.stop();
			}
			System.exit(0);
		}
    }

    /**
     * Stops the server if is running
     * 
     * @param evt mouse click
     */
    private void stopjButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stopjButtonMouseClicked
    	
    	if( server != null) {
    		// server = new Server(this) ;
    		server.saveCache();
    		server.stop();
    		server = null;
    	}
    }

    /**
     * Starts the server if it isn't still running otherwise send an advice on log area
     * 
     * @param evt mouse click
     */
    private void startjButtonMouseClicked(java.awt.event.MouseEvent evt) {  	   		
    		worker = new SwingWorker<Boolean,Void>() {
	            public Boolean doInBackground() {	            	
	            	if( server == null ) {	            		
	            		server = new Server(getMe()) ;	            		
	            	}   	            	
					server.start(); 
					return true;
	            }
        	};
        	worker.execute(); 	  	
    }
    
    /**
     * Create a new database and in advance stop the server if running  
     * 
     * @param evt mouse click
     */
    private void createDBjButtonjButtonMouseClicked(java.awt.event.MouseEvent evt) {
    	
    	int selection = javax.swing.JOptionPane.showConfirmDialog(null,
				"<html>Sei sicuro di voler creare un nuovo database?<br><br>I dati esistenti verranno sovrascritti<br>e il server se attivo verr� arrestato<br><br></html>","Conferma",JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		
		if(selection == JOptionPane.OK_OPTION){
	    
			worker = new SwingWorker<Boolean,Void>() {
	            public Boolean doInBackground() {	            	
	            	
	            	if(Server.running) {
	            		server.stop();
	            		server = null;
	            	}
	            	
	            	updateLogWindow("Creating database...\n"); 
	            	
	            	long start, end, time;
	    			start = System.currentTimeMillis();	
	    			
	    			try {
						Database.create();
					} catch (FileNotFoundException e) {
						updateLogWindow("File non trovato");
						e.printStackTrace();
					} catch (IOException e) {
						updateLogWindow("Generic IO exception");
					}
	    			
	    			end = System.currentTimeMillis();	
	    			time = end - start;	
	    			
	    			updateLogWindow("Database created in " + time + "[ms]\n"); 
	    			
	    			server.disk.close();
	    			server.disk = new Disk<Long, String>(false);
	    			
					return true;
	            }
        	};
        	worker.execute(); 
						
		}
	    	
    	
    }
   
    /** 
     * Returns the current object istance
     * 
     * @return this
     */
    private ServerGUI getMe() { 
    	return this; 
    	}
    
    /**
     * Open a confirm dialog that advice about the exit consequences and then close the GUI
     * 
     * @param evt mouse click
     */
    private void exitForm(java.awt.event.WindowEvent evt) {
    	
    	int selection = javax.swing.JOptionPane.showConfirmDialog(null,
				"<html>Sei sicuro di voler chiudere il server?<br><br>Il server se attivo verr� terminato e si<br>potrebbero presentare inconsistenze<br>nei dati<br></html>","Conferma",JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		
    	if(selection == JOptionPane.OK_OPTION) {
    		server.stop();
			server.saveCache();
	    	System.exit(0);
		}
		
    }
          
    /**
     * Update the log area opening a new thread to write the give message
     * 
     * @param msg the message
     */
    public void updateLogWindow(final String msg) {
        Runnable doUpdateLogWindow = new Runnable() {
            public void run() {
            	getMe().writeLogMsg(msg);
            }
        };
        SwingUtilities.invokeLater(doUpdateLogWindow);
    }
    
    /**
     * Appends the given message to the log area
     * 
     * @param msg the message
     */
    private void writeLogMsg(String msg){
    	logjTextArea.append(msg);
    }
         
    /** 
     * Start the GUI
     * 
     * @param args
     */
    public static void main(String args[]) {
    	
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	ServerGUI serverGui = new ServerGUI();
            	serverGui.setLocationRelativeTo(null);
            	serverGui.setVisible(true);
            }
        })
        ;
    }

    
}
