package linda.test.shm.asynchronous;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.shm.AbstractCentralizedLinda;

public class ManyReadManyWriteTest {
	
	final static int N = 256;

    public static void main(String[] a) {
                
        final ExtendedLinda sequentialLinda = new linda.shm.CentralizedSequentialLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaServer");
    	sequentialLinda.debug("Start reading");
    	Callback action = new PrintCallback();
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    			sequentialLinda.eventRegister( eventMode.READ, eventTiming.IMMEDIATE, new Tuple(i, j), action);
//    	        linda.debug( result.toString()  );
    	    }
    	}
        
    	sequentialLinda.debug("Start writing");
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    			sequentialLinda.write(new Tuple(i, j));
    		}
    	}

    	sequentialLinda.debug("Finished");
    	sequentialLinda.stop();
    	
    	final ExtendedLinda concurrentLinda = new linda.shm.CentralizedLinda(16,16);
    	concurrentLinda.debug("Start reading");
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    			concurrentLinda.eventRegister( eventMode.READ, eventTiming.IMMEDIATE, new Tuple(i, j), action);
//    	        linda.debug( result.toString()  );
    	    }
    	}
    	
    	concurrentLinda.debug("Start writing");
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    			concurrentLinda.write(new Tuple(i, j));
    		}
    	}

    	concurrentLinda.debug("Finished");
    	concurrentLinda.stop();
    }
}
