package linda.search.student;

import linda.*;

public class MainClientServer {
	/***
	 * 
	 * @param args The argument Array. The first is the file, the second is the number of searchers, and the following ones are the terms researched.
	 */
	static Linda lindaMain;
    public static void main(String args[]) {
    	if (args.length < 3) {
            System.err.println("linda.search.basic.Main file numer_of_searchers search1 search2 ...");
            return;
    	}
    	
    	
    	lindaMain = new linda.server.LindaClient("//localhost:4000/LindaServer");
    	
    	for(int chercheur = 0; chercheur < Integer.parseInt(args[1]); chercheur ++) {
        	Main.startSearcher();
        }
    	
    	for(int recherche = 2; recherche < args.length ; recherche++) {
    		Linda linda = new linda.server.LindaClient("//localhost:4000/LindaServer");
    		Manager manager = new Manager(linda, args[0], args[recherche]);
    		(new Thread(manager)).start();
    	}
        
        
        
        
        
    }
    public static void startSearcher() {
    	
    	//En cas de fin non prévue d'un chercheur.
    	Thread.UncaughtExceptionHandler searcherSuddenEnd = new Thread.UncaughtExceptionHandler() {
    		@Override
    	    public void uncaughtException(Thread th, Throwable ex) {
    	        lindaMain.write(new Tuple(Code.Signal, SignalID.SearcherEnd));
    		}
    	};
    	
    	Linda linda = new linda.server.LindaClient("//basile-HP:4000/LindaServer");
        Searcher searcher = new Searcher(linda);
    	Thread search = new Thread(searcher);
    	search.setUncaughtExceptionHandler(searcherSuddenEnd);
    	search.start();
 
    }
}
