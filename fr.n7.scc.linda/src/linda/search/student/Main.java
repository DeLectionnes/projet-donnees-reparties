package linda.search.student;

import linda.*;

public class Main {
	/***
	 * 
	 * @param args The argument Array. The first is the file, the second is the number of searchers, and the following ones are the terms researched.
	 */
	static Linda linda;
    public static void main(String args[]) {
    	if (args.length < 3) {
            System.err.println("linda.search.basic.Main file numer_of_searchers search1 search2 ...");
            return;
    	}
    	
    	
    	linda = new linda.shm.CentralizedConcurrentLinda(16,4);
    	
    	for(int chercheur = 0; chercheur < Integer.parseInt(args[1]); chercheur ++) {
        	Main.startSearcher();
        }
    	
    	for(int recherche = 2; recherche < args.length ; recherche++) {
    		Manager manager = new Manager(linda, args[0], args[recherche]);
    		(new Thread(manager)).start();
    	}
        
        
        
        
        
    }
    public static void startSearcher() {
    	//En cas de fin non prévue d'un chercheur.
    	Thread.UncaughtExceptionHandler searcherSuddenEnd = new Thread.UncaughtExceptionHandler() {
    		@Override
    	    public void uncaughtException(Thread th, Throwable ex) {
    	        linda.write(new Tuple(Code.Signal, SignalID.SearcherEnd));
    		}
    	};
        Searcher searcher = new Searcher(linda);
    	Thread search = new Thread(searcher);
    	search.setUncaughtExceptionHandler(searcherSuddenEnd);
    	search.start();
 
    }
}
