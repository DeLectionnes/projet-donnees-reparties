package linda.test.shm.synchronous;

import linda.*;
import linda.shm.AbstractCentralizedLinda;

public class ManyWriteManyReadTest {
	
	final static int N = 256;

    public static void main(String[] a) {
                
        final ExtendedLinda sequentialLinda = new linda.shm.CentralizedLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaServer");
    	sequentialLinda.debug("Start writing");
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    			sequentialLinda.write(new Tuple(i, j));
    		}
    	}
    	sequentialLinda.debug("Start reading");
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    	    	Tuple result = sequentialLinda.take(new Tuple(i, j));
//    	        linda.debug( result.toString()  );
    	    }
    	}
    	sequentialLinda.debug("Finished");
    	sequentialLinda.stop();
    	final ExtendedLinda concurrentlinda = new linda.shm.CentralizedConcurrentLinda(16,16);
    	concurrentlinda.debug("Start writing");
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    			concurrentlinda.write(new Tuple(i, j));
    		}
    	}
    	concurrentlinda.debug("Start reading");
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    	    	Tuple result = concurrentlinda.take(new Tuple(i, j));
//    	        linda.debug( result.toString()  );
    	    }
    	}
    	concurrentlinda.debug("Finished");
    	concurrentlinda.stop();
    }
}