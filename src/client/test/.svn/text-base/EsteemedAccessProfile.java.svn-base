package client.test;

public class EsteemedAccessProfile {
	
	static private final boolean DEBUG = false;
	
	private int 		domainCardinality;
	private int			domainWindow;
	private int			initialNumberOfAccess;
	private int 		numResearches;
	private long[]		domain;
	private int			currDomainSize;
	private int			currDomainAdd;
	private long[]		fdf;
	private double[]	pdf;
	private double[]	cdf;	
	private int			numsamplesfdf;
	private double 		profile;
	
	public EsteemedAccessProfile(	int 	domainCardinality, 
									int 	domainWindow,
									int 	initialNumberOfAccess,
									int 	numResearches,
									double 	profile){
		
		this.domainCardinality 		= domainCardinality;
		this.domainWindow			= domainWindow;
		this.initialNumberOfAccess 	= initialNumberOfAccess;
		this.numResearches 			= numResearches;
		this.domain					= new long[this.domainWindow];
		this.currDomainSize			= 0;
		this.currDomainAdd			= 0;
		this.fdf					= new long[this.domainWindow];
		this.pdf					= new double[this.domainWindow];
		this.cdf					= new double[this.domainWindow];
		this.numsamplesfdf			= 0;
		this.profile				= profile;
		
		int i = 0;
		long dValue;
		while ((this.currDomainSize < this.domainWindow || i < this.initialNumberOfAccess)) {
			dValue = genselfsimilar();
	        assessFdf(dValue);
	        i++;
	    }
		computePdfCdf();    
		
		if(DEBUG){
			System.err.println("Input Pdf: i = " + i);
		    for ( int a = 0 ; a < this.domainWindow; a++ ){ 
		    	System.err.print("[" + domain[a] + "]: " + pdf[a] + "  ");
		    }
		    System.err.println("");  
		}
	    
	    i = 0; 
	    while (i < this.numResearches 	) {
	          dValue = extrRand();
	          assessFdf(dValue);
	          computePdfCdf();
	          i++;
	    }
	    
	    if(DEBUG){
			System.err.println("Esteemed Pdf: i = " + i);
		    for ( int a = 0 ; a < this.domainWindow; a++ ){ 
		    	System.err.print("[" + domain[a] + "]: " + pdf[a] + "  ");
		    }
		    System.err.println("");  
		}
	    
	}

	public void assessFdf(long dValue ){
		
		int i = 0;
		while(i < currDomainSize && domain[i] != dValue){
			i++;
		}
		if( i == currDomainSize ){
			domain[currDomainAdd] = dValue;
			numsamplesfdf -= fdf[currDomainAdd];
			fdf[currDomainAdd] = 1;
			currDomainAdd = 1 + currDomainAdd;
			if(currDomainAdd == domainWindow) { 
	            currDomainSize = domainWindow;
			}else if(currDomainSize != domainWindow){
	            currDomainSize++;
			}
			if (currDomainAdd >= domainWindow){
	            currDomainAdd = currDomainAdd % domainWindow;
			}
	     }else{
	    	 fdf[i]++;    
	     }
		 numsamplesfdf++; 
		 
	}
	
	public void computePdfCdf() {
		
	    for (int i = 0 ; i < currDomainSize ; i++){
	        pdf[i] = fdf[i] / (double)numsamplesfdf;
	    }
	    cdf[0] = pdf[0];
	    for (int i = 1 ; i < currDomainSize ; i++){
	       cdf[i] = cdf[i-1] + pdf[i];
	    }
	    
	}
	
	public long extrRand() 
	{
	    double p = Math.random();
	    int i;
	    for(i = 0; p > cdf[i]; i++);
	    return domain[i];
	}
	
	public long genselfsimilar() {
		
		return (long)(domainCardinality * Math.pow(Math.random(), Math.log(profile)/Math.log(1-profile)) + 1 );
		
	}
	
}
