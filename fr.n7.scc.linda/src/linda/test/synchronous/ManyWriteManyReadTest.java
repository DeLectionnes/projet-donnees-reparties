package linda.test.synchronous;

import linda.*;
import linda.shm.AbstractCentralizedLinda;

public class ManyWriteManyReadTest {
	
	final static int N = 256;

    public static void main(String[] a) {
                
        // final Linda linda = new linda.shm.CentralizedLinda();
    	final Linda linda = new linda.shm.CentralizedConcurrentLinda(16,16);
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaServer");
        linda.debug("Start writing");
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    			linda.write(new Tuple(i, j));
    		}
    	}
    	linda.debug("Start reading");
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    	    	Tuple result = linda.read(new Tuple(i, j));
//    	        linda.debug( result.toString()  );
    	    }
    	}
    	linda.debug("Finished");
    	((AbstractCentralizedLinda)linda).stop();
    }
}
